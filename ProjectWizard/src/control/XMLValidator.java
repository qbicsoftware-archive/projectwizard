/*******************************************************************************
 * QBiC Project Wizard enables users to create hierarchical experiments including different study conditions using factorial design.
 * Copyright (C) "2016"  Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
