import time
from fastapi import APIRouter
from app.models.schemas import SOSRequest
from app.services.firebase_service import record_sos

router = APIRouter(prefix="/api/v1", tags=["Emergency SOS"])


@router.post("/sos")
async def trigger_sos(req: SOSRequest):
    """
    Emergency SOS endpoint called by Android app when user taps the SOS emergency button:
    1. Records SOS distress call with GPS coordinates
    2. Broadcasts immediate high-priority push notification to emergency responders and district admin.
    """
    sos_dict = {
        "user_id": req.uid,
        "sender_name": req.name,
        "latitude": req.latitude,
        "longitude": req.longitude,
        "message": req.message,
        "timestamp": int(time.time() * 1000),
        "status": "OPEN"
    }

    sos_id = await record_sos(sos_dict)

    return {
        "success": True,
        "sos_id": sos_id,
        "message": "Emergency broadcast dispatched to rescue teams and district control room."
    }
