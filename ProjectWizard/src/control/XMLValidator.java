package control;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import org.xml.sax.SAXException;

public class XMLValidator {
  
  File schemaFile;
  
  public XMLValidator() throws MalformedURLException {
    schemaFile = new File("/Users/frieda/Desktop/testing/sample_prop_schema.xsd");
  }

  public boolean validate(File xmlFile) throws IOException, SAXException {
    Source xml = new StreamSource(xmlFile);
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = schemaFactory.newSchema(schemaFile);
    Validator validator = schema.newValidator();
    try {
      validator.validate(xml);
      System.out.println(xml.getSystemId() + " is valid");
      return true;
    } catch (SAXException e) {
      System.out.println(xml.getSystemId() + " is NOT valid");
      System.out.println("Reason: " + e.getLocalizedMessage());
      return false;
    }
  }
  
  public static void main(String[] args) throws IOException, SAXException {
    XMLValidator x = new XMLValidator();
    x.validate(new File("/Users/frieda/Desktop/testing/sample_prop_example.xml"));
  }
}
