package control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.OpenBisClient;
import model.AOpenbisSample;
import model.ExperimentType;
import model.NewSampleModelBean;
import model.OpenbisBiologicalEntity;
import model.OpenbisBiologicalSample;
import model.OpenbisExperiment;
import model.OpenbisTestSample;
import model.XMLProperties;

import org.vaadin.teemu.wizards.WizardStep;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import ui.BioFactorStep;
import ui.EntityStep;
import ui.ExtractFactorStep;
import ui.ExtractionStep;
import ui.ProjectContextStep;
import ui.TestStep;

public class WizardDataAggregator {

  private ProjectContextStep s1;
  private EntityStep s2;
  private BioFactorStep s3;
  private ExtractionStep s4;
  private ExtractFactorStep s5;
  private TestStep s6;

  OpenBisClient openbis;
  Map<String, String> taxMap;
  Map<String, String> tissueMap;
  int firstFreeExperimentID;
  int firstFreeEntityID;
  int firstFreeBarcodeID;
  char classChar = 'X';

  // mandatory openBIS fields
  private String spaceCode;
  private String projectCode;
  List<OpenbisExperiment> experiments;
  private String species;
  private String tissue;
  private String sampleType;

  // additional metainfo
  private String tissueInfo;

  // info needed to create samples
  private int bioReps;
  private int extractReps;
  private int techReps;

  private Map<String, List<String>> bioFactors;
  private Map<String, List<String>> extractFactors;
  private boolean inheritEntities;
  private boolean inheritExtracts;

  public WizardDataAggregator(List<WizardStep> steps, OpenBisClient openbis,
      Map<String, String> taxMap, Map<String, String> tissueMap) {
    s1 = (ProjectContextStep) steps.get(0);
    s2 = (EntityStep) steps.get(1);
    s3 = (BioFactorStep) steps.get(2);
    s4 = (ExtractionStep) steps.get(3);
    s5 = (ExtractFactorStep) steps.get(4);
    s6 = (TestStep) steps.get(5);

    this.openbis = openbis;
    this.taxMap = taxMap;
    this.tissueMap = tissueMap;
  }

  public void prepareData() {
    firstFreeExperimentID = 1;
    firstFreeEntityID = 1;
    firstFreeBarcodeID = 1;
    species = s2.getSpecies();
    tissue = s4.getTissue();
    sampleType = s6.getSampleType();
    spaceCode = s1.getSpaceCode();
    projectCode = s1.getProjectCode();

    for (Experiment e : openbis.getExperimentsOfProjectByCode(projectCode)) {
      String code = e.getCode();
      String[] split = code.split(projectCode + "E");
      if (code.startsWith(projectCode + "E") && split.length > 1) {
        int num = Integer.parseInt(split[1]);
        if (firstFreeExperimentID <= num)
          firstFreeExperimentID = num + 1;
      }
    }

    experiments = new ArrayList<OpenbisExperiment>();
    if (!inheritEntities)
      experiments.add(new OpenbisExperiment(buildExperimentName(), ExperimentType.Q_EXPERIMENTAL_DESIGN));
    if (!inheritExtracts)
      experiments.add(new OpenbisExperiment(buildExperimentName(), ExperimentType.Q_SAMPLE_EXTRACTION));
    experiments.add(new OpenbisExperiment(buildExperimentName(), ExperimentType.Q_SAMPLE_PREPARATION));

    for (Sample s : openbis.getSamplesOfType("Q_BIOLOGICAL_ENTITY")) {
      int num = Integer.parseInt(s.getCode().split("-")[1]);
      if (num >= firstFreeEntityID)
        firstFreeEntityID = num + 1;
    }

    List<Sample> samples = new ArrayList<Sample>();
    if (openbis.projectExists(spaceCode, projectCode)) {
      samples.addAll(openbis.getSamplesOfProject(projectCode));
    }
    for (Sample s : samples) {
      String code = s.getCode();
      if (Functions.isQbicBarcode(code)) {
        System.out.println(code);
        int num = Integer.parseInt(code.substring(5, 8));
        if (num >= firstFreeBarcodeID)
          firstFreeBarcodeID = num + 1;
      }
    }

    bioReps = s2.getBioRepAmount();
    extractReps = s4.getExtractRepAmount();
    techReps = s6.getTechRepAmount();

    List<String> factorList = s2.getFactors();
    List<List<String>> valueLists = s3.getFactorValues();
    bioFactors = getFactorInfo(factorList, valueLists);

    factorList = s4.getFactors();
    valueLists = s5.getFactorValues();
    extractFactors = getFactorInfo(factorList, valueLists);
  }

  private Map<String, List<String>> getFactorInfo(List<String> fList, List<List<String>> vLists) {
    Map<String, List<String>> factorInfo = new HashMap<String, List<String>>();
    // for each filled in factor
    for (int i = 0; i < fList.size(); i++) {
      String name = fList.get(i);
      List<String> values = vLists.get(i);
      factorInfo.put(name, values);
    }
    return factorInfo;
  }

  private String buildExperimentName() {
    firstFreeExperimentID++;
    return projectCode + "E" + (firstFreeExperimentID - 1);
  }

  public List<OpenbisExperiment> getExperiments() {
    return experiments;
  }

  // I want to thank my parents and Stack Overflow, but mostly Stack Overflow.
  private void generatePermutations(List<List<String>> lists, List<String> result, int depth,
      String current) {
    String separator = "-";
    if (depth == lists.size()) {
      result.add(current);
      return;
    }
    for (int i = 0; i < lists.get(depth).size(); ++i) {
      if (current.equals(""))
        separator = "";
      generatePermutations(lists, result, depth + 1, current + separator + lists.get(depth).get(i));
    }
  }

  public File createTSV() throws FileNotFoundException, UnsupportedEncodingException {
    List<AOpenbisSample> samples = buildSamples();
    List<String> rows = new ArrayList<String>();
    List<String> header =
        new ArrayList<String>(Arrays.asList("SAMPLE TYPE", "EXPERIMENT", "Q_SECONDARY_NAME",
            "PARENT", "Q_PRIMARY_TISSUE", "Q_TISSUE_DETAILED", "Q_PROPERTIES", "Q_ADDITIONAL_INFO",
            "Q_NCBI_ORGANISM", "Q_SAMPLE_TYPE", "Q_EXTERNALDB_ID"));
    String headerLine = "Identifier";
    for (String col : header) {
      headerLine += "\t" + col;
    }
    for (AOpenbisSample s : samples) {
      Map<String, String> data = s.getValueMap();
      String row = "/" + spaceCode + "/" + s.getCode();
      for (String col : header) {
        String val = "";
        if (data.containsKey(col))
          val = data.get(col);
        if (val == null)
          val = "";
        row += "\t" + val;
      }
      rows.add(row);
    }
    String fileName = "/Users/frieda/Desktop/" + spaceCode + "_" + projectCode + ".tsv";
    PrintWriter writer = new PrintWriter(fileName, "UTF-8");
    writer.println(headerLine);
    for (String line : rows) {
      writer.println(line);

    }
    writer.close();
    return new File(fileName);
  }

  private List<OpenbisBiologicalEntity> buildEntities() {
    List<OpenbisBiologicalEntity> entities = new ArrayList<OpenbisBiologicalEntity>();
    List<List<String>> factorLists = new ArrayList<List<String>>();
    factorLists.addAll(bioFactors.values());
    List<String> permutations = new ArrayList<String>();
    generatePermutations(factorLists, permutations, 0, "");
    int entityNum = firstFreeEntityID;
    for (int i = bioReps; i > 0; i--) {
      for (String secondaryNameEntity : permutations) {
        entityNum++;
        if (s2.speciesIsFactor()) {
          for (String factor : secondaryNameEntity.split("-")) {
            if (taxMap.containsKey(factor))
              species = factor;
          }
        }
        String taxID = taxMap.get(species);
        entities.add(new OpenbisBiologicalEntity("QENTITY-" + entityNum, experiments.get(0).getOpenbisName(),
            secondaryNameEntity, "", new XMLProperties(), taxID));
      }
    }
    return entities;
  }

  private List<OpenbisBiologicalSample> buildExtracts(List<OpenbisBiologicalEntity> entities,
      Map<String, Character> classChars) {
    List<OpenbisBiologicalSample> extracts = new ArrayList<OpenbisBiologicalSample>();
    for (OpenbisBiologicalEntity e : entities) {
      List<List<String>> factorLists = new ArrayList<List<String>>();
      factorLists.add(new ArrayList<String>(Arrays.asList(e.getQ_SECONDARY_NAME())));
      factorLists.addAll(extractFactors.values());
      List<String> permutations = new ArrayList<String>();
      generatePermutations(factorLists, permutations, 0, "");
      for (String secondaryNameBio : permutations) {
        for (int i = extractReps; i > 0; i--) {
          firstFreeBarcodeID++;
          if (s4.tissueIsFactor()) {
            for (String factor : secondaryNameBio.split("-")) {
              if (tissueMap.containsKey(factor))
                tissue = factor;
            }
          }
          String tissueCode = tissueMap.get(tissue);
          if (classChars.containsKey(secondaryNameBio)) {
            classChar = classChars.get(secondaryNameBio);
          } else {
            classChar = Functions.incrementUppercase(classChar);
            classChars.put(secondaryNameBio, classChar);
          }
          String code =
              projectCode + Functions.createCountString(firstFreeBarcodeID, 3) + classChar;
          code = code + Functions.checksum(code);
          extracts.add(new OpenbisBiologicalSample(code, experiments.get(experiments.size()-2).getOpenbisName(), secondaryNameBio,
              "", new XMLProperties(), tissueCode, "", e.getCode()));
        }
      }
    }
    return extracts;
  }

  private List<OpenbisTestSample> buildTestSamples(List<OpenbisBiologicalSample> extracts,
      Map<String, Character> classChars) {
    List<OpenbisTestSample> tests = new ArrayList<OpenbisTestSample>();
    for (OpenbisBiologicalSample s : extracts) {
      for (int i = techReps; i > 0; i--) {
        firstFreeBarcodeID++;
        String secondaryName = s.getQ_SECONDARY_NAME();
        if (classChars.containsKey(secondaryName)) {
          classChar = classChars.get(secondaryName);
        } else {
          classChar = Functions.incrementUppercase(classChar);
          classChars.put(secondaryName, classChar);
        }
        String code = projectCode + Functions.createCountString(firstFreeBarcodeID, 3) + classChar;
        code = code + Functions.checksum(code);
        tests.add(new OpenbisTestSample(code, experiments.get(experiments.size()-1).getOpenbisName(), secondaryName, "",
            new XMLProperties(), sampleType, s.getCode()));
      }
    }
    return tests;
  }

  private List<AOpenbisSample> buildSamples() {
    List<OpenbisBiologicalEntity> entities = new ArrayList<OpenbisBiologicalEntity>();
    List<OpenbisBiologicalSample> extracts = new ArrayList<OpenbisBiologicalSample>();
    Map<String, Character> classChars = new HashMap<String, Character>();
    List<String> selectedCodes = getSampleCodes(s1.getSamples());
    if (inheritEntities) {
      entities =
          parseEntities(openbis.getSamplesofExperiment(s1.getExperimentName().getCode()),
              selectedCodes);
      extracts = buildExtracts(entities, classChars);
    } else if (inheritExtracts) {
      extracts =
          parseExtracts(openbis.getSamplesofExperiment(s1.getExperimentName().getCode()),
              selectedCodes);
    } else {
      entities = buildEntities();
      extracts = buildExtracts(entities, classChars);
    }
    List<OpenbisTestSample> tests = buildTestSamples(extracts, classChars);
    List<AOpenbisSample> res = new ArrayList<AOpenbisSample>();
    res.addAll(entities);
    res.addAll(extracts);
    res.addAll(tests);
    return res;
  }

  public void setInheritEntities(boolean inherit) {
    this.inheritEntities = inherit;
  }

  public void setInheritExtracts(boolean inherit) {
    this.inheritExtracts = inherit;
  }

  // PARENT Q_PRIMARY_TISSUE Q_TISSUE_DETAILED Q_PROPERTIES Q_ADDITIONAL_INFO Q_NCBI_ORGANISM
  // Q_SAMPLE_TYPE Q_EXTERNALDB_ID TODO ?
  private List<OpenbisBiologicalSample> parseExtracts(List<Sample> extracts, List<String> selected) {
    List<OpenbisBiologicalSample> res = new ArrayList<OpenbisBiologicalSample>();
    for (Sample s : extracts) {
      String code = s.getCode();
      if (selected.contains(code)) {
        String[] eSplit = s.getExperimentIdentifierOrNull().split("/");
        Map<String, String> p = s.getProperties();
        res.add(new OpenbisBiologicalSample(code, eSplit[eSplit.length - 1], p
            .get("Q_SECONDARY_NAME"), p.get("Q_ADDITIONAL_INFO"), new XMLProperties(p
            .get("Q_PROPERTIES")), p.get("Q_PRIMARY_TISSUE"), p.get("Q_TISSUE_DETAILED"), ""));// TODO
                                                                                               // parents
                                                                                               // if
                                                                                               // needed
      }
    }
    return res;
  }

  private List<String> getSampleCodes(List<NewSampleModelBean> beans) {
    List<String> res = new ArrayList<String>();
    for (NewSampleModelBean b : beans)
      res.add(b.getCode());
    return res;
  }

  private List<OpenbisBiologicalEntity> parseEntities(List<Sample> entities, List<String> selected) {
    List<OpenbisBiologicalEntity> res = new ArrayList<OpenbisBiologicalEntity>();
    for (Sample s : entities) {
      String code = s.getCode();
      if (selected.contains(code)) {
        String[] eSplit = s.getExperimentIdentifierOrNull().split("/");
        Map<String, String> p = s.getProperties();
        res.add(new OpenbisBiologicalEntity(code, eSplit[eSplit.length - 1], p
            .get("Q_SECONDARY_NAME"), p.get("Q_ADDITIONAL_INFO"), new XMLProperties(p
            .get("Q_PROPERTIES")), p.get("Q_NCBI_ORGANISM")));
      }
    }
    return res;
  }
}
