package no.sintef.giot.bhp.fitbit;

import android.util.Log;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import no.sintef.giot.bhp.interfaces.ProcessDataService;

public class ChaquopyPythonInvoker extends ProcessDataService {

  private static final String TAG = "ChaquopyPythonInvoker";

  public ChaquopyPythonInvoker() {
    //Start python
    if (!Python.isStarted()) {
      Python.start(new AndroidPlatform(FitbitApplication.getAppContext()));
    }
  }

  @Override
  public String infer(String inputData) {

    Python py = Python.getInstance();

    // Obtain the system's input stream (available from Chaquopy)
    PyObject sys = py.getModule("sys");
    PyObject io = py.getModule("io");
    // Obtain the right python module
    PyObject module = py.getModule("infer_fas");

    // Redirect the system's output stream to the Python interpreter
    PyObject textOutputStream = io.callAttr("StringIO");
    sys.put("stdout", textOutputStream);

    // Create a string variable that will contain the standard output of the Python interpreter
    String interpreterOutput = "";

    // Execute the Python code
    try {
      //String csvData = DBHelper.getInstance(this).getAllEntriesAsCsv();
      module.callAttr("preprocess_and_infer", inputData, "input_scaler.z", "model.h5", "input_columns.csv", "params.json");

      //module.callAttrThrows("main", "hrv.csv");
      interpreterOutput = textOutputStream.callAttr("getvalue").toString();
    } catch (PyException e) {
      // If there's an error, you can obtain its output as well
      // e.g. if you mispell the code
      // Missing parentheses in call to 'print'
      // Did you mean print("text")?
      // <string>, line 1
      interpreterOutput = e.getMessage().toString();
    }

    // Outputs the results:
    Log.i(TAG, "RESULTS FROM INFERENCE: " + interpreterOutput);

    return interpreterOutput;
  }
}
