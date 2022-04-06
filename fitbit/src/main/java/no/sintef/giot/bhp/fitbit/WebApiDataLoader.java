package no.sintef.giot.bhp.fitbit;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import no.sintef.giot.bhp.interfaces.LoadDataService;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebApiDataLoader extends LoadDataService {

    private static final String TAG = "WebApiDataLoader";

    private static final String FITBIT_URL_SLEEP = "https://api.fitbit.com/1.2/user/-/sleep/date/2022-01-01/2022-04-01.json";

    private static final String FITBIT_URL_PROFILE = "https://api.fitbit.com/1/user/-/profile.json";

    private static final String FITBIT_URL_ACTIVITY_INTRADAY_STEPS = "https://api.fitbit.com/1/user/-/activities/steps/date/2022-01-01/2022-04-01/1min.json";

    private final Gson gson = new Gson();

    /**
     * Get data from FitBit Web API
     *
     * @return data from FitBit Web API
     */
    public String getData() {

        // should be a singleton
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + BuildConfig.fitbit_access_token)
                .url(FITBIT_URL_ACTIVITY_INTRADAY_STEPS)
                .build();

        //this is an ugly workaround to write the http response into the result
        final String[] result = {""};

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
                    //JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
                    //Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    //String prettyJson = gson.toJson(json);
                    //Log.i(TAG, "Result " + prettyJson);
                    //result[0] = prettyJson;

                    result[0] = response.body().string();
                }
            }
        });
        return result[0];
    }

    /**
     * Implements the parent method
     *
     * @return FitBit data in CSV format
     */
    @Override
    public String getDataAsCSV() {
        //TODO
        return null;
    }

    /**
     * Implements the parent method
     *
     * @return FitBit data in Json format
     */
    @Override
    public String getDataAsJson() {
        //no transformation  needed since FitBit data is already in Json
        return getData();
    }
}
