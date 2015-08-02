package model;

import java.util.List;

public abstract class AExperiment {
  
  private String space;
  private String project;
  private String code;
  
  private List<AOpenbisSample> samples;
  
  public AExperiment(String space, String project, String code) {
    this.space = space;
    this.project = project;
    this.code = code;
  }
  
  public String getID() {
    return "/"+space+"/"+project+"/"+code;
  }
  
  /**
   * Adds a single sample to the list of samples of this experiment
   */
  public void addSample(AOpenbisSample sample) {
    samples.add(sample);
  }
  
  /**
   * Sets the list of samples in this experiment
   */
  public void setSamples(List<AOpenbisSample> samples) {
    this.samples = samples;
  }
  
  public List<AOpenbisSample> getSamples() {
    return samples;
  }
  
}
