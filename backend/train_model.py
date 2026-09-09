"""
NER Landslide Early Warning System — ML Model Training Script
Trains a Random Forest Classifier on synthetic Himalayan geotechnical & meteorological data
mimicking Geological Survey of India (GSI) & National Remote Sensing Centre (NRSC) records.

Usage:
    python train_model.py
"""

import numpy as np
import joblib
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, roc_auc_score


def generate_synthetic_himalayan_data(n_samples: int = 2500, random_state: int = 42):
    np.random.seed(random_state)

    # 1. 24-hour rainfall in mm (Log-normal distribution reflecting monsoon bursts in NER)
    rainfall_mm = np.random.exponential(scale=45.0, size=n_samples)
    rainfall_mm = np.clip(rainfall_mm, 0.0, 350.0)

    # 2. Slope angle in degrees (Himalayan terrain: 10 to 65 degrees)
    slope_deg = np.random.normal(loc=34.0, scale=10.0, size=n_samples)
    slope_deg = np.clip(slope_deg, 5.0, 70.0)

    # 3. Soil moisture percentage (20% to 98%)
    soil_moisture_pct = 0.3 * rainfall_mm + np.random.normal(loc=45.0, scale=15.0, size=n_samples)
    soil_moisture_pct = np.clip(soil_moisture_pct, 15.0, 99.0)

    # 4. Antecedent 3-day rainfall in mm (Pre-monsoon ground saturation)
    antecedent_rain_3d = np.random.exponential(scale=80.0, size=n_samples)
    antecedent_rain_3d = np.clip(antecedent_rain_3d, 0.0, 600.0)

    # Failure mechanics formula based on Mohr-Coulomb limit equilibrium
    # Probability increases exponentially when slope > 30 deg AND rainfall > 80mm AND moisture > 70%
    z = (
        0.025 * (rainfall_mm - 70.0) +
        0.055 * (slope_deg - 32.0) +
        0.040 * (soil_moisture_pct - 65.0) +
        0.012 * (antecedent_rain_3d - 120.0)
    )

    # Sigmoid link function
    prob_failure = 1.0 / (1.0 + np.exp(-z))

    # Binary label with some geotechnical noise
    noise = np.random.normal(0, 0.08, size=n_samples)
    noisy_prob = np.clip(prob_failure + noise, 0.0, 1.0)
    labels = (noisy_prob >= 0.50).astype(int)

    X = np.column_stack([rainfall_mm, slope_deg, soil_moisture_pct, antecedent_rain_3d])
    return X, labels


def train_and_save():
    print("Generating synthetic Himalayan landslide training records (N=2,500)...")
    X, y = generate_synthetic_himalayan_data()

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

    print(f"Training dataset: {len(X_train)} samples | Test dataset: {len(X_test)} samples")
    print(f"Positive landslide incidents in data: {np.sum(y)} ({np.mean(y)*100:.1f}%)")

    print("\nTraining Random Forest Classifier...")
    model = RandomForestClassifier(
        n_estimators=120,
        max_depth=8,
        min_samples_split=4,
        random_state=42,
        class_weight="balanced"
    )
    model.fit(X_train, y_train)

    # Evaluation
    preds = model.predict(X_test)
    probs = model.predict_proba(X_test)[:, 1]

    auc = roc_auc_score(y_test, probs)
    print(f"\nModel Performance on Test Set:")
    print(f"ROC-AUC Score: {auc:.4f}")
    print("\nClassification Report:")
    print(classification_report(y_test, preds, target_names=["Stable (0)", "Hazard (1)"]))

    feature_names = ["rainfall_mm", "slope_deg", "soil_moisture_pct", "antecedent_rain_3d"]
    print("Feature Importances:")
    for name, imp in zip(feature_names, model.feature_importances_):
        print(f"  - {name:<22}: {imp*100:.1f}%")

    output_file = "landslide_model.joblib"
    joblib.dump(model, output_file)
    print(f"\nTrained model successfully saved to: {output_file}")
    print("You can now start the FastAPI server and it will automatically use this model!")


if __name__ == "__main__":
    train_and_save()
