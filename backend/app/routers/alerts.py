import time
from fastapi import APIRouter, Response
from typing import List
from app.models.schemas import AlertCreate, AlertResponse, FCMBroadcastRequest, FCMResponse
from app.services.firebase_service import (
    save_alert,
    get_active_alerts_list,
    broadcast_fcm_notification
)

router = APIRouter(prefix="/api/v1", tags=["Alerts"])


@router.post("/alerts", response_model=AlertResponse)
async def create_alert(alert_in: AlertCreate):
    """
    Publish an official early warning alert.
    1. Saves to Firestore
    2. Broadcasts high-priority push notification via FCM to 'ner-alerts'
       and district-specific topic so all citizen phones receive instant alerts.
    """
    now_ms = int(time.time() * 1000)
    alert_dict = {
        "title": alert_in.title,
        "description": alert_in.description,
        "severity": alert_in.severity.value,
        "affected_district": alert_in.affected_district,
        "affected_villages": alert_in.affected_villages,
        "latitude": alert_in.latitude,
        "longitude": alert_in.longitude,
        "issued_at": now_ms,
        "is_active": True,
        "fcm_sent": False
    }

    # Save to storage / Firestore
    alert_id = await save_alert(alert_dict)
    alert_dict["id"] = alert_id

    # Broadcast to FCM
    fcm_result = await broadcast_fcm_notification(
        title=f"⚠️ {alert_in.severity.value} WARNING: {alert_in.title}",
        body=alert_in.description,
        topic="ner-alerts",
        data={
            "alert_id": alert_id,
            "severity": alert_in.severity.value,
            "district": alert_in.affected_district,
            "lat": str(alert_in.latitude),
            "lon": str(alert_in.longitude)
        }
    )

    alert_dict["fcm_sent"] = fcm_result.get("success", False)

    return AlertResponse(**alert_dict)


@router.get("/alerts", response_model=List[AlertResponse])
async def list_active_alerts():
    """
    Retrieve all current active landslide and slope hazard alerts.
    """
    alerts_data = await get_active_alerts_list()
    return [AlertResponse(**a) for a in alerts_data]


@router.post("/alerts/broadcast-fcm", response_model=FCMResponse)
async def manual_fcm_broadcast(req: FCMBroadcastRequest):
    """
    Directly trigger an FCM push notification across subscribed Android devices.
    """
    res = await broadcast_fcm_notification(
        title=req.title,
        body=req.body,
        topic=req.topic,
        data=req.data
    )
    return FCMResponse(
        success=res.get("success", False),
        message_id=res.get("message_id"),
        error=res.get("error")
    )


@router.get("/alerts/cap.xml")
async def get_cap_xml_feed():
    """
    Returns active alerts formatted according to the OASIS Common Alerting Protocol (CAP v1.2),
    which is the standard required by NDMA (National Disaster Management Authority) and
    WMO (World Meteorological Organization).
    """
    alerts = await get_active_alerts_list()
    now_iso = time.strftime("%Y-%m-%dT%H:%M:%S+05:30", time.localtime())

    xml_entries = ""
    for a in alerts:
        xml_entries += f"""
  <info>
    <category>Geo</category>
    <event>Landslide / Slope Failure Warning</event>
    <urgency>Immediate</urgency>
    <severity>{a.get('severity', 'High')}</severity>
    <certainty>Observed</certainty>
    <eventCode>
      <valueName>SAME</valueName>
      <value>LSW</value>
    </eventCode>
    <headline>{a.get('title', '')}</headline>
    <description>{a.get('description', '')}</description>
    <area>
      <areaDesc>{a.get('affected_district', 'North East Region')}</areaDesc>
      <circle>{a.get('latitude', 0.0)},{a.get('longitude', 0.0)},10.0</circle>
    </area>
  </info>"""

    cap_xml = f"""<?xml version="1.0" encoding="UTF-8"?>
<alert xmlns="urn:oasis:names:tc:emergency:cap:1.2">
  <identifier>NER-LANDSLIDE-{int(time.time())}</identifier>
  <sender>ner-landslide-warning-system@gov.in</sender>
  <sent>{now_iso}</sent>
  <status>Actual</status>
  <msgType>Alert</msgType>
  <scope>Public</scope>
  {xml_entries}
</alert>"""

    return Response(content=cap_xml, media_type="application/xml")
