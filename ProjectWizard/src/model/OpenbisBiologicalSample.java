package model;

import java.util.List;
import java.util.Map;

import properties.Factor;

/**
 * Class representing a sample created in a sample extraction experiment and from which test samples
 * may be prepared
 * 
 * @author Andreas Friedrich
 * 
 */
public class OpenbisBiologicalSample extends AOpenbisSample {

  String primaryTissue;
  String tissueDetailed;

  /**
   * Create a new Biological Sample
   * 
   * @param openbisName Code of the sample
   * @param experiment Experiment the sample is attached to
   * @param secondaryName Secondary name of the sample (e.g. humanly readable identifier)
   * @param additionalNotes Free text notes for the sample
   * @param factors A list of conditions for the sample
   * @param primaryTissue The primary tissue of this biological sample
   * @param tissueDetailed Detailed tissue information
   * @param parent Entity parent this sample was extracted from
   */
  public OpenbisBiologicalSample(String openbisName, String space, String experiment,
      String secondaryName, String additionalNotes, List<Factor> factors, String primaryTissue,
      String tissueDetailed, String parent, String extID) {
    super(openbisName, space, experiment, secondaryName, additionalNotes, factors, parent, extID,
        "Q_BIOLOGICAL_SAMPLE");
    this.primaryTissue = primaryTissue;
    this.tissueDetailed = tissueDetailed;
  }

  public OpenbisBiologicalSample(int tempID, List<AOpenbisSample> parents, String primaryTissue,
      String tissueDetailed, String secondaryName, String externalID, List<Factor> newFactors,
      String additionalNotes) {
    super(tempID, parents, "Q_BIOLOGICAL_SAMPLE", secondaryName, externalID, newFactors, additionalNotes);
    this.primaryTissue = primaryTissue;
    this.tissueDetailed = tissueDetailed;
  }

  public Map<String, String> getValueMap() {
    Map<String, String> res = super.getValueMap();
    res.put("Q_PRIMARY_TISSUE", primaryTissue);
    res.put("Q_TISSUE_DETAILED", tissueDetailed);
    return res;
  }
}
