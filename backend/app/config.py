from typing import Optional
from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    # Firebase
    firebase_credentials_path: str = "serviceAccountKey.json"
    firebase_service_account_json: Optional[str] = None
    firebase_service_account_base64: Optional[str] = None
    firebase_project_id: str = ""

    # Server
    app_host: str = "0.0.0.0"
    app_port: int = 8000
    app_env: str = "development"

    # CORS
    allowed_origins: str = "*"

    # ML Model
    ml_model_path: str = ""

    # Weather
    weather_api_base_url: str = "https://api.open-meteo.com/v1/forecast"

    class Config:
        env_file = ".env"
        extra = "ignore"

    @property
    def allowed_origins_list(self) -> list[str]:
        if self.allowed_origins == "*":
            return ["*"]
        return [o.strip() for o in self.allowed_origins.split(",")]


@lru_cache()
def get_settings() -> Settings:
    return Settings()
