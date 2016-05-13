/*******************************************************************************
 * QBiC Project Wizard enables users to create hierarchical experiments including different study
 * conditions using factorial design. Copyright (C) "2016" Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package sorters;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import logging.Log4j2Logger;
import model.IBarcodeBean;
import model.NewModelBarcodeBean;

/**
 * Compares IBarcodeBeans by sample ID
 * 
 * @author Andreas Friedrich
 *
 */
public class SampleCodeComparator implements Comparator<IBarcodeBean> {

  private static final SampleCodeComparator instance = new SampleCodeComparator();
  logging.Logger logger = new Log4j2Logger(SampleCodeComparator.class);

  public static SampleCodeComparator getInstance() {
    return instance;
  }

  private SampleCodeComparator() {}

  @Override
  public int compare(IBarcodeBean o1, IBarcodeBean o2) {
    String c1 = o1.getCode();
    String c2 = o2.getCode();
    if (!c1.startsWith("Q") || c1.contains("ENTITY") || !c2.startsWith("Q")
        || c2.contains("ENTITY"))
      return o1.getCode().compareTo(o2.getCode());
    try {
      // compares sample codes by projects, ending letters (999A --> 001B) and numbers (001A -->
      // 002A)
      int projCompare = c1.substring(0, 5).compareTo(c2.substring(0, 5));
      int numCompare = c1.substring(5, 8).compareTo(c2.substring(5, 8));
      int letterCompare = c1.substring(8, 9).compareTo(c2.substring(8, 9));
      if (projCompare != 0)
        return projCompare;
      else {
        if (letterCompare != 0)
          return letterCompare;
        else
          return numCompare;
      }
    } catch (Exception e) {
      logger.warn("Could not split code " + c1 + " or " + c2
          + ". Falling back to primitive lexicographical comparison.");
    }
    return o1.getCode().compareTo(o2.getCode());
  }
}
