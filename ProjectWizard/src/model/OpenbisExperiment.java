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

import java.util.Map;

/**
 * Class representing an experiment with some metadata
 * @author Andreas Friedrich
 *
 */
public class OpenbisExperiment {

  String openbisName;
  ExperimentType type;
  String Q_SECONDARY_NAME;
  String Q_ADDITIONAL_NOTES;
  Map<String, Object> properties;

  /**
   * Creates a new Openbis Experiment
   * @param name Name of the experiment
   * @param type Experiment type
   * @param props Map with experimental properties
   */
  public OpenbisExperiment(String name, ExperimentType type, Map<String, Object> props) {
    this.properties = props;
    this.openbisName = name;
    this.type = type;
  }
  
  /**
   * Creates a new Openbis Experiment
   * @param name Name of the experiment
   * @param type Experiment type
   */
  public OpenbisExperiment(String name, ExperimentType type) {
    this.openbisName = name;
    this.type = type;
  }

  /**
   * Creates a new Openbis Experiment
   * @param name Name of the experiment
   * @param type Experiment type
   * @param secondaryName Secondary name of the experiment
   * @param additionalNotes Free text additonal notes concerning the experiment
   */
  OpenbisExperiment(String openbisName, ExperimentType type, String secondaryName,
      String additionalNotes) {
    this(openbisName, type);
    this.Q_ADDITIONAL_NOTES = additionalNotes;
    this.Q_SECONDARY_NAME = secondaryName;
  }

  public String getOpenbisName() {
    return openbisName;
  }

  public ExperimentType getType() {
    return type;
  }
  
  public Map<String, Object> getMetadata() {
    return properties;
  }

  public String getQ_SECONDARY_NAME() {
    return Q_SECONDARY_NAME;
  }

  public String getQ_ADDITIONAL_NOTES() {
    return Q_ADDITIONAL_NOTES;
  }
}
