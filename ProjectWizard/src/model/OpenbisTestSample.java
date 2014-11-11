package model;

import java.util.Map;

public class OpenbisTestSample extends AOpenbisSample {

  String Q_SAMPLE_TYPE;

  public OpenbisTestSample(String openbisName, String experiment, String secondaryName, String additionalNotes,
      XMLProperties xmlProperties, String sampleType, String parent) {
    super(openbisName, experiment, secondaryName, additionalNotes, xmlProperties, parent);
    this.Q_SAMPLE_TYPE = sampleType;
    this.sampleType = "Q_TEST_SAMPLE";
  }

  public Map<String, String> getValueMap() {
    Map<String, String> res = super.getValueMap();
    res.put("Q_SAMPLE_TYPE", Q_SAMPLE_TYPE);
    return res;
  }

}
