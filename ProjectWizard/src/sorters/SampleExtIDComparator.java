package sorters;


import java.util.Comparator;

import model.IBarcodeBean;

/**
 * Compares IBarcodeBeans by external db id
 * 
 * @author Andreas Friedrich
 * 
 */
public class SampleExtIDComparator implements Comparator<IBarcodeBean> {

  private static final SampleExtIDComparator instance = new SampleExtIDComparator();

  public static SampleExtIDComparator getInstance() {
    return instance;
  }

  private SampleExtIDComparator() {}

  @Override
  public int compare(IBarcodeBean o1, IBarcodeBean o2) {
    return o1.getExtID().compareTo(o2.getExtID());
  }

}
