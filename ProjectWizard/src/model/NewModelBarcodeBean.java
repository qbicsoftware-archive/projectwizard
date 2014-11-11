package model;

import java.util.List;

public class NewModelBarcodeBean implements IBarcodeBean {

  String Code;
  String Secondary_Name;
  List<String> parents;
  String type;

  public NewModelBarcodeBean(String code, String secondaryName, String type, List<String> parents) {
    this.Code = code;
    this.Secondary_Name = secondaryName;
    this.type = type;
    this.parents = parents;
  }
  
  @Override
  public String toString() {
    return Code+" "+Secondary_Name+" "+type+" "+parents;
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

  @Override
  public String getDescription() {
    return Secondary_Name;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public List<String> fetchParentIDs() {
    return parents;
  }

  @Override
  public boolean hasParents() {
    return parents.size() > 0;
  }


}
