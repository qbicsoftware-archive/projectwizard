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
    String res = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>   <qproperties>";
    for (Property p : properties) {
      res +=
          "   <property label=\"" + p.getName() + "\" type=\"" + p.getType()
              + "\" unit=\"" + p.getUnit() + "\" value=\"" + p.getValue() + "\"/>";
    }
    res += "</qproperties>";
    return res;
  }
}
