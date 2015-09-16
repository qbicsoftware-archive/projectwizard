package processes;

import java.util.Collections;
import java.util.List;

import main.BarcodeCreator;
import model.IBarcodeBean;
import model.SortBy;
import sorters.*;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;

import views.WizardBarcodeView;

/**
 * Class implementing the Runnable interface so it can trigger a response in the view after the
 * barcode creation thread finishes
 * 
 * @author Andreas Friedrich
 * 
 */
public class SheetBarcodesReadyRunnable implements Runnable {

  private WizardBarcodeView view;
  private List<IBarcodeBean> barcodeBeans;
  BarcodeCreator creator;

  public SheetBarcodesReadyRunnable(WizardBarcodeView view, BarcodeCreator creator,
      List<IBarcodeBean> barcodeBeans) {
    this.view = view;
    this.barcodeBeans = barcodeBeans;
    this.creator = creator;
  }

  private void attachDownloadToButton() {
    SortBy sorter = view.getSorter();
    switch (sorter) {
      case BARCODE_ID:
        Collections.sort(barcodeBeans, SampleCodeComparator.getInstance());
        break;
      case EXT_ID:
        Collections.sort(barcodeBeans, SampleExtIDComparator.getInstance());
        break;
      case SAMPLE_TYPE:
        Collections.sort(barcodeBeans, SampleTypeComparator.getInstance());
        break;
      case SECONDARY_NAME:
        Collections.sort(barcodeBeans, SampleSecondaryNameComparator.getInstance());
        break;
      default:
        break;
    }
    FileResource sheetSource = creator.createAndDLSheet(barcodeBeans, view.getHeaders());
    FileDownloader sheetDL = new FileDownloader(sheetSource);
    sheetDL.extend(view.getDownloadButton());
  }

  @Override
  public void run() {
    attachDownloadToButton();
    view.creationDone();
    view.sheetReady();
  }
}
