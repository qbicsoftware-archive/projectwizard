package sorters;


import java.util.Comparator;

import model.IBarcodeBean;

/**
 * Compares IBarcodeBeans by sample ID
 * @author Andreas Friedrich
 *
 */
public class SampleCodeComparator implements Comparator<IBarcodeBean> {

	private static final SampleCodeComparator instance = 
			new SampleCodeComparator();

	public static SampleCodeComparator getInstance() {
		return instance;
	}

	private SampleCodeComparator() {
	}

	@Override
	public int compare(IBarcodeBean o1, IBarcodeBean o2) {
		return o1.getCode().compareTo(o2.getCode());
	}

}
