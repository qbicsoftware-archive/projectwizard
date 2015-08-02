package model;

/**
 * Class representing an experiment with some metadata
 * @author Andreas Friedrich
 *
 */
public class OpenbisExperiment {

  String openbisName;
  ExperimentType type;
  String Q_SECONDARY_NAME;
  String Q_ADDITIONAL_NOTES;

  /**
   * Creates a new Openbis Experiment
   * @param name Name of the experiment
   * @param type Experiment type
   */
  public OpenbisExperiment(String name, ExperimentType type) {
    this.openbisName = name;
    this.type = type;
  }

  /**
   * Creates a new Openbis Experiment
   * @param name Name of the experiment
   * @param type Experiment type
   * @param secondaryName Secondary name of the experiment
   * @param additionalNotes Free text additonal notes concerning the experiment
   */
  OpenbisExperiment(String openbisName, ExperimentType type, String secondaryName,
      String additionalNotes) {
    this(openbisName, type);
    this.Q_ADDITIONAL_NOTES = additionalNotes;
    this.Q_SECONDARY_NAME = secondaryName;
  }

  public String getOpenbisName() {
    return openbisName;
  }

  public ExperimentType getType() {
    return type;
  }

  public String getQ_SECONDARY_NAME() {
    return Q_SECONDARY_NAME;
  }

  public String getQ_ADDITIONAL_NOTES() {
    return Q_ADDITIONAL_NOTES;
  }
}
