package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import com.sun.javafx.scene.traversal.Algorithm;

import data.algorithm.settings.SettingsManager;
import data.algorithm.settings.domain.EdgeDetectSettings;
import data.algorithm.settings.domain.MogSettings;
import data.algorithm.settings.domain.PreprocessingSettings;
import data.image.AlgHelper;
import data.image.ImageConvertor;
import data.image.MatOperations;
import data.image.domain.Contour;
import data.reciever.FlirDataReciever;
import data.reciever.WebcamDataReciever;
import data.reciever.domain.Status;
import data.reciever.service.FlirDataService;
import data.reciever.service.WebcamService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import utils.Config;
import utils.Utils;


public class MainController {
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 150;
	public static final int PANEL_IMAGE_WIDTH = 160;
	public static final int PANEL_IMAGE_HEIGHT = 37;
	
	private static final int MOG_THRESHOLD = 80;
	private static final int MOG_HISTORY = 50;

	//Main container
	@FXML private BorderPane mainBorderPane;
	//Menu
	@FXML private MenuItem loadFilesFromFolder, loadPreprocesSettings, storePreprocesSettings;
	//Image containers
	@FXML private ImageView mainImageView, helperImageView, handImageView, goodsImageView, originalImageView, histogramImageView, originalCroppedImageView;
	//Stream buttons
	@FXML private Button connectToStreamButton, readStreamButton, closeStreamButton, pauseStreamButton;
	@FXML private AnchorPane openPausePane;
	//Overall status
	@FXML private Label streamStatusLabel, recognizedLabel;
	@FXML private CheckBox saveImagesCheckbox;
	@FXML private Spinner<Double> playbackSpeedSpinner;
	
	//Canny edge
	@FXML private Spinner<Double> cannyEdge1Spinner, cannyEdge2Spinner;
	//Dilate and erode
	@FXML private Spinner<Double> dilateSpinner, erodeSpinner, morphIterSpinner;
	@FXML private CheckBox morphOpenCheckbox, morphCloseCheckbox;
	//Contours
	@FXML private Spinner<Double> contourMinSizeSpinner;
	@FXML private Label foundContoursLabel;	
	//Binary hand threshold
	@FXML private Slider handThresholdSlider;
	@FXML private Spinner<Double> handThresholdSpinner, handDilationSpinner, handIterSpinner;
	@FXML private AnchorPane binaryThresholdPane;		
	//Main image settings
	@FXML private CheckBox  exposureMainCheckbox, blurMainCheckbox, cannyMainCheckbox, morphologyMainCheckbox, hullHandGoodsMainCheckbox, hullGoodsMainCheckbox, roiMainCheckbox;
	
	//preprocessing
	@FXML private AnchorPane minTempPane, maxTempPane;
	@FXML private Slider minTempSlider, maxTempSlider;	
	@FXML private Spinner<Double> prep_tempMinSpinner, prep_tempMaxSpinner, prep_clacheSize1Spinner, prep_clacheSize2Spinner, prep_clacheClipSpinner,
		prep_brightnessParam1Spinner, prep_brightnessParam2Spinner, prep_contrastParam1Spinner, prep_contrastParam2Spinner, prep_bilateralSize1Spinner, prep_bilateralSize2Spinner,
		prep_gaussianSize1Spinner, prep_gaussianSize2Spinner; 	
	@FXML private Spinner<Integer> prep_medianSizeSpinner, prep_bilateralSigmaSpinner, prep_gaussianSigmaSpinner;
	@FXML private CheckBox prep_scaleCheckbox;
	private DoubleProperty minTempDoubleProperty, maxTempDoubleProperty,  binaryThresholdDoubleProperty;  //prevents GC from cleaning weak listeners
	
	//class variables
	private FlirDataService fds;
	private FlirDataReciever flirDataReciever;
	private Path flirDummyFolder;
	private boolean printInfo;	
	private BackgroundSubtractorMOG2 mog = Video.createBackgroundSubtractorMOG2(MOG_HISTORY, MOG_THRESHOLD, true);
	
	//in memory settings
	private PreprocessingSettings ps = new PreprocessingSettings();
	private MogSettings ms = new MogSettings();
	private EdgeDetectSettings es = new EdgeDetectSettings();
	private Mode mode = Mode.MOG_DETECTION;

	
	public MainController() {
	}
	
	 @FXML 
	 public void initialize() {
		 bindSpinnersToSliders();
		 bindPreprocessSettings();
		 focusInit();
		 flirDataReciever = new FlirDataReciever(Config.getInstance().getValue(Config.SOCKET_HOSTNAME), Integer.parseInt(Config.getInstance().getValue(Config.SOCKET_PORT)), IMAGE_WIDTH*IMAGE_HEIGHT*2, playbackSpeedSpinner.getValue().intValue());
		 playbackSpeedSpinner.valueProperty().addListener((obs, oldValue, newValue) -> { 
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
			readStream(event);		
			loadPreprocesSettingsClicked(event);
		}
	 }	
	 
	@FXML 
	public void loadPreprocesSettingsClicked(ActionEvent event) {
		 if (flirDummyFolder == null) return;
		 ps = SettingsManager.loadPreproc(flirDummyFolder.toAbsolutePath().toString());
		 if (ps == null) ps = new PreprocessingSettings();
		 bindPreprocessSettings();
	 }
	
	@FXML 
	public void storePreprocesSettingsClicked(ActionEvent event) {
		 if (flirDummyFolder == null) return;
		 try {
			 Alert alert = new Alert(AlertType.CONFIRMATION);
			 alert.setTitle("File exists");
			 alert.setHeaderText("Config file already exists");
			 alert.setContentText("Overwrite?");

			 Optional<ButtonType> result = alert.showAndWait();
			 if (result.get() != ButtonType.OK) return;
			 
			 SettingsManager.storePreproc(new PreprocessingSettings(prep_tempMinSpinner.getValueFactory().valueProperty(), prep_tempMaxSpinner.getValueFactory().valueProperty(),prep_scaleCheckbox.selectedProperty(), prep_clacheSize1Spinner.getValueFactory().valueProperty(),
					 prep_clacheSize2Spinner.getValueFactory().valueProperty(), prep_clacheClipSpinner.getValueFactory().valueProperty(), prep_brightnessParam1Spinner.getValueFactory().valueProperty(), prep_brightnessParam2Spinner.getValueFactory().valueProperty(),
					 prep_contrastParam1Spinner.getValueFactory().valueProperty(),prep_contrastParam2Spinner.getValueFactory().valueProperty(),prep_medianSizeSpinner.getValueFactory().valueProperty(),prep_bilateralSize1Spinner.getValueFactory().valueProperty(),
					 prep_bilateralSize2Spinner.getValueFactory().valueProperty(),prep_bilateralSigmaSpinner.getValueFactory().valueProperty(),prep_gaussianSize1Spinner.getValueFactory().valueProperty(),prep_gaussianSize2Spinner.getValueFactory().valueProperty(),
					 prep_gaussianSigmaSpinner.getValueFactory().valueProperty()),
					 flirDummyFolder.toAbsolutePath().toString());
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
	protected void readStream(ActionEvent event) throws IOException {
		flirDataReciever.setStatus(Status.STREAMING);
		toggleOpenPause();		
		if (fds == null || !fds.isRunning()) {		
			fds = new FlirDataService(flirDataReciever);
			fds.valueProperty().addListener((obs, oldValue, newValue) -> { 
				printInfo = false;
				mode = Mode.MOG_DETECTION;
				System.out.println(ps);
				
				byte [] byteArray = newValue.getData();
		 	    Mat originalMat, scaledMat, mainMat, workMat;		     

				originalMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, false);
				if (prep_scaleCheckbox.isSelected())
					scaledMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT,  prep_tempMinSpinner.getValue().floatValue() , prep_tempMaxSpinner.getValue().floatValue());
				else 
					scaledMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, true);
	

				mainMat = AlgHelper.preprocessMainMat(scaledMat, ps, exposureMainCheckbox.isSelected(), blurMainCheckbox.isSelected(), false);			
				workMat = AlgHelper.preprocessMat(scaledMat, ps);				
				
				if (mode.equals(Mode.MOG_DETECTION)) detectUsingMog(mainMat, workMat);
				else if (mode.equals(Mode.EDGE_DETECTION)) detectUsingEdges(workMat);


				Utils.updateFXControl(mainImageView.imageProperty(), ImageConvertor.convertMatToImage(mainMat));
				Utils.updateFXControl(originalImageView.imageProperty(), ImageConvertor.convertMatToImage(originalMat));
				Utils.updateFXControl(histogramImageView.imageProperty(), ImageConvertor.convertMatToImage(MatOperations.createHistogram(workMat)));
				Utils.updateFXControl(originalCroppedImageView.imageProperty(), ImageConvertor.convertMatToImage(originalMat));
				
				AlgHelper.refreshBackground(mog, mainMat, printInfo);
				if(printInfo && !AlgHelper.isBackgroundOnly(workMat)) System.out.println(newValue.getFilename() + ";" );				
			});
			
			fds.messageProperty().addListener((obs, oldValue, newValue) -> { 
				streamStatusLabel.setText(newValue);
				});
			
			fds.start();
		}		
	}
		
	private void detectUsingEdges(Mat workMat) {
		Mat handMat = AlgHelper.segmentHandBinary(workMat, handThresholdSlider.getValue());
		List <MatOfPoint> contours = MatOperations.findContours(handMat, contourMinSizeSpinner.getValue(), 0);
		MatOfPoint biggest = MatOperations.findBiggestContour(contours);
		Mat edgesMat = new Mat(workMat.size(), workMat.type(), Scalar.all(0));
		
		if (contours.size() > 0 && Imgproc.contourArea(biggest) > 100) {
			Rect roiRect = AlgHelper.findExtendedRegion(handMat);
	
			if (roiRect.width != 0 && roiRect.height != 0)
				edgesMat = segmentGoodsUsingEdges(handMat, workMat, roiRect);
			
//			Imgproc.rectangle(handMat, new Point(roiRect.x, roiRect.y), new Point(roiRect.x + roiRect.width, roiRect.y + roiRect.height), Scalar.all(255));

		}		

		Utils.updateFXControl(goodsImageView.imageProperty(), ImageConvertor.convertMatToImage(edgesMat));
		Utils.updateFXControl(handImageView.imageProperty(), ImageConvertor.convertMatToImage(handMat));	
		Utils.updateFXControl(helperImageView.imageProperty(), ImageConvertor.convertMatToImage(workMat));	
	}
	
	private void detectUsingMog(Mat mainMat, Mat workMat) {
		Mat substractedMask = new Mat();
		mog.setVarThreshold(40);
		mog.apply(mainMat, substractedMask, 0);		
		
		Rect roiRect = new Rect();
		Mat substracted = MatOperations.maskMat(workMat, substractedMask);	
		Mat segmentedHandMask = substracted.clone();
		Mat segmentedHandRoiMask =  Mat.zeros(substracted.size(), substracted.type());
		Mat segmentedGoods = AlgHelper.getGoods(substracted, segmentedHandMask, segmentedHandRoiMask ,roiRect);
	
//		
////		segmentedGoods = MatOperations.erode(segmentedGoods, 5, 5);
////		segmentedGoods = MatOperations.morphology(segmentedGoods, true, false, 5, 4, 5);
//		
//		
//		if (!AlgHelper.isHandInShelf(substracted)) {
			List <MatOfPoint> substractedContours = MatOperations.findContours(segmentedHandRoiMask, 0, 0);			
			Point [] contoursPoints = new Point[0];
			for (int i = 0; i < substractedContours.size(); ++i) contoursPoints = Stream.concat(Arrays.stream(contoursPoints), Arrays.stream(substractedContours.get(i).toArray())).toArray(Point[]::new);				
			MatOfPoint joinedContours = new MatOfPoint(contoursPoints);
			
			if (!joinedContours.empty()) {
				MatOfPoint aprox = MatOperations.aproxCurve(joinedContours, 0.5, false);
				if (!aprox.empty()) { 
					MatOfInt convexHull2 = new MatOfInt();
					Imgproc.convexHull(aprox, convexHull2);
					if (convexHull2.toArray().length > 2) {
						MatOfInt4 convexDefects = MatOperations.convexityDefects(aprox, convexHull2);
						List <Integer> convexDefectsList = convexDefects.toList(); 
						Point data[] = aprox.toArray();

						Point biggestDefect = null, biggestStart = null, biggestEnd = null;
						int biggestSize = -1;
					    for (int j = 0; j < convexDefectsList.size(); j = j+4) {
					        Point start = data[convexDefectsList.get(j)];
					        Point end = data[convexDefectsList.get(j+1)];
					        Point defect = data[convexDefectsList.get(j+2)];
					        int depth = convexDefectsList.get(j+3)/256;
				            if (biggestSize < depth) {
				            	biggestSize = depth;
				            	biggestDefect = defect;
				            	biggestStart = start;
				            	biggestEnd = end;
				            }
					    }
					    if (convexDefectsList.size() > 0 && biggestSize > 20 ) {
					        AlgHelper.drawBiggestHandPoints(segmentedHandRoiMask, biggestDefect, biggestStart, biggestEnd);
					        AlgHelper.drawHandSplitLines(segmentedGoods, roiRect, biggestDefect, biggestStart, biggestEnd);					       
					    }
					}
				}
			}
//			
			segmentedGoods = MatOperations.erode(segmentedGoods, 4, 3);
			
			List <MatOfPoint> filteredGoodsContours = AlgHelper.findAndfilterGoodsContours(segmentedGoods);
			
			if(printInfo && !AlgHelper.isBackgroundOnly(workMat)) {
				AlgHelper.goodsContourFeatures(filteredGoodsContours, printInfo);
//				System.out.println("Hand with goods" );
			}
//		}

		Utils.updateFXControl(helperImageView.imageProperty(), ImageConvertor.convertMatToImage(substracted));			
		Utils.updateFXControl(handImageView.imageProperty(), ImageConvertor.convertMatToImage(segmentedHandMask));
		Utils.updateFXControl(goodsImageView.imageProperty(), ImageConvertor.convertMatToImage(segmentedGoods));			
	}
	

	

	private Mat segmentGoodsUsingEdges(Mat handMat, Mat workMat, Rect roiRect) {
		Mat roi = workMat.submat(roiRect);
		Mat edges = new Mat(roi.size(), roi.type());
		Mat handMatRoi = handMat.submat(roiRect);

		Imgproc.morphologyEx(handMatRoi, handMatRoi, Imgproc.MORPH_DILATE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7,7)));

		handMatRoi = MatOperations.invert(handMatRoi);
//		
		edges = MatOperations.doCannyEdgeDetection(roi, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
//		edges = MatOperations.dilate(edges, 1, 2);
//		edges = MatOperations.erode(edges, 3, 1);
//		edges = MatOperations.morphClose(edges, 3, 5);
//
		Core.bitwise_and(handMatRoi, edges, edges);	
//
		List<MatOfPoint> contours = MatOperations.findContours(edges, 25, 0);
	    Imgproc.drawContours(roi, contours, -1, new Scalar(255, 0, 0), 2);
		AlgHelper.goodsContourFeatures(contours,printInfo);
		return workMat;
	}		

	

	

	
	@FXML
	private void closeStream(ActionEvent event) {		
		flirDataReciever.closeConnection();
		fds.cancel();
		toggleOpenPause();
	}	
	
	@FXML
	protected void pauseStream(ActionEvent event) {
		flirDataReciever.setStatus(Status.PAUSED);
		toggleOpenPause();
	}

	//left panel

		
	//right panel
	@FXML
	protected void saveImagesCheckboxClicked(ActionEvent event) {
		if (saveImagesCheckbox.isSelected()) flirDataReciever.setSaveImages(true);
		else flirDataReciever.setSaveImages(false);		
	}
	
	//helper methods
	
	//bind helpers
	private void bindSpinnersToSliders() {	 
		 minTempDoubleProperty = DoubleProperty.doubleProperty(prep_tempMinSpinner.getValueFactory().valueProperty());
		 maxTempDoubleProperty = DoubleProperty.doubleProperty(prep_tempMaxSpinner.getValueFactory().valueProperty());
		 binaryThresholdDoubleProperty = DoubleProperty.doubleProperty(handThresholdSpinner.getValueFactory().valueProperty());
//	
	     minTempSlider.valueProperty().bindBidirectional(minTempDoubleProperty);
	     maxTempSlider.valueProperty().bindBidirectional(maxTempDoubleProperty);
	     handThresholdSlider.valueProperty().bindBidirectional(binaryThresholdDoubleProperty);	    
	}
	
	private void bindPreprocessSettings() {	
		prep_tempMinSpinner.getValueFactory().valueProperty().bindBidirectional(ps.tempMinProperty());
		prep_tempMaxSpinner.getValueFactory().valueProperty().bindBidirectional(ps.tempMaxProperty());
		prep_scaleCheckbox.selectedProperty().bindBidirectional(ps.scaleProperty());	 
			
		prep_clacheSize1Spinner.getValueFactory().valueProperty().bindBidirectional(ps.clacheSize1Property());
		prep_clacheSize2Spinner.getValueFactory().valueProperty().bindBidirectional(ps.clacheSize2Property());
		prep_clacheClipSpinner.getValueFactory().valueProperty().bindBidirectional(ps.clacheClipProperty());	
		prep_brightnessParam1Spinner.getValueFactory().valueProperty().bindBidirectional(ps.brightnessParam1Property());
		prep_brightnessParam2Spinner.getValueFactory().valueProperty().bindBidirectional(ps.brightnessParam2Property());
		prep_contrastParam1Spinner.getValueFactory().valueProperty().bindBidirectional(ps.contrastParam1Property());
		prep_contrastParam2Spinner.getValueFactory().valueProperty().bindBidirectional(ps.contrastParam2Property());
		 
		prep_medianSizeSpinner.getValueFactory().valueProperty().bindBidirectional(ps.medianSizeProperty());
		prep_bilateralSize1Spinner.getValueFactory().valueProperty().bindBidirectional(ps.bilateralSize1Property());
		prep_bilateralSize2Spinner.getValueFactory().valueProperty().bindBidirectional(ps.bilateralSize2Property());
		prep_bilateralSigmaSpinner.getValueFactory().valueProperty().bindBidirectional(ps.bilateralSigmaProperty());
		prep_gaussianSize1Spinner.getValueFactory().valueProperty().bindBidirectional(ps.gaussianSize1Property());
		prep_gaussianSize2Spinner.getValueFactory().valueProperty().bindBidirectional(ps.gaussianSize2Property());
		prep_gaussianSigmaSpinner.getValueFactory().valueProperty().bindBidirectional(ps.gaussianSigmaProperty());	
			
	}	 
	
	//init
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
