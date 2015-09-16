package sorters;


import java.util.Comparator;

import model.IBarcodeBean;

/**
 * Compares IBarcodeBeans by sample type (tissue or prepared samples)
 * @author Andreas Friedrich
 *
 */
public class SampleTypeComparator implements Comparator<IBarcodeBean> {

	private static final SampleTypeComparator instance = 
			new SampleTypeComparator();

	public static SampleTypeComparator getInstance() {
		return instance;
	}

	private SampleTypeComparator() {
	}

	@Override
	public int compare(IBarcodeBean o1, IBarcodeBean o2) {
		return o1.getType().compareTo(o2.getType());
	}

}
