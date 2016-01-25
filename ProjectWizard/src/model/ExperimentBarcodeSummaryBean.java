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

/**
 * Bean Object representing an experiment with some information about its samples to provide an
 * overview, e.g. for barcode creation
 * 
 * @author Andreas Friedrich
 * 
 */
public class ExperimentBarcodeSummaryBean {

  String Bio_Type;
  String Amount;
  String experimentID;

  /**
   * Creates a new ExperimentBarcodeSummaryBean
   * 
   * @param bioType the type of samples in this experiment, for example tissue or measurement type
   * @param amount the amount of samples in this experiment
   * @param experimentID the experiment identifier
   */
  public ExperimentBarcodeSummaryBean(String bioType, String amount, String expID) {
    Bio_Type = bioType;
    Amount = amount;
    this.experimentID = expID;
  }

  // show only code
  public String getExperiment() {
    String[] split = experimentID.split("/");
    return split[split.length - 1];
  }

  public String fetchExperimentID() {
    return experimentID;
  }

  public String getBio_Type() {
    return Bio_Type;
  }

  public void setBio_Type(String bio_Type) {
    Bio_Type = bio_Type;
  }

  public String getAmount() {
    return Amount;
  }

  public void setAmount(String amount) {
    Amount = amount;
  }

}
