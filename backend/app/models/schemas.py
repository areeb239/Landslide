from pydantic import BaseModel, Field
from typing import Optional
from enum import Enum


# ─── Enums ────────────────────────────────────────────────────────────────────

class RiskLevel(str, Enum):
    LOW = "LOW"
    MODERATE = "MODERATE"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"


class AlertSeverity(str, Enum):
    LOW = "LOW"
    MODERATE = "MODERATE"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"


class IncidentType(str, Enum):
    LANDSLIDE = "LANDSLIDE"
    CRACK = "CRACK"
    ROAD_BLOCKAGE = "ROAD_BLOCKAGE"
    FLASH_FLOOD = "FLASH_FLOOD"
    SLOPE_MOVEMENT = "SLOPE_MOVEMENT"
    OTHER = "OTHER"


class RoadStatus(str, Enum):
    OPEN = "OPEN"
    BLOCKED = "BLOCKED"
    PARTIALLY_BLOCKED = "PARTIALLY_BLOCKED"
    UNKNOWN = "UNKNOWN"


# ─── Prediction ───────────────────────────────────────────────────────────────

class PredictionRequest(BaseModel):
    # Geotechnical & Meteorological Inputs (Manual / Mobile Telemetry)
    rainfall_mm: Optional[float] = Field(default=None, ge=0, le=1000, description="Rainfall in last 24 hours (mm)")
    slope_deg: Optional[float] = Field(default=None, ge=0, le=90, description="Slope angle in degrees")
    soil_moisture_pct: Optional[float] = Field(default=None, ge=0, le=100, description="Soil moisture percentage")
    antecedent_rain_3d: Optional[float] = Field(default=None, ge=0, le=2000, description="Antecedent rainfall over 3 days (mm)")

    # BhuRakshak ML Pipeline Features (From GIS feature_extraction.py)
    elevation: Optional[float] = Field(default=None, description="Elevation in meters MSL")
    slope: Optional[float] = Field(default=None, description="Slope angle in degrees")
    rainfall_previous_1d: Optional[float] = Field(default=None, description="Precipitation previous 1 day (mm)")
    rainfall_previous_3d: Optional[float] = Field(default=None, description="Precipitation previous 3 days (mm)")
    rainfall_previous_7d: Optional[float] = Field(default=None, description="Precipitation previous 7 days (mm)")
    lithology_group: Optional[str] = Field(default=None, description="GLiM lithological group")
    land_cover: Optional[str] = Field(default=None, description="ESA WorldCover land cover class")

    # Spatial & Temporal Coordinates
    latitude: float = Field(default=0.0, description="Latitude of the location")
    longitude: float = Field(default=0.0, description="Longitude of the location")
    date: Optional[str] = Field(default=None, description="Date for rainfall calculation (YYYY-MM-DD)")

    class Config:
        json_schema_extra = {
            "example": {
                "latitude": 27.3389,
                "longitude": 88.6065,
                "rainfall_mm": 85.0,
                "slope_deg": 38.5,
                "soil_moisture_pct": 78.0,
                "antecedent_rain_3d": 195.0
            }
        }



class PredictionResponse(BaseModel):
    risk_level: RiskLevel
    probability: float = Field(..., ge=0.0, le=1.0)
    confidence: float = Field(..., ge=0.0, le=1.0)
    factors: dict[str, float]
    recommendation: str
    is_mock: bool = False
    model_version: str = "1.0.0"


# ─── Alerts ───────────────────────────────────────────────────────────────────

class AlertCreate(BaseModel):
    title: str = Field(..., min_length=3, max_length=200)
    description: str = Field(default="", max_length=1000)
    severity: AlertSeverity
    affected_district: str = Field(default="")
    affected_villages: list[str] = Field(default_factory=list)
    latitude: float = Field(default=0.0)
    longitude: float = Field(default=0.0)

    class Config:
        json_schema_extra = {
            "example": {
                "title": "Landslide Warning — East Sikkim",
                "description": "Heavy rainfall has exceeded threshold. Risk of landslide on NH-10 near Rangpo.",
                "severity": "HIGH",
                "affected_district": "East Sikkim",
                "affected_villages": ["Rangpo", "Singtam"],
                "latitude": 27.17,
                "longitude": 88.53
            }
        }


class AlertResponse(BaseModel):
    id: str
    title: str
    description: str
    severity: AlertSeverity
    affected_district: str
    affected_villages: list[str]
    latitude: float
    longitude: float
    issued_at: int
    is_active: bool
    fcm_sent: bool = False


# ─── Sensor Telemetry ─────────────────────────────────────────────────────────

class SensorTelemetry(BaseModel):
    sensor_id: str = Field(..., description="Unique sensor node ID")
    location_name: str = Field(default="", description="Human-readable location name")
    latitude: float = Field(default=0.0)
    longitude: float = Field(default=0.0)
    tilt_angle_deg: Optional[float] = Field(default=None, ge=-90, le=90)
    soil_moisture_pct: Optional[float] = Field(default=None, ge=0, le=100)
    pore_water_pressure_kpa: Optional[float] = Field(default=None)
    rainfall_mm_last_hour: Optional[float] = Field(default=None, ge=0)
    temperature_c: Optional[float] = Field(default=None)
    battery_pct: Optional[float] = Field(default=None, ge=0, le=100)

    class Config:
        json_schema_extra = {
            "example": {
                "sensor_id": "SLOPE_NODE_04",
                "location_name": "NH-10 KM 24, East Sikkim",
                "latitude": 27.17,
                "longitude": 88.53,
                "tilt_angle_deg": 14.2,
                "soil_moisture_pct": 82.5,
                "pore_water_pressure_kpa": 45.1,
                "rainfall_mm_last_hour": 12.3,
                "temperature_c": 18.5,
                "battery_pct": 87.0
            }
        }


class SensorResponse(BaseModel):
    sensor_id: str
    location_name: str
    latitude: float
    longitude: float
    tilt_angle_deg: Optional[float]
    soil_moisture_pct: Optional[float]
    pore_water_pressure_kpa: Optional[float]
    rainfall_mm_last_hour: Optional[float]
    temperature_c: Optional[float]
    battery_pct: Optional[float]
    risk_level: Optional[RiskLevel]
    last_updated: int
    alert_triggered: bool = False


# ─── FCM Notification ─────────────────────────────────────────────────────────

class FCMBroadcastRequest(BaseModel):
    title: str
    body: str
    topic: str = Field(default="ner-alerts", description="FCM topic to broadcast to")
    data: dict[str, str] = Field(default_factory=dict)


class FCMResponse(BaseModel):
    success: bool
    message_id: Optional[str] = None
    error: Optional[str] = None


# ─── SOS ──────────────────────────────────────────────────────────────────────

class SOSRequest(BaseModel):
    uid: str
    name: str
    latitude: float
    longitude: float
    message: str = "SOS — Need Help!"


# ─── Health ───────────────────────────────────────────────────────────────────

class HealthResponse(BaseModel):
    status: str
    version: str
    firebase_connected: bool
    ml_model_loaded: bool
    environment: str
