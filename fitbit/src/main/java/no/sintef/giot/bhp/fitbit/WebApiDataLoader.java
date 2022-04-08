package no.sintef.giot.bhp.fitbit;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import no.sintef.giot.bhp.interfaces.LoadDataService;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebApiDataLoader extends LoadDataService {

    private static final String TAG = "WebApiDataLoader";

    private static final String FITBIT_URL_SLEEP = "https://api.fitbit.com/1.2/user/-/sleep/date/%s/%s.json";

    private static final String FITBIT_URL_PROFILE = "https://api.fitbit.com/1/user/-/profile.json";

    private static final String FITBIT_URL_ACTIVITY_INTRADAY_STEPS = "https://api.fitbit.com/1/user/-/activities/steps/date/%s/%s/1min.json";

    private static final String FITBIT_URL_ACTIVITY_INTRADAY_HEARTRATE = "https://api.fitbit.com/1/user/-/activities/heart/date/%s/%s/1min.json";

    private static Gson gson;

    private static DateFormat df;

    private static OkHttpClient okClient;

    public WebApiDataLoader() {
        super();
        df = new SimpleDateFormat("yyyy-MM-dd");
        gson = new Gson();
        okClient = new OkHttpClient();
    }

    /**
     * Get data from FitBit Web API
     *
     * @return data from FitBit Web API
     */
    public String getData() {

        return getProfileData();
        //return getSleepData(-10);
        //return getHeartRateData(-10);
        //return getStepsData(-10);
    }

    /**
     * Gets user profile data from FitBit
     *
     * @return user profile data as json
     */
    private String getProfileData() {
        return callFitbitApiRequest(FITBIT_URL_PROFILE);
    }

    /**
     * Gets sleep data from FitBit
     *
     * @param numDays offset
     * @return sleep data as json
     */
    private String getSleepData(int numDays) {
        //be careful about the order of date!
        return callFitbitApiRequest(String.format(FITBIT_URL_SLEEP, getDatePast(numDays), getDateToday()));
    }



    /**
     * Gets heart data from FitBit
     *
     * @param numDays offset
     * @return heart rate data as json
     */
    private String getHeartRateData(int numDays) {
        return callFitbitApiRequest(String.format(FITBIT_URL_ACTIVITY_INTRADAY_HEARTRATE, getDateToday(), getDatePast(numDays)));
    }

    /**
     * Gets steps data from FitBit
     *
     * @param numDays offset
     * @return steps data as json
     */
    private String getStepsData(int numDays) {
        return callFitbitApiRequest(String.format(FITBIT_URL_ACTIVITY_INTRADAY_STEPS, getDateToday(), getDatePast(numDays)));
    }

    /**
     * Generic method to call the FitBit API
     *
     * @param url specific API endpoint
     * @return rquest results as a json string
     */
    private String callFitbitApiRequest(String url) {

        Log.i(TAG, "FitBitAPI entrypoint: " + url);
        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + BuildConfig.fitbit_access_token)
                .url(url)
                .build();

        //this is an ugly workaround to write the http response into the result
        final String[] result = {""};

        // Get a handler that can be used to post to the main thread
        okClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected http response code " + response);
                } else {
                    result[0] = response.body().string();
                    Log.i(TAG, "Response body: " + getPrettyJson(result[0]));
                    response.close();
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

    /**
     * Returns today's date as string YYYY-MM-DD
     *
     * @return today's date
     */
    private String getDateToday() {

        //DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return df.format(new Date());
    }

    /**
     * Returns a date in the past as string YYYY-MM-DD
     *
     * @param numDays an offset in days to calculate the date in the past (should be a negative integer)
     * @return date in the past
     */
    private String getDatePast(int numDays) {

        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, numDays);
        Date pastDate = cal.getTime();

        return df.format(pastDate);
    }

    /**
     * Beuatify Json
     *
     * @param json input json string
     * @return pretty json string
     */
    private String getPrettyJson(String inputJson) {

        JsonObject json = JsonParser.parseString(inputJson).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);
        //Log.i(TAG, "Result " + prettyJson);
        return prettyJson;
    }
}
