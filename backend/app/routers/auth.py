import os
import sqlite3
import hashlib
import secrets
import time
import re
from typing import Optional
from fastapi import APIRouter, HTTPException, Header, status
from pydantic import BaseModel, EmailStr, Field

router = APIRouter(prefix="/api/v1/auth", tags=["Authentication"])

DB_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "data")
os.makedirs(DB_DIR, exist_ok=True)
DB_PATH = os.path.join(DB_DIR, "users.db")


def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def init_db():
    with get_db() as conn:
        conn.execute("""
            CREATE TABLE IF NOT EXISTS users (
                uid TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'CITIZEN',
                token TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
        """)
        conn.commit()


init_db()


def hash_password(password: str, salt: str) -> str:
    return hashlib.pbkdf2_hmac(
        "sha256", password.encode("utf-8"), salt.encode("utf-8"), 100_000
    ).hex()


# Pydantic Schemas
class UserRegisterRequest(BaseModel):
    name: str = Field(..., min_length=2, max_length=100)
    email: str = Field(..., min_length=5, max_length=100)
    password: str = Field(..., min_length=6, max_length=128)
    role: Optional[str] = "CITIZEN"


class UserLoginRequest(BaseModel):
    email: str = Field(..., min_length=5, max_length=100)
    password: str = Field(..., min_length=1, max_length=128)


class UserDto(BaseModel):
    uid: str
    name: str
    email: str
    role: str


class AuthResponse(BaseModel):
    success: bool = True
    token: str
    user: UserDto
    message: str


EMAIL_REGEX = re.compile(r"^[\w\.-]+@[\w\.-]+\.\w+$")


@router.post("/register", response_model=AuthResponse, status_code=status.HTTP_201_CREATED)
async def register(request: UserRegisterRequest):
    email_clean = request.email.strip().lower()
    if not EMAIL_REGEX.match(email_clean):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid email format. Please provide a valid email address."
        )

    if len(request.password) < 6:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Password must be at least 6 characters long."
        )

    with get_db() as conn:
        # Check if already registered
        cursor = conn.execute("SELECT uid FROM users WHERE email = ?", (email_clean,))
        if cursor.fetchone():
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="An account with this email already exists. Please sign in."
            )

        uid = f"usr_{secrets.token_hex(8)}"
        salt = secrets.token_hex(16)
        pw_hash = hash_password(request.password, salt)
        token = f"tok_{secrets.token_urlsafe(32)}"
        now = int(time.time() * 1000)

        conn.execute(
            "INSERT INTO users (uid, name, email, password_hash, salt, role, token, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            (uid, request.name.strip(), email_clean, pw_hash, salt, request.role or "CITIZEN", token, now)
        )
        conn.commit()

    user_dto = UserDto(
        uid=uid,
        name=request.name.strip(),
        email=email_clean,
        role=request.role or "CITIZEN"
    )

    return AuthResponse(
        success=True,
        token=token,
        user=user_dto,
        message="Account created successfully."
    )


@router.post("/login", response_model=AuthResponse)
async def login(request: UserLoginRequest):
    email_clean = request.email.strip().lower()
    if not EMAIL_REGEX.match(email_clean):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid email format."
        )

    with get_db() as conn:
        cursor = conn.execute(
            "SELECT uid, name, email, password_hash, salt, role, token FROM users WHERE email = ?",
            (email_clean,)
        )
        row = cursor.fetchone()

        if not row:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Account not found with this email. Please create an account first."
            )

        expected_hash = hash_password(request.password, row["salt"])
        if not secrets.compare_digest(expected_hash, row["password_hash"]):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid password. Please verify your credentials."
            )

        # Issue fresh token on login
        new_token = f"tok_{secrets.token_urlsafe(32)}"
        conn.execute("UPDATE users SET token = ? WHERE uid = ?", (new_token, row["uid"]))
        conn.commit()

    user_dto = UserDto(
        uid=row["uid"],
        name=row["name"],
        email=row["email"],
        role=row["role"]
    )

    return AuthResponse(
        success=True,
        token=new_token,
        user=user_dto,
        message="Authentication successful."
    )


@router.get("/verify", response_model=UserDto)
async def verify(authorization: Optional[str] = Header(None)):
    if not authorization:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing Authorization header."
        )

    token = authorization.replace("Bearer ", "").strip()
    with get_db() as conn:
        cursor = conn.execute(
            "SELECT uid, name, email, role FROM users WHERE token = ?",
            (token,)
        )
        row = cursor.fetchone()
        if not row:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid or expired session token. Please log in again."
            )

    return UserDto(
        uid=row["uid"],
        name=row["name"],
        email=row["email"],
        role=row["role"]
    )
