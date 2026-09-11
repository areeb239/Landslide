import os
import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.responses import FileResponse
from fastapi.middleware.cors import CORSMiddleware
from app.config import get_settings
from app.routers import predict, alerts, sensors, sos, health, data_sources

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("ner_backend")


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    settings = get_settings()
    logger.info("================================================================")
    logger.info("  🏔️  NER Landslide Early Warning & Prediction Backend")
    logger.info(f"  Env: {settings.app_env} | Host: {settings.app_host}:{settings.app_port}")
    logger.info("  Swagger Docs: http://localhost:8000/docs")
    logger.info("================================================================")
    yield
    # Shutdown
    logger.info("Shutting down NER Landslide Early Warning Backend.")


settings = get_settings()

app = FastAPI(
    title="NER Landslide Early Warning & Prediction API",
    description=(
        "AI-enabled real-time monitoring and early warning platform for landslides, "
        "flash floods, and slope failures in the North Eastern Region of India. "
        "Compliant with NDMA / Geological Survey of India early warning guidelines."
    ),
    version="1.0.0",
    lifespan=lifespan
)

# CORS configuration to allow Android devices and web portals
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.allowed_origins_list,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount Routers
app.include_router(health.router)
app.include_router(predict.router)
app.include_router(alerts.router)
app.include_router(sensors.router)
app.include_router(sos.router)
app.include_router(data_sources.router)


@app.get("/dashboard", response_class=FileResponse)
async def get_dashboard():
    static_file = os.path.join(os.path.dirname(__file__), "static", "dashboard.html")
    if os.path.exists(static_file):
        return FileResponse(static_file)
    return {"error": "Dashboard template not found"}


@app.get("/")
async def root():
    return {
        "platform": "NER Landslide Early Warning & Prediction Platform",
        "region": "North Eastern Region (NER) India",
        "status": "online",
        "version": "1.0.0",
        "dashboard": "/dashboard",
        "documentation": "/docs",
        "openapi": "/openapi.json",
        "endpoints": {
            "dashboard": "GET /dashboard",
            "prediction": "POST /api/v1/predict",
            "prediction_metadata": "GET /api/v1/predict/metadata",
            "prediction_by_location": "GET /api/v1/predict/location?latitude=27.17&longitude=88.53",
            "hotspots": "GET /api/v1/predict/hotspots",
            "alerts": "GET /api/v1/alerts | POST /api/v1/alerts",
            "cap_xml": "GET /api/v1/alerts/cap.xml",
            "sensor_telemetry": "POST /api/v1/sensors/telemetry | GET /api/v1/sensors",
            "simulate_sensors": "POST /api/v1/sensors/simulate",
            "imd_weather_bulletins": "GET /api/v1/imd/warnings",
            "satellite_earth_observation": "GET /api/v1/satellite/earth-observation?latitude=27.17&longitude=88.53",
            "sos": "POST /api/v1/sos",
            "health": "GET /api/v1/health"
        }
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.app_host,
        port=settings.app_port,
        reload=True
    )
