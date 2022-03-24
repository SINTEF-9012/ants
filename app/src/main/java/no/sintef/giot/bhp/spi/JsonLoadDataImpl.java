package no.sintef.giot.bhp.spi;

import no.sintef.giot.bhp.interfaces.LoadDataService;
import no.sintef.giot.bhp.sqlite.DBHelper;

public class JsonLoadDataImpl extends LoadDataService {

  private static final String TAG = "CsvLoadDataImpl";

  /**
   * Get data from SQLite as JSON
   *
   * @return data from SQLite as a JSON string
   */
  @Override
  public String getData() {
    String jsonData = DBHelper.getInstance(null).getAllEntriesAsJson();
    return jsonData;
  }
}

