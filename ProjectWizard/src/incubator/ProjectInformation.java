package incubator;

import java.util.List;

public class ProjectInformation {

  private String principalInvestigatorName;
  private List<String> contactPersonNames;
  private String identifier;
  private String label;
  private String description;

  public ProjectInformation(String identifier, String principalInvestigatorName,
      List<String> contactPersonNames, String label, String description) {
    this.principalInvestigatorName = principalInvestigatorName;
    this.contactPersonNames = contactPersonNames;
    this.label = label;
    this.description = description;
    this.identifier = identifier;
  }

  public ProjectInformation(String project_id, String principalInvestigatorName,
      String project_label, String project_description) {
    this.principalInvestigatorName = principalInvestigatorName;
    this.label = project_label;
    this.description = project_description;
    this.identifier = project_id;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getPrincipalInvestigatorName() {
    return principalInvestigatorName;
  }

  public List<String> getContactPersonNames() {
    return contactPersonNames;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }



}
