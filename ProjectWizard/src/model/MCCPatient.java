package model;

public class MCCPatient {

  private String ID;

  private String Treatment;
  private String Timepoint;

  public MCCPatient(String ID, String Treatment, String Timepoint) {
    this.ID = ID;
    this.Timepoint = Timepoint;
    this.Treatment = Treatment;
  }

  public String getTreatment() {
    return Treatment;
  }

  public void setTimepoint(String tp) {
    Timepoint = tp;
  }

  public String getTimepoint() {
    return Timepoint;
  }

  public void setTreatment(String treat) {
    Treatment = treat;
  }

  public String getID() {
    return ID;
  }

  public void setID(String iD) {
    ID = iD;
  }

}
