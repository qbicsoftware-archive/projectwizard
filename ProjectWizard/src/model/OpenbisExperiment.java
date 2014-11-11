package model;

public class OpenbisExperiment {

  String openbisName;
  ExperimentType type;
  String Q_SECONDARY_NAME;
  User user;
  String Q_ADDITIONAL_NOTES;

  public OpenbisExperiment(String name, ExperimentType type) {
    this.openbisName = name;
    this.type = type;
  }

  OpenbisExperiment(String openbisName, ExperimentType type, String secondaryName,
      String additionalNotes, User user) {
    this(openbisName, type);
    this.Q_ADDITIONAL_NOTES = additionalNotes;
    this.Q_SECONDARY_NAME = secondaryName;
    this.user = user;
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

  public User getUser() {
    return user;
  }

  public String getQ_ADDITIONAL_NOTES() {
    return Q_ADDITIONAL_NOTES;
  }
}
