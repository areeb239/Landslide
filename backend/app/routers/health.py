from fastapi import APIRouter
from app.models.schemas import HealthResponse
from app.services.firebase_service import is_firebase_connected
from app.services.ml_service import is_model_loaded
from app.config import get_settings

router = APIRouter(tags=["Health"])


@router.get("/health", response_model=HealthResponse)
@router.get("/api/v1/health")
async def health_check():
    """
    Service healthcheck and diagnostics.
    Returns status of Firebase Admin connection, ML inference engine, and environment.
    """
    settings = get_settings()
    fb_connected = is_firebase_connected()
    ml_loaded = is_model_loaded()

    return {
        "status": "healthy",
        "version": "1.0.0",
        "firebase_connected": fb_connected,
        "ml_model_loaded": ml_loaded,
        "environment": settings.app_env,
        "message": "NER Landslide Early Warning Backend Operational"
    }
