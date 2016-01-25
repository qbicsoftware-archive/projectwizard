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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import logging.Log4j2Logger;
import model.AOpenbisSample;
import control.SampleCounter;
import control.WizardDataAggregator;

public class NewAggregator {

  private SampleCounter counter;

  private NewProjectInfo projectInfo;
  private Map<Integer, String> tempIDsToCodes;
  private ExperimentLevel species;
  private ExperimentLevel extracts;
  private ExperimentLevel extractPools;
  private ExperimentLevel analytes;
  private ExperimentLevel analytePools;
  private ExperimentLevel msExperiment;

  logging.Logger logger = new Log4j2Logger(WizardDataAggregator.class);

  public void setProjectInfo(NewProjectInfo projectInfo) {
    this.projectInfo = projectInfo;
  }

  public void createCodes(SampleCounter counter) {
    this.counter = counter;
    tempIDsToCodes = new HashMap<Integer, String>();
    if (species != null)
      for (AOpenbisSample s : species.getSamples()) {
        String code = counter.getNewEntity();
        tempIDsToCodes.put(s.getTempID(), code);
        s.setCode(code);
      }
    if (extracts != null)
      for (AOpenbisSample s : extracts.getSamples()) {
        createCode(s);
        addParents(s);
      }
    if (extractPools != null)
      for (AOpenbisSample s : extracts.getSamples()) {
        createCode(s);
        addParents(s);
      }
    if (analytes != null)
      for (AOpenbisSample s : extracts.getSamples()) {
        createCode(s);
        addParents(s);
      }
    if (analytePools != null)
      for (AOpenbisSample s : extracts.getSamples()) {
        createCode(s);
        addParents(s);
      }
    if (msExperiment != null)
      for (AOpenbisSample s : extracts.getSamples()) {
        createCode(s);
        addParents(s);
      }
  }
  
  private void createCode(AOpenbisSample s) {
    String code = counter.getNewBarcode();
    s.setCode(code);
    tempIDsToCodes.put(s.getTempID(), code);
  }

  private void addParents(AOpenbisSample s) {
    List<String> parents = new ArrayList<String>();
    for (int p : s.getTempParentIDs()) {
      parents.add(tempIDsToCodes.get(p));
    }
    s.setParent(StringUtils.join(parents, " "));
  }

  public NewProjectInfo getProjectInfo() {
    return projectInfo;
  }

  public void setEntities(List<AOpenbisSample> samples) {
    // TODO Auto-generated method stub
  }

  public String getTSVContent() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getTSVName() {
    // TODO Auto-generated method stub
    return null;
  }

  public File getTSV() {
    // TODO Auto-generated method stub
    return null;
  }

  public void createTSV() {
    // TODO Auto-generated method stub

  }
}
