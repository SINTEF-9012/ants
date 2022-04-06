#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Simple prototype for processing Fitbit data on mobile.

Author:
    Erik Johannes Husom

Created:
    2022-02-25

"""
from io import StringIO
import sys

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from tensorflow.keras import models
from os.path import dirname, join



def read_data_from_file(filepath):
    """Read data from filepath.

    Args:
        filepath (str): Path to data.

    Returns:
        X (array): Predictors, which is the input to a machine learning model.

    """

    df = pd.read_csv(join(dirname(__file__), filepath))

    # Create predictors (skip the first column, which contains the timestamps)
    X = df.to_numpy()[:,1:]
    X = np.asarray(X).astype(np.float32)

    # Print results
    # print(X)

    return X

def read_data_from_csv_string(input_string):
    """Read data from csv-string.

    Args:
        input_string (str): Data passed as a string in csv format.

    Returns:
        X (array): Predictors, which is the input to a machine learning model.

    """

    df = pd.read_csv(StringIO(input_string))

    # Create predictors (skip the first column, which contains the timestamps)
    X = df.to_numpy()[:,1:]
    X = np.asarray(X).astype(np.float32)

    # Print results
    # print(X)

    return X

def read_data_from_csv_string(input_string):
    """Read data from string.

    Args:
        input_string (str): Data passed as a string.

    Returns:
        X (array): Predictors, which is the input to a machine learning model.

    """

    df = pd.read_csv(StringIO(input_string))

    # Create predictors (skip the first column, which contains the timestamps)
    X = df.to_numpy()[:,1:]
    X = np.asarray(X).astype(np.float32)

    # Print results
    # print(X)

    return X

def preprocess_data():
    pass

def scale_data(X, mean=None, std=None):
    """Scale input data.

    Args:
        X (array): Predictors.
        mean (array): Mean of each feature.
        std (array): Standard deviation of each feature.

    Returns:
        scaled_X (array): Scaled predictors.

    """

    if mean is None or std is None:
        mean = np.mean(X, axis=0)
        std = np.std(X, axis=0)
        X_scaled = (X - mean) / std
    else:
        X_scaled = (X - mean) / std

    return X_scaled, mean, std

def infer(model, X):
    """Infer values based on model and input data.

    Args:
        model (object): Tensorflow model.
        X (array): Predictors.

    Returns:
        y (array): Target.

    """

    y = model.predict(X)

    return y

def infer_from_string(string):

    # Load data
    X = read_data_from_csv_string(string)

    # Load model
    model_filepath = "model.h5"
    model = models.load_model(join(dirname(__file__), model_filepath), compile = False) # added extra argument compile = False

    # Load scaler
    scaler = np.load(join(dirname(__file__), "scaler.npz"))
    mean = scaler["mean"]
    std = scaler["std"]

    # Infer
    y = model.predict(X)
    # y = infer(model, X)

    # Print results
    print(y)

def main(filepath):

    # Load data
    X = read_data_from_file(filepath)

    # Load model
    model_filepath = "model.h5"
    model = models.load_model(join(dirname(__file__), model_filepath), compile = False) # added extra argument compile = False

    # Load scaler
    scaler = np.load(join(dirname(__file__), "scaler.npz"))
    mean = scaler["mean"]
    std = scaler["std"]

    # Infer
    y = model.predict(X)
    # y = infer(model, X)

    # Print results
    print(y)

if __name__ == '__main__':

    # Infer from csv-file
    filepath = sys.argv[1]
    main(filepath)

    # Infer from csv-string passed on the command line
    # string = "".join(sys.argv[1:])
    # infer_from_string(string)

