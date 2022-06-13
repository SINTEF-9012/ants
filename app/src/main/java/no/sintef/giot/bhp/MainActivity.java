package no.sintef.giot.bhp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

import dalvik.system.PathClassLoader;
import no.sintef.giot.bhp.core.CoreSystem;
import no.sintef.giot.bhp.interfaces.LoadDataService;
import no.sintef.giot.bhp.interfaces.ProcessDataService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //search for load data implementations
        ServiceLoader<LoadDataService> ldLoader = ServiceLoader.load(LoadDataService.class);
        Log.i(TAG, "Found LoadDataService implementations: ");
        for (LoadDataService ld : ldLoader) {
            Log.i(TAG, ld.getClass().toString());
            //ld.getDataAsJson();
            //Log.i(TAG, ld.getDataAsCSV());
            //Log.i(TAG, ld.getDataAsJson());
        }

        //search for inference implementations
        ServiceLoader<ProcessDataService> pdLoader = ServiceLoader.load(ProcessDataService.class);
        Log.i(TAG, "Found InferenceService implementations: ");
        for (ProcessDataService pd : pdLoader) {
            Log.i(TAG, pd.getClass().toString());
        }
    }

    /**
     * Run the fatigue detection inference
     *
     * @param view current view
     */
    public void testInference(View view) {

       String inputData = Utils.getJsonFromAssets(this, "sample_input_2.json");

        String result = CoreSystem.getInstance().runInference(inputData);
        Log.i(TAG, "Obtained results: " + result);
    }

}
