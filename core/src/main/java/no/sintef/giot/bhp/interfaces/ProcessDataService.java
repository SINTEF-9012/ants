package no.sintef.giot.bhp.interfaces;

/**
 * An abstract class for creating different inference implementations
 */
public abstract class ProcessDataService {

  public abstract String infer(String inputData);
}
