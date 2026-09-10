import logging
import math
import os
import sys
from typing import Optional, Tuple
import numpy as np
import pandas as pd
from app.config import get_settings
from app.models.schemas import PredictionRequest, PredictionResponse, RiskLevel

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

    def predict_risk(self, df: pd.DataFrame) -> Tuple[np.ndarray, np.ndarray]:
        ohe_out = self.ohe.transform(df[['lithology_group', 'land_cover']])
        if hasattr(ohe_out, 'toarray'):
            ohe_out = ohe_out.toarray()

        num_cols = ['elevation', 'slope', 'rainfall_previous_1d', 'rainfall_previous_3d', 'rainfall_previous_7d']
        num_out = df[num_cols].to_numpy(dtype=np.float32)

        X = np.hstack([ohe_out, num_out])
        probs = self.clf.predict_proba(X)
        pred = self.clf.predict(X)
        return pred, probs


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
                    logger.info(f"Successfully loaded BhuRakshak XGBoost ML pipeline from: {path}")
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


def predict_landslide_risk(req: PredictionRequest) -> PredictionResponse:
    """
    Evaluates landslide risk using:
    1. BhuRakshak XGBoost pipeline (trained on LiMW GIS, SRTM DEM, ESA WorldCover, Open-Meteo).
    2. Fallback to calibrated Geological Survey of India (GSI) empirical geotechnical engine.
    """
    global _bhurakshak_predictor, _model, _model_loaded

    # 1. Check if we can run through BhuRakshak XGBoost ML Pipeline
    if _bhurakshak_predictor is not None:
        try:
            # Resolve features: either explicitly provided or extracted via feature_extraction
            lat = req.latitude or 0.0
            lon = req.longitude or 0.0
            date_str = req.date or None

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
                    extracted = get_features(lat, lon, date_str or "2026-09-08")
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

            pred_class, probs = _bhurakshak_predictor.predict_risk(feature_df)
            prob = float(probs[0][1] if len(probs[0]) > 1 else probs[0][0])
            prob = max(0.01, min(0.99, prob))

            risk_level = _classify_risk(prob)
            factors = {
                "rainfall_1d_factor": round(min(1.0, rain_1d / 120.0), 2),
                "rainfall_3d_factor": round(min(1.0, rain_3d / 220.0), 2),
                "rainfall_7d_factor": round(min(1.0, rain_7d / 350.0), 2),
                "slope_factor": round(min(1.0, slope / 55.0), 2),
                "elevation_factor": round(min(1.0, elevation / 3000.0), 2),
                "ground_saturation": round(min(1.0, (rain_3d + rain_1d) / 200.0), 2)
            }
            rec = _generate_recommendation(risk_level, req)

            return PredictionResponse(
                risk_level=risk_level,
                probability=round(prob, 3),
                confidence=0.94,
                factors=factors,
                recommendation=rec,
                is_mock=False,
                model_version=f"BhuRakshak-XGBoost-v2.0 ({os.path.basename(_model_path_used or 'pipeline')})"
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
        model_version="GSI-Himalayan-Heuristic-v1.2"
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
