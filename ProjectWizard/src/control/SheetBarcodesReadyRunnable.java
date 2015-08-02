package control;

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
    FileResource sheetSource = creator.createAndDLSheet(barcodeBeans, view.getSorter());
    FileDownloader sheetDL = new FileDownloader(sheetSource);
    sheetDL.extend(view.getButtonSheet());
  }

  @Override
  public void run() {
    attachDownloadToButton();
    view.creationDone();
    view.sheetReady();
  }
}
