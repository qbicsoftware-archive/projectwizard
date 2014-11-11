package model;

import java.util.Map;

public class OpenbisBiologicalEntity extends AOpenbisSample {

  String ncbiOrganism;


  public OpenbisBiologicalEntity(String openbisName, String experiment, String secondaryName, String additionalNotes,
      XMLProperties xmlProperties, String ncbiOrganism) {
    super(openbisName, experiment, secondaryName, additionalNotes, xmlProperties, "");
    this.ncbiOrganism = ncbiOrganism;
    this.sampleType = "Q_BIOLOGICAL_ENTITY";
  }
  
  public Map<String,String> getValueMap() {
    Map<String,String> res = super.getValueMap();
    res.put("Q_NCBI_ORGANISM", ncbiOrganism);
    return res;
  }
}
