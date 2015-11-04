package main;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import parser.XMLParser;
import properties.Factor;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

public class Incubator {

  private String DATASOURCE_USER = "datasource.user";
  private String DATASOURCE_PASS = "datasource.password";
  private String DATASOURCE_URL = "datasource.url";
  private String dataSourceUser;
  private String dataSourcePass;
  private String dataSourceURL;
  OpenBisClient openbis;

  public static void main(String[] args) {
    // DecimalFormat df = new DecimalFormat("#.###");
    // List<Double> input = new ArrayList<Double>(Arrays.asList(0.5,0.8,1.3,1.5,2.0,2.4,2.7));
    // List<Double> V = new ArrayList<Double>();
    // double n = 3;
    // double k = 1;
    // for(Double L : input) {
    // double L_pow = Math.pow(L, n);
    // double res = L_pow / (Math.pow(k, n) + L_pow);
    // System.out.println(L+"\t"+df.format(res));
    // V.add(res);
    // // System.out.println(Math.log10(L));
    // // System.out.println(Math.log10(res/(1-res)));
    // }


    Incubator m = new Incubator();
    m.init();
    OpenBisClient o = m.openbis;
    o.login();
    Map<String, Object> params = new HashMap<String, Object>();
    List<String> codes = new ArrayList<String>();
    System.out.println("collect ids");
    for (Sample s : o.getSamplesOfProject("/CHICKEN_FARM/QQNHR")) {
      codes.add(s.getIdentifier());
    }
    params.put("types", "Q_BIOLOGICAL_ENTITY");
    params.put("ids", codes);
    System.out.println("query\n");

    params.put("types", "Q_TEST_SAMPLE");
    QueryTableModel res = o.getAggregationService("get-experimental-design-tsv", params);
    try {
      m.printTSV(res);
    } catch (JAXBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void printTSV(QueryTableModel data) throws JAXBException {
    XMLParser p = new XMLParser();
    StringBuilder header =
        new StringBuilder("QBiC ID\tSecondary Name\tSample Source\tExternal ID\tExtract Type");
    String xml = (String) data.getRows().get(0)[5];
    for (Factor f : p.getFactorsFromXML(xml)) {
      header.append("\t" + f.getLabel());
    }
    System.out.println(header);
    for (Serializable[] ss : data.getRows()) {
      StringBuilder line =
          new StringBuilder(ss[0] + "\t" + ss[1] + "\t" + ss[2] + "\t" + ss[3] + "\t" + ss[4]);
      xml = (String) ss[5];
      for (Factor f : p.getFactorsFromXML(xml)) {
        line.append("\t" + f.getValue());
        if (f.hasUnit())
          line.append(f.getUnit());
      }
      System.out.println(line);
    }
  }

  public void lastDatasetRegistered(List<DataSet> datasets, Date lastModifiedDate,
      StringBuilder lastModifiedExperiment, StringBuilder lastModifiedSample) {
    String exp = "N/A";
    String samp = "N/A";
    for (DataSet dataset : datasets) {
      Date date = dataset.getRegistrationDate();

      if (date.after(lastModifiedDate)) {
        samp = dataset.getSampleIdentifierOrNull();
        if (samp == null) {
          samp = "N/A";
        }
        exp = dataset.getExperimentIdentifier();
        lastModifiedDate.setTime(date.getTime());
        break;
      }
    }
    lastModifiedExperiment.append(exp);
    lastModifiedSample.append(samp);
  }

  private void init() {
    readConfig();
    this.openbis = new OpenBisClient(dataSourceUser, dataSourcePass, dataSourceURL);
  }

  private void readConfig() {
    Properties config = new Properties();
    try {
      config.load(new FileReader("/Users/frieda/Desktop/testing/portlet.properties"));
      StringWriter configDebug = new StringWriter();
      config.list(new PrintWriter(configDebug));
      dataSourceUser = config.getProperty(DATASOURCE_USER);
      dataSourcePass = config.getProperty(DATASOURCE_PASS);
      dataSourceURL = config.getProperty(DATASOURCE_URL);
    } catch (IOException e) {
      System.err.println("Failed to load configuration: " + e);
    }
  }

}
