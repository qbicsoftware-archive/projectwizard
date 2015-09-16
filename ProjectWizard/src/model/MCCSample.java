package model;

public class MCCSample {
  
  private String ID;
  private String info;
  private String type;
  
  public MCCSample(String iD, String info, String type) {
    ID = iD;
    this.info = info;
    this.type = type;
  }

  public String getID() {
    return ID;
  }

  public void setID(String iD) {
    ID = iD;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
  

}
