package model;

import java.util.List;
import java.util.Map;

import properties.Factor;

/**
 * Class representing a sample created in a sample preparation that will be used to measure data
 * @author Andreas Friedrich
 *
 */
public class OpenbisTestSample extends AOpenbisSample {

  String Q_SAMPLE_TYPE;

  /**
   * Create a new Test Sample
   * @param openbisName Code of the sample
   * @param experiment Experiment the sample is attached to
   * @param secondaryName Secondary Name of the sample (e.g. humanly readable identifier) 
   * @param additionalNotes Free text notes for the sample
   * @param factors A list of conditions of this sample
   * @param sampleType Measurement type of this sample (e.g. protein)
   * @param parent Extract parent of this sample
   */
  public OpenbisTestSample(String openbisName, String space, String experiment, String secondaryName, String additionalNotes,
      List<Factor> factors, String sampleType, String parent, String extID) {
    super(openbisName, space, experiment, secondaryName, additionalNotes, factors, parent, extID, "Q_TEST_SAMPLE");
    this.Q_SAMPLE_TYPE = sampleType;
  }
  
  public OpenbisTestSample(int tempID, List<AOpenbisSample> parents, String sampleType, String secondaryName,
      String externalID, List<Factor> newFactors, String additionalNotes) {
    super(tempID, parents, "Q_TEST_SAMPLE", secondaryName, externalID, newFactors, additionalNotes);
    this.Q_SAMPLE_TYPE = sampleType;
  }

  public Map<String, String> getValueMap() {
    Map<String, String> res = super.getValueMap();
    res.put("Q_SAMPLE_TYPE", Q_SAMPLE_TYPE);
    return res;
  }

}
