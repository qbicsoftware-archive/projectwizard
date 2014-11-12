package model;

import java.util.ArrayList;
import java.util.List;

public class XMLProperties {

  List<Property> properties;

  public XMLProperties(List<Property> properties) {
    this.properties = properties;
  }

  public XMLProperties() {
    this(new ArrayList<Property>());
  }

  public XMLProperties(String xml) {
    parseProperties(xml);
  }

  private void parseProperties(String xml) {
    //TODO
    this.properties = new ArrayList<Property>();
  }

  //TODO
  public String getXML() {
    String res = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    		"<qproperties xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
    		"xsi:noNamespaceSchemaLocation=\"sample_prop_schema.xsd\">";
    for (Property p : properties) {
      res +=
          "   <property label=\"" + p.getName() + "\" type=\"" + p.getType()
              + "\" unit=\"" + p.getUnit() + "\" value=\"" + p.getValue() + "\"/>";
    }
    res += "</qproperties>";
    return res;
  }
}