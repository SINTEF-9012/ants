package no.sintef.giot.bhp.interfaces;

/**
 * An abstract class for creating different inference implementations
 */
public abstract class InferenceService {

  public abstract String infer();

  public abstract String getType();
}
