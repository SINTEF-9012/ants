package no.sintef.giot.bhp.fitbit;

import android.app.Application;
import android.content.Context;

/**
 * This class is needed to provide the context to the Chaquopy library.
 */
public class FitbitApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        FitbitApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return FitbitApplication.context;
    }
}
