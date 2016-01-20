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
    String s1 = o1.getExtID();
    String s2 = o2.getExtID();
    if (s1 == null)
      s1 = "";
    if (s2 == null)
      s2 = "";
    return s1.compareTo(s2);
  }

}
