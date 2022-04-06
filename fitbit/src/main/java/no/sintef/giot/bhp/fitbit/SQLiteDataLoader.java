package no.sintef.giot.bhp.fitbit;

import android.util.Log;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.IOException;

import no.sintef.giot.bhp.interfaces.LoadDataService;
import no.sintef.giot.bhp.fitbit.sqlite.DBHelper;

/**
 * Loads data from local SQLite as CSV
 */
public class SQLiteDataLoader extends LoadDataService {

    private static final String TAG = "SQLiteDataLoader";

    /**
     * Get data from SQLite as CSV
     *
     * @return data from SQLite as CSV string
     */
    public String getData() {
        String csvData = null;
        try {
            csvData = DBHelper.getInstance(null).getAllEntriesAsCsv();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (CsvDataTypeMismatchException e) {
            Log.e(TAG, e.getMessage());
        } catch (CsvRequiredFieldEmptyException e) {
            Log.e(TAG, e.getMessage());
        }
        return csvData;
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
        //TODO
        return null;
    }
}
