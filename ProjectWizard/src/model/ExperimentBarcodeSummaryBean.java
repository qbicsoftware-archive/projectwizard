package model;

public class ExperimentBarcodeSummaryBean {
  
  String ID_Range;
  String Bio_Type;
  String Amount;
  String experiment;
  
  public ExperimentBarcodeSummaryBean(String idRange, String bioType, String amount, String experiment) {
    ID_Range = idRange;
    Bio_Type = bioType;
    Amount = amount;
    this.experiment = experiment;
  }

  public String getExperiment() {
    return experiment;
  }
  
  public String getID_Range() {
    return ID_Range;
  }

  public void setID_Range(String iD_Range) {
    ID_Range = iD_Range;
  }

  public String getBio_Type() {
    return Bio_Type;
  }

  public void setBio_Type(String bio_Type) {
    Bio_Type = bio_Type;
  }

  public String getAmount() {
    return Amount;
  }

  public void setAmount(String amount) {
    Amount = amount;
  }

}
