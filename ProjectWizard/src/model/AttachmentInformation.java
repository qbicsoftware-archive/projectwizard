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
