package no.sintef.giot.bhp.fitbit.sqlite;

import com.opencsv.bean.CsvBindByName;

/**
 * A POJO bean to represent HRV records from FitBit
 */
public class HeartRateVariability {

  @CsvBindByName(column = "timestamp")
  private String timestamp;

  @CsvBindByName(column = "rmssd")
  private String rmssd;

  @CsvBindByName(column = "coverage")
  private String coverage;

  @CsvBindByName(column = "low_frequency")
  private String low_frequency;

  @CsvBindByName(column = "high_frequency")
  private String high_frequency;

  //  getters, setters, toString

  /**
   * Getter
   *
   * @return timestamp
   */
  public String getTimestamp() {
    return this.timestamp;
  }

  /**
   * Setter
   *
   * @param aTimestamp timestamp
   */
  public void setTimestamp(String aTimestamp) {
    this.timestamp = aTimestamp;
  }

  /**
   * Getter
   *
   * @return rmssd
   */
  public String getRmssd() {
    return this.rmssd;
  }

  /**
   * Setter
   *
   * @param aRmssd rmssd
   */
  public void setRmssd(String aRmssd) {
    this.rmssd = aRmssd;
  }

  /**
   * Getter
   *
   * @return coverage
   */
  public String getCoverage() {
    return this.coverage;
  }

  /**
   * Setter
   *
   * @param aCoverage coverage
   */
  public void setCoverage(String aCoverage) {
    this.coverage = aCoverage;
  }

  /**
   * Getter
   *
   * @return low frequency
   */
  public String getLowFrequency() {
    return this.low_frequency;
  }

  /**
   * Setter
   *
   * @param aLowFrequency low frequency
   */
  public void setLowFrequency(String aLowFrequency) {
    this.low_frequency = aLowFrequency;
  }

  /**
   * Getter
   *
   * @return high frequency
   */
  public String getHighFrequency() {
    return this.high_frequency;
  }

  /**
   * Setter
   *
   * @param aHighFrequency high frequency
   */
  public void setHighFrequency(String aHighFrequency) {
    this.high_frequency = aHighFrequency;
  }

  @Override
  public String toString() {
    return "HeartRateVariability{" +
            "timestamp='" + timestamp + '\'' +
            ", rmssd='" + rmssd + '\'' +
            ", coverage='" + coverage + '\'' +
            ", low_frequency='" + low_frequency + '\'' +
            ", high_frequency='" + high_frequency + '\'' +
            '}';
  }
}
