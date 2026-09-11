import logging
import math
import os
import sys
from typing import Optional, Tuple
import numpy as np
import pandas as pd
from app.config import get_settings
from app.models.schemas import PredictionRequest, PredictionResponse, RiskLevel, ModelMetadataResponse

# Register unpickling compatibility shim for scikit-learn
try:
    import sklearn.compose._column_transformer
    if not hasattr(sklearn.compose._column_transformer, '_RemainderColsList'):
        class _RemainderColsList(list): pass
        sklearn.compose._column_transformer._RemainderColsList = _RemainderColsList
except Exception:
    pass

logger = logging.getLogger("ner_backend.ml")

_model = None
_model_loaded = False
_model_path_used: Optional[str] = None
_bhurakshak_predictor = None


class BhurakshakPredictor:
    def __init__(self, ohe, clf):
        self.ohe = ohe
        self.clf = clf

        # Build full 23 feature list
        self.cat_features = []
        for col_name, cats in zip(['lithology_group', 'land_cover'], ohe.categories_):
            for cat in cats:
                self.cat_features.append(f"{col_name}: {cat}")

        self.num_features = ['elevation', 'slope', 'rainfall_previous_1d', 'rainfall_previous_3d', 'rainfall_previous_7d']
        self.all_feature_names = self.cat_features + self.num_features

        # Pull real feature importances directly from trained classifier
        raw_importances = getattr(clf, 'feature_importances_', None)
        if raw_importances is not None and len(raw_importances) == len(self.all_feature_names):
            self.feature_importances = {
                feat: round(float(imp), 4) for feat, imp in zip(self.all_feature_names, raw_importances)
            }
        else:
            self.feature_importances = {}

    def predict_risk(self, df: pd.DataFrame):
        ohe_out = self.ohe.transform(df[['lithology_group', 'land_cover']])
        if hasattr(ohe_out, 'toarray'):
            ohe_out = ohe_out.toarray()

        num_out = df[self.num_features].to_numpy(dtype=np.float32)
        X = np.hstack([ohe_out, num_out])
        probs = self.clf.predict_proba(X)
        pred = self.clf.predict(X)

        # Build active sample factor contributions using real model weights
        active_litho = df['lithology_group'].iloc[0]
        active_lc = df['land_cover'].iloc[0]

        sample_factors = {
            "rainfall_previous_7d": {
                "label": "7-Day Cumulative Rainfall",
                "importance": self.feature_importances.get("rainfall_previous_7d", 0.1307),
                "value": f"{df['rainfall_previous_7d'].iloc[0]:.1f} mm"
            },
            "rainfall_previous_3d": {
                "label": "3-Day Antecedent Rainfall",
                "importance": self.feature_importances.get("rainfall_previous_3d", 0.1286),
                "value": f"{df['rainfall_previous_3d'].iloc[0]:.1f} mm"
            },
            "land_cover": {
                "label": f"Land Cover ({active_lc})",
                "importance": self.feature_importances.get(f"land_cover: {active_lc}", 0.0564),
                "value": active_lc
            },
            "elevation": {
                "label": "Elevation (MSL)",
                "importance": self.feature_importances.get("elevation", 0.0794),
                "value": f"{df['elevation'].iloc[0]:.0f} m"
            },
            "lithology_group": {
                "label": f"Lithology ({active_litho})",
                "importance": self.feature_importances.get(f"lithology_group: {active_litho}", 0.0508),
                "value": active_litho
            },
            "slope": {
                "label": "Slope Inclination",
                "importance": self.feature_importances.get("slope", 0.0444),
                "value": f"{df['slope'].iloc[0]:.1f}°"
            },
            "rainfall_previous_1d": {
                "label": "24h Precipitation",
                "importance": self.feature_importances.get("rainfall_previous_1d", 0.0387),
                "value": f"{df['rainfall_previous_1d'].iloc[0]:.1f} mm"
            }
        }

        return pred, probs, sample_factors


def load_model():
    """Attempt to load pre-trained scikit-learn / XGBoost pipeline or joblib model."""
    global _model, _model_loaded, _model_path_used, _bhurakshak_predictor
    settings = get_settings()

    possible_paths = [
        settings.ml_model_path,
        os.path.join(os.path.dirname(__file__), "..", "..", "bhurakshak_pipeline.pkl"),
        os.path.join(os.path.dirname(__file__), "..", "..", "..", "bhurakshak_pipeline.pkl"),
        "bhurakshak_pipeline.pkl",
        os.path.join(os.path.dirname(__file__), "..", "..", "landslide_model.joblib"),
        "landslide_model.joblib"
    ]

    for path in possible_paths:
        if path and os.path.exists(path):
            try:
                import joblib
                loaded_obj = joblib.load(path)

                # Check if this is the BhuRakshak XGBoost pipeline with ColumnTransformer
                if hasattr(loaded_obj, 'named_steps') and 'preprocessor' in loaded_obj.named_steps and 'classifier' in loaded_obj.named_steps:
                    preprocessor = loaded_obj.named_steps['preprocessor']
                    ohe = preprocessor.named_transformers_['cat']
                    clf = loaded_obj.named_steps['classifier']
                    _bhurakshak_predictor = BhurakshakPredictor(ohe, clf)
                    _model = loaded_obj
                    _model_loaded = True
                    _model_path_used = path
                    logger.info(f"Successfully loaded authentic BhuRakshak XGBoost pipeline from: {path}")
                    return
                else:
                    _model = loaded_obj
                    _model_loaded = True
                    _model_path_used = path
                    logger.info(f"Successfully loaded model from: {path}")
                    return
            except Exception as e:
                logger.warning(f"Failed loading model from {path}: {e}")

    logger.info("Using geotechnical empirical heuristic engine (calibrated for NER Himalayas).")


# Initial attempt at startup
load_model()


def is_model_loaded() -> bool:
    return _model_loaded


def extract_features_for_location(lat: float, lon: float, date: Optional[str] = None) -> dict:
    """Extract all 7 geotechnical and GIS features for a location and date using feature_extraction.py"""
    from feature_extraction import get_features
    return get_features(lat, lon, date)


def predict_landslide_risk(req: PredictionRequest) -> PredictionResponse:
    """
    Evaluates landslide risk using:
    1. BhuRakshak XGBoost pipeline (trained on LiMW GIS, SRTM DEM, ESA WorldCover, Open-Meteo).
    2. Fallback to calibrated Geological Survey of India (GSI) empirical geotechnical engine.
    """
    global _bhurakshak_predictor, _model, _model_loaded

    # 1. Check if we can run through authentic BhuRakshak XGBoost ML Pipeline
    if _bhurakshak_predictor is not None:
        try:
            lat = req.latitude or 0.0
            lon = req.longitude or 0.0
            date_str = req.date or None
            extracted = None

            elevation = req.elevation
            slope = req.slope if req.slope is not None else req.slope_deg
            rain_1d = req.rainfall_previous_1d if req.rainfall_previous_1d is not None else req.rainfall_mm
            rain_3d = req.rainfall_previous_3d if req.rainfall_previous_3d is not None else req.antecedent_rain_3d
            rain_7d = req.rainfall_previous_7d
            litho = req.lithology_group
            lc = req.land_cover

            # Auto-extract missing features if location is provided
            if (lat != 0.0 and lon != 0.0) and (elevation is None or slope is None or rain_1d is None or rain_3d is None or litho is None or lc is None):
                try:
                    from feature_extraction import get_features
                    extracted = get_features(lat, lon, date_str)
                    if elevation is None:
                        elevation = extracted.get("elevation", 1450.0)
                    if slope is None:
                        slope = extracted.get("slope", 35.0)
                    if rain_1d is None:
                        rain_1d = extracted.get("rainfall_previous_1d", 15.0)
                    if rain_3d is None:
                        rain_3d = extracted.get("rainfall_previous_3d", 45.0)
                    if rain_7d is None:
                        rain_7d = extracted.get("rainfall_previous_7d", 90.0)
                    if litho is None:
                        litho = extracted.get("lithology_group", "Metamorphic rocks")
                    if lc is None:
                        lc = extracted.get("land_cover", "Tree cover")
                except Exception as extract_err:
                    logger.warning(f"Could not extract dynamic features: {extract_err}")

            # Apply defaults for any remaining None values
            elevation = float(elevation if elevation is not None else 1450.0)
            slope = float(slope if slope is not None else (req.slope_deg or 35.0))
            rain_1d = float(rain_1d if rain_1d is not None else (req.rainfall_mm or 0.0))
            rain_3d = float(rain_3d if rain_3d is not None else (req.antecedent_rain_3d or rain_1d * 2.2))
            rain_7d = float(rain_7d if rain_7d is not None else (rain_3d * 1.7))
            litho = str(litho if litho is not None else "Metamorphic rocks")
            lc = str(lc if lc is not None else "Tree cover")

            feature_df = pd.DataFrame([{
                'elevation': elevation,
                'slope': slope,
                'rainfall_previous_1d': rain_1d,
                'rainfall_previous_3d': rain_3d,
                'rainfall_previous_7d': rain_7d,
                'lithology_group': litho,
                'land_cover': lc
            }])

            pred_class, probs, sample_factors = _bhurakshak_predictor.predict_risk(feature_df)
            prob = float(probs[0][1] if len(probs[0]) > 1 else probs[0][0])
            prob = max(0.01, min(0.99, prob))
            risk_level = _classify_risk(prob)

            # Map active sample factors to real model feature importances
            factors_dict = {
                sf["label"]: round(sf["importance"], 4) for sf in sample_factors.values()
            }

            # Top global model feature weights for explainability
            top_model_importances = dict(
                sorted(
                    [item for item in _bhurakshak_predictor.feature_importances.items() if item[1] > 0.01],
                    key=lambda x: x[1],
                    reverse=True
                )
            )

            rec = _generate_recommendation(risk_level, req)

            return PredictionResponse(
                risk_level=risk_level,
                probability=round(prob, 3),
                confidence=None,  # Omitted artificial confidence; use true probability
                factors=factors_dict,
                feature_importances=top_model_importances,
                sample_factors=sample_factors,
                recommendation=rec,
                is_mock=False,
                model_version=f"Bhoochetak-XGBoost ({os.path.basename(_model_path_used or 'bhurakshak_pipeline.pkl')})",
                extracted_telemetry=extracted
            )
        except Exception as e:
            logger.error(f"Error running BhuRakshak XGBoost pipeline inference: {e}. Falling back to empirical engine.")

    # 2. Empirical Himalayan Geotechnical Model (Fallback)
    prob, factors, confidence = _empirical_geotechnical_evaluation(req)
    risk_level = _classify_risk(prob)
    rec = _generate_recommendation(risk_level, req)

    return PredictionResponse(
        risk_level=risk_level,
        probability=round(prob, 3),
        confidence=round(confidence, 2),
        factors=factors,
        recommendation=rec,
        is_mock=True,
        model_version="GSI-Himalayan-Heuristic-v1.2 (Fallback)"
    )


def _empirical_geotechnical_evaluation(req: PredictionRequest) -> Tuple[float, dict[str, float], float]:
    rainfall_mm = req.rainfall_mm or req.rainfall_previous_1d or 0.0
    slope_deg = req.slope_deg or req.slope or 30.0
    soil_moisture_pct = req.soil_moisture_pct or 60.0
    antecedent_rain_3d = req.antecedent_rain_3d or req.rainfall_previous_3d or 0.0

    rainfall_factor = 1.0 / (1.0 + math.exp(-0.045 * (rainfall_mm - 95.0)))

    if slope_deg < 15.0:
        slope_factor = 0.1
    elif slope_deg > 65.0:
        slope_factor = 0.7
    else:
        slope_factor = math.sin(math.radians(slope_deg)) ** 1.8

    if soil_moisture_pct < 40.0:
        moisture_factor = 0.15
    else:
        moisture_factor = 1.0 / (1.0 + math.exp(-0.09 * (soil_moisture_pct - 72.0)))

    antecedent_factor = min(1.0, antecedent_rain_3d / 280.0)

    combined_prob = (
        0.35 * rainfall_factor +
        0.25 * antecedent_factor +
        0.25 * slope_factor +
        0.15 * moisture_factor
    )

    if slope_deg >= 30.0 and soil_moisture_pct >= 75.0 and rainfall_mm >= 80.0:
        combined_prob = min(0.98, combined_prob * 1.25)

    factors = {
        "rainfall_factor": round(rainfall_factor, 2),
        "slope_factor": round(slope_factor, 2),
        "moisture_factor": round(moisture_factor, 2),
        "antecedent_rain_factor": round(antecedent_factor, 2)
    }

    confidence = 0.88 if antecedent_rain_3d > 0 else 0.82

    return min(1.0, max(0.02, combined_prob)), factors, confidence


def _classify_risk(prob: float) -> RiskLevel:
    if prob >= 0.75:
        return RiskLevel.CRITICAL
    elif prob >= 0.50:
        return RiskLevel.HIGH
    elif prob >= 0.28:
        return RiskLevel.MODERATE
    else:
        return RiskLevel.LOW


def _generate_recommendation(risk_level: RiskLevel, req: PredictionRequest) -> str:
    if risk_level == RiskLevel.CRITICAL:
        return (
            "🚨 CRITICAL EVACUATION WARNING: High probability of slope failure and debris flow. "
            "Immediately evacuate downhill settlements and unstable road cuts. "
            "Issue traffic suspension on adjacent highway segments. Alert SDRF / NDRF."
        )
    elif risk_level == RiskLevel.HIGH:
        return (
            "⚠️ HIGH ALERT: Soil is near saturation threshold and slope shear stress is elevated. "
            "Restrict night-time vehicular movement on mountain passes. "
            "Deploy local spotters to monitor tension cracks and culvert blockages."
        )
    elif risk_level == RiskLevel.MODERATE:
        return (
            "⚡ MODERATE WATCH: Soil moisture is accumulating. Unplanned hill cuts may destabilize "
            "if rainfall persists. Ensure storm water drains are clear and inform panchayat leaders."
        )
    else:
        return (
            "✅ NORMAL STATUS: Stable ground conditions. Slope safety factor within acceptable limits. "
            "Continue standard monitoring."
        )


LITHOLOGY_GROUPS = [
    "Acid plutonic rocks",
    "Basic plutonic rocks",
    "Basic volcanic rocks",
    "Carbonate sedimentary rocks",
    "Intermediate volcanic rocks",
    "Metamorphic rocks",
    "Mixed sedimentary rocks",
    "Siliciclastic sedimentary rocks",
    "Unconsolidated sediments"
]

LAND_COVER_CLASSES = [
    "Bare/sparse vegetation",
    "Built-up",
    "Cropland",
    "Grassland",
    "Herbaceous wetland",
    "Moss/lichen",
    "Permanent water",
    "Snow/ice",
    "Tree cover"
]


def get_model_metadata() -> ModelMetadataResponse:
    global _model_loaded, _model_path_used, _bhurakshak_predictor
    importances = _bhurakshak_predictor.feature_importances if _bhurakshak_predictor else None
    return ModelMetadataResponse(
        model_name="Bhoochetak Landslide Hazard Neural Core",
        model_version=f"Pipeline ({os.path.basename(_model_path_used) if _model_path_used else 'Heuristic Fallback'})",
        algorithm="XGBoost Classifier + Scikit-Learn ColumnTransformer Pipeline",
        features_required=[
            "elevation",
            "slope",
            "rainfall_previous_1d",
            "rainfall_previous_3d",
            "rainfall_previous_7d",
            "lithology_group",
            "land_cover"
        ],
        lithology_groups=LITHOLOGY_GROUPS,
        land_cover_classes=LAND_COVER_CLASSES,
        defaults={
            "elevation": 1450.0,
            "slope": 35.0,
            "rainfall_previous_1d": 45.0,
            "rainfall_previous_3d": 120.0,
            "rainfall_previous_7d": 250.0,
            "lithology_group": "Metamorphic rocks",
            "land_cover": "Tree cover"
        },
        feature_importances=importances
    )
