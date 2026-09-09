import time
from fastapi import APIRouter
from typing import List
from app.models.schemas import SensorTelemetry, SensorResponse, RiskLevel
from app.services.firebase_service import (
    update_sensor_data,
    get_all_sensors_list,
    broadcast_fcm_notification
)

router = APIRouter(prefix="/api/v1", tags=["Sensors & IoT"])


@router.post("/sensors/telemetry", response_model=SensorResponse)
async def ingest_sensor_telemetry(data: SensorTelemetry):
    """
    Ingest real-time IoT node telemetry (Tiltmeters, Time-Domain Reflectometry Soil Moisture,
    Vibrating Wire Piezometers / Pore Pressure, and Rain Gauges).

    Automatically evaluates safety thresholds and triggers FCM early warnings if
    critical slope displacement or ground saturation is detected.
    """
    now_ms = int(time.time() * 1000)

    # Threshold evaluation
    alert_triggered = False
    risk_level = RiskLevel.LOW

    tilt = abs(data.tilt_angle_deg or 0.0)
    moisture = data.soil_moisture_pct or 0.0
    pore_press = data.pore_water_pressure_kpa or 0.0
    rain_rate = data.rainfall_mm_last_hour or 0.0

    if tilt >= 15.0 or (moisture >= 85.0 and pore_press >= 45.0) or (rain_rate >= 25.0 and moisture >= 80.0):
        risk_level = RiskLevel.CRITICAL
        alert_triggered = True
    elif tilt >= 8.0 or moisture >= 75.0 or rain_rate >= 15.0:
        risk_level = RiskLevel.HIGH
    elif tilt >= 4.0 or moisture >= 60.0:
        risk_level = RiskLevel.MODERATE

    sensor_dict = {
        "sensor_id": data.sensor_id,
        "location_name": data.location_name or data.sensor_id,
        "latitude": data.latitude,
        "longitude": data.longitude,
        "tilt_angle_deg": data.tilt_angle_deg,
        "soil_moisture_pct": data.soil_moisture_pct,
        "pore_water_pressure_kpa": data.pore_water_pressure_kpa,
        "rainfall_mm_last_hour": data.rainfall_mm_last_hour,
        "temperature_c": data.temperature_c,
        "battery_pct": data.battery_pct,
        "risk_level": risk_level.value,
        "last_updated": now_ms,
        "alert_triggered": alert_triggered
    }

    await update_sensor_data(sensor_dict)

    if alert_triggered:
        await broadcast_fcm_notification(
            title=f"🚨 CRITICAL SENSOR ALARM: {data.sensor_id}",
            body=f"Sudden ground displacement detected at {data.location_name or 'unnamed site'}. Tilt: {tilt}°, Moisture: {moisture}%. Evacuate slope!",
            topic="ner-alerts",
            data={
                "type": "SENSOR_ALARM",
                "sensor_id": data.sensor_id,
                "lat": str(data.latitude),
                "lon": str(data.longitude)
            }
        )

    return SensorResponse(**sensor_dict)


@router.get("/sensors", response_model=List[SensorResponse])
async def list_sensors():
    """
    Retrieve all registered IoT sensor nodes and their latest telemetry status.
    """
    sensors = await get_all_sensors_list()
    return [SensorResponse(**s) for s in sensors]


@router.post("/sensors/simulate", response_model=List[SensorResponse])
async def simulate_sensor_stream():
    """
    Convenience endpoint for testing & live demos:
    Simulates real-time telemetry from 3 Himalayan field nodes.
    """
    test_nodes = [
        SensorTelemetry(
            sensor_id="NODE_SIKKIM_NH10",
            location_name="NH-10 KM 28 Rangpo Slope",
            latitude=27.1765,
            longitude=88.5321,
            tilt_angle_deg=14.8,
            soil_moisture_pct=83.2,
            pore_water_pressure_kpa=46.5,
            rainfall_mm_last_hour=16.0,
            temperature_c=18.2,
            battery_pct=91.0
        ),
        SensorTelemetry(
            sensor_id="NODE_ASSAM_DIMA_HASAO",
            location_name="Haflong Hill Cut KM 12",
            latitude=25.1824,
            longitude=93.0182,
            tilt_angle_deg=3.5,
            soil_moisture_pct=58.0,
            pore_water_pressure_kpa=14.2,
            rainfall_mm_last_hour=2.0,
            temperature_c=25.0,
            battery_pct=85.0
        ),
        SensorTelemetry(
            sensor_id="NODE_NAGALAND_KOHIMA",
            location_name="NH-29 Phesama Bypass",
            latitude=25.6751,
            longitude=94.1086,
            tilt_angle_deg=9.2,
            soil_moisture_pct=76.4,
            pore_water_pressure_kpa=32.0,
            rainfall_mm_last_hour=8.5,
            temperature_c=20.1,
            battery_pct=78.0
        )
    ]

    responses = []
    for node in test_nodes:
        resp = await ingest_sensor_telemetry(node)
        responses.append(resp)

    return responses
