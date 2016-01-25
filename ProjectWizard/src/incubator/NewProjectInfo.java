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

public class NewProjectInfo {

  private String space;
  private String code;
  private String description;
  private String secondaryName;
  private String principalInvestigatorAccount;

  public NewProjectInfo(String space, String code, String description, String secondaryName,
      String principalInvestigatorAccount) {
    this.space = space;
    this.code = code;
    this.description = description;
    this.secondaryName = secondaryName;
    this.principalInvestigatorAccount = principalInvestigatorAccount;
  }

  public NewProjectInfo(String spaceCode, String projectCode) {
    this.space = spaceCode;
    this.code = projectCode;
  }

  public String getSpace() {
    return space;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public String getSecondaryName() {
    return secondaryName;
  }

  public String getPrincipalInvestigatorAccount() {
    return principalInvestigatorAccount;
  }

}
