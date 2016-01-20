package io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Implements {@see ConfigurationManager}. Does not need Portal environment.
 * 
 * @author wojnar
 * 
 */

public enum LiferayIndependentConfigurationManager implements ConfigurationManager {
  Instance;
  public static final String CONFIGURATION_SUFFIX = ".configuration";
  public static final String DATASOURCE_KEY = "datasource";
  public static final String DATASOURCE_USER = "datasource.user";
  public static final String DATASOURCE_PASS = "datasource.password";
  public static final String DATASOURCE_URL = "datasource.url";

  public static final String GENOMEVIEWER_URL = "genomeviewer.url";
  public static final String GENOMEVIEWER_RESTAPI = "genomeviewer.restapi";

  public static final String TMP_FOLDER = "tmp.folder";
  public static final String SCRIPTS_FOLDER = "barcode.scripts";
  public static final String PATH_VARIABLE = "path.variable";

  public static final String PATH_TO_GUSE_WORKFLOWS = "path_to_guse_workflows";
  public static final String PATH_TO_GUSE_CERTIFICATE = "path_to_certificate";
  public static final String PATH_TO_WF_CONFIG = "path_to_wf_config";

  public static final String PATH_TO_DROPBOXES = "path_to_dropboxes";

  public static final String GUSE_REMOTE_API_URL = "guse_remoteapi_url";
  public static final String GUSE_REMOTE_API_PASS = "guse_remoteapi_password";

  public static final String ATTACHMENT_URI = "attachment.uri";
  public static final String ATTACHMENT_USER = "attachment.user";
  public static final String ATTACHMENT_PASS = "attachment.password";
  public static final String ATTACHMENT_MAX_SIZE = "max.attachment.size";

  public static final String MSQL_HOST = "mysql.host";
  public static final String MSQL_DB = "mysql.db";
  public static final String MSQL_USER = "mysql.user";
  public static final String MSQL_PORT = "mysql.port";
  public static final String MSQL_PASS = "mysql.pass";
  
  private String LABELING_METHODS = "vocabulary.ms.labeling";

  private String configurationFileName;
  private String dataSourceUser;
  private String dataSourcePass;
  private String dataSourceUrl;

  private String tmpFolder;
  private String scriptsFolder;
  private String pathVariable;

  private String attachmentURI;
  private String attachmentUser;
  private String attachmentPass;
  private String attachmentMaxSize;

  private String msqlHost;
  private String msqlDB;
  private String msqlUser;
  private String msqlPort;
  private String msqlPass;
  
  private String labelingMethods;

//  private String portletPropertiesFileName = "portlet.properties";

  private boolean initialized = false;


  public boolean isInitialized() {
    return initialized;
  }

//  /**
//   * init the portlet with the following properties file.
//   * 
//   * @param portletPropertiesFileName
//   */
//  public void init(String portletPropertiesFileName) {
//    this.portletPropertiesFileName = portletPropertiesFileName;
//    init();
//  }

  public void init() {
    Properties portletConfig = new Properties();
    try {
      List<String> configs =
          new ArrayList<String>(Arrays.asList("/Users/frieda/Desktop/testing/portlet.properties",
              "/home/luser/liferay-portal-6.2-ce-ga4/portlets.properties",
              "/usr/local/share/guse/portlets.properties",
              "/home/tomcat-liferay/liferay_production/portlets.properties"));
      for (String s : configs) {
        File f = new File(s);
        if (f.exists())
          portletConfig.load(new FileReader(s));
      }
      dataSourceUser = portletConfig.getProperty(DATASOURCE_USER);
      dataSourcePass = portletConfig.getProperty(DATASOURCE_PASS);
      dataSourceUrl = portletConfig.getProperty(DATASOURCE_URL);

      tmpFolder = portletConfig.getProperty(TMP_FOLDER);
      scriptsFolder = portletConfig.getProperty(SCRIPTS_FOLDER);
      pathVariable = portletConfig.getProperty(PATH_VARIABLE);

      attachmentURI = portletConfig.getProperty(ATTACHMENT_URI);
      attachmentUser = portletConfig.getProperty(ATTACHMENT_USER);
      attachmentPass = portletConfig.getProperty(ATTACHMENT_PASS);
      attachmentMaxSize = portletConfig.getProperty(ATTACHMENT_MAX_SIZE);
      
      msqlHost = portletConfig.getProperty(MSQL_HOST);
      msqlDB = portletConfig.getProperty(MSQL_DB);
      msqlPort = portletConfig.getProperty(MSQL_PORT);
      msqlUser = portletConfig.getProperty(MSQL_USER);
      msqlPass = portletConfig.getProperty(MSQL_PASS);
      
      labelingMethods = portletConfig.getProperty(LABELING_METHODS);

    } catch (IOException ex) {
      ex.printStackTrace();
    }
    initialized = true;
  }

  @Override
  public String getConfigurationFileName() {
    return configurationFileName;
  }

  @Override
  public String getDataSourceUser() {
    return dataSourceUser;
  }

  @Override
  public String getDataSourcePassword() {
    return dataSourcePass;
  }

  @Override
  public String getDataSourceUrl() {
    return dataSourceUrl;
  }

  @Override
  public String getBarcodeScriptsFolder() {
    return scriptsFolder;
  }

  @Override
  public String getTmpFolder() {
    return tmpFolder;
  }

  @Override
  public String getBarcodePathVariable() {
    return pathVariable;
  }

  @Override
  public String getAttachmentURI() {
    return attachmentURI;
  }

  @Override
  public String getAttachmentUser() {
    return attachmentUser;
  }

  @Override
  public String getAttachmenPassword() {
    return attachmentPass;
  }

  @Override
  public String getAttachmentMaxSize() {
    return attachmentMaxSize;
  }

  @Override
  public String getMysqlHost() {
    return msqlHost;
  }

  @Override
  public String getMysqlPort() {
    return msqlPort;
  }

  @Override
  public String getMysqlDB() {
    return msqlDB;
  }

  @Override
  public String getMysqlUser() {
    return msqlUser;
  }

  @Override
  public String getMysqlPass() {
    return msqlPass;
  }

  @Override
  public String getVocabularyMSLabeling() {
    return labelingMethods;
  }
}
