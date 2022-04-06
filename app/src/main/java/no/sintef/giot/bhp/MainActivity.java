package no.sintef.giot.bhp;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.ServiceLoader;

import no.sintef.giot.bhp.interfaces.InferenceService;
import no.sintef.giot.bhp.interfaces.LoadDataService;
import no.sintef.giot.bhp.fitbit.sqlite.DBHelper;
import no.sintef.giot.bhp.fitbit.sqlite.HeartRateVariability;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    static final String EXTRA_MESSAGE = "BHP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Start python
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        //Loads csv data into SQLite DB on start-up
        loadSQLite();

        //search for load data implementations
        ServiceLoader<LoadDataService> ldLoader = ServiceLoader.load(LoadDataService.class);
        Log.i(TAG, "Found LoadDataService implementations: ");
        for (LoadDataService ld : ldLoader) {
            Log.i(TAG, ld.getClass().toString());
            //Log.i(TAG, ld.getDataAsCSV());
            //Log.i(TAG, ld.getDataAsJson());
        }

        //search for inference implementations
        ServiceLoader<InferenceService> inLoader = ServiceLoader.load(InferenceService.class);
        Log.i(TAG, "Found InferenceService implementations: ");
        for (InferenceService in : inLoader) {
            Log.i(TAG, in.getClass().toString());
        }
    }

    /**
     * Method for testing the FitBit Web Api
     *
     * @param view current view
     */
    public void testFitbitApi(View view) throws IOException {

        // should be a singleton
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + BuildConfig.fitbit_access_token)
                //.url("https://api.fitbit.com/1/user/-/profile.json")
                //.url("https://api.fitbit.com/1/user/-/activities.json")
                .url("https://api.fitbit.com/1/user/-/activities/steps/date/2022-01-01/2022-04-01/1min.json")
                .build();

        Log.i(TAG, request.toString());

        // Get a handler that can be used to post to the main thread
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String prettyJson = gson.toJson(json);
                    Log.i(TAG, "Result " + prettyJson);
                }
            }
        });

        // Outputs the results:
        Log.i(TAG, "My 'token' is: " + BuildConfig.fitbit_access_token);
    }

    /**
     * Trains the ML model
     *
     * @param view current view
     */
    public void trainModel(View view) {
        //Start python
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();

        // Obtain the system's input stream (available from Chaquopy)
        PyObject sys = py.getModule("sys");
        PyObject io = py.getModule("io");
        // Obtain the right python module
        PyObject module = py.getModule("create_assets");

        // Redirect the system's output stream to the Python interpreter
        PyObject textOutputStream = io.callAttr("StringIO");
        sys.put("stdout", textOutputStream);

        // Create a string variable that will contain the standard output of the Python interpreter
        String interpreterOutput = "";

        // Execute the Python code
        try {
            module.callAttrThrows("main", "hrv.csv");
            interpreterOutput = textOutputStream.callAttr("getvalue").toString();
        } catch (PyException e) {
            // If there's an error, you can obtain its output as well
            // e.g. if you mispell the code
            // Missing parentheses in call to 'print'
            // Did you mean print("text")?
            // <string>, line 1
            interpreterOutput = e.getMessage().toString();
        } catch (Throwable throwable) {
            Log.e(TAG, throwable.getMessage());
        }
        // Outputs the results:
        Log.i(TAG, "RESULTS FROM CREATE_ASSETS: " + interpreterOutput);
    }

    /**
     * Calls the ML model
     *
     * @param view current view
     */
    public void runInference(View view) {

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
            // TODO: read the contents either from CSV file or from the database and pass them to Python as a CSV string argument
            String csvData = DBHelper.getInstance(this).getAllEntriesAsCsv();
            module.callAttrThrows("infer_from_string", csvData);

            //module.callAttrThrows("main", "hrv.csv");
            interpreterOutput = textOutputStream.callAttr("getvalue").toString();
        } catch (PyException e) {
            // If there's an error, you can obtain its output as well
            // e.g. if you mispell the code
            // Missing parentheses in call to 'print'
            // Did you mean print("text")?
            // <string>, line 1
            interpreterOutput = e.getMessage().toString();
        } catch (Throwable throwable) {
            Log.e(TAG, throwable.getMessage());
        }

        // Outputs the results:
        Log.i(TAG, "RESULTS FROM INFERENCE: " + interpreterOutput);

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

    /**
     * Loads the ML model
     *
     * @return ML model as a MappedByteBuffer
     * @throws IOException exception is file not found
     */
    private MappedByteBuffer loadModelFile() throws IOException {
        // private MappedByteBuffer loadModelFile(Activity activity) throws IOException {

        AssetFileDescriptor fileDescriptor = getApplicationContext().getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * Load CSV data into SQLite
     */
    public void loadSQLite() {

        try {
            AssetManager mng = getApplicationContext().getAssets();
            InputStream is = mng.open("hrv.csv");

            // Get singleton instance of database
            DBHelper dbHelper = DBHelper.getInstance(this);
            List<HeartRateVariability> beans =
                    new CsvToBeanBuilder(new CSVReader(new InputStreamReader(is)))
                            //.withSkipLines(1) // ignore the first line with column captions, if needed
                            .withType(HeartRateVariability.class)
                            .build()
                            .parse();
            dbHelper.addNewEntries(beans);
            //for (HeartRateVariability hrvBean : beans) {
            // Add hrv entry to the database
            //  dbHelper.addNewEntry(hrvBean);
            //}
            //Log.d(TAG, String.valueOf(dbHelper.getAllEntriesFromTo("2021-11-28T08:45:00", "2021-11-28T09:45:00").size()));

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(this, "The specified file was not found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Runs something.
     * TODO: Not needed probably?
     *
     * @param view current view
     */
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
}
