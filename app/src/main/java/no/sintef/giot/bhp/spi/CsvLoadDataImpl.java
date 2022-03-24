package no.sintef.giot.bhp.spi;

import android.util.Log;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.io.IOException;

import no.sintef.giot.bhp.interfaces.LoadDataService;
import no.sintef.giot.bhp.sqlite.DBHelper;

public class CsvLoadDataImpl extends LoadDataService {

  private static final String TAG = "CsvLoadDataImpl";

  /**
   * Get data from SQLite as CSV
   *
   * @return data from SQLite as CSV string
   */
  @Override
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
}

