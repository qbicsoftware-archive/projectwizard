package model;

/**
 * Bean item representing identifier, secondary name and sample type of samples to visualize in a table etc.
 * @author Andreas Friedrich
 *
 */
public class NewSampleModelBean {

  String Code;
  String Secondary_Name;
  String Type;
  
  public NewSampleModelBean(String code, String secondaryName, String type) {
    this.Code = code;
    this.Secondary_Name = secondaryName;
    this.Type = type;
  }

  public String getType() {
    return Type;
  }
  
  public void setType(String type) {
    this.Type = type;
  }
  
  public String getCode() {
    return Code;
  }

  public void setCode(String code) {
    this.Code = code;
  }

  public String getSecondary_Name() {
    return Secondary_Name;
  }

  public void setSecondary_Name(String secondaryName) {
    this.Secondary_Name = secondaryName;
  }


}
