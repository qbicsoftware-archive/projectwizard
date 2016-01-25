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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.AOpenbisSample;

public class ExperimentLevel {

  private String experimentType;
  private List<AOpenbisSample> samples;
  private Map<String, String> metaInfos;

  public ExperimentLevel(String experimentType, List<AOpenbisSample> samples) {
    this.metaInfos = new HashMap<String, String>();
    this.experimentType = experimentType;
    this.samples = samples;
  }

  public List<AOpenbisSample> getSamples() {
    return samples;
  }

  public void addMetaInfo(String name, String value) {
    metaInfos.put(name, value);
  }

  public Map<String, String> GetMetaInfos() {
    return metaInfos;
  }

  public String getExperimentType() {
    return experimentType;
  }
}
