import logging
import os
from typing import Optional, Any
from app.config import get_settings

logger = logging.getLogger("ner_backend.firebase")

# Check if firebase_admin is installed and credentials exist
_firebase_initialized = False
_db = None

try:
    import json
    import base64
    import firebase_admin
    from firebase_admin import credentials, firestore, messaging

    settings = get_settings()
    cred_path = settings.firebase_credentials_path

    # 1. Direct JSON string in environment variable (Ideal for Render / Heroku / Container deployments)
    if settings.firebase_service_account_json:
        try:
            cred_dict = json.loads(settings.firebase_service_account_json)
            cred = credentials.Certificate(cred_dict)
            firebase_admin.initialize_app(cred)
            _db = firestore.client()
            _firebase_initialized = True
            logger.info("Firebase Admin SDK initialized successfully via FIREBASE_SERVICE_ACCOUNT_JSON.")
        except Exception as json_err:
            logger.warning(f"Failed to parse FIREBASE_SERVICE_ACCOUNT_JSON: {json_err}")

    # 2. Base64-encoded JSON string in environment variable
    elif settings.firebase_service_account_base64:
        try:
            decoded = base64.b64decode(settings.firebase_service_account_base64).decode("utf-8")
            cred_dict = json.loads(decoded)
            cred = credentials.Certificate(cred_dict)
            firebase_admin.initialize_app(cred)
            _db = firestore.client()
            _firebase_initialized = True
            logger.info("Firebase Admin SDK initialized successfully via FIREBASE_SERVICE_ACCOUNT_BASE64.")
        except Exception as b64_err:
            logger.warning(f"Failed to parse FIREBASE_SERVICE_ACCOUNT_BASE64: {b64_err}")

    # 3. File path on disk (Local development with serviceAccountKey.json)
    elif os.path.exists(cred_path):
        cred = credentials.Certificate(cred_path)
        firebase_admin.initialize_app(cred)
        _db = firestore.client()
        _firebase_initialized = True
        logger.info(f"Firebase Admin SDK initialized successfully with credentials file: {cred_path}")

    # 4. Attempt Google Cloud Application Default Credentials if project_id is specified
    elif settings.firebase_project_id:
        try:
            firebase_admin.initialize_app(options={"projectId": settings.firebase_project_id})
            _db = firestore.client()
            _firebase_initialized = True
            logger.info(f"Firebase Admin SDK initialized with project ID: {settings.firebase_project_id}")
        except Exception as adc_err:
            logger.info(
                f"Application Default Credentials not present on this host ({adc_err}). "
                "Running in fallback mode (in-memory mock alerts & sensor telemetry). "
                "To enable live Firestore & FCM on Render, set FIREBASE_SERVICE_ACCOUNT_JSON in Render environment variables."
            )
    else:
        logger.info(
            "No Firebase credentials configured. Running in fallback mode with in-memory state. "
            "ML inference, weather, and sensor simulation are fully functional."
        )
except Exception as e:
    logger.warning(f"Firebase Admin SDK initialization bypassed: {e}. Running in fallback mode.")


def is_firebase_connected() -> bool:
    return _firebase_initialized


# In-memory mock storage for demo / offline development
_mock_alerts: list[dict[str, Any]] = [
    {
        "id": "ALERT_MOCK_01",
        "title": "Landslide Hazard Alert — NH-10 Rangpo",
        "description": "Continuous rainfall exceeded 120mm in 24h. Slope saturation critical near KM 28.",
        "severity": "HIGH",
        "affected_district": "East Sikkim",
        "affected_villages": ["Rangpo", "Singtam", "Dikchu"],
        "latitude": 27.1765,
        "longitude": 88.5321,
        "issued_at": 1725700000000,
        "is_active": True,
        "fcm_sent": True
    },
    {
        "id": "ALERT_MOCK_02",
        "title": "Flash Flood & Mudflow Watch — Guwahati Hills",
        "description": "Intense hill cutting in Narakasur Hills. Risk of mudflow affecting residential areas.",
        "severity": "MODERATE",
        "affected_district": "Kamrup Metropolitan",
        "affected_villages": ["Narakasur", "Kahilipara"],
        "latitude": 26.1445,
        "longitude": 91.7362,
        "issued_at": 1725710000000,
        "is_active": True,
        "fcm_sent": True
    }
]

_mock_sensors: dict[str, dict[str, Any]] = {
    "NODE_SIKKIM_01": {
        "sensor_id": "NODE_SIKKIM_01",
        "location_name": "NH-10 KM 24, East Sikkim",
        "latitude": 27.1765,
        "longitude": 88.5321,
        "tilt_angle_deg": 12.4,
        "soil_moisture_pct": 79.2,
        "pore_water_pressure_kpa": 42.0,
        "rainfall_mm_last_hour": 14.5,
        "temperature_c": 19.2,
        "battery_pct": 89.0,
        "risk_level": "HIGH",
        "last_updated": 1725712000000,
        "alert_triggered": False
    },
    "NODE_ASSAM_02": {
        "sensor_id": "NODE_ASSAM_02",
        "location_name": "Dima Hasao Hill Section, Assam",
        "latitude": 25.1824,
        "longitude": 93.0182,
        "tilt_angle_deg": 4.1,
        "soil_moisture_pct": 54.0,
        "pore_water_pressure_kpa": 18.5,
        "rainfall_mm_last_hour": 3.2,
        "temperature_c": 24.1,
        "battery_pct": 94.0,
        "risk_level": "LOW",
        "last_updated": 1725712000000,
        "alert_triggered": False
    }
}


async def broadcast_fcm_notification(
    title: str,
    body: str,
    topic: str = "ner-alerts",
    data: Optional[dict[str, str]] = None
) -> dict[str, Any]:
    """
    Broadcasts high-priority push notification via FCM topic.
    Can be received by all Android clients subscribed to this topic.
    """
    if not _firebase_initialized:
        logger.info(f"[MOCK FCM] Broadcast to topic '{topic}': {title} — {body}")
        return {"success": True, "message_id": f"mock-msg-{os.urandom(4).hex()}", "mock": True}

    try:
        from firebase_admin import messaging

        message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body,
            ),
            data=data or {},
            topic=topic,
            android=messaging.AndroidConfig(
                priority="high",
                notification=messaging.AndroidNotification(
                    channel_id="ner_critical_alerts",
                    sound="default",
                    priority="max"
                )
            )
        )
        response = messaging.send(message)
        logger.info(f"FCM message sent successfully: {response}")
        return {"success": True, "message_id": response}
    except Exception as e:
        logger.error(f"FCM broadcast failed: {e}")
        return {"success": False, "error": str(e)}


async def save_alert(alert_dict: dict[str, Any]) -> str:
    """Save alert to Firestore 'alerts' collection or mock list."""
    if _firebase_initialized and _db:
        doc_ref = _db.collection("alerts").document()
        alert_dict["id"] = doc_ref.id
        doc_ref.set(alert_dict)
        return doc_ref.id
    else:
        alert_id = f"ALERT_{os.urandom(4).hex().upper()}"
        alert_dict["id"] = alert_id
        _mock_alerts.insert(0, alert_dict)
        return alert_id


async def get_active_alerts_list() -> list[dict[str, Any]]:
    """Retrieve active alerts from Firestore or mock storage."""
    if _firebase_initialized and _db:
        try:
            docs = _db.collection("alerts").where("is_active", "==", True).stream()
            return [doc.to_dict() for doc in docs]
        except Exception as e:
            logger.error(f"Error fetching Firestore alerts: {e}")
            return _mock_alerts
    return [a for a in _mock_alerts if a.get("is_active", True)]


async def update_sensor_data(sensor_dict: dict[str, Any]) -> dict[str, Any]:
    """Ingest sensor telemetry into Firestore or mock state."""
    sensor_id = sensor_dict["sensor_id"]
    if _firebase_initialized and _db:
        try:
            _db.collection("sensors").document(sensor_id).set(sensor_dict, merge=True)
            # Also log to timeseries subcollection
            _db.collection("sensors").document(sensor_id).collection("telemetry").add(sensor_dict)
        except Exception as e:
            logger.error(f"Error writing sensor telemetry to Firestore: {e}")
    _mock_sensors[sensor_id] = sensor_dict
    return sensor_dict


async def get_all_sensors_list() -> list[dict[str, Any]]:
    """Retrieve all sensors."""
    if _firebase_initialized and _db:
        try:
            docs = _db.collection("sensors").stream()
            result = [doc.to_dict() for doc in docs]
            if result:
                return result
        except Exception as e:
            logger.error(f"Error reading sensors: {e}")
    return list(_mock_sensors.values())


async def record_sos(sos_dict: dict[str, Any]) -> str:
    """Save emergency SOS record and notify responders."""
    sos_id = f"SOS_{os.urandom(4).hex().upper()}"
    sos_dict["id"] = sos_id

    if _firebase_initialized and _db:
        try:
            _db.collection("sos_alerts").document(sos_id).set(sos_dict)
        except Exception as e:
            logger.error(f"Error saving SOS: {e}")

    # Push high-priority FCM notification to emergency responders
    await broadcast_fcm_notification(
        title="🚨 CRITICAL SOS ALERT",
        body=f"Emergency distress from {sos_dict.get('name', 'Citizen')} at Lat: {sos_dict.get('latitude')}, Lon: {sos_dict.get('longitude')}",
        topic="ner-emergency-responders",
        data={"type": "SOS", "sos_id": sos_id, "lat": str(sos_dict.get("latitude")), "lon": str(sos_dict.get("longitude"))}
    )
    return sos_id
