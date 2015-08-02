package control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import logging.Log4j2Logger;
import main.OpenBisClient;
import main.ProjectwizardUI;
import model.AOpenbisSample;
import model.ExperimentBean;
import model.ExperimentType;
import model.OpenbisBiologicalEntity;
import model.OpenbisBiologicalSample;
import model.OpenbisExperiment;
import model.OpenbisTestSample;
import model.TestSampleInformation;
import parser.XMLParser;
import properties.Factor;

import org.vaadin.teemu.wizards.WizardStep;

import control.WizardController.Steps;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import steps.ConditionInstanceStep;
import steps.EntityStep;
import steps.ExtractionStep;
import steps.ProjectContextStep;
import steps.TestStep;

/**
 * Aggregates the data from the wizard needed to create the experimental setup in the form of TSVs
 * 
 * @author Andreas Friedrich
 * 
 */
public class WizardDataAggregator {

  private ProjectContextStep s1;
  private EntityStep s2;
  private ConditionInstanceStep s3;
  private ExtractionStep s5;
  private ConditionInstanceStep s6;
  private TestStep s8;

  private String tsvContent;

  private OpenBisClient openbis;
  private XMLParser xmlParser = new XMLParser();
  private Map<String, String> taxMap;
  private Map<String, String> tissueMap;
  private Map<String, Factor> factorMap;
  private int firstFreeExperimentID;
  private int firstFreeEntityID;
  private HashSet<String> existingBarcodes;
  private String nextBarcode;
  private int firstFreeBarcodeID;
  private char classChar = 'X';

  // mandatory openBIS fields
  private String spaceCode;
  private String projectCode;
  private List<OpenbisExperiment> experiments;
  private String species;
  private String tissue;
  private List<TestSampleInformation> techTypeInfo = new ArrayList<TestSampleInformation>();

  // info needed to create samples
  private int bioReps;
  private int extractReps;
  // private List<Integer> techRepAmounts = new ArrayList<Integer>();

  private List<List<String>> bioFactors;
  private List<List<String>> extractFactors;
  private boolean inheritEntities;
  private boolean inheritExtracts;

  private List<AOpenbisSample> entities = new ArrayList<AOpenbisSample>();
  private List<AOpenbisSample> extracts;
  private List<AOpenbisSample> tests;
  private List<AOpenbisSample> extractPools;
  private List<AOpenbisSample> testPools;
  private Map<String, Character> classChars;
  logging.Logger logger = new Log4j2Logger(WizardDataAggregator.class);

  /**
   * Creates a new WizardDataAggregator
   * 
   * @param steps the steps of the Wizard to extract the data from
   * @param openbis openBIS client connection to query for existing experiments
   * @param taxMap mapping between taxonomy IDs and species names
   * @param tissueMap mapping of tissue names and labels
   */
  public WizardDataAggregator(Map<Steps, WizardStep> steps, OpenBisClient openbis,
      Map<String, String> taxMap, Map<String, String> tissueMap) {
    s1 = (ProjectContextStep) steps.get(Steps.Project_Context);
    s2 = (EntityStep) steps.get(Steps.Entities);
    s3 = (ConditionInstanceStep) steps.get(Steps.Entity_Conditions);
    s5 = (ExtractionStep) steps.get(Steps.Extraction);
    s6 = (ConditionInstanceStep) steps.get(Steps.Extract_Conditions);
    s8 = (TestStep) steps.get(Steps.Test_Samples);

    this.openbis = openbis;
    this.taxMap = taxMap;
    this.tissueMap = tissueMap;
  }

  /**
   * Fetches context information like space and project and computes first unused IDs of samples and
   * experiments.
   */
  private void prepareBasics() {
    firstFreeExperimentID = 1;
    firstFreeEntityID = 1;
    firstFreeBarcodeID = 1;// TODO cleanup where not needed
    existingBarcodes = new HashSet<String>();
    spaceCode = s1.getSpaceCode();
    projectCode = s1.getProjectCode();

    for (Experiment e : openbis.getExperimentsOfProjectByCode(projectCode)) {
      String code = e.getCode();
      String[] split = code.split(projectCode + "E");
      if (code.startsWith(projectCode + "E") && split.length > 1) {
        int num = 0;
        try {
          num = Integer.parseInt(split[1]);
        } catch (Exception e2) {
        }
        if (firstFreeExperimentID <= num)
          firstFreeExperimentID = num + 1;
      }
    }

    List<Sample> samples = new ArrayList<Sample>();
    if (openbis.projectExists(spaceCode, projectCode)) {
      samples.addAll(openbis.getSamplesOfProject("/" + spaceCode + "/" + projectCode));
    }
    for (Sample s : samples) {
      String code = s.getCode();
      if (Functions.isQbicBarcode(code)) {
        existingBarcodes.add(code);
        int num = Integer.parseInt(code.substring(5, 8));
        if (num >= firstFreeBarcodeID)
          firstFreeBarcodeID = num + 1;
      } else if (s.getSampleTypeCode().equals(("Q_BIOLOGICAL_ENTITY"))) {
        int num = Integer.parseInt(s.getCode().split("-")[1]);
        if (num >= firstFreeEntityID)
          firstFreeEntityID = num + 1;
      }
    }
  }

  /**
   * Creates the list of biological entities from the input information collected in the aggregator
   * fields and wizard steps and fetches or creates the associated experiments
   * 
   * @param map
   * 
   * @return
   * @throws JAXBException
   */
  public List<AOpenbisSample> prepareEntities(Map<Object, Integer> map) throws JAXBException {
    prepareBasics();
    this.factorMap = new HashMap<String, Factor>();
    experiments = new ArrayList<OpenbisExperiment>();
    species = s2.getSpecies();
    bioReps = s2.getBioRepAmount();

    // entities are not created new, but parsed from registered ones
    if (inheritEntities) {
      entities = parseEntities(openbis.getSamplesofExperiment(s1.getExperimentName().getID()));
      // create new entities and an associated experiment from collected inputs
    } else {
      experiments.add(new OpenbisExperiment(buildExperimentName(),
          ExperimentType.Q_EXPERIMENTAL_DESIGN));

      List<List<Factor>> valueLists = s3.getFactors();
      bioFactors = createFactorInfo(valueLists);

      entities = buildEntities(map);
    }
    return entities;
  }

  /**
   * Creates the list of biological extracts from the input information collected in the aggregator
   * fields and wizard steps and fetches or creates the associated experiments
   * 
   * @param map
   * 
   * @return
   * @throws JAXBException
   */
  public List<AOpenbisSample> prepareExtracts(Map<Object, Integer> map) throws JAXBException {
    tissue = s5.getTissue();
    extractReps = s5.getExtractRepAmount();

    // extracts are not created new, but parsed from registered ones
    if (inheritExtracts) {
      prepareBasics();
      this.factorMap = new HashMap<String, Factor>();
      experiments = new ArrayList<OpenbisExperiment>();
      extracts = parseExtracts(openbis.getSamplesofExperiment(s1.getExperimentName().getID()));
      // create new entities and an associated experiment from collected inputs
    } else {
      experiments.add(new OpenbisExperiment(buildExperimentName(),
          ExperimentType.Q_SAMPLE_EXTRACTION));
      List<List<Factor>> valueLists = s6.getFactors();
      extractFactors = createFactorInfo(valueLists);
      // keep track of id letters for different conditions
      classChars = new HashMap<String, Character>();
      extracts = buildExtracts(entities, classChars, map);
    }
    return extracts;
  }

  /**
   * Creates the list of samples prepared for testing from the input information collected in the
   * aggregator fields and wizard steps and fetches or creates the associated experiments
   * 
   * @return
   */
  public List<List<AOpenbisSample>> prepareTestSamples() {
    techTypeInfo = s8.getSampleTypes();
    if (inheritExtracts) {
      prepareBasics();
      classChars = new HashMap<String, Character>();
      experiments = new ArrayList<OpenbisExperiment>();
    }
    for (@SuppressWarnings("unused")
    TestSampleInformation x : techTypeInfo)
      experiments.add(new OpenbisExperiment(buildExperimentName(),
          ExperimentType.Q_SAMPLE_PREPARATION));
    List<List<AOpenbisSample>> techSortedTests = buildTestSamples(extracts, classChars);
    tests = new ArrayList<AOpenbisSample>();
    for (List<AOpenbisSample> group : techSortedTests)
      tests.addAll(group);
    for (int i = techSortedTests.size() - 1; i > -1; i--) {
      if (!techTypeInfo.get(i).isPooled())
        techSortedTests.remove(i);
    }
    return techSortedTests;
  }

  /**
   * Set the list of biological entities (e.g. after filtering it) used in further steps
   * 
   * @param entities
   */
  public void setEntities(List<AOpenbisSample> entities) {
    this.entities = entities;
  }

  /**
   * Set the list of sample extracts (e.g. after filtering it) used in further steps
   * 
   * @param extracts
   */
  public void setExtracts(List<AOpenbisSample> extracts) {
    this.extracts = extracts;
  }

  /**
   * Set the list of test samples
   * 
   * @param tests
   */
  public void setTests(List<AOpenbisSample> tests) {
    this.tests = tests;
  }

  /**
   * Collects conditions as Strings in a list of their instance lists. Also puts the conditions
   * (factors) in a HashMap for later lookup, using value and unit as a key
   * 
   * @param factors List of a list of condition instances (one list per condition)
   * @return List of a list of condition instances (one list per condition) as Strings
   */
  private List<List<String>> createFactorInfo(List<List<Factor>> factors) {
    List<List<String>> res = new ArrayList<List<String>>();
    for (List<Factor> instances : factors) {
      List<String> factorValues = new ArrayList<String>();
      for (Factor f : instances) {
        String name = f.getValue() + f.getUnit();
        factorValues.add(name);
        factorMap.put(name, f);
      }
      res.add(factorValues);
    }
    return res;
  }

  /**
   * Builds an experiment name from the current unused id and increments the id
   * 
   * @return
   */
  private String buildExperimentName() {
    firstFreeExperimentID++;
    return projectCode + "E" + (firstFreeExperimentID - 1);
  }

  /**
   * Generates all permutations of a list of experiment conditions
   * 
   * @param lists Instance lists of different conditions
   * @return List of all possible permutations of the input conditions
   */
  public List<String> generatePermutations(List<List<String>> lists) {
    List<String> res = new ArrayList<String>();
    generatePermutationsHelper(lists, res, 0, "");
    return res;
  }

  /**
   * recursive helper
   */
  private void generatePermutationsHelper(List<List<String>> lists, List<String> result, int depth,
      String current) {
    String separator = "###";
    if (depth == lists.size()) {
      result.add(current);
      return;
    }
    for (int i = 0; i < lists.get(depth).size(); ++i) {
      if (current.equals(""))
        separator = "";
      generatePermutationsHelper(lists, result, depth + 1, current + separator
          + lists.get(depth).get(i));
    }
  }

  /**
   * Build and return a list of all possible biological entities given their conditions, keep track
   * of conditions in a HashMap for later
   * 
   * @param map
   * 
   * @return List of AOpenbisSamples containing entity samples
   */
  private List<AOpenbisSample> buildEntities(Map<Object, Integer> map) {
    List<AOpenbisSample> entities = new ArrayList<AOpenbisSample>();
    List<List<String>> factorLists = new ArrayList<List<String>>();
    factorLists.addAll(bioFactors);
    List<String> permutations = generatePermutations(factorLists);
    List<List<String>> permLists = new ArrayList<List<String>>();
    for (String concat : permutations) {
      permLists.add(new ArrayList<String>(Arrays.asList(concat.split("###"))));
    }
    int entityNum = firstFreeEntityID;
    int defBioReps = bioReps;
    int permID = 0;
    for (List<String> secondaryNameList : permLists) {
      permID++;
      String secondaryName = nameListToSecondaryName(secondaryNameList);
      if (map.containsKey(permID))
        defBioReps = map.get(permID);
      for (int i = defBioReps; i > 0; i--) {
        List<Factor> factors = new ArrayList<Factor>();
        for (String name : secondaryNameList) {
          if (factorMap.containsKey(name))
            factors.add(factorMap.get(name));
        }
        if (s2.speciesIsFactor()) {
          for (String factor : secondaryNameList) {
            if (taxMap.containsKey(factor))
              species = factor;
          }
        }
        String taxID = taxMap.get(species);
        entities.add(new OpenbisBiologicalEntity(projectCode + "ENTITY-" + entityNum, spaceCode,
            experiments.get(0).getOpenbisName(), secondaryName, "", factors, taxID));
        entityNum++;
      }
    }
    return entities;
  }

  /**
   * Build and return a list of all possible biological extracts given their conditions, using
   * existing entities. Keep track of condition in a HashMap for later
   * 
   * @param entities Existing (or prepared) biological entity samples these extracts will be
   *        attached to
   * @param classChars Empty map of different class letters used for the identifiers, to keep track
   *        of for test samples
   * @param map
   * @return List of AOpenbisSamples containing extract samples
   */
  private List<AOpenbisSample> buildExtracts(List<AOpenbisSample> entities,
      Map<String, Character> classChars, Map<Object, Integer> map) {
    int expNum = experiments.size() - techTypeInfo.size() - 1;
    List<AOpenbisSample> extracts = new ArrayList<AOpenbisSample>();
    int permID = 0;
    for (AOpenbisSample e : entities) {
      List<List<String>> factorLists = new ArrayList<List<String>>();
      String secName = e.getQ_SECONDARY_NAME();
      if (secName == null)
        secName = "";
      factorLists.add(new ArrayList<String>(Arrays.asList(secName)));

      factorLists.addAll(extractFactors);
      List<String> permutations = generatePermutations(factorLists);
      List<List<String>> permLists = new ArrayList<List<String>>();
      for (String concat : permutations) {
        permLists.add(new ArrayList<String>(Arrays.asList(concat.split("###"))));
      }
      for (List<String> secondaryNameList : permLists) {
        permID++;
        List<Factor> factors = new ArrayList<Factor>();
        factors.addAll(e.getFactors());
        for (String name : secondaryNameList)
          for (String element : name.split(";")) {
            element = element.trim();
            if (factorMap.containsKey(element)) {
              if (!factors.contains(factorMap.get(element)))
                factors.add(factorMap.get(element));
            }
          }
        String secondaryName = nameListToSecondaryName(secondaryNameList);
        int defExtrReps = extractReps;
        if (map.containsKey(permID))
          defExtrReps = map.get(permID);
        for (int i = defExtrReps; i > 0; i--) {
          if (s5.tissueIsFactor()) {
            for (String factor : secondaryNameList) {
              if (tissueMap.containsKey(factor))
                tissue = factor;
            }
          }
          String tissueCode = tissueMap.get(tissue);
          if (classChars.containsKey(secondaryName)) { // TODO does this seem right to you?
            classChar = classChars.get(secondaryName);
          } else {
            classChar = Functions.incrementUppercase(classChar);
            classChars.put(secondaryName, classChar);
          }
          incrementOrCreateBarcode();
          extracts.add(new OpenbisBiologicalSample(nextBarcode, spaceCode, experiments.get(expNum)
              .getOpenbisName(), secondaryName, "", factors, tissueCode, "", e.getCode()));
        }
      }
    }
    return extracts;
  }

  private void incrementOrCreateBarcode() {
    if (nextBarcode == null) {
      classChar = 'A';
      nextBarcode = projectCode + Functions.createCountString(firstFreeBarcodeID, 3) + classChar;
      nextBarcode = nextBarcode + Functions.checksum(nextBarcode);
    } else {
      nextBarcode = Functions.incrementSampleCode(nextBarcode);
    }
  }

  public List<AOpenbisSample> createPoolingSamples(Map<String, List<AOpenbisSample>> pools) {
    if (pools.size() > 0) {
      AOpenbisSample dummy = pools.values().iterator().next().get(0);
      boolean extracts = dummy instanceof OpenbisBiologicalSample;
      if (extracts)
        extractPools = new ArrayList<AOpenbisSample>();
      else
        testPools = new ArrayList<AOpenbisSample>();
      String exp = dummy.getValueMap().get("EXPERIMENT");
      List<Factor> factors = new ArrayList<Factor>();
      for (String secName : pools.keySet()) {
        incrementOrCreateBarcode();
        String parents = "";
        for (AOpenbisSample s : pools.get(secName)) {
          parents += s.getCode() + " ";
        }
        parents = parents.trim();
        if (extracts) {
          extractPools.add(new OpenbisBiologicalSample(nextBarcode, spaceCode, exp, secName, "",
              factors, "Other", "", parents));
        } else {
          String type = dummy.getValueMap().get("Q_SAMPLE_TYPE");
          testPools.add(new OpenbisTestSample(nextBarcode, spaceCode, exp, secName, "", factors,
              type, parents));
        }
      }
      if (extracts)
        return extractPools;
      else
        return testPools;
    }
    return new ArrayList<AOpenbisSample>();
  }

  /**
   * Build and return a list of all possible sample preparations (test samples), using existing
   * extracts.
   * 
   * @param extracts Existing (or prepared) sample extracts these test samples will be attached to
   * @param classChars Filled map of different class letters used for the extracts
   * @return List of lists of AOpenbisSamples containing test samples, sorted by different
   *         technology types
   */
  private List<List<AOpenbisSample>> buildTestSamples(List<AOpenbisSample> extracts,
      Map<String, Character> classChars) {
    List<List<AOpenbisSample>> tests = new ArrayList<List<AOpenbisSample>>();
    for (int j = 0; j < techTypeInfo.size(); j++) {// different technologies
      List<AOpenbisSample> techTests = new ArrayList<AOpenbisSample>();
      int techReps = techTypeInfo.get(j).getReplicates();
      String sampleType = techTypeInfo.get(j).getTechnology();
      int expNum = experiments.size() - techTypeInfo.size() + j;
      for (AOpenbisSample s : extracts) {
        for (int i = techReps; i > 0; i--) {
          String secondaryName = s.getQ_SECONDARY_NAME();
          if (classChars.containsKey(secondaryName)) { // TODO see above
            classChar = classChars.get(secondaryName);
          } else {
            classChar = Functions.incrementUppercase(classChar);
            classChars.put(secondaryName, classChar);
          }
          incrementOrCreateBarcode();
          techTests.add(new OpenbisTestSample(nextBarcode, spaceCode, experiments.get(expNum)
              .getOpenbisName(), secondaryName, "", s.getFactors(), sampleType, s.getCode()));
        }
      }
      tests.add(techTests);
    }
    return tests;
  }

  /**
   * parse secondary name from a list of condition permutations
   * 
   * @param secondaryNameList
   * @return
   */
  private String nameListToSecondaryName(List<String> secondaryNameList) {
    String res = secondaryNameList.toString().replace(", ", " ; ");
    return res.substring(1, res.length() - 1);
  }

  /**
   * set flag denoting the inheritance from entities existing in the system
   * 
   * @param inherit
   */
  public void setInheritEntities(boolean inherit) {
    this.inheritEntities = inherit;
  }

  /**
   * set flag denoting the inheritance from extracts existing in the system
   * 
   * @param inherit
   */
  public void setInheritExtracts(boolean inherit) {
    this.inheritExtracts = inherit;
  }

  /**
   * Parse existing entities from the system
   * 
   * @param entities List of biological entities in the form of openBIS Samples
   * @return List of AOpenbisSamples containing entities
   * @throws JAXBException
   */
  private List<AOpenbisSample> parseEntities(List<Sample> entities) throws JAXBException {
    List<AOpenbisSample> res = new ArrayList<AOpenbisSample>();
    for (Sample s : entities) {
      String code = s.getCode();
      String[] eSplit = s.getExperimentIdentifierOrNull().split("/");
      Map<String, String> p = s.getProperties();
      List<Factor> factors = xmlParser.getFactors(xmlParser.parseXMLString(p.get("Q_PROPERTIES")));
      for (Factor f : factors) {
        String name = f.getValue() + f.getUnit();
        factorMap.put(name, f);
      }
      res.add(new OpenbisBiologicalEntity(code, spaceCode, eSplit[eSplit.length - 1], p
          .get("Q_SECONDARY_NAME"), p.get("Q_ADDITIONAL_INFO"), factors, p.get("Q_NCBI_ORGANISM")));
    }
    return res;
  }

  /**
   * Parse existing extracts from the system
   * 
   * @param entities List of biological extracts in the form of openBIS Samples
   * @return List of AOpenbisSamples containing extracts
   * @throws JAXBException
   */
  private List<AOpenbisSample> parseExtracts(List<Sample> extracts) throws JAXBException {
    List<AOpenbisSample> res = new ArrayList<AOpenbisSample>();
    for (Sample s : extracts) {
      String code = s.getCode();

      String[] eSplit = s.getExperimentIdentifierOrNull().split("/");
      Map<String, String> p = s.getProperties();
      List<Factor> factors = xmlParser.getFactors(xmlParser.parseXMLString(p.get("Q_PROPERTIES")));
      for (Factor f : factors) {
        String name = f.getValue() + f.getUnit();
        factorMap.put(name, f);
      }
      res.add(new OpenbisBiologicalSample(code, spaceCode, eSplit[eSplit.length - 1], p
          .get("Q_SECONDARY_NAME"), p.get("Q_ADDITIONAL_INFO"), factors, p.get("Q_PRIMARY_TISSUE"),
          p.get("Q_TISSUE_DETAILED"), parseParents(code)));
    }
    return res;
  }

  /**
   * Parse existing test samples from the system
   * 
   * @param entities List of test samples in the form of openBIS Samples
   * @return List of AOpenbisSamples containing test samples
   * @throws JAXBException
   */
  private List<AOpenbisSample> parseTestSamples(List<Sample> entities) throws JAXBException {
    List<AOpenbisSample> res = new ArrayList<AOpenbisSample>();
    for (Sample s : entities) {
      String code = s.getCode();
      String[] eSplit = s.getExperimentIdentifierOrNull().split("/");
      Map<String, String> p = s.getProperties();
      List<Factor> factors = xmlParser.getFactors(xmlParser.parseXMLString(p.get("Q_PROPERTIES")));
      for (Factor f : factors) {
        String name = f.getValue() + f.getUnit();
        factorMap.put(name, f);
      }
      res.add(new OpenbisTestSample(code, spaceCode, eSplit[eSplit.length - 1], p
          .get("Q_SECONDARY_NAME"), p.get("Q_ADDITIONAL_INFO"), factors, p.get("Q_SAMPLE_TYPE"),
          parseParents(code)));
    }
    return res;
  }

  /**
   * Get the parents of a sample give its code and return them space delimited so they can be added
   * to a tsv
   * 
   * @param code
   * @return
   */
  private String parseParents(String code) {
    String res = "";
    for (Sample s : openbis.getParents(code))
      res += " " + s.getCode();
    return res.trim();
  }

  /**
   * Copy existing experiments and their samples from the information set in the wizard and the
   * wizard steps. After this function a tsv with the copied experiments can be created
   * 
   * @throws JAXBException
   */
  public void copyExperiment() throws JAXBException {
    prepareBasics();
    factorMap = new HashMap<String, Factor>();
    experiments = new ArrayList<OpenbisExperiment>();

    ExperimentBean exp = s1.getExperimentName();
    String type = exp.getExperiment_type();

    List<Sample> openbisEntities = new ArrayList<Sample>();
    List<Sample> openbisExtracts = new ArrayList<Sample>();
    List<Sample> openbisTests = new ArrayList<Sample>();

    List<Sample> originals = openbis.getSamplesofExperiment(exp.getID());
    Map<String, String> copies = new HashMap<String, String>();

    if (type.equals(ExperimentType.Q_EXPERIMENTAL_DESIGN.toString())) {

      openbisEntities = originals;
      openbisExtracts = getLowerSamples(openbisEntities);
      openbisTests = getLowerSamples(openbisExtracts);

      entities = copySamples(parseEntities(openbisEntities), copies);
      extracts = copySamples(parseExtracts(openbisExtracts), copies);
      tests = copySamples(parseTestSamples(openbisTests), copies);
    } else if (type.equals(ExperimentType.Q_SAMPLE_EXTRACTION.toString())) {

      openbisExtracts = originals;
      openbisEntities = getUpperSamples(openbisExtracts);
      openbisTests = getLowerSamples(openbisExtracts);

      entities = parseEntities(openbisEntities);

      extracts = copySamples(parseExtracts(openbisExtracts), copies);
      tests = copySamples(parseTestSamples(openbisTests), copies);

    } else if (type.equals(ExperimentType.Q_SAMPLE_PREPARATION.toString())) {

      openbisTests = originals;
      openbisExtracts = getUpperSamples(openbisTests);
      openbisEntities = getUpperSamples(openbisExtracts);

      entities = parseEntities(openbisEntities);
      extracts = parseExtracts(openbisExtracts);

      tests = copySamples(parseTestSamples(openbisTests), copies);
    }
  }

  /**
   * Copy a list of samples, used by the copy experiments function
   * 
   * @param samples
   * @param copies
   * @return
   */
  private List<AOpenbisSample> copySamples(List<AOpenbisSample> samples, Map<String, String> copies) {
    String newExp = buildExperimentName();
    String type = samples.get(0).getValueMap().get("SAMPLE TYPE");
    ExperimentType eType = ExperimentType.Q_EXPERIMENTAL_DESIGN;
    if (type.equals("Q_BIOLOGICAL_ENTITY"))
      eType = ExperimentType.Q_EXPERIMENTAL_DESIGN;
    else if (type.equals("Q_BIOLOGICAL_SAMPLE"))
      eType = ExperimentType.Q_SAMPLE_EXTRACTION;
    else if (type.equals("Q_TEST_SAMPLE"))
      eType = ExperimentType.Q_SAMPLE_PREPARATION;
    else
      logger.error("Unexpected type: " + type);
    experiments.add(new OpenbisExperiment(newExp, eType));

    for (AOpenbisSample s : samples) {
      s.setExperiment(newExp);
      String code = s.getCode();
      String newCode = code;
      if (s instanceof OpenbisBiologicalEntity) {
        newCode = projectCode + "ENTITY-" + firstFreeEntityID;
        firstFreeEntityID++;
      } else {
        if (nextBarcode == null) {
          classChar = 'A';
          nextBarcode =
              projectCode + Functions.createCountString(firstFreeBarcodeID, 3) + classChar;
          nextBarcode = nextBarcode + Functions.checksum(nextBarcode);
        } else {
          nextBarcode = Functions.incrementSampleCode(nextBarcode);
        }
        newCode = nextBarcode;
      }
      copies.put(code, newCode);
      s.setCode(newCode);
      String p = s.getParent();
      // change parent if parent was copied
      if (p != null && p.length() > 0)
        if (copies.containsKey(p))
          s.setParent(copies.get(p));
    }
    return samples;
  }

  /**
   * Gets all samples that are one level higher in the sample hierarchy of an attached experiment
   * than a given list of samples
   * 
   * @param originals
   * @return
   */
  private List<Sample> getUpperSamples(List<Sample> originals) {
    for (Sample s : originals) {
      List<Sample> parents = openbis.getParents(s.getCode());
      if (parents.size() > 0) {
        return openbis.getSamplesofExperiment(parents.get(0).getExperimentIdentifierOrNull());
      }
    }
    return null;
  }

  /**
   * Gets all samples that are one level lower in the sample hierarchy of an attached experiment
   * than a given list of samples
   * 
   * @param originals
   * @return
   */
  private List<Sample> getLowerSamples(List<Sample> originals) {
    for (Sample s : originals) {
      List<Sample> children = openbis.getChildrenSamples(s);
      if (children.size() > 0) {
        return openbis.getSamplesofExperiment(children.get(0).getExperimentIdentifierOrNull());
      }
    }
    return null;
  }

  /**
   * Creates a tab separated values file of the experiments created by the wizard, given that
   * samples have been prepared in the aggregator class
   * 
   * @return
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public String createTSV() throws FileNotFoundException, UnsupportedEncodingException {
    List<AOpenbisSample> samples = new ArrayList<AOpenbisSample>();
    samples.addAll(entities);
    samples.addAll(extracts);
    samples.addAll(tests);
    List<String> rows = new ArrayList<String>();

    List<String> header =
        new ArrayList<String>(Arrays.asList("SAMPLE TYPE", "SPACE", "EXPERIMENT",
            "Q_SECONDARY_NAME", "PARENT", "Q_PRIMARY_TISSUE", "Q_TISSUE_DETAILED",
            "Q_ADDITIONAL_INFO", "Q_NCBI_ORGANISM", "Q_SAMPLE_TYPE", "Q_EXTERNALDB_ID"));
    // TODO current assumption: tests should have more or an equal number of xml entries than
    // ancestors, because they inherit their entries
    int factorRowSize = 0;
    AOpenbisSample a = samples.get(0);
    for (AOpenbisSample b : tests) {
      if (factorRowSize < b.getFactors().size()) {
        factorRowSize = b.getFactors().size();
        a = b;
      }
    }
    String description = s1.getDescription();
    String secondaryName = s1.getProjectSecondaryName();
    
    String headerLine = "Identifier";
    for (String col : header)
      headerLine += "\t" + col;
    for (Factor f : a.getFactors()) {
      String label = f.getLabel();
      headerLine += "\tCondition: " + label;
    }
    for (AOpenbisSample s : samples) {
      String code = s.getCode();
      if (isEntity(code) || Functions.isQbicBarcode(code)) {
        Map<String, String> data = s.getValueMap();
        String row = s.getCode();
        List<String> factors = s.getFactorStringsWithoutLabel();

        for (String col : header) {
          String val = "";
          if (data.containsKey(col))
            val = data.get(col);
          if (val == null)
            val = "";
          row += "\t" + val;
        }
        for (int i = 0; i < factors.size(); i++)
          row += "\t" + factors.get(i);
        for (int i = factors.size(); i < factorRowSize; i++)
          row += "\t";

        rows.add(row);
      }
    }
    String result = "";
    result += "#PROJECT_DESCRIPTION="+description + "\n";
    result += "#ALTERNATIVE_NAME="+secondaryName + "\n";
    result += headerLine + "\n";
    for (String line : rows)
      result += line + "\n";
    this.tsvContent = result;
    return result;
  }

  private static boolean isEntity(String code) {
    String pattern = "Q[A-Z0-9]{4}ENTITY-[0-9]+";
    return code.matches(pattern);
  }

  public File getTSV() throws FileNotFoundException, UnsupportedEncodingException {
    String file = ProjectwizardUI.tmpFolder + "tmp_" + getTSVName() + ".tsv";
    PrintWriter writer = new PrintWriter(file, "UTF-8");
    for (String line : tsvContent.split("\n"))
      writer.println(line);
    writer.close();
    return new File(file);
  }

  public String getTSVName() {
    return spaceCode + "_" + projectCode;
  }

  public String getTSVContent() {
    return tsvContent;
  }

  public List<AOpenbisSample> getEntities() {
    return entities;
  }

  public void parseAll() throws JAXBException {
    prepareBasics();
    factorMap = new HashMap<String, Factor>();
    // experiments = new ArrayList<OpenbisExperiment>();

    ExperimentBean exp = s1.getExperimentName();
    String type = exp.getExperiment_type();

    List<Sample> openbisEntities = new ArrayList<Sample>();
    List<Sample> openbisExtracts = new ArrayList<Sample>();
    List<Sample> openbisTests = new ArrayList<Sample>();

    List<Sample> originals = openbis.getSamplesofExperiment(exp.getID());

    if (type.equals(ExperimentType.Q_EXPERIMENTAL_DESIGN.toString())) {

      openbisEntities = originals;
      openbisExtracts = getLowerSamples(openbisEntities);
      openbisTests = getLowerSamples(openbisExtracts);

      entities = parseEntities(openbisEntities);
      extracts = parseExtracts(openbisExtracts);
      tests = parseTestSamples(openbisTests);
    } else if (type.equals(ExperimentType.Q_SAMPLE_EXTRACTION.toString())) {

      openbisExtracts = originals;
      openbisEntities = getUpperSamples(openbisExtracts);
      openbisTests = getLowerSamples(openbisExtracts);

      entities = parseEntities(openbisEntities);

      extracts = parseExtracts(openbisExtracts);
      tests = parseTestSamples(openbisTests);

    } else if (type.equals(ExperimentType.Q_SAMPLE_PREPARATION.toString())) {

      openbisTests = originals;
      openbisExtracts = getUpperSamples(openbisTests);
      openbisEntities = getUpperSamples(openbisExtracts);

      entities = parseEntities(openbisEntities);
      extracts = parseExtracts(openbisExtracts);

      tests = parseTestSamples(openbisTests);
    }
  }

  public List<AOpenbisSample> getTests() {
    return tests;
  }

  public void resetExtracts() {
    extracts = new ArrayList<AOpenbisSample>();
  }

  public void resetTests() {
    tests = new ArrayList<AOpenbisSample>();
  }
}
