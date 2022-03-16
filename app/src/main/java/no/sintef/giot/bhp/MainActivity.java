package no.sintef.giot.bhp;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
//import com.chaquo.python.PyException;
import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import no.sintef.giot.bhp.sqlite.DBHelper;
import no.sintef.giot.bhp.sqlite.HeartRateVariability;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  public static final String EXTRA_MESSAGE = "no.sintef.giot.bhp.BHP";
  TextView stressLevelTextView;

  public static float calculateMean(float[] array) {
    float mean;
    float sum = 0;

    for (float v : array) {
      sum += v;
    }

    mean = sum / (float) array.length;

    return mean;
  }

  public static float findMin(float[] array) {
    float min = array[0];

    for (int i = 1; i < array.length; i++) {
      if (array[i] < min) {
        min = array[i];
      }
    }

    return min;
  }

  public static float findMax(float[] array) {
    float max = array[0];

    for (int i = 1; i < array.length; i++) {
      if (array[i] > max) {
        max = array[i];
      }
    }
    return max;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //Start python
    /*if (! Python.isStarted()) {
      Python.start(new AndroidPlatform(this));
    }
    Python py = Python.getInstance();

    // Obtain the system's input stream (available from Chaquopy)
    PyObject sys = py.getModule("sys");
    PyObject io = py.getModule("io");
    // Obtain the right python module
    PyObject module = py.getModule("inference");

    // Redirect the system's output stream to the Python interpreter
    PyObject textOutputStream = io.callAttr("StringIO");
    sys.put("stdout", textOutputStream);

    // Create a string variable that will contain the standard output of the Python interpreter
    String interpreterOutput = "";

    // Execute the Python code
    try {
      module.callAttrThrows("main", "hrv.csv");
      interpreterOutput = textOutputStream.callAttr("getvalue").toString();
    } catch (PyException e){
      // If there's an error, you can obtain its output as well
      // e.g. if you mispell the code
      // Missing parentheses in call to 'print'
      // Did you mean print("text")?
      // <string>, line 1
      interpreterOutput = e.getMessage().toString();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }

    // Outputs in the case of the fibonacci script:
    Log.i(TAG, "RESULT: " + interpreterOutput);*/


    stressLevelTextView = findViewById(R.id.stressLevelValueTextField);
  }

  /**
   * Called when the user taps the Send button
   */
  public void sendMessage(View view) {
    Intent intent = new Intent(this, DisplayMessageActivity.class);
    EditText editText = findViewById(R.id.editTextTextPersonName);
    String message = editText.getText().toString();
    intent.putExtra(EXTRA_MESSAGE, message);
    startActivity(intent);
  }

  public void loadData(View view) {

    try {

      AssetManager mng = getApplicationContext().getAssets();
      InputStream is = mng.open("data3.csv");
      CSVReader reader = new CSVReader(new InputStreamReader(is));

      int subsequenceSize = 1;
      int numRawFeatures = 3;
      // float[][] subsequence = new float[subsequenceSize][numRawFeatures];
      // float[][] subsequence = new float[numRawFeatures][subsequenceSize];
      float[] subsequence = new float[numRawFeatures];

      String[] nextLine;
      float[] preprocessedSubsequence;

      int counter = 0;
      int trueValue = 0;
      // Looping through all data
      //while ((nextLine = reader.readNext()) != null) {
      for (int k = 0; k < 300; k++) {

        // Loop to get subsequence of input data
        for (int i = 0; i < subsequenceSize; i++) {
          nextLine = reader.readNext();

          // Saving each column as a row for easy access later
          for (int j = 0; j < numRawFeatures; j++) {
            // System.out.println("ROW NUMBER");
            // System.out.println(nextLine[0]);
            // subsequence[i][j] = Float.parseFloat(nextLine[j+5]);
            // subsequence[j][i] = Float.parseFloat(nextLine[j+5]);
            subsequence[j] = Float.parseFloat(nextLine[j + 5]);
            trueValue = Integer.parseInt(nextLine[1]);
          }
          // System.out.println(subsequence[i][0] + ", " + subsequence[i][1]);
        }
        // preprocessedSubsequence = preprocess(subsequence);
        // System.out.println(subsequence[0][0]);
        // System.out.println(preprocessedSubsequence);

        Interpreter tflite = new Interpreter(loadModelFile());

        int[] inputShape = tflite.getInputTensor(0).shape();
        int[] outputShape = tflite.getOutputTensor(0).shape();

        // System.out.println("INPUTSHAPE:");
        // System.out.println(inputShape[0]);
        // System.out.println(inputShape[1]);
        // System.out.println("OUTPUTSHAPE:");
        // System.out.println(outputShape[0]);
        // System.out.println(outputShape[1]);
        // System.out.println("SUBSEQSHAPE:");
        // System.out.println(subsequence[0].length);

        DataType inputDataType = tflite.getInputTensor(0).dataType();
        TensorBuffer inputBuffer = TensorBuffer.createFixedSize(inputShape, inputDataType);
        inputBuffer.loadArray(subsequence);
        System.out.println(("INPUT"));
        System.out.println(inputBuffer.getFloatArray()[0]);
        System.out.println(inputBuffer.getFloatArray()[1]);
        System.out.println(inputBuffer.getFloatArray()[2]);
        DataType probabilityDataType = tflite.getOutputTensor(0).dataType();
        TensorBuffer outputProbabilityBuffer =
            TensorBuffer.createFixedSize(outputShape, probabilityDataType);
        tflite.run(inputBuffer.getBuffer(), outputProbabilityBuffer.getBuffer());
        // tflite.run(inputBuffer.getFloatArray(), outputProbabilityBuffer.getFloatArray());
        // tflite.run(inputBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind());

        float output = outputProbabilityBuffer.getFloatArray()[0];

        System.out.println("Prediction: " + output);

        // stressLevelTextView.setText(String.valueOf(output));
        // stressLevelTextView.invalidate();
        System.out.println("True: " + trueValue);

        // if (output > 0.0) {
        //
        // System.out.println("Prediction: " + output);
        // }

        // Map<String, Object> inputs = new HashMap<>();
        // inputs.put("input_1", subsequence[0]);
        // inputs.put("input_2", subsequence[1]);
        // inputs.put("input_3", subsequence[2]);
        // inputs.put("input_4", subsequence[3]);
        // inputs.put("input_5", subsequence[4]);
        // inputs.put("input_6", subsequence[5]);
        //
        // tflite.run(inputs);

        // Thread.sleep(1000);
        // System.out.println("COUNTER: " + counter);
        counter++;
      }

    } catch (Exception e) {
      e.printStackTrace();
      Toast.makeText(this, "The specified file was not found", Toast.LENGTH_SHORT).show();
    }
  }

  public void inference(View view) {

    //Start python
    if (! Python.isStarted()) {
      Python.start(new AndroidPlatform(this));
    }
    Python py = Python.getInstance();

    // Obtain the system's input stream (available from Chaquopy)
    PyObject sys = py.getModule("sys");
    PyObject io = py.getModule("io");
    // Obtain the right python module
    PyObject module = py.getModule("inference");

    // Redirect the system's output stream to the Python interpreter
    PyObject textOutputStream = io.callAttr("StringIO");
    sys.put("stdout", textOutputStream);

    // Create a string variable that will contain the standard output of the Python interpreter
    String interpreterOutput = "";

    // Execute the Python code
    try {
      module.callAttrThrows("main", "hrv.csv");
      interpreterOutput = textOutputStream.callAttr("getvalue").toString();
    } catch (PyException e){
      // If there's an error, you can obtain its output as well
      // e.g. if you mispell the code
      // Missing parentheses in call to 'print'
      // Did you mean print("text")?
      // <string>, line 1
      interpreterOutput = e.getMessage().toString();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }

    // Outputs in the case of the fibonacci script:
    Log.i(TAG, "RESULT: " + interpreterOutput);

  /*
        Object[] inputs = {input0, input1, ...};
        Map<Integer, Object> map_of_indices_to_outputs = new HashMap <>();
        FloatBuffer ith_output = FloatBuffer.allocateDirect(3 * 2 * 4);  // Float tensor, shape 3x2x4.
        ith_output.order(ByteOrder.nativeOrder());
        map_of_indices_to_outputs.put(i, ith_output);
        try (Interpreter interpreter = new Interpreter(file_of_a_tensorflowlite_model)) {
            interpreter.runForMultipleInputsOutputs(inputs, map_of_indices_to_outputs);
        }
  */

  /*
        protected Interpreter tflite;
        android.app.Activity activity;
        tflite = new Interpreter(loadModelFile(activity));
        tflite.run();
  */


  /*
        try (Interpreter interpreter = new Interpreter(file_of_tensorflowlite_model)) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("input_1", input1);
            inputs.put("input_2", input2);
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("output_1", output1);
            interpreter.runSignature(inputs, outputs, "mySignature");
        }
  */

  }

  private MappedByteBuffer loadModelFile() throws IOException {
    // private MappedByteBuffer loadModelFile(Activity activity) throws IOException {

    AssetFileDescriptor fileDescriptor = getApplicationContext().getAssets().openFd("model.tflite");
    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();
    long startOffset = fileDescriptor.getStartOffset();
    long declaredLength = fileDescriptor.getDeclaredLength();
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
  }

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

  //  public static double calculateVariance(double[] array) {
  //    double var;
  //    double mean = calculateMean(array);
  //    double x;
  //
  //    for (int i = 0; i < array.length; i++) {
  //      x = Math.abs(array[i] - mean);
  //    }
  //
  //    var = calculateMean(x);
  //
  //    return var;
  //  }

  public void loadSQLite(View view) {

    try {
      AssetManager mng = getApplicationContext().getAssets();
      InputStream is = mng.open("hrv.csv");

      // Get singleton instance of database
      DBHelper dbHelper = DBHelper.getInstance(this);
      List<HeartRateVariability> beans = new CsvToBeanBuilder(new CSVReader(new InputStreamReader(is)))
          //.withSkipLines(1)
          .withType(HeartRateVariability.class)
          .build()
          .parse();
      dbHelper.addNewEntries(beans);
      //for (HeartRateVariability hrvBean : beans) {
      // Add hrv entry to the database
      //  dbHelper.addNewEntry(hrvBean);
      //}
      Log.d(TAG, String.valueOf(dbHelper.getAllEntriesFromTo("2021-11-28T08:45:00", "2021-11-28T09:45:00").size()));

    } catch (Exception e) {
      Log.e(TAG, e.getMessage(), e);
      Toast.makeText(this, "The specified file was not found", Toast.LENGTH_SHORT).show();
    }
  }
}
