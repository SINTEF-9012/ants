package no.sintef.giot.bhp.core;

public class MathUtils {
    /**
     * Calculates mean value
     *
     * @param array input numeric array
     * @return average value
     */
    public static float calculateMean(float[] array) {
        float mean;
        float sum = 0;

        for (float v : array) {
            sum += v;
        }

        mean = sum / (float) array.length;

        return mean;
    }

    /**
     * Finds minimum value in an array
     *
     * @param array input array
     * @return minimum value
     */
    public static float findMin(float[] array) {
        float min = array[0];

        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    /**
     * Finds maximum value in an array
     *
     * @param array input array
     * @return maximum value
     */
    public static float findMax(float[] array) {
        float max = array[0];

        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    /**
     * Pre-processes raw data
     *
     * @param rawData input data
     * @return float array
     */
    public float[] preprocess(float[][] rawData) {

        int numRawFeatures = rawData[1].length;
        int numFeatures = 3 * numRawFeatures;
        float[] preprocessedData = new float[numFeatures];

        int idx = 0;

        for (int i = 0; i < numRawFeatures; i++) {
            preprocessedData[idx] = calculateMean(rawData[i]);
            preprocessedData[idx + 1] = findMin(rawData[i]);
            preprocessedData[idx + 2] = findMax(rawData[i]);
            idx += 3;
        }

        return preprocessedData;
    }

    /**
     * Calculate variance in the numeric array
     * TODO: check if this method is actually correct and whether it is needed at all. Otherwise, delete.
     * @param array input array
     * @return variance
     */
    public static double calculateVariance(float[] array) {
        float var;
        float mean = calculateMean(array);
        float[] x = null;

        for (int i = 0; i < array.length; i++) {
            x[i] = Math.abs(array[i] - mean);
        }
        var = calculateMean(x);
        return var;
    }
}
