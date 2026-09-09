from fastapi import APIRouter, Query
from typing import List, Dict, Any
from app.models.schemas import PredictionRequest, PredictionResponse
from app.services.ml_service import predict_landslide_risk
from app.services.weather_service import fetch_weather_and_soil

router = APIRouter(prefix="/api/v1", tags=["Prediction"])


@router.post("/predict", response_model=PredictionResponse)
async def predict_risk(request: PredictionRequest):
    """
    Main endpoint called by the Android application:
    Accepts rainfall (24h), slope angle, soil moisture, antecedent rain (3d),
    and coordinates to output landslide risk level, failure probability,
    contributing factors, and disaster mitigation recommendation.
    """
    return predict_landslide_risk(request)


@router.get("/predict/location", response_model=Dict[str, Any])
async def predict_by_location(
    latitude: float = Query(..., ge=-90, le=90, description="Latitude"),
    longitude: float = Query(..., ge=-180, le=180, description="Longitude"),
    slope_deg: float = Query(default=35.0, ge=0, le=90, description="Estimated slope angle in degrees")
):
    """
    One-click automated prediction:
    Fetches real-time weather, 3-day antecedent rainfall, and soil moisture
    from Open-Meteo for the given coordinate, then runs the prediction model.
    """
    weather = await fetch_weather_and_soil(latitude, longitude)

    req = PredictionRequest(
        rainfall_mm=weather["rainfall_mm"],
        slope_deg=slope_deg,
        soil_moisture_pct=weather["soil_moisture_pct"],
        antecedent_rain_3d=weather["antecedent_rain_3d"],
        latitude=latitude,
        longitude=longitude
    )

    prediction = predict_landslide_risk(req)

    return {
        "location": {"latitude": latitude, "longitude": longitude},
        "weather_data": weather,
        "prediction": prediction
    }


@router.get("/predict/hotspots", response_model=List[Dict[str, Any]])
async def get_hotspot_predictions():
    """
    Returns real-time risk predictions for 8 critical NER mountain corridor zones.
    """
    hotspots = [
        {"name": "NH-10 Sevoke - Gangtok", "state": "Sikkim", "lat": 27.1765, "lon": 88.5321, "slope": 42.0},
        {"name": "Dzongu Special Zone", "state": "North Sikkim", "lat": 27.5210, "lon": 88.5412, "slope": 48.0},
        {"name": "Haflong - Dima Hasao Corridor", "state": "Assam", "lat": 25.1824, "lon": 93.0182, "slope": 36.0},
        {"name": "Guwahati Hill Cutting Zone", "state": "Assam", "lat": 26.1445, "lon": 91.7362, "slope": 28.0},
        {"name": "NH-29 Kohima - Dimapur Pass", "state": "Nagaland", "lat": 25.6751, "lon": 94.1086, "slope": 38.0},
        {"name": "Cherrapunji - Shella Escarpment", "state": "Meghalaya", "lat": 25.2986, "lon": 91.7324, "slope": 45.0},
        {"name": "Aizawl Sairang Highway Cut", "state": "Mizoram", "lat": 23.7271, "lon": 92.7176, "slope": 34.0},
        {"name": "NH-13 Bhalukpong - Bomdila", "state": "Arunachal Pradesh", "lat": 27.2645, "lon": 92.4227, "slope": 44.0}
    ]

    results = []
    for h in hotspots:
        weather = await fetch_weather_and_soil(h["lat"], h["lon"])
        req = PredictionRequest(
            rainfall_mm=weather["rainfall_mm"],
            slope_deg=h["slope"],
            soil_moisture_pct=weather["soil_moisture_pct"],
            antecedent_rain_3d=weather["antecedent_rain_3d"],
            latitude=h["lat"],
            longitude=h["lon"]
        )
        pred = predict_landslide_risk(req)
        results.append({
            "name": h["name"],
            "state": h["state"],
            "latitude": h["lat"],
            "longitude": h["lon"],
            "slope_deg": h["slope"],
            "weather": weather,
            "risk_level": pred.risk_level,
            "probability": pred.probability,
            "factors": pred.factors,
            "recommendation": pred.recommendation
        })

    return results
