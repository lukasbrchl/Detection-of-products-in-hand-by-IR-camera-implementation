package application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;

import data.reciever.FlirDataReciever;
import data.reciever.WebcamDataReciever;
import data.reciever.domain.Status;
import data.reciever.service.FlirDataService;
import data.reciever.service.WebcamService;
import image.MatOperations;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button ;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import utils.Config;
import utils.ImageConvertor;
import utils.Utils;

public class MainController {
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 512;
	public static final int IMAGE_CROPPED_WIDTH = 640;
	public static final int IMAGE_CROPPED_HEIGHT = 150;
	public static final int PANEL_IMAGE_WIDTH = 160;
	public static final int PANEL_IMAGE_HEIGHT = 128;
	public static final int PANEL_CROPPED_IMAGE_WIDTH = 160;
	public static final int PANEL_CROPPED_IMAGE_HEIGHT = 37;
	
	public static final int CROP_OFFSET_X = 0;
	public static final int CROP_OFFSET_Y = 200;


	//Image containers
	@FXML private ImageView mainImageView, webcamImageView, handImageView, goodsImageView, originalImageView, histogramImageView, originalCroppedImageView;
	//Stream buttons
	@FXML private Button connectToStreamButton, readStreamButton, closeStreamButton, pauseStreamButton;
	@FXML private AnchorPane openPausePane;
	//Overall status
	@FXML private Label streamStatusLabel, recognizedLabel;
	@FXML private CheckBox saveImagesCheckbox;
	@FXML private Spinner<Double> playbackSpeedSpinner;
	//Scale
	@FXML private Slider minTempSlider, maxTempSlider;	
	@FXML private Spinner<Double> minTempSpinner, maxTempSpinner;
	@FXML private CheckBox scaleTempCheckbox;
	@FXML private AnchorPane minTempPane, maxTempPane;
	//Brightness and contrast	
	@FXML private Spinner<Double> clache1Spinner, clache2Spinner, clacheClipSpinner, brightnessSpinner, addSpinner, contrastSpinner, multSpinner;
	//Blur
	@FXML private Spinner<Double> blur1Spinner, blur2Spinner, blurSigmaSpinner;
	@FXML private CheckBox blurCheckbox;
	@FXML private HBox blurPane;
	//Canny edge
	@FXML private Spinner<Double> cannyEdge1Spinner, cannyEdge2Spinner;
	//Dilate and erode
	@FXML private Spinner<Double> dilateSpinner;
	//Contours
	@FXML private Spinner<Double> contourMinSizeSpinner;
	@FXML private Label foundContoursLabel;	
	//Binary hand threshold
	@FXML private Slider binaryThresholdSlider;
	@FXML private Spinner<Double> binaryThresholdSpinner;
	@FXML private AnchorPane binaryThresholdPane;		
	//Main image settings
	@FXML private CheckBox scaleMainCheckbox, exposureMainCheckbox, blurMainCheckbox, cannyMainCheckbox, dilateMainCheckbox ;

	private DoubleProperty minTempDoubleProperty, maxTempDoubleProperty,  binaryThresholdDoubleProperty;  //prevents GC from cleaning weak listeners
	private FlirDataService fds;
	private FlirDataReciever flirDataReciever;
	private WebcamService wcs;
	private WebcamDataReciever webcamDataReciever;


	public MainController() {
		flirDataReciever = new FlirDataReciever(Config.getInstance().getValue(Config.SOCKET_HOSTNAME), Integer.parseInt(Config.getInstance().getValue(Config.SOCKET_PORT)), 655360);
		webcamDataReciever = new WebcamDataReciever(1);
	}
	
	 @FXML 
	 public void initialize() {
		 bindSpinnersToSliders();
		 playbackSpeedSpinner.valueProperty().addListener((obs, oldValue, newValue) -> { 
			 flirDataReciever.setPlaybackSpeed(newValue.intValue());
			 webcamDataReciever.setPlaybackSpeed(newValue.intValue());
		 });
		 setInitialValues();
    }

	//buttons
	@FXML 
	protected void connectToStream(ActionEvent event) {
		flirDataReciever.openConnection();
		webcamDataReciever.openConnection();
		streamStatusLabel.setText(flirDataReciever.getStatus().getStrStatus());
		toggleOpenPause();	
	}
	
	@FXML
	protected void readStream(ActionEvent event) throws IOException {
		flirDataReciever.setStatus(Status.STREAMING);
		webcamDataReciever.setStatus(Status.STREAMING);
		toggleOpenPause();		
		if (fds == null || !fds.isRunning()) {		
			fds = new FlirDataService(flirDataReciever);
			fds.valueProperty().addListener((obs, oldValue, newValue) -> { 
				byte [] byteArray = newValue.getData();
				Mat originalMat = createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT),	workMat;
				Mat originalCroppedMat = new Mat(originalMat, new Rect(CROP_OFFSET_X, CROP_OFFSET_Y, IMAGE_CROPPED_WIDTH, IMAGE_CROPPED_HEIGHT)), workCroppedMat;
	
				if (scaleTempCheckbox.isSelected())
					workMat = preprocessMat(byteArray, true);	
				else
					workMat= preprocessMat(byteArray, false);
				workCroppedMat = new Mat(workMat, new Rect(CROP_OFFSET_X, CROP_OFFSET_Y, IMAGE_CROPPED_WIDTH, IMAGE_CROPPED_HEIGHT));
				
				Mat mainMat = processMainMat(byteArray);			
				Mat goodsMat = workCroppedMat.clone();
				Mat handMat = workCroppedMat.clone();
				
				goodsMat = segmentGoods(workCroppedMat, goodsMat, cannyEdge1Spinner.getValue(), cannyEdge2Spinner.getValue(), dilateSpinner.getValue(), binaryThresholdSpinner.getValue(), contourMinSizeSpinner.getValue());		
				handMat = segmentHand(workCroppedMat, binaryThresholdSpinner.getValue());
	//			detectHand(handMat);
				
				//center
				Image mainImage = ImageConvertor.convertMatToImage(mainMat);
				Image handImage = ImageConvertor.convertMatToImage(handMat);
				Image goodsImage = ImageConvertor.convertMatToImage(goodsMat);
				Utils.updateFXControl(mainImageView.imageProperty(), mainImage);
				Utils.updateFXControl(handImageView.imageProperty(), handImage);
				Utils.updateFXControl(goodsImageView.imageProperty(), goodsImage);			
				//panel
				Image originalImage = ImageConvertor.convertMatToImage(originalMat);
				Image histogramImage = ImageConvertor.convertMatToImage(MatOperations.createHistogram(workMat));	
				Image originalCroppedImage = ImageConvertor.convertMatToImage(originalCroppedMat);
				Utils.updateFXControl(originalImageView.imageProperty(), originalImage);
				Utils.updateFXControl(histogramImageView.imageProperty(), histogramImage);
				Utils.updateFXControl(originalCroppedImageView.imageProperty(), originalCroppedImage);
			});
			fds.messageProperty().addListener((obs, oldValue, newValue) -> { 
				streamStatusLabel.setText(newValue);
				});
			fds.start();
		}
		if (wcs == null || !wcs.isRunning()) {
			wcs = new WebcamService(webcamDataReciever);
			wcs.valueProperty().addListener((obs, oldValue, newValue) -> { 
				Mat mainMat = newValue;
				Image mainImage = ImageConvertor.convertMatToImage(mainMat);
				Utils.updateFXControl(webcamImageView.imageProperty(), mainImage);
				
			});
			wcs.start();
		}
	}

	
	private Mat processMainMat(byte[] byteArray) {
		Mat mat;
		if (scaleMainCheckbox.isSelected()) mat = createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, minTempDoubleProperty.getValue().floatValue(), maxTempDoubleProperty.getValue().floatValue());
		else mat = createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT);
		if (clache1Spinner.getValue() != 0 && clache2Spinner.getValue() != 0 ) {
			CLAHE clahe = Imgproc.createCLAHE(clacheClipSpinner.getValue(), new Size(clache1Spinner.getValue(), clache2Spinner.getValue()));
			clahe.apply(mat, mat);
		}
		if ((brightnessSpinner.getValue() != 0 || contrastSpinner.getValue() != 0) && exposureMainCheckbox.isSelected()) MatOperations.brightnessContrast(mat, brightnessSpinner.getValue(), contrastSpinner.getValue());
		if ((addSpinner.getValue() != 0 || multSpinner.getValue() != 0) && exposureMainCheckbox.isSelected()) MatOperations.addMult(mat, addSpinner.getValue(), multSpinner.getValue());
		if (blurCheckbox.isSelected() && blurMainCheckbox.isSelected()) mat = MatOperations.blurImage(mat, blur1Spinner.getValue(), blur2Spinner.getValue(), blurSigmaSpinner.getValue());	
		if (cannyMainCheckbox.isSelected()) mat = MatOperations.doCannyEdgeDetection(mat, cannyEdge1Spinner.getValue(), cannyEdge2Spinner.getValue());
		if (dilateMainCheckbox.isSelected()) mat = MatOperations.dilate(mat, dilateSpinner.getValue());
		
		
		return mat;
	}

	private Mat createMat(byte [] byteArray, int width, int height, float min, float max) {				
		return ImageConvertor.convertBinaryToMat(byteArray, width, height, min, max);
	}
	
	private Mat segmentGoods(Mat original, Mat mat, double cannyThresh1, double cannyThresh2, double dilate, double handThreshold, double contourMinSize) {
		Mat result = new Mat(mat.size(), mat.type());
		Mat withoutHand = new Mat(mat.size(), mat.type()); 

		result = MatOperations.doCannyEdgeDetection(mat, cannyThresh1 , cannyThresh2); //detect edges
		result = MatOperations.dilate(mat, dilate); //try to link them	
		
		List <MatOfPoint> contours = MatOperations.findContours(result, contourMinSize); //find contours + filter them
		Mat convexHull = MatOperations.convexHull(result, contours); //draw convex hull mask
		Mat segmentedHand = segmentHand(original, handThreshold); //find hand mask
		
		Core.subtract(convexHull, segmentedHand, withoutHand); //Mat without hand but with noise
		result = MatOperations.maskMat(original, withoutHand);  //masked original image - only goods + noise remains	
		Mat noiseGoods = result.clone();
		
		List <MatOfPoint> contours2 = MatOperations.findContours(noiseGoods, 0);
		MatOfPoint mop = MatOperations.findBiggestContour(contours2); //find biggest contour - might be a goods only
		result = MatOperations.convexHull(noiseGoods, Arrays.asList(mop));
		result = MatOperations.maskMat(noiseGoods, result); //mask biggest contour - only goods should remain				
		return result;
	}
	
	
	private Mat segmentHand(Mat mat, double threshold) {
		Mat result = MatOperations.binaryTreshold(mat, threshold);
		return result;
	}
		
	private void detectHand(Mat mat) {
		List <MatOfPoint> mopList = MatOperations.findContours(mat, 0);
		MatOfPoint mopHand = MatOperations.findBiggestContour(mopList);
		double countourArea = Imgproc.contourArea(mopHand);
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
	
	@FXML 	
	protected void scaleTempCheckboxClicked(ActionEvent event) {
		if(scaleTempCheckbox.isSelected()) {
			minTempPane.setDisable(false);
			maxTempPane.setDisable(false);
		}
		else {
			minTempPane.setDisable(true);
			maxTempPane.setDisable(true);
		}		
	}
	
	
	@FXML 
	protected void blurCheckboxClicked(ActionEvent event) {
	}
	
	//right panel
	@FXML
	protected void saveImagesCheckboxClicked(ActionEvent event) {
		if (saveImagesCheckbox.isSelected())  {
			flirDataReciever.setSaveImages(true);
			webcamDataReciever.setSaveImages(true); //FIXME: NPE
		}
		else {
			flirDataReciever.setSaveImages(false);
			webcamDataReciever.setSaveImages(false);
		}
	}
	//helper methods
	
	//init helpers
	private void bindSpinnersToSliders() {	 
		 minTempDoubleProperty = DoubleProperty.doubleProperty(minTempSpinner.getValueFactory().valueProperty());
		 maxTempDoubleProperty = DoubleProperty.doubleProperty(maxTempSpinner.getValueFactory().valueProperty());
		 binaryThresholdDoubleProperty = DoubleProperty.doubleProperty(binaryThresholdSpinner.getValueFactory().valueProperty());
	
	     minTempSlider.valueProperty().bindBidirectional(minTempDoubleProperty);
	     maxTempSlider.valueProperty().bindBidirectional(maxTempDoubleProperty);
	     binaryThresholdSlider.valueProperty().bindBidirectional(binaryThresholdDoubleProperty);
	   
	     //focus
	     pauseStreamButton.visibleProperty().addListener((observable, oldValue, newValue) -> {
	    	    if (newValue.equals(false)) readStreamButton.requestFocus();
	    	});
	     readStreamButton.visibleProperty().addListener((observable, oldValue, newValue) -> {
	    	 	if (newValue.equals(false)) pauseStreamButton.requestFocus();
	 	});
	}
	
	 
	//Mat operations		
	private Mat createMat(byte [] byteArray, int width, int height) {				
		return ImageConvertor.convertBinaryToMat(byteArray,width, height);
	}
	
	
	private Mat preprocessMat(byte [] byteArray, boolean scale) {
		Mat mat;
		if (scale) mat = createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, minTempDoubleProperty.getValue().floatValue(), maxTempDoubleProperty.getValue().floatValue());
		else mat = createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT);
		if (clache1Spinner.getValue() != 0 && clache2Spinner.getValue() != 0 ) {
			CLAHE clahe = Imgproc.createCLAHE(clacheClipSpinner.getValue(), new Size(clache1Spinner.getValue(), clache2Spinner.getValue()));
			clahe.apply(mat, mat);
		}
		if (brightnessSpinner.getValue() != 0 || contrastSpinner.getValue() != 0) MatOperations.brightnessContrast(mat, brightnessSpinner.getValue(), contrastSpinner.getValue());
		if (addSpinner.getValue() != 0 || multSpinner.getValue() != 0) MatOperations.addMult(mat, addSpinner.getValue(), multSpinner.getValue());
		if (blurCheckbox.isSelected()) mat = MatOperations.blurImage(mat, blur1Spinner.getValue(), blur2Spinner.getValue(), blurSigmaSpinner.getValue());	
		return mat;
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
	 
	private void setInitialValues() {
		saveImagesCheckbox.setSelected(false);
		playbackSpeedSpinner.getValueFactory().setValue(100.0);
		minTempSpinner.getValueFactory().setValue(28.0);
		maxTempSpinner.getValueFactory().setValue(34.5);
		scaleTempCheckbox.setSelected(true);
		clache1Spinner.getValueFactory().setValue(10.0); 
		clache2Spinner.getValueFactory().setValue(10.0); 
		clacheClipSpinner.getValueFactory().setValue(4.0); 
		brightnessSpinner.getValueFactory().setValue(0.0); 
		addSpinner.getValueFactory().setValue(0.0); 
		contrastSpinner.getValueFactory().setValue(0.0); 
		multSpinner.getValueFactory().setValue(0.0);
		blur1Spinner.getValueFactory().setValue(100.0); 
		blur2Spinner.getValueFactory().setValue(100.0); 
		blurSigmaSpinner.getValueFactory().setValue(10.0);
		blurCheckbox.setSelected(true);
		cannyEdge1Spinner.getValueFactory().setValue(20.0); 
		cannyEdge2Spinner.getValueFactory().setValue(40.0);
		dilateSpinner.getValueFactory().setValue(3.5);
		contourMinSizeSpinner.getValueFactory().setValue(15.0);;
		binaryThresholdSpinner.getValueFactory().setValue(180.0);;
		scaleMainCheckbox.setSelected(false); 
		exposureMainCheckbox.setSelected(false); 
		blurMainCheckbox.setSelected(false); 
		cannyMainCheckbox.setSelected(false);
		dilateMainCheckbox.setSelected(false);
	}
	
	private void thermalCameraSegmentationDefaults() {
		saveImagesCheckbox.setSelected(false);
		playbackSpeedSpinner.getValueFactory().setValue(100.0);
		minTempSpinner.getValueFactory().setValue(28.0);
		maxTempSpinner.getValueFactory().setValue(34.5);
		scaleTempCheckbox.setSelected(true);
		clache1Spinner.getValueFactory().setValue(10.0); 
		clache2Spinner.getValueFactory().setValue(10.0); 
		clacheClipSpinner.getValueFactory().setValue(4.0); 
		brightnessSpinner.getValueFactory().setValue(0.0); 
		addSpinner.getValueFactory().setValue(0.0); 
		contrastSpinner.getValueFactory().setValue(0.0); 
		multSpinner.getValueFactory().setValue(0.0);
		blur1Spinner.getValueFactory().setValue(100.0); 
		blur2Spinner.getValueFactory().setValue(100.0); 
		blurSigmaSpinner.getValueFactory().setValue(10.0);
		blurCheckbox.setSelected(true);
		cannyEdge1Spinner.getValueFactory().setValue(20.0); 
		cannyEdge2Spinner.getValueFactory().setValue(40.0);
		dilateSpinner.getValueFactory().setValue(3.5);
		contourMinSizeSpinner.getValueFactory().setValue(15.0);;
		binaryThresholdSpinner.getValueFactory().setValue(180.0);;
		scaleMainCheckbox.setSelected(false); 
		exposureMainCheckbox.setSelected(false); 
		blurMainCheckbox.setSelected(false); 
		cannyMainCheckbox.setSelected(false);
		dilateMainCheckbox.setSelected(false);
	}
	
	//getters, setters	

}
