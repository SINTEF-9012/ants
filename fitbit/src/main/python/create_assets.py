#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Create assets for prototype pipeline.

Author:
    Erik Johannes Husom

Created:
    2022-02-25

"""
import sys

import tensorflow as tf
from tensorflow.keras import layers, models, optimizers

from inference import *

def create_training_data(filepath):
    """Create training data set from filepath.

    Args:
        filepath (str): Path to data set.

    Returns:
        X (array): Predictors.
        y (array): Target.

    """

    df = pd.read_csv(filepath)

    # Create predictors
    X = df.to_numpy()[:,1:]
    X = np.asarray(X).astype(np.float32)

    # Create random target values
    y = np.random.random(len(df))

    return X, y

def build_model(input_size):
    """Build Tensorflow model.

    Returns:
        model (object): Tensorflow model.

    """
    tf.random.set_seed(2022)

    model = models.Sequential()
    model.add(layers.Dense(16, activation="relu", input_dim=input_size))
    model.add(layers.Dense(1, activation="relu"))
    model.compile(optimizer="adam", loss="mse", metrics=["mse"])

    return model

def train_model(model, X, y):
    """Trains Tensorflow model.

    Args:
        model (object): Tensorflow model.
        X (array): Predictors.
        y (array): Target.

    Returns:
        model (object): Trained model.

    """

    model.fit(
        X,
        y,
        epochs=10,
        batch_size=256,
        validation_split=0.25,
    )

    return model

def main(filepath):
    # filepath = "~/OneDrive/Datasets/Fitbit/fitbit_export_partners/original_data/fitbit_subject_01/Sleep/Heart Rate Variability Details - 2021-11-26.csv"
    #filepath = sys.argv[1]

    X, y = create_training_data(join(dirname(__file__), filepath))
    X, mean, std = scale_data(X)
    np.savez(join(dirname(__file__), "scaler-lite.npz"), mean=mean, std=std)
    model = build_model(input_size=X.shape[1])
    model = train_model(model, X, y)
    model.save(join(dirname(__file__), "model-lite.h5"))


if __name__ == '__main__': 

    # filepath = "~/OneDrive/Datasets/Fitbit/fitbit_export_partners/original_data/fitbit_subject_01/Sleep/Heart Rate Variability Details - 2021-11-26.csv"
    filepath = sys.argv[1]

    X, y = create_training_data(filepath)
    X, mean, std = scale_data(X)
    np.savez("scaler.npz", mean=mean, std=std)
    model = build_model(input_size=X.shape[1])
    model = train_model(model, X, y)
    model.save("model.h5")
