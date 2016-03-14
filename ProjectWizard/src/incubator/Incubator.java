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
package incubator;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import main.OpenBisClient;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;

public class Incubator {

  private static String DATASOURCE_USER = "datasource.user";
  private static String DATASOURCE_PASS = "datasource.password";
  private static String DATASOURCE_URL = "datasource.url";
  private static String dataSourceUser;
  private static String dataSourcePass;
  private static String dataSourceURL;
  private static OpenBisClient openbis;

  public static void main(String[] args) {
    openbis = new OpenBisClient(dataSourcePass, dataSourcePass, dataSourceUser);
    init();
    System.out.println(openbis.getOpenbisInfoService().listProjectsOnBehalfOfUser(
        openbis.getSessionToken(), "admin"));
  }

  private static void readConfig() {
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

  private static void init() {
    readConfig();
    openbis = new OpenBisClient(dataSourceUser, dataSourcePass, dataSourceURL);
    openbis.login();
  }

}
