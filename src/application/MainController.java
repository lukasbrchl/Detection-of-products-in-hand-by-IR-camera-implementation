package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import algorithm.detector.AbstractDetector;
import algorithm.detector.BackgroundDetector;
import algorithm.detector.EdgeDetector;
import algorithm.detector.MogDetector;
import algorithm.detector.domain.DetectionResult;
import algorithm.detector.domain.Mode;
import algorithm.settings.SettingsManager;
import algorithm.settings.domain.EdgeDetectSettings;
import algorithm.settings.domain.MogSettings;
import algorithm.settings.domain.PreprocessingSettings;
import algorithm.settings.domain.PreviewSettings;
import algorithm.settings.domain.SettingsWrapper;
import data.reciever.FlirDataReciever;
import data.reciever.domain.Status;
import data.reciever.service.FlirDataService;
import image.ImageConvertor;
import image.MatOperations;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button ;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import utils.Config;
import utils.Utils;

/**
* This is the main controller of the application. The related view file is MainLayout.fxml.
* This controller handles all UI activities and delegates them to the corresponding service classes.
* 
* @author Lukas Brchl
*/
public class MainController {
	
	/** * main container */
	@FXML private BorderPane mainBorderPane;	
	/** * menu items */
	@FXML private MenuItem loadFilesFromFolder, loadPreprocesSettings, storePreprocesSettings;
	/** * image container */
	@FXML private ImageView previewImageView, backgroundImageView, workImageView, handImageView, goodsImageView, originalImageView, histogramImageView;
	/** * stream controll */
	@FXML private Button connectToStreamButton, readStreamButton, closeStreamButton, pauseStreamButton;
	@FXML private AnchorPane openPausePane;
	/** * overall status  */
	@FXML private Label streamStatusLabel, recognizedLabel;
	
	/** * preview tab */
	@FXML private CheckBox prev_exposureCheckbox, prev_blurCheckbox, prev_cannyCheckbox, prev_saveImagesCheckbox;
	@FXML private Slider prev_cannyThresh1Slider, prev_cannyThresh2Slider;
	@FXML private Spinner<Double> prev_cannyThresh1Spinner, prev_cannyThresh2Spinner;
	@FXML private Spinner<Integer> prev_playbackSpeedSpinner;
	private DoubleProperty prev_cannyTresh1Property, prev_cannyTresh2Property;  //prevents Garbage collector from cleaning weak listeners
	
	/** * preprocessing tab */
	@FXML private Slider minTempSlider, maxTempSlider;	
	@FXML private Spinner<Double> prep_tempMinSpinner, prep_tempMaxSpinner, prep_clacheSize1Spinner, prep_clacheSize2Spinner, prep_clacheClipSpinner,
		prep_brightnessParam1Spinner, prep_brightnessParam2Spinner, prep_contrastParam1Spinner, prep_contrastParam2Spinner, prep_bilateralSize1Spinner, prep_bilateralSize2Spinner,
		prep_gaussianSize1Spinner, prep_gaussianSize2Spinner; 	
	@FXML private Spinner<Integer> prep_medianSizeSpinner, prep_bilateralSigmaSpinner, prep_gaussianSigmaSpinner;
	@FXML private CheckBox prep_scaleCheckbox;
	private DoubleProperty prep_minTempDoubleProperty, prep_maxTempDoubleProperty,  prep_binaryThresholdDoubleProperty;  //prevents Garbage collector from cleaning weak listeners
	
	/** * alg 1 - mog tab */	
	@FXML private Spinner<Integer> mog_threshold, mog_history, mog_bcgCanny1, mog_bcgCanny2, mog_handThreshold, mog_handDilateSize, mog_handMinConvexDepth,
		mog_goodsErodeSize, mog_goodsErodeIter, mog_goodsCloseSize, mog_goodsCloseIter, mog_goodsOpenSize, mog_goodsOpenIter;
	@FXML private Spinner<Double> mog_bcgLarningRate;
	
//	/** * alg 2 - edge detect tab */		
	
	private FlirDataService fds;
	private FlirDataReciever flirDataReciever;
	private Path flirDummyFolder;		
	
	/** * in memory settings */
	private SettingsWrapper settings = new SettingsWrapper();
	/** * choosen detector for evaluating */
	private AbstractDetector detector;	
	/** * detection mode */
	private Mode mode;

	
	public MainController() {
	}
	
	
	protected void readStream() {			
		if (fds == null || !fds.isRunning()) {		
			fds = new FlirDataService(flirDataReciever);
			fds.valueProperty().addListener((obs, oldData, newData) -> {
//				if (flirDataReciever.getStatus().equals(Status.PAUSED)) return;
				detector.setPrintInfo(true);
				detector.initMats(newData);
				DetectionResult result = detector.detect();
//
				Utils.updateFXControl(previewImageView.imageProperty(), ImageConvertor.convertMatToImage(detector.getPreviewMat()));
				Utils.updateFXControl(workImageView.imageProperty(), ImageConvertor.convertMatToImage(detector.getWorkMat()));
				Utils.updateFXControl(handImageView.imageProperty(), ImageConvertor.convertMatToImage(detector.getHandMat()));
				Utils.updateFXControl(goodsImageView.imageProperty(), ImageConvertor.convertMatToImage(detector.getGoodsMat()));
				Utils.updateFXControl(originalImageView.imageProperty(), ImageConvertor.convertMatToImage(detector.getOriginalMat()));
				Utils.updateFXControl(histogramImageView.imageProperty(), ImageConvertor.convertMatToImage(MatOperations.createHistogram(detector.getWorkMat())));	
				recognizedLabel.setText(result.getResult());
//				if (detector.getResult().equals(DetectionResult.HAND_WITH_GOODS))
//					System.out.println("hand with goods " + newData.getFilename());
//				else if (detector.getResult().equals(DetectionResult.EMPTY_HAND))
//					System.out.println("empty hand " + newData.getFilename());
//				else if (detector.getResult().equals(DetectionResult.UNDEFINED))
//					System.out.println("undefined " + newData.getFilename());
				Utils.updateFXControl(backgroundImageView.imageProperty(), ImageConvertor.convertMatToImage(((MogDetector) detector).getBackground()));
//				List <MatOfPoint> filteredGoodsContours = ((MogDetector) detector).findAndfilterGoodsContours(detector.getGoodsMat());		 
//				detector.goodsContourFeatures(filteredGoodsContours, true);
			});
			
			fds.messageProperty().addListener((obs, oldValue, newValue) -> { 
				streamStatusLabel.setText(newValue);
				});
			
			fds.start();
		}		
	}
		
	
	 @FXML 
	 public void initialize() {
		 bindSpinnersToSliders();
		 bindAllSettings();
		 focusInit();
		 flirDataReciever = new FlirDataReciever(Config.getInstance().getValue(Config.SOCKET_HOSTNAME), Integer.parseInt(Config.getInstance().getValue(Config.SOCKET_PORT)), AbstractDetector.IMAGE_WIDTH*AbstractDetector.IMAGE_HEIGHT*2, prev_playbackSpeedSpinner.getValue().intValue());
		 prev_playbackSpeedSpinner.valueProperty().addListener((obs, oldValue, newValue) -> { 
			 flirDataReciever.setPlaybackSpeed(newValue.intValue());
		 });
	 }
	 
	 //menu

	@FXML 
	 public void loadFilesFromFolderClicked(ActionEvent event) throws IOException {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(new File("D:\\ThesisProjectImages\\"));
		File file = directoryChooser.showDialog(mainBorderPane.getScene().getWindow());
		if (file != null) {
			flirDummyFolder = file.toPath();
			flirDataReciever.initDummyHost(flirDummyFolder);
			loadSettingsClicked(event);
			startReading();					
		}
	 }	
	 
	@FXML 
	public void loadSettingsClicked(ActionEvent event) {
		 if (flirDummyFolder == null) return;
		 settings = SettingsManager.loadSettings(flirDummyFolder.toAbsolutePath().toString());
		 if (settings == null) settings = new SettingsWrapper();
		 bindAllSettings();
	 }
	
	@FXML 
	public void storeSettingsClicked(ActionEvent event) {
		 if (flirDummyFolder == null) return;
		 try {
			 if (SettingsManager.fileExists(flirDummyFolder.toAbsolutePath().toString())) {
				 Alert alert = new Alert(AlertType.CONFIRMATION);
				 alert.setTitle("File exists");
				 alert.setHeaderText("Config file already exists");
				 alert.setContentText("Overwrite?");	
				 Optional<ButtonType> result = alert.showAndWait();
				 if (result.get() != ButtonType.OK) return;
			 }			 
			 SettingsManager.storeSettings(settings, flirDummyFolder.toAbsolutePath().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	 }	
	 
	
	//buttons
	@FXML 
	protected void connectToStream(ActionEvent event) {
		flirDataReciever.openConnection();
		streamStatusLabel.setText(flirDataReciever.getStatus().getStrStatus());
		toggleOpenPause();	
	}
	
	@FXML
	private void startReading() {
		flirDataReciever.setStatus(Status.STREAMING);
		toggleOpenPause();	
		mode = Mode.MOG_DETECTION;
//		mode = Mode.BACKGROUND_DETECTION;
		if (detector == null)
			if (mode.equals(Mode.MOG_DETECTION)) detector = new MogDetector(settings);
			else if (mode.equals(Mode.EDGE_DETECTION)) detector = new EdgeDetector(settings);
			else if (mode.equals(Mode.BACKGROUND_DETECTION)) detector = new BackgroundDetector(settings, "D:\\ThesisProjectImages\\4_13_03_termo7_cat\\background\\");
		
		readStream();		
	}		
	
	@FXML
	private void closeStream(ActionEvent event) {		
		flirDataReciever.closeConnection();
		fds.cancel();
		detector = null;
		toggleOpenPause();
	}	
	
	@FXML
	protected void pauseStream(ActionEvent event) {
		flirDataReciever.setStatus(Status.PAUSED);
		toggleOpenPause();
	}
		
	@FXML
	protected void saveImagesCheckboxClicked(ActionEvent event) {
		if (prev_saveImagesCheckbox.isSelected()) flirDataReciever.setSaveImages(true);
		else flirDataReciever.setSaveImages(false);		
	}
	
	//helper methods
	
	//bind helpers
	private void bindAllSettings() {
		 bindPreviewSettings();
		 bindPreprocessSettings();
		 bindEdgeDetectSettings();
		 bindMogSettings();
	}
	
	private void bindSpinnersToSliders() {	 
		 prep_minTempDoubleProperty = DoubleProperty.doubleProperty(prep_tempMinSpinner.getValueFactory().valueProperty());
		 prep_maxTempDoubleProperty = DoubleProperty.doubleProperty(prep_tempMaxSpinner.getValueFactory().valueProperty());
		 prev_cannyTresh1Property =  DoubleProperty.doubleProperty(prev_cannyThresh1Spinner.getValueFactory().valueProperty());
		 prev_cannyTresh2Property =  DoubleProperty.doubleProperty(prev_cannyThresh2Spinner.getValueFactory().valueProperty());
	
	     minTempSlider.valueProperty().bindBidirectional(prep_minTempDoubleProperty);
	     maxTempSlider.valueProperty().bindBidirectional(prep_maxTempDoubleProperty);
	     prev_cannyThresh1Slider.valueProperty().bindBidirectional(prev_cannyTresh1Property);
	     prev_cannyThresh2Slider.valueProperty().bindBidirectional(prev_cannyTresh2Property);
	}
	
	private void bindPreviewSettings() {	
		PreviewSettings pws = (PreviewSettings) settings.getByCls(PreviewSettings.class);
		prev_exposureCheckbox.selectedProperty().bindBidirectional(pws.exposureProperty());
		
		prev_blurCheckbox.selectedProperty().bindBidirectional(pws.blurProperty());
		
		prev_cannyThresh1Spinner.getValueFactory().valueProperty().bindBidirectional(pws.cannyThresh1Property());
		prev_cannyThresh2Spinner.getValueFactory().valueProperty().bindBidirectional(pws.cannyThresh2Property());
		prev_cannyCheckbox.selectedProperty().bindBidirectional(pws.cannyProperty());
		
		prev_playbackSpeedSpinner.getValueFactory().valueProperty().bindBidirectional(pws.playbackSpeedProperty());			
	}	 
	
	private void bindPreprocessSettings() {	
		PreprocessingSettings pgs = (PreprocessingSettings) settings.getByCls(PreprocessingSettings.class);
		prep_tempMinSpinner.getValueFactory().valueProperty().bindBidirectional(pgs.tempMinProperty());
		prep_tempMaxSpinner.getValueFactory().valueProperty().bindBidirectional(pgs.tempMaxProperty());
		prep_scaleCheckbox.selectedProperty().bindBidirectional(pgs.scaleProperty());	 
			
		prep_clacheSize1Spinner.getValueFactory().valueProperty().bindBidirectional(pgs.clacheSize1Property());
		prep_clacheSize2Spinner.getValueFactory().valueProperty().bindBidirectional(pgs.clacheSize2Property());
		prep_clacheClipSpinner.getValueFactory().valueProperty().bindBidirectional(pgs.clacheClipProperty());	
		prep_brightnessParam1Spinner.getValueFactory().valueProperty().bindBidirectional(pgs.brightnessParam1Property());
		prep_brightnessParam2Spinner.getValueFactory().valueProperty().bindBidirectional(pgs.brightnessParam2Property());
		prep_contrastParam1Spinner.getValueFactory().valueProperty().bindBidirectional(pgs.contrastParam1Property());
		prep_contrastParam2Spinner.getValueFactory().valueProperty().bindBidirectional(pgs.contrastParam2Property());
		 
		prep_medianSizeSpinner.getValueFactory().valueProperty().bindBidirectional(pgs.medianSizeProperty());
		prep_bilateralSize1Spinner.getValueFactory().valueProperty().bindBidirectional(pgs.bilateralSize1Property());
		prep_bilateralSize2Spinner.getValueFactory().valueProperty().bindBidirectional(pgs.bilateralSize2Property());
		prep_bilateralSigmaSpinner.getValueFactory().valueProperty().bindBidirectional(pgs.bilateralSigmaProperty());
		prep_gaussianSize1Spinner.getValueFactory().valueProperty().bindBidirectional(pgs.gaussianSize1Property());
		prep_gaussianSize2Spinner.getValueFactory().valueProperty().bindBidirectional(pgs.gaussianSize2Property());
		prep_gaussianSigmaSpinner.getValueFactory().valueProperty().bindBidirectional(pgs.gaussianSigmaProperty());				
	}	
	
	private void bindMogSettings() {	
		MogSettings mog = (MogSettings) settings.getByCls(MogSettings.class);		
		mog_threshold.getValueFactory().valueProperty().bindBidirectional(mog.mogThresholdProperty());
		mog_history.getValueFactory().valueProperty().bindBidirectional(mog.mogHistoryProperty());
			
		mog_bcgCanny1.getValueFactory().valueProperty().bindBidirectional(mog.bcgCanny1Property());
		mog_bcgCanny2.getValueFactory().valueProperty().bindBidirectional(mog.bcgCanny2Property());
		mog_bcgLarningRate.getValueFactory().valueProperty().bindBidirectional(mog.bcgLearningRateProperty());
		
		mog_handThreshold.getValueFactory().valueProperty().bindBidirectional(mog.handThresholdProperty());			
		mog_handDilateSize.getValueFactory().valueProperty().bindBidirectional(mog.handMaskDilateProperty());
		mog_handMinConvexDepth.getValueFactory().valueProperty().bindBidirectional(mog.handMinConvexDepthProperty());
		
		mog_goodsErodeSize.getValueFactory().valueProperty().bindBidirectional(mog.goodsErodeSizeProperty());
		mog_goodsErodeIter.getValueFactory().valueProperty().bindBidirectional(mog.goodsErodeIterProperty());		 
		mog_goodsCloseSize.getValueFactory().valueProperty().bindBidirectional(mog.goodsCloseSizeProperty());
		mog_goodsCloseIter.getValueFactory().valueProperty().bindBidirectional(mog.goodsCloseIterProperty());
		mog_goodsOpenSize.getValueFactory().valueProperty().bindBidirectional(mog.goodsOpenSizeProperty());
		mog_goodsOpenIter.getValueFactory().valueProperty().bindBidirectional(mog.goodsOpenIterProperty());	
	}
	
	private void bindEdgeDetectSettings() {	
		EdgeDetectSettings eds = (EdgeDetectSettings) settings.getByCls(EdgeDetectSettings.class);	
	}
	
	
	//initialize focus properties
	 private void focusInit() {
	     pauseStreamButton.visibleProperty().addListener((observable, oldValue, newValue) -> {
	    	    if (newValue.equals(false)) readStreamButton.requestFocus();
	    	});
	     readStreamButton.visibleProperty().addListener((observable, oldValue, newValue) -> {
	    	 	if (newValue.equals(false)) pauseStreamButton.requestFocus();
	 	});	 
     }
	
	//other helpers
	private void toggleOpenPause() {
		if (flirDataReciever.getStatus().equals(Status.PAUSED)) {
			connectToStreamButton.setVisible(false);
			readStreamButton.setVisible(true);
			pauseStreamButton.setVisible(false);
		} else if (flirDataReciever.getStatus().equals(Status.CONNECTED)){
			connectToStreamButton.setVisible(false);
			readStreamButton.setVisible(true);
			pauseStreamButton.setVisible(false);
		} else if (flirDataReciever.getStatus().equals(Status.CLOSED)){
			connectToStreamButton.setVisible(true);
			readStreamButton.setVisible(false);
			pauseStreamButton.setVisible(false);
		} else if (flirDataReciever.getStatus().equals(Status.STREAMING)){
			connectToStreamButton.setVisible(false);
			readStreamButton.setVisible(false);
			pauseStreamButton.setVisible(true);
		}
	}
	 

}
