package processes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import parser.XMLParser;
import properties.Factor;
import steps.FinishStep;

import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

import com.vaadin.server.StreamResource;

public class TSVReadyRunnable implements Runnable {

  FinishStep layout;
  QueryTableModel table;
  String project;

  public TSVReadyRunnable(FinishStep layout, QueryTableModel table, String project) {
    this.layout = layout;
    this.table = table;
    this.project = project;
  }

  @Override
  public void run() {
    List<StreamResource> streams = new ArrayList<StreamResource>();
    Map<String, List<Serializable[]>> tables = sortRows(table);
    streams.add(getTSVStream(getTSVString(tables.get("Q_BIOLOGICAL_ENTITY")), project
        + "_sample_sources"));
    streams.add(getTSVStream(getTSVString(tables.get("Q_BIOLOGICAL_SAMPLE")), project
        + "_sample_extracts"));
    if (tables.containsKey("Q_TEST_SAMPLE"))
      streams.add(getTSVStream(getTSVString(tables.get("Q_TEST_SAMPLE")), project
          + "_sample_preparations"));
    layout.armButtons(streams);
  }

  private Map<String, List<Serializable[]>> sortRows(QueryTableModel table) {
    Map<String, List<Serializable[]>> res = new HashMap<String, List<Serializable[]>>();
    for (Serializable[] row : table.getRows()) {
      String type = (String) row[6];
      if (res.containsKey(type))
        res.get(type).add(row);
      else {
        List<Serializable[]> subtable = new ArrayList<Serializable[]>();
        subtable.add(row);
        res.put(type, subtable);
      }
    }
    return res;
  }

  public StreamResource getTSVStream(final String content, String name) {
    StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
      @Override
      public InputStream getStream() {
        try {
          InputStream is = new ByteArrayInputStream(content.getBytes());
          return is;
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }
    }, String.format("%s.tsv", name));
    return resource;
  }

  private String getTSVString(List<Serializable[]> table) {
    XMLParser p = new XMLParser();
    StringBuilder tsv =
        new StringBuilder("QBiC ID\tSecondary Name\tSample Source\tExternal ID\tExtract Type");
    String xml = (String) table.get(0)[5];
    List<Factor> factors = new ArrayList<Factor>();
    if (!xml.isEmpty()) {
      try {
        factors = p.getFactorsFromXML(xml);
      } catch (JAXBException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      for (Factor f : factors) {
        tsv.append("\t" + f.getLabel());
      }
    }
    for (Serializable[] ss : table) {
      StringBuilder line =
          new StringBuilder("\n" + ss[0] + "\t" + ss[1] + "\t" + ss[2] + "\t" + ss[3] + "\t"
              + ss[4]);
      xml = (String) ss[5];
      if (!xml.isEmpty()) {
        try {
          factors = p.getFactorsFromXML(xml);
        } catch (JAXBException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        for (Factor f : factors) {
          line.append("\t" + f.getValue());
          if (f.hasUnit())
            line.append(f.getUnit());
        }
      }
      tsv.append(line);
    }
    return tsv.toString();
  }

}
