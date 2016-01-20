package io;

import java.util.List;
import java.util.Map;


public class DBVocabularies {

  private Map<String, String> taxMap;
  private Map<String, String> tissueMap;
  private Map<String, String> deviceMap;
  private Map<String, String> cellLinesMap;
  private List<String> measureTypes;
  private List<String> spaces;
  private Map<String, Integer> investigators;
  private List<String> experimentTypes;
  private List<String> enzymes;
  private List<String> msProtocols;
  private List<String> lcmsMethods;
  private List<String> chromTypes;

  public DBVocabularies(Map<String, String> taxMap, Map<String, String> tissueMap,
      Map<String, String> cellLinesMap, List<String> measureTypes, List<String> spaces,
      Map<String, Integer> piMap, List<String> experimentTypes, List<String> enzymes,
      Map<String, String> deviceMap, List<String> msProtocols, List<String> lcmsMethods,
      List<String> chromTypes) {
    this.taxMap = taxMap;
    this.tissueMap = tissueMap;
    this.cellLinesMap = cellLinesMap;
    this.deviceMap = deviceMap;
    this.measureTypes = measureTypes;
    this.spaces = spaces;
    this.investigators = piMap;
    this.experimentTypes = experimentTypes;
    this.enzymes = enzymes;
    this.msProtocols = msProtocols;
    this.lcmsMethods = lcmsMethods;
    this.chromTypes = chromTypes;
  }

  public Map<String, String> getCellLinesMap() {
    return cellLinesMap;
  }

  public Map<String, String> getTaxMap() {
    return taxMap;
  }

  public Map<String, String> getTissueMap() {
    return tissueMap;
  }

  public Map<String, String> getDeviceMap() {
    return deviceMap;
  }

  public List<String> getMeasureTypes() {
    return measureTypes;
  }

  public List<String> getSpaces() {
    return spaces;
  }

  public Map<String, Integer> getInvestigators() {
    return investigators;
  }

  public List<String> getExperimentTypes() {
    return experimentTypes;
  }

  public List<String> getEnzymes() {
    return enzymes;
  }

  public List<String> getMsProtocols() {
    return msProtocols;
  }

  public List<String> getLcmsMethods() {
    return lcmsMethods;
  }

  public List<String> getChromTypes() {
    return chromTypes;
  }

}
