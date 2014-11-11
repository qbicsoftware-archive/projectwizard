package model;

public class ExperimentBean {
  
  private String code;
  private String experiment_type;
  private String samples;

  public ExperimentBean(String code, String experimentTypeCode, String samples) {
    this.code = code;
    this.experiment_type = experimentTypeCode;
    this.samples = samples;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getExperiment_type() {
    return experiment_type;
  }

  public void setExperiment_type(String experiment_type) {
    this.experiment_type = experiment_type;
  }

  public String getSamples() {
    return samples;
  }

  public void setSamples(String samples) {
    this.samples = samples;
  }

}
