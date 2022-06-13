package no.sintef.giot.bhp.core;

import android.util.Log;
import java.util.ServiceLoader;

import no.sintef.giot.bhp.interfaces.LoadDataService;
import no.sintef.giot.bhp.interfaces.ProcessDataService;

/**
 * The core part of the plug-in architecture.
 */
public class CoreSystem {

    private static final String TAG = "CoreSystem";

    //singleton pattern
    private static CoreSystem instance = new CoreSystem();

    //Get the only object available
    public static CoreSystem getInstance(){
        return instance;
    }

    private CoreSystem() {

        //search for load data implementations
        /*ServiceLoader<LoadDataService> ldLoader = ServiceLoader.load(LoadDataService.class);
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
        }*/
    }

    /**
     * Run the fatigue detection inference
     *
     * @param inputData input data as json string
     * @return inference results as json string
     */
    public String runInference(String inputData) {
        //search for inference implementations
        ServiceLoader<ProcessDataService> pdLoader = ServiceLoader.load(ProcessDataService.class);
        //Log.i(TAG, "Found InferenceService implementations: ");
        //for (ProcessDataService pd : pdLoader) {
        //    Log.i(TAG, pd.getClass().toString());
        //}

        //assuming there is only one implementation of ProcessDataService
        String result = pdLoader.iterator().next().infer(inputData);
        return result;
    }
}
