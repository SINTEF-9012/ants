package no.sintef.giot.bhp;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Some utility functions.
 */
public class Utils {

    /**
     * Read json from file
     *
     * @param context  app context
     * @param fileName file name
     * @return json as sting
     */
    static String getJsonFromAssets(Context context, String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return jsonString;
    }
}
