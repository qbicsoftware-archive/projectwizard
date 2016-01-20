package io;

/**
 * The ConfigurationManger interface represents the entire .properties file. One might think about
 * adding a getAttribute method in order to make it more generic.
 * 
 * @author wojnar
 * 
 */
public interface ConfigurationManager {

  public String getVocabularyMSLabeling();

  public String getConfigurationFileName();

  public String getDataSourceUser();

  public String getDataSourcePassword();

  public String getDataSourceUrl();

  public String getBarcodeScriptsFolder();

  public String getTmpFolder();

  public String getBarcodePathVariable();

  public String getAttachmentURI();

  public String getAttachmentUser();

  public String getAttachmenPassword();

  public String getAttachmentMaxSize();

  public String getMysqlHost();

  public String getMysqlPort();

  public String getMysqlDB();

  public String getMysqlUser();

  public String getMysqlPass();
}
