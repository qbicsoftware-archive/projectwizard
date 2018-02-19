package control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import life.qbic.openbis.openbisclient.IOpenBisClient;

public class OpenbisSummaryFetcher {

  private IOpenBisClient openbis;

  public OpenbisSummaryFetcher(IOpenBisClient openbis) {
    this.openbis = openbis;
  }

  public List<String> makeMeOneWithEverything() {
    List<String> res = new ArrayList<String>();
    // List<String> spaces = openbis.listSpaces();
    List<String> spaces = new ArrayList<String>(Arrays.asList("CHICKEN_FARM"));
    for (String space : spaces) {
      for (Project p : openbis.getProjectsOfSpace(space)) {
        String code = p.getCode();
        Map<String, Integer> samplesOfType = new HashMap<String, Integer>();
        Map<String, Integer> datasetsOfType = new HashMap<String, Integer>();
        List<Sample> samples =
            openbis.getSamplesWithParentsAndChildrenOfProjectBySearchService(code);
        for (Sample s : samples) {
          if (s.getSampleTypeCode().equals("Q_TEST_SAMPLE")) {
            String type = s.getProperties().get("Q_SAMPLE_TYPE");
            if (samplesOfType.containsKey(type))
              samplesOfType.put(type, samplesOfType.get(type) + 1);
            else
              samplesOfType.put(type, 1);
          }
        }
        List<DataSet> dss =
            openbis.getDataSetsOfProjectByIdentifierWithSearchCriteria(p.getIdentifier());
        for (DataSet ds : dss) {
          String type = ds.getDataSetTypeCode();
          if (datasetsOfType.containsKey(type))
            datasetsOfType.put(type, datasetsOfType.get(type) + 1);
          else
            datasetsOfType.put(type, 1);
        }
        System.out.println(space + " - " + code);
        for (String type : samplesOfType.keySet()) {
          System.out.println(type + ": " + samplesOfType.get(type) + " samples");
        }
        for (String type : datasetsOfType.keySet()) {
          System.out.println(type + ": " + datasetsOfType.get(type) + " datasets");
        }
        System.out.println();
      }
    }
    return res;
  }

}
