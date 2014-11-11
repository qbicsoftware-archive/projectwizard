package model;

import java.util.HashMap;
import java.util.Map;

public abstract class AOpenbisSample {
  
  String sampleType;
  String code;
  String experiment;
  String Q_SECONDARY_NAME;
  XMLProperties xmlProperties;
  String Q_ADDITIONAL_NOTES;
  String parent;

  AOpenbisSample(String code, String experiment, String secondaryName, String additionalNotes, XMLProperties xmlProperties, String parent) {
    this.code = code;
    this.experiment = experiment;
    this.Q_ADDITIONAL_NOTES = additionalNotes;
    this.Q_SECONDARY_NAME = secondaryName;
    this.xmlProperties = xmlProperties;
    this.parent = parent;
  }
  
  public Map<String,String> getValueMap() {
    Map<String,String> res = new HashMap<String,String>();
    res.put("EXPERIMENT", experiment);
    res.put("SAMPLE TYPE", sampleType);
    res.put("code", code);
    res.put("Q_ADDITIONAL_INFO", Q_ADDITIONAL_NOTES);
    res.put("Q_SECONDARY_NAME", Q_SECONDARY_NAME);
    res.put("Q_PROPERTIES", xmlProperties.getXML());
    res.put("PARENT", parent);
    return res;
  }
  
  public String getCode() {
    return code;
  }

  public String getQ_SECONDARY_NAME() {
    return Q_SECONDARY_NAME;
  }

  public XMLProperties getXMLProperties() {
    return xmlProperties;
  }

  public String getQ_ADDITIONAL_NOTES() {
    return Q_ADDITIONAL_NOTES;
  }
  
  public String getParent() {
    return parent;
  }
}
