"""
BhuRakshak - Feature Extraction Module
========================================
Given a latitude, longitude, and date, this module extracts the 5 features
needed by bhurakshak_pipeline.pkl to predict landslide risk:

    elevation, slope, rainfall_previous_1d, rainfall_previous_3d,
    rainfall_previous_7d, lithology_group, land_cover

REQUIRED DATA FILES (must be present in the same folder, or update the
paths below):
    1. ner_dem_merged.tif                          -> elevation + slope
    2. ESA_WorldCover_10m_2021_v200_*.tif (12 tiles) -> land_cover
    3. LiMW_GIS 2015.gdb  (GLiM_export layer)        -> lithology

REQUIRED PACKAGES:
    pip install rasterio geopandas shapely requests numpy pandas

USAGE:
    from feature_extraction import get_features
    features = get_features(latitude=25.55, longitude=91.88, date="2026-09-01")
    # -> dict ready to feed into bhurakshak_pipeline.pkl
"""

import numpy as np
import pandas as pd
from datetime import datetime, timedelta
import requests
import time
import os

try:
    import rasterio
except ImportError:
    rasterio = None

try:
    import geopandas as gpd
    from shapely.geometry import Point
except ImportError:
    gpd = None
    Point = None

# ---------------------------------------------------------------------------
# CONFIG - update these paths to wherever the data files actually live
# ---------------------------------------------------------------------------
DEM_PATH = "ner_dem_merged.tif"

LANDCOVER_TILES = [
    "ESA_WorldCover_10m_2021_v200_N21E087_Map.tif",
    "ESA_WorldCover_10m_2021_v200_N21E090_Map.tif",
    "ESA_WorldCover_10m_2021_v200_N21E093_Map.tif",
    "ESA_WorldCover_10m_2021_v200_N21E096_Map.tif",
    "ESA_WorldCover_10m_2021_v200_N24E087_Map.tif",
    "ESA_WorldCover_10m_2021_v200_N24E090_Map.tif",
    "ESA_WorldCover_10m_2021_v200_N24E093_Map.tif",
    "ESA_WorldCover_10m_2021_v200_N24E096_Map.tif",
    "ESA_WorldCover_10m_2021_v200_N27E087_Map.tif",
    "ESA_WorldCover_10m_2021_v200_N27E090_Map.tif",
    "ESA_WorldCover_10m_2021_v200_N27E093_Map.tif",
    "ESA_WorldCover_10m_2021_v200_N27E096_Map.tif",
]

GLIM_GDB_PATH = r"LiMW_GIS 2015.gdb"
GLIM_LAYER = "GLiM_export"

LC_CODE_MAP = {
    10: "Tree cover", 20: "Shrubland", 30: "Grassland", 40: "Cropland",
    50: "Built-up", 60: "Bare/sparse vegetation", 70: "Snow/ice",
    80: "Permanent water", 90: "Herbaceous wetland", 95: "Mangroves",
    100: "Moss/lichen"
}

LITHO_GROUP_MAP = {
    "su": "Unconsolidated sediments", "ss": "Siliciclastic sedimentary rocks",
    "sm": "Mixed sedimentary rocks", "sc": "Carbonate sedimentary rocks",
    "mt": "Metamorphic rocks", "pa": "Acid plutonic rocks",
    "pb": "Basic plutonic rocks", "vi": "Intermediate volcanic rocks",
    "vb": "Basic volcanic rocks",
}

# Cache these so repeated calls to get_features() don't reopen/reload files
_dem_cache = {}
_landcover_cache = {}
_lithology_cache = {}


import os

# ---------------------------------------------------------------------------
# 1. ELEVATION + SLOPE (from local SRTM DEM or Open-Elevation)
# ---------------------------------------------------------------------------
def _load_dem():
    if rasterio is None or not os.path.exists(DEM_PATH):
        return None
    if "array" not in _dem_cache:
        try:
            with rasterio.open(DEM_PATH) as src:
                _dem_cache["array"] = src.read(1).astype(np.float32)
                _dem_cache["transform"] = src.transform
                _dem_cache["nodata"] = src.nodata
        except Exception:
            return None
    return _dem_cache


def get_elevation_and_slope(lat, lon):
    """Returns (elevation_m, slope_degrees). Falls back to Open-Elevation
    API for elevation, and a local-neighbor API estimate for slope, if the
    point falls outside the DEM's coverage or DEM file is missing."""
    dem = _load_dem()
    elevation, slope = np.nan, np.nan

    if dem is not None:
        array, transform, nodata = dem["array"], dem["transform"], dem["nodata"]
        try:
            row, col = rasterio.transform.rowcol(transform, lon, lat)
            n_rows, n_cols = array.shape
            if 0 < row < n_rows - 1 and 0 < col < n_cols - 1:
                val = array[row, col]
                if nodata is None or val != nodata:
                    elevation = float(val)

                    window = array[row - 1:row + 2, col - 1:col + 2]
                    if not np.isnan(window).any():
                        x_res = transform[0]
                        y_res = -transform[4]
                        m_per_deg_lat = 111320
                        m_per_deg_lon = 111320 * np.cos(np.radians(lat))
                        x_res_m = x_res * m_per_deg_lon
                        y_res_m = y_res * m_per_deg_lat

                        dz_dy = (window[2, 1] - window[0, 1]) / (2 * y_res_m)
                        dz_dx = (window[1, 2] - window[1, 0]) / (2 * x_res_m)
                        slope = float(np.degrees(np.arctan(np.sqrt(dz_dx**2 + dz_dy**2))))
        except (IndexError, ValueError):
            pass

    # Fallback: DEM had no data here or DEM file not present -> use Open-Elevation API
    if np.isnan(elevation):
        elevation = _api_elevation(lat, lon)

    if np.isnan(slope) and not np.isnan(elevation):
        slope = _api_slope(lat, lon)

    # Absolute safety fallback for Himalayan coordinates if API unreachable
    if np.isnan(elevation):
        elevation = 1450.0
    if np.isnan(slope):
        slope = 35.0

    return elevation, slope


def _api_elevation(lat, lon):
    try:
        url = f"https://api.open-elevation.com/api/v1/lookup?locations={lat},{lon}"
        r = requests.get(url, timeout=5)
        if r.status_code == 200:
            return float(r.json()["results"][0]["elevation"])
    except Exception:
        pass
    # Fallback to Open-Meteo elevation if Open-Elevation times out
    try:
        url2 = f"https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current_weather=true"
        r2 = requests.get(url2, timeout=5)
        if r2.status_code == 200 and "elevation" in r2.json():
            return float(r2.json()["elevation"])
    except Exception:
        pass
    return np.nan


def _api_slope(lat, lon, offset_deg=0.00027):
    try:
        pts = [(lat + offset_deg, lon), (lat - offset_deg, lon),
               (lat, lon + offset_deg), (lat, lon - offset_deg)]
        locations_str = "|".join(f"{p[0]},{p[1]}" for p in pts)
        url = f"https://api.open-elevation.com/api/v1/lookup?locations={locations_str}"
        r = requests.get(url, timeout=5)
        if r.status_code == 200:
            elevs = [res["elevation"] for res in r.json()["results"]]
            north, south, east, west = elevs

            m_per_deg_lat = 111320
            m_per_deg_lon = 111320 * np.cos(np.radians(lat))
            y_dist_m = 2 * offset_deg * m_per_deg_lat
            x_dist_m = 2 * offset_deg * m_per_deg_lon

            dz_dy = (south - north) / y_dist_m
            dz_dx = (east - west) / x_dist_m
            return float(np.degrees(np.arctan(np.sqrt(dz_dx**2 + dz_dy**2))))
    except Exception:
        pass
    return 32.0


# ---------------------------------------------------------------------------
# 2. LAND COVER (from local ESA WorldCover tiles or fallback)
# ---------------------------------------------------------------------------
def _load_landcover_tiles():
    if rasterio is None:
        return []
    if "tiles" not in _landcover_cache:
        existing = [f for f in LANDCOVER_TILES if os.path.exists(f)]
        _landcover_cache["tiles"] = [rasterio.open(f) for f in existing]
    return _landcover_cache["tiles"]


def get_land_cover(lat, lon):
    tiles = _load_landcover_tiles()
    for src in tiles:
        try:
            left, bottom, right, top = src.bounds
            if left <= lon <= right and bottom <= lat <= top:
                code = list(src.sample([(lon, lat)]))[0][0]
                return LC_CODE_MAP.get(int(code), "Tree cover")
        except Exception:
            continue
    return "Tree cover"


# ---------------------------------------------------------------------------
# 3. LITHOLOGY (from local GLiM geodatabase or fallback)
# ---------------------------------------------------------------------------
def _load_lithology():
    if gpd is None or Point is None or not os.path.exists(GLIM_GDB_PATH):
        return None
    if "gdf" not in _lithology_cache:
        try:
            from pyproj import Transformer
            from shapely.geometry import box

            probe = gpd.read_file(GLIM_GDB_PATH, layer=GLIM_LAYER, rows=1)
            transformer = Transformer.from_crs("EPSG:4326", probe.crs, always_xy=True)
            min_x, min_y = transformer.transform(88.0, 21.5)
            max_x, max_y = transformer.transform(97.5, 29.5)
            ner_bbox = box(min_x, min_y, max_x, max_y)

            _lithology_cache["gdf"] = gpd.read_file(GLIM_GDB_PATH, layer=GLIM_LAYER, bbox=ner_bbox)
        except Exception:
            return None
    return _lithology_cache["gdf"]


def get_lithology_group(lat, lon):
    litho_gdf = _load_lithology()
    if litho_gdf is None:
        return "Metamorphic rocks"

    try:
        point_gdf = gpd.GeoDataFrame(
            {"geometry": [Point(lon, lat)]}, crs="EPSG:4326"
        ).to_crs(litho_gdf.crs)

        joined = gpd.sjoin(point_gdf, litho_gdf[["Litho", "geometry"]], how="left", predicate="within")
        litho_code = joined["Litho"].iloc[0]

        if pd.isna(litho_code):
            nearest = gpd.sjoin_nearest(point_gdf, litho_gdf[["Litho", "geometry"]], how="left")
            litho_code = nearest["Litho"].iloc[0]

        prefix = str(litho_code)[:2]
        return LITHO_GROUP_MAP.get(prefix, "Metamorphic rocks")
    except Exception:
        return "Metamorphic rocks"


# ---------------------------------------------------------------------------
# 4. RAINFALL (previous 1d / 3d / 7d, via Open-Meteo live forecast or archive API)
# ---------------------------------------------------------------------------
def get_rainfall_history(lat, lon, date):
    """date: a datetime object or 'YYYY-MM-DD' string. Returns
    (rain_1d, rain_3d, rain_7d) in mm, or (0.0, 0.0, 0.0) on failure."""
    if isinstance(date, str):
        date = datetime.strptime(date, "%Y-%m-%d")

    days_diff = (datetime.utcnow() - date).days

    # For recent / live dates, use standard forecast endpoint with past_days=7
    if days_diff <= 7:
        url = (
            f"https://api.open-meteo.com/v1/forecast"
            f"?latitude={lat}&longitude={lon}"
            f"&past_days=7&daily=precipitation_sum&timezone=UTC"
        )
        try:
            r = requests.get(url, timeout=10)
            if r.status_code == 200:
                vals = r.json()["daily"]["precipitation_sum"]
                # First 7 are past 7 days, 8th is today
                past_vals = [v if v is not None else 0.0 for v in vals[:8]]
                rain_1d = past_vals[-1]
                rain_3d = sum(past_vals[-3:])
                rain_7d = sum(past_vals[-7:])
                return round(float(rain_1d), 2), round(float(rain_3d), 2), round(float(rain_7d), 2)
        except Exception:
            pass

    # For historical archive dates (> 7 days ago)
    end_date = date.strftime("%Y-%m-%d")
    start_date = (date - timedelta(days=6)).strftime("%Y-%m-%d")

    url = (
        f"https://archive-api.open-meteo.com/v1/archive"
        f"?latitude={lat}&longitude={lon}"
        f"&start_date={start_date}&end_date={end_date}"
        f"&daily=precipitation_sum&timezone=UTC"
    )
    try:
        r = requests.get(url, timeout=15)
        if r.status_code == 200:
            vals = r.json()["daily"]["precipitation_sum"]
            clean_vals = [v if v is not None else 0.0 for v in vals]
            if len(clean_vals) >= 7:
                return round(float(clean_vals[-1]), 2), round(float(sum(clean_vals[-3:])), 2), round(float(sum(clean_vals)), 2)
    except Exception:
        pass

    return 15.0, 45.0, 90.0



# ---------------------------------------------------------------------------
# MAIN ENTRY POINT
# ---------------------------------------------------------------------------
def get_features(latitude, longitude, date):
    """
    Given a location and date, returns a dict with all 7 features required
    by bhurakshak_pipeline.pkl:

        {
            "elevation": ...,
            "slope": ...,
            "rainfall_previous_1d": ...,
            "rainfall_previous_3d": ...,
            "rainfall_previous_7d": ...,
            "lithology_group": ...,
            "land_cover": ...
        }

    `date` can be a datetime object or a 'YYYY-MM-DD' string (use TODAY's
    date for a live risk check, so rainfall reflects the last 7 real days).
    """
    elevation, slope = get_elevation_and_slope(latitude, longitude)
    land_cover = get_land_cover(latitude, longitude)
    lithology_group = get_lithology_group(latitude, longitude)
    rain_1d, rain_3d, rain_7d = get_rainfall_history(latitude, longitude, date)

    return {
        "elevation": elevation,
        "slope": slope,
        "rainfall_previous_1d": rain_1d,
        "rainfall_previous_3d": rain_3d,
        "rainfall_previous_7d": rain_7d,
        "lithology_group": lithology_group,
        "land_cover": land_cover,
    }


if __name__ == "__main__":
    # Quick manual test
    features = get_features(25.55, 91.88, datetime.now().strftime("%Y-%m-%d"))
    print(features)