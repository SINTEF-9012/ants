package no.sintef.giot.bhp.interfaces;

/**
 * An abstract class for loading time-series data
 */
public abstract class LoadDataService {

  /**
   * Get data as a CSV string
   *
   * @return CSV data
   */
  public abstract String getDataAsCSV();

  /**
   * Get data as a JSON string
   *
   * @return JSON data
   */
  public abstract String getDataAsJson();
}
