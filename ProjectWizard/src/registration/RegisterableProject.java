package registration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.ISampleBean;
import model.OpenbisExperiment;

public class RegisterableProject {

  private String code;
  private String description;
  private String space;
  private List<RegisterableExperiment> experiments;
  private Map<String, String> sampleTypesToExpTypes = new HashMap<String, String>() {
    {
      put("Q_BIOLOGICAL_ENTITY", "Q_EXPERIMENTAL_DESIGN");
      put("Q_BIOLOGICAL_SAMPLE", "Q_SAMPLE_EXTRACTION");
      put("Q_TEST_SAMPLE", "Q_SAMPLE_PREPARATION");
      put("Q_NGS_SINGLE_SAMPLE_RUN", "Q_NGS_SINGLE_SAMPLE_RUN");
      put("Q_MS_RUN", "Q_MS_MEASUREMENT");
      put("Q_MHC_LIGAND_EXTRACT", "Q_MHC_LIGAND_EXTRACTION");
      put("Q_ATTACHMENT_SAMPLE", "Q_PROJECT_DETAILS");
    }
  };
  //these experiment types can't be flagged as "pilot experiments" (i.e. since they are project-wide)
  private final List<String> notPilotable =
      new ArrayList<String>(Arrays.asList("Q_PROJECT_DETAILS"));

  public RegisterableProject(String code, String description, String space,
      List<List<ISampleBean>> samples) {
    this.code = code;
    this.description = description;
    this.space = space;
  }

  public RegisterableProject(List<List<ISampleBean>> tsvSampleHierarchy, String description,
      String secondaryName, Map<String, Map<String, Object>> mhcExperimentMetadata,
      List<OpenbisExperiment> informativeExperiments, boolean isPilot) {
    this.description = description;
    this.experiments = new ArrayList<RegisterableExperiment>();
    Map<String, Map<String, Object>> mhcExperiments = new HashMap<String, Map<String, Object>>();
    if (mhcExperimentMetadata != null) {
      for (Map<String, Object> entries : mhcExperimentMetadata.values()) {
        mhcExperiments.put((String) entries.get("Code"), entries);
      }
    }
    for (Map<String, Object> entries : mhcExperiments.values()) {
      entries.remove("Code");
    }
    Map<String, OpenbisExperiment> knownExperiments = new HashMap<String, OpenbisExperiment>();
    for (OpenbisExperiment e : informativeExperiments) {
      knownExperiments.put(e.getOpenbisName(), e);
    }
    for (List<ISampleBean> inner : tsvSampleHierarchy) {
      // needed since we collect some samples that don't have the same experiment now - TODO not the
      // best place here
      ISampleBean sa = inner.get(0);
      this.space = sa.getSpace();
      this.code = sa.getProject();
      Map<String, RegisterableExperiment> expMap = new HashMap<String, RegisterableExperiment>();
      for (ISampleBean s : inner) {
        String expCode = s.getExperiment();
        if (expMap.containsKey(expCode)) {
          expMap.get(expCode).addSample(s);
        } else {
          String expType = sampleTypesToExpTypes.get(s.getType());
          Map<String, Object> metadata = new HashMap<String, Object>();
          if (secondaryName != null && !secondaryName.isEmpty()
              && expType.equals("Q_EXPERIMENTAL_DESIGN"))
            metadata.put("Q_SECONDARY_NAME", secondaryName);
          if (expType.equals("Q_MHC_LIGAND_EXTRACTION")) {
            metadata = mhcExperiments.get(expCode);
          }
          if (knownExperiments.containsKey(expCode)) {
            metadata = knownExperiments.get(expCode).getMetadata();
          }
          if (!notPilotable.contains(expType))
            metadata.put("Q_IS_PILOT", isPilot);
          RegisterableExperiment e = new RegisterableExperiment(s.getExperiment(), expType,
              new ArrayList<ISampleBean>(Arrays.asList(s)), metadata);
          this.experiments.add(e);
          expMap.put(expCode, e);
        }
      }
    }

  }

  public String getSpace() {
    return space;
  }

  public String getProjectCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public List<RegisterableExperiment> getExperiments() {
    return experiments;
  }

  public void addExperimentFromProperties(Map<String, Object> experimentProperties, String type) {
    int maxID = 0;
    for (RegisterableExperiment e : experiments) {
      String[] splt = e.getCode().split("E");
      int id = Integer.parseInt(splt[splt.length - 1]);
      if (id > maxID)
        maxID = id;
    }
    experiments.add(new RegisterableExperiment(code + "E" + Integer.toString(maxID + 1), type,
        new ArrayList<ISampleBean>(), experimentProperties));
  }
}
