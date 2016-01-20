package uicomponents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import main.ProjectwizardUI;
import model.SampleToBarcodeFieldTranslator;
import model.SortBy;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import componentwrappers.StandardTextField;

public class SheetOptionComponent extends VerticalLayout {

  private ComboBox firstOption;
  private ComboBox secondOption;
  private OptionGroup sortby;

  SampleToBarcodeFieldTranslator translator;

  private List<String> options = new ArrayList<String>(Arrays.asList("Lab ID",
      "Tissue/Extr. Material", "Secondary Name", "Parent Samples (Source)"));

  public SheetOptionComponent(SampleToBarcodeFieldTranslator translator) {
    this.translator = translator;

    setMargin(true);
    setSpacing(true);

    StandardTextField firstCol = new StandardTextField("First Column");
    firstCol.setValue("QBiC Barcode");
    firstCol.setEnabled(false);
    addComponent(ProjectwizardUI
        .questionize(
            firstCol,
            "Choose which columns will be in the spread sheet containing your samples and how they will be sorted. "
                + "The first column always contains a scannable barcode and the last is reserved for notes.",
            "Design your Sample Sheet"));
    firstOption = new ComboBox("Second Column", options);
    firstOption.setNullSelectionAllowed(false);
    firstOption.setStyleName(ProjectwizardUI.boxTheme);
    firstOption.setValue("Secondary Name");
    secondOption = new ComboBox("Third Column", options);
    secondOption.setNullSelectionAllowed(false);
    secondOption.setStyleName(ProjectwizardUI.boxTheme);
    secondOption.setValue("Parent Samples (Source)");
    sortby = new OptionGroup("Sort Sheet By");
    sortby.addItems(SortBy.values());
    sortby.setValue(SortBy.BARCODE_ID);

    addComponent(firstOption);
    addComponent(secondOption);
    addComponent(sortby);
  }

  public SortBy getSorter() {
    return (SortBy) sortby.getValue();
  }

  public String getInfo1(Sample s, String parents) {
    return translator.buildInfo(firstOption, s, parents, false);
  }

  public String getInfo2(Sample s, String parents) {
    return translator.buildInfo(secondOption, s, parents, false);
  }

  public List<String> getHeaders() {
    return new ArrayList<String>(Arrays.asList((String) firstOption.getValue(),
        (String) secondOption.getValue()));
  }


}
