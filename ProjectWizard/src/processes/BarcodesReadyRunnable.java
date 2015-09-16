package processes;

import java.util.List;

import main.BarcodeCreator;
import model.IBarcodeBean;

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
public class BarcodesReadyRunnable implements Runnable {

  private WizardBarcodeView view;
  private List<IBarcodeBean> barcodeBeans;
  BarcodeCreator creator;

  public BarcodesReadyRunnable(WizardBarcodeView view, BarcodeCreator creator,
      List<IBarcodeBean> barcodeBeans2) {
    this.view = view;
    this.barcodeBeans = barcodeBeans2;
    this.creator = creator;
  }

  private void attachDownloadsToButtons() {
    FileResource sheetSource = creator.createAndDLSheet(barcodeBeans, view.getHeaders());
    FileDownloader sheetDL = new FileDownloader(sheetSource);
    // sheetDL.extend(view.getButtonSheet());

    FileResource pdfSource = creator.zipAndDownloadBarcodes(barcodeBeans);
    FileDownloader pdfDL = new FileDownloader(pdfSource);
    // pdfDL.extend(view.getButtonTube());
  }

  @Override
  public void run() {
    attachDownloadsToButtons();
    view.creationDone();
    view.sheetReady();
    view.tubesReady();
  }
}
