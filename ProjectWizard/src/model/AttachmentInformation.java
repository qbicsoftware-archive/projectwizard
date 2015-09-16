package model;

import org.apache.commons.io.FilenameUtils;

public class AttachmentInformation {

  private String name;
  private String secondary_name;
  private String user;
  private String barcode;

  public AttachmentInformation(String name, String secondary_name, String user, String barcode) {
    this.name = name;
    this.secondary_name = secondary_name;
    this.user = user;
    this.barcode = barcode;
  }

  public String getName() {
    return name;
  }

  public String getInfo() {
    return secondary_name;
  }

  public String getUser() {
    return user;
  }

  /**
   * Returns the file name as it should be sent to the dropbox handler, including barcode, user name
   * and optional secondary name. Paths still need to be handled by a controller class.
   * 
   * @return
   */
  public String getTargetFileName() {
    String ext = "";
    int i = name.lastIndexOf('.');
    if (i > 0) {
      ext = "." + name.substring(i + 1);
    }
    String res = barcode + "_" + name.replace(ext, "") + "_user_" + user;
    if (secondary_name != null && !secondary_name.isEmpty())
      res += "_secname_" + secondary_name;
    return res.replace(" ", "_").replace("up_", "") + ext;
  }

}
