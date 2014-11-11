package model;

import java.util.Map;

public class OpenbisBiologicalSample extends AOpenbisSample {

  String primaryTissue;
  String tissueDetailed;

  public OpenbisBiologicalSample(String openbisName, String experiment, String secondaryName, String additionalNotes,
      XMLProperties xmlProperties, String primaryTissue, String tissueDetailed, String parent) {
    super(openbisName, experiment, secondaryName, additionalNotes, xmlProperties, parent);
    this.primaryTissue = primaryTissue;
    this.tissueDetailed = tissueDetailed;
    this.sampleType = "Q_BIOLOGICAL_SAMPLE";
  }
  
  public Map<String,String> getValueMap() {
    Map<String,String> res = super.getValueMap();
    res.put("Q_PRIMARY_TISSUE", primaryTissue);
    res.put("Q_TISSUE_DETAILED", tissueDetailed);
    return res;
  }
}
