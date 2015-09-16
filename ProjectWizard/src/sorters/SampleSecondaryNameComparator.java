package sorters;


import java.util.Comparator;

import model.IBarcodeBean;

/**
 * Compares IBarcodeBeans by secondary name
 * 
 * @author Andreas Friedrich
 * 
 */
public class SampleSecondaryNameComparator implements Comparator<IBarcodeBean> {

  private static final SampleSecondaryNameComparator instance = new SampleSecondaryNameComparator();

  public static SampleSecondaryNameComparator getInstance() {
    return instance;
  }

  private SampleSecondaryNameComparator() {}

  @Override
  public int compare(IBarcodeBean o1, IBarcodeBean o2) {
    return o1.getSecondaryName().compareTo(o2.getSecondaryName());
  }

}
