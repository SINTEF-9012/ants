package no.sintef.giot.bhp.spi;

/**
 * An abstract class for loading time-series data
 */
public abstract class LoadDataService {

  /**
   * Get data as a string
   *
   * @return data
   */
  public abstract String getData();
}
