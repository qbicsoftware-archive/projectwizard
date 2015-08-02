package model;

import java.util.List;

/**
 * Bean item representing identifier, secondary name, a list of parent samples and sample type of samples to visualize in a table etc.
 * @author Andreas Friedrich
 *
 */
public class NewModelBarcodeBean implements IBarcodeBean {

  private String code;
  private String codedString;
  private String info1;
  private String info2;
  private List<String> parents;
  private String type;
  private String treatment;

  public NewModelBarcodeBean(String code, String codedString, String info1, String info2, String type, List<String> parents, String treatment) {
    this.treatment = treatment;
    this.code = code;
    this.codedString = codedString;
    this.info1 = info1;
    this.info2 = info2;
    this.type = type;
    this.parents = parents;
  }
  
  @Override
  public String toString() {
    return code+" "+info1+" "+info2+" "+parents;
  }

  public String getCode() {
    return code;
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

  @Override
  public String getTopInfo() {
    return info1;
  }

  @Override
  public String getBottomInfo() {
    return info2;
  }

  @Override
  public String getCodedString() {
    return codedString;
  }

  @Override
  public String getSheetTreatment() {
    return treatment;
  }

}
