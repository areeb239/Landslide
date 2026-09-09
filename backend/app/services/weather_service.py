import logging
import httpx
from typing import Optional, Dict, Any
from app.config import get_settings

logger = logging.getLogger("ner_backend.weather")


async def fetch_weather_and_soil(latitude: float, longitude: float) -> Dict[str, Any]:
    """
    Fetches live precipitation, 3-day antecedent rainfall, and soil moisture
    from the Open-Meteo API for coordinates in the North Eastern Region.
    Free service, no API key required.
    """
    settings = get_settings()
    params = {
        "latitude": latitude,
        "longitude": longitude,
        "hourly": "precipitation,temperature_2m,relative_humidity_2m,soil_moisture_0_to_1cm,soil_moisture_1_to_3cm",
        "past_days": 3,
        "forecast_days": 1,
        "timezone": "Asia/Kolkata"
    }

    try:
        async with httpx.AsyncClient(timeout=8.0) as client:
            resp = await client.get(settings.weather_api_base_url, params=params)
            resp.raise_for_status()
            data = resp.json()

            hourly = data.get("hourly", {})
            precip_list = hourly.get("precipitation", [])
            soil_list = hourly.get("soil_moisture_0_to_1cm", [])
            temp_list = hourly.get("temperature_2m", [])
            humidity_list = hourly.get("relative_humidity_2m", [])

            # Compute last 24 hours rainfall
            rainfall_24h = sum(precip_list[-24:]) if len(precip_list) >= 24 else sum(precip_list)

            # Compute 3-day antecedent rainfall (prior 72 hours excluding current 24 hours)
            if len(precip_list) >= 96:
                antecedent_3d = sum(precip_list[:72])
            else:
                antecedent_3d = sum(precip_list[:-24]) if len(precip_list) > 24 else 0.0

            # Latest soil moisture (m³/m³ -> converted to percentage: 0.1 to 0.5 typical, saturate at ~0.50)
            raw_soil = soil_list[-1] if soil_list and soil_list[-1] is not None else 0.35
            soil_moisture_pct = min(100.0, max(0.0, (raw_soil / 0.50) * 100.0))

            current_temp = temp_list[-1] if temp_list else 22.0
            current_humidity = humidity_list[-1] if humidity_list else 80.0

            return {
                "rainfall_mm": round(rainfall_24h, 2),
                "antecedent_rain_3d": round(antecedent_3d, 2),
                "soil_moisture_pct": round(soil_moisture_pct, 1),
                "temperature_c": current_temp,
                "humidity_pct": current_humidity,
                "source": "Open-Meteo Realtime API"
            }
    except Exception as e:
        logger.warning(f"Error fetching real weather for ({latitude}, {longitude}): {e}. Using seasonal estimate.")
        # Fallback realistic seasonal estimate for NER Monsoon
        return {
            "rainfall_mm": 65.0,
            "antecedent_rain_3d": 180.0,
            "soil_moisture_pct": 74.0,
            "temperature_c": 21.5,
            "humidity_pct": 88.0,
            "source": "Monsoon Seasonal Climatology Fallback"
        }
