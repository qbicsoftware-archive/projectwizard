package model;

/**
 * Bean item representing context with their ID, type and the number of numOfSamples they contain
 * @author Andreas Friedrich
 *
 */
public class ExperimentBean {
  
  private String ID;
  private String experiment_type;
  private String numOfSamples;

  /**
   * Creates a new ExperimentBean
   * @param id of experiment
   * @param experimentTypeCode the type code of the experiment
   * @param numOfSamples number of samples in this experiment
   */
  public ExperimentBean(String ID, String experimentTypeCode, String numOfSamples) {
    this.ID = ID;
    this.experiment_type = experimentTypeCode;
    this.numOfSamples = numOfSamples;
  }

  public String getID() {
    return ID;
  }

  public void setID(String ID) {
    this.ID = ID;
  }

  public String getExperiment_type() {
    return experiment_type;
  }

  public void setExperiment_type(String experiment_type) {
    this.experiment_type = experiment_type;
  }

  public String getSamples() {
    return numOfSamples;
  }

  public void setSamples(String samples) {
    this.numOfSamples = samples;
  }

}
