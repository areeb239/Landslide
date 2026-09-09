from fastapi import APIRouter, Query
from typing import Dict, Any, List
from app.services.imd_service import fetch_imd_district_warning, get_all_ner_imd_bulletins
from app.services.satellite_service import fetch_satellite_earth_observation

router = APIRouter(prefix="/api/v1", tags=["IMD & Satellite Earth Observation"])


@router.get("/imd/warnings", response_model=List[Dict[str, Any]])
async def get_imd_warnings_for_all_states():
    """
    Fetches official India Meteorological Department (IMD) color-coded weather
    bulletins and alerts across all 8 North Eastern Region (NER) states:
    Sikkim, Assam, Meghalaya, Arunachal Pradesh, Nagaland, Manipur, Mizoram, Tripura.
    """
    return await get_all_ner_imd_bulletins()


@router.get("/imd/warning/{state_name}", response_model=Dict[str, Any])
async def get_imd_state_warning(state_name: str):
    """
    Fetches official IMD warning for a specific state (e.g. 'Sikkim', 'Assam', 'Meghalaya').
    """
    return await fetch_imd_district_warning(state_name)


@router.get("/satellite/earth-observation", response_model=Dict[str, Any])
async def get_satellite_data(
    latitude: float = Query(..., ge=-90, le=90, description="Latitude"),
    longitude: float = Query(..., ge=-180, le=180, description="Longitude")
):
    """
    Retrieves satellite-derived data for landslide monitoring:
    1. NASA GPM (Global Precipitation Measurement - IMERG) satellite rainfall
    2. Copernicus Sentinel-1 SAR Soil Moisture Index (SMI) & Ground Saturation
    3. Copernicus GLO-30 Digital Elevation Model (DEM)
    4. Sentinel-2 NDVI Unplanned Hill Cutting Scar Detection
    """
    return await fetch_satellite_earth_observation(latitude, longitude)
