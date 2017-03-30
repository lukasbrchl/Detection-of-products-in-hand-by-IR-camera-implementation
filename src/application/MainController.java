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
import org.opencv.core.RotatedRect;
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
	public static final int IMAGE_CROPPED_HEIGHT = 200;
	public static final int PANEL_IMAGE_WIDTH = 160;
	public static final int PANEL_IMAGE_HEIGHT = 128;
	public static final int PANEL_CROPPED_IMAGE_WIDTH = 160;
	public static final int PANEL_CROPPED_IMAGE_HEIGHT = 37;
	
	public static final int CROP_OFFSET_X = 0;
	public static final int CROP_OFFSET_Y = 110;


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
	
	@FXML private Spinner<Double> optionalSpinner, optionalSpinner2, optionalSpinner3, optionalSpinner4, optionalSpinner5;
	
	private DoubleProperty minTempDoubleProperty, maxTempDoubleProperty,  binaryThresholdDoubleProperty;  //prevents GC from cleaning weak listeners
	private FlirDataService fds;
	private FlirDataReciever flirDataReciever;
	private WebcamService wcs;
	private WebcamDataReciever webcamDataReciever;


	public MainController() {
	}
	
	 @FXML 
	 public void initialize() {
		 bindSpinnersToSliders();
		 thermalCameraSegmentationDefaults();
		 flirDataReciever = new FlirDataReciever(Config.getInstance().getValue(Config.SOCKET_HOSTNAME), Integer.parseInt(Config.getInstance().getValue(Config.SOCKET_PORT)), 655360, playbackSpeedSpinner.getValue().intValue());
		 webcamDataReciever = new WebcamDataReciever(1, playbackSpeedSpinner.getValue().intValue());
		 playbackSpeedSpinner.valueProperty().addListener((obs, oldValue, newValue) -> { 
			 flirDataReciever.setPlaybackSpeed(newValue.intValue());
			 webcamDataReciever.setPlaybackSpeed(newValue.intValue());
		 });
			
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
				byte [] croppedByteArray = new byte[IMAGE_WIDTH*IMAGE_CROPPED_HEIGHT*2];				
		 	    System.arraycopy(byteArray, CROP_OFFSET_Y * IMAGE_WIDTH * 2, croppedByteArray, 0,IMAGE_WIDTH*IMAGE_CROPPED_HEIGHT*2);  
		 	    float origMin = ImageConvertor.bytesToCelsius(ImageConvertor.getMin(croppedByteArray));
		 	    float origMax = ImageConvertor.bytesToCelsius(ImageConvertor.getMax(croppedByteArray));
		 	    Mat originalMat, originalCroppedMat, scaledMat, scaledCroppedMat;		     

				originalMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, false, minTempSpinner.getValue().floatValue() , maxTempSpinner.getValue().floatValue(), -1);
				if (scaleTempCheckbox.isSelected())
					scaledMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, scaleTempCheckbox.isSelected(), minTempSpinner.getValue().floatValue() , maxTempSpinner.getValue().floatValue(), -1);
				else 
					scaledMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, true, origMin , origMax, -1);

				originalCroppedMat = new Mat(originalMat, new Rect(CROP_OFFSET_X, CROP_OFFSET_Y, IMAGE_CROPPED_WIDTH, IMAGE_CROPPED_HEIGHT));
				Mat mainMat = processMainMat(scaledMat);			
				Mat workMat = preprocessMat(scaledMat);				
				Mat workCroppedMat = new Mat(workMat, new Rect(CROP_OFFSET_X, CROP_OFFSET_Y, IMAGE_CROPPED_WIDTH, IMAGE_CROPPED_HEIGHT));				

				Mat handMat = workCroppedMat.clone();				
				handMat = segmentHand(handMat, handThresholdSpinner.getValue(), handDilationSpinner.getValue(), handIterSpinner.getValue());
				List <MatOfPoint> contours = MatOperations.findContours(handMat, contourMinSizeSpinner.getValue());
				MatOfPoint biggest = MatOperations.findBiggestContour(contours);
				
				if (contours.size() > 0 && Imgproc.contourArea(biggest) > 100) {					
					Rect rect = findExtendedHandRegion(handMat);
					Imgproc.rectangle(handMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), Scalar.all(255));
					Mat roiMat = new Mat(workCroppedMat, rect);
					Mat goodsMat = roiMat.clone();
	
					if (rect.width != 0 && rect.height != 0) {
						goodsMat = segmentGoods2(roiMat);
						Image goodsImage = ImageConvertor.convertMatToImage(goodsMat);
						Utils.updateFXControl(goodsImageView.imageProperty(), goodsImage);			
					}
				}
				//center
				Image mainImage = ImageConvertor.convertMatToImage(mainMat);
				Image handImage = ImageConvertor.convertMatToImage(handMat);
				Utils.updateFXControl(mainImageView.imageProperty(), mainImage);
				Utils.updateFXControl(handImageView.imageProperty(), handImage);
//				//panel
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
//				Mat mainMat = newValue;
//				Image mainImage = ImageConvertor.convertMatToImage(mainMat);
//				Utils.updateFXControl(webcamImageView.imageProperty(), mainImage);
				
			});
//			wcs.start();
		}
	}

	private Rect findExtendedHandRegion(Mat mat) {
		Mat result = MatOperations.dilate(mat, 10, 20);
		Point [] points = new Point [4];
		MatOfPoint mop = new MatOfPoint();
		Core.findNonZero(result, mop);
		Rect rect = Imgproc.boundingRect(mop);
		return rect;
	}
	
	private Mat roiBox(Mat mat, Mat preprocessed) {
		Mat result = mat.clone();		
		Mat workmat = preprocessed.submat(CROP_OFFSET_Y, CROP_OFFSET_Y + IMAGE_CROPPED_HEIGHT, CROP_OFFSET_X, CROP_OFFSET_X +  IMAGE_CROPPED_WIDTH);
		Mat smallResult = mat.submat(CROP_OFFSET_Y, CROP_OFFSET_Y + IMAGE_CROPPED_HEIGHT, CROP_OFFSET_X, CROP_OFFSET_X +  IMAGE_CROPPED_WIDTH);
		workmat = segmentHand(workmat, handThresholdSpinner.getValue(), handDilationSpinner.getValue(), handIterSpinner.getValue());
		Rect rect = findExtendedHandRegion(workmat);
		Imgproc.rectangle(smallResult, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), Scalar.all(255), 2 );

		byte [] data = new byte[smallResult.channels()*smallResult.cols()*smallResult.rows()];
		smallResult.get(0, 0, data);
		result.put(CROP_OFFSET_Y, CROP_OFFSET_X, data);
		
		return result;
	}
	
	private Mat hullHandWithGoodsRegion(Mat mat, Mat untouched) {
		Mat result = mat.clone();
		Mat workmat = untouched.submat(CROP_OFFSET_Y, CROP_OFFSET_Y + IMAGE_CROPPED_HEIGHT, CROP_OFFSET_X, CROP_OFFSET_X +  IMAGE_CROPPED_WIDTH);
		workmat = preprocessMat(workmat);
		Mat handRegionPreprocessed = workmat.clone();
		Mat handContoursMat = workmat.clone();

		workmat = MatOperations.doCannyEdgeDetection(workmat, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
		workmat = MatOperations.dilate(workmat, dilateSpinner.getValue(), 1); //try to link them	
		List<MatOfPoint> contours = MatOperations.findContours(workmat, contourMinSizeSpinner.getValue());
		handRegionPreprocessed = MatOperations.convexHull(workmat, handRegionPreprocessed, contours, false);

		if (!handRegionPreprocessed.equals(workmat)) 
			MatOperations.replaceMatArea(result, handRegionPreprocessed, CROP_OFFSET_X,  CROP_OFFSET_Y);
		return result;
	}	
	
	
	
	private Mat hullGoodsRegion(Mat mat, Mat untouched) {
		Mat result = mat.clone();
		Mat handRegionPreprocessed = mat.submat(CROP_OFFSET_Y, CROP_OFFSET_Y + IMAGE_CROPPED_HEIGHT, CROP_OFFSET_X, CROP_OFFSET_X +  IMAGE_CROPPED_WIDTH);
		Mat workmat = untouched.submat(CROP_OFFSET_Y, CROP_OFFSET_Y + IMAGE_CROPPED_HEIGHT, CROP_OFFSET_X, CROP_OFFSET_X +  IMAGE_CROPPED_WIDTH);
		workmat = preprocessMat(workmat);
		Mat withoutHand = new Mat(workmat.size(), workmat.type()); 

		workmat = MatOperations.doCannyEdgeDetection(workmat, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
		workmat = MatOperations.morphology(workmat, morphOpenCheckbox.isSelected(), morphCloseCheckbox.isSelected(), erodeSpinner.getValue(), dilateSpinner.getValue(), morphIterSpinner.getValue());
		List <MatOfPoint> contours = MatOperations.findContours(workmat, contourMinSizeSpinner.getValue()); //find contours + filter them
		Mat convexHull = MatOperations.convexHull(workmat, null, contours, true); //draw convex hull mask
		Mat segmentedHand = segmentHand(handRegionPreprocessed, 220, 4, 3); //find hand mask
		
		Core.subtract(convexHull, segmentedHand, withoutHand); //Mat without hand but with noise

		List <MatOfPoint> contours2 = MatOperations.findContours(withoutHand, 0);
		MatOfPoint mop = MatOperations.findBiggestContour(contours2); //find biggest contour - might be a goods only
		handRegionPreprocessed = MatOperations.convexHull(withoutHand, handRegionPreprocessed, Arrays.asList(mop), false);		
		
		if (!withoutHand.equals(handRegionPreprocessed)) {
			MatOperations.replaceMatArea(result, handRegionPreprocessed, CROP_OFFSET_X,  CROP_OFFSET_Y);			
		}
		
		return result;
	}	
	
	private Mat segmentGoods(Mat original, Mat mat, double cannyThresh1, double cannyThresh2, boolean open, boolean close, double erode, double dilate, double handThreshold, double contourMinSize) {
		Mat result = new Mat(mat.size(), mat.type());
		Mat withoutHand = new Mat(mat.size(), mat.type()); 

		result = MatOperations.doCannyEdgeDetection(mat, cannyThresh1 , cannyThresh2); //detect edges
		result = MatOperations.morphology(result, open, close, erode, dilate, morphIterSpinner.getValue());
		
		List <MatOfPoint> contours = MatOperations.findContours(result, contourMinSize); //find contours + filter them
		Mat convexHull = MatOperations.convexHull(result, null, contours, true); //draw convex hull mask
		Mat segmentedHandMask = segmentHand(original, handThreshold, handDilationSpinner.getValue(), handIterSpinner.getValue()); //find hand mask
		
		Core.subtract(convexHull, segmentedHandMask, withoutHand); //Mat without hand but with noise
		result = MatOperations.maskMat(original, withoutHand);  //masked original image - only goods + noise remains	
		Mat noiseGoods = result.clone();
		
		List <MatOfPoint> contours2 = MatOperations.findContours(noiseGoods, 0);
		MatOfPoint mop = MatOperations.findBiggestContour(contours2); //find biggest contour - might be a goods only
		result = MatOperations.convexHull(noiseGoods, null, Arrays.asList(mop), true);
		result = MatOperations.maskMat(noiseGoods, result); //mask biggest contour - only goods should remain				
		return result;
	}
	
	private Mat segmentGoods2(Mat mat) {
		Mat result = new Mat(mat.size(), mat.type());
		Mat handMask = segmentHand(mat, handThresholdSlider.getValue(), handDilationSpinner.getValue(), handIterSpinner.getValue());
		Mat edges = new Mat();

		handMask = MatOperations.invert(handMask);
		result = MatOperations.doCannyEdgeDetection(mat, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
		Core.bitwise_and(handMask, result, result);	
		result = MatOperations.morphology(result, false, true, erodeSpinner.getValue(), dilateSpinner.getValue(), morphIterSpinner.getValue());
		
		goodsContourFeatures(MatOperations.findContours(result, contourMinSizeSpinner.getValue()));
	
		return result;
	}
	
	private void goodsContourFeatures(List <MatOfPoint> mopList) {
		int contoursCount = mopList.size();
		float areaSum=0, areaMin = Float.MAX_VALUE, areaMax = 0, areaAvg = 0;
		float lengthSum=0, lengthMin = Float.MAX_VALUE, lengthMax = 0, lengthAvg = 0;

		for (MatOfPoint mop : mopList) {
			float area = (float) Imgproc.contourArea(mop);
			float length = (float) Imgproc.arcLength(new MatOfPoint2f(mop.toArray()), false);
			areaSum += area;
			lengthSum += length;
			if (areaMin > area) areaMin = area;
			if (areaMax < area) areaMax = area;
			if (lengthMin > length) lengthMin = length;
			if (lengthMax < length) lengthMax = length;
		}
		areaAvg = contoursCount != 0 ? areaSum/contoursCount : 0 ;
		lengthAvg = contoursCount != 0 ? lengthSum/contoursCount : 0;
		System.out.print(contoursCount + ";" +  lengthMin + ";" + lengthMax + ";" + lengthSum + ";" + lengthAvg + ";" + areaMin + ";" + areaMax + ";" + areaSum + ";" + areaAvg);
//		System.out.println(";Hand only");
		System.out.println(";Hand with goods");
//		System.out.println(";Hand in shelf");


	}
	
	private Mat segmentHand(Mat mat, double threshold, double dilation, double iterations) {
		Mat result = new Mat(mat.size(), mat.type());	    
		result = MatOperations.dilate(mat, dilation, (int) iterations);		
		result =  MatOperations.binaryTreshold(result, threshold);
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
		 binaryThresholdDoubleProperty = DoubleProperty.doubleProperty(handThresholdSpinner.getValueFactory().valueProperty());
	
	     minTempSlider.valueProperty().bindBidirectional(minTempDoubleProperty);
	     maxTempSlider.valueProperty().bindBidirectional(maxTempDoubleProperty);
	     handThresholdSlider.valueProperty().bindBidirectional(binaryThresholdDoubleProperty);
	   
	     //focus
	     pauseStreamButton.visibleProperty().addListener((observable, oldValue, newValue) -> {
	    	    if (newValue.equals(false)) readStreamButton.requestFocus();
	    	});
	     readStreamButton.visibleProperty().addListener((observable, oldValue, newValue) -> {
	    	 	if (newValue.equals(false)) pauseStreamButton.requestFocus();
	 	});
	}
	 
	//Mat operations		
	private Mat processMainMat(Mat mat) {
		Mat result = mat.clone();
		Mat untouched = mat.clone();
		if ((clache1Spinner.getValue() != 0 || clache2Spinner.getValue() != 0) && exposureMainCheckbox.isSelected()) result = MatOperations.clache(result, clache1Spinner.getValue(), clache2Spinner.getValue(), clacheClipSpinner.getValue());
		if ((brightnessSpinner.getValue() != 0 || contrastSpinner.getValue() != 0) && exposureMainCheckbox.isSelected()) result = MatOperations.brightnessContrast(result, brightnessSpinner.getValue(), contrastSpinner.getValue());
		if ((addSpinner.getValue() != 0 || multSpinner.getValue() != 0) && exposureMainCheckbox.isSelected()) result=  MatOperations.addMult(result, addSpinner.getValue(), multSpinner.getValue());
		if (blurCheckbox.isSelected() && blurMainCheckbox.isSelected()) result = MatOperations.blurImage(result, blur1Spinner.getValue(), blur2Spinner.getValue(), blurSigmaSpinner.getValue());	
		Mat preprocessed = result.clone();
		if (cannyMainCheckbox.isSelected()) result = MatOperations.doCannyEdgeDetection(result, cannyEdge1Spinner.getValue(), cannyEdge2Spinner.getValue());
		if (morphologyMainCheckbox.isSelected()) result = MatOperations.morphology(result, morphOpenCheckbox.isSelected(), morphCloseCheckbox.isSelected(), erodeSpinner.getValue(), dilateSpinner.getValue(), morphIterSpinner.getValue());
		
		if (hullHandGoodsMainCheckbox.isSelected())	result = hullHandWithGoodsRegion(result, untouched);
		if (hullGoodsMainCheckbox.isSelected()) result = hullGoodsRegion(result, untouched);
		if (roiMainCheckbox.isSelected()) result = roiBox(result, preprocessed);
		
		return result;
	}

	private Mat preprocessMat(Mat mat) {
		Mat result = mat.clone();
		if (clache1Spinner.getValue() != 0 && clache2Spinner.getValue() != 0) result =  MatOperations.clache(result, clache1Spinner.getValue(), clache2Spinner.getValue(), clacheClipSpinner.getValue());
		if (brightnessSpinner.getValue() != 0 || contrastSpinner.getValue() != 0) result = MatOperations.brightnessContrast(result, brightnessSpinner.getValue(), contrastSpinner.getValue());
		if (addSpinner.getValue() != 0 || multSpinner.getValue() != 0) result = MatOperations.addMult(result, addSpinner.getValue(), multSpinner.getValue());
		if (blurCheckbox.isSelected()) result = MatOperations.blurImage(result, blur1Spinner.getValue(), blur2Spinner.getValue(), blurSigmaSpinner.getValue());	
		return result;
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
		contourMinSizeSpinner.getValueFactory().setValue(15.0);
		handThresholdSpinner.getValueFactory().setValue(180.0);
		exposureMainCheckbox.setSelected(false); 
		blurMainCheckbox.setSelected(false); 
		cannyMainCheckbox.setSelected(false);
		morphologyMainCheckbox.setSelected(false);
	}
	
	private void thermalCameraSegmentationDefaults() {
		saveImagesCheckbox.setSelected(false);
		playbackSpeedSpinner.getValueFactory().setValue(100.0);
		minTempSpinner.getValueFactory().setValue(29.5);
		maxTempSpinner.getValueFactory().setValue(33.0);
		scaleTempCheckbox.setSelected(true);
		clache1Spinner.getValueFactory().setValue(0.0); 
		clache2Spinner.getValueFactory().setValue(0.0); 
		clacheClipSpinner.getValueFactory().setValue(0.0); 
		brightnessSpinner.getValueFactory().setValue(-0.0); 
		addSpinner.getValueFactory().setValue(0.0); 
		contrastSpinner.getValueFactory().setValue(0.0); 
		multSpinner.getValueFactory().setValue(0.0);
		blur1Spinner.getValueFactory().setValue(4.0); 
		blur2Spinner.getValueFactory().setValue(6.0); 
		blurSigmaSpinner.getValueFactory().setValue(5.0);
		blurCheckbox.setSelected(true);
		cannyEdge1Spinner.getValueFactory().setValue(20.0); 
		cannyEdge2Spinner.getValueFactory().setValue(40.0);
		dilateSpinner.getValueFactory().setValue(10.0);
		erodeSpinner.getValueFactory().setValue(2.0);
		morphIterSpinner.getValueFactory().setValue(3.0);
		morphCloseCheckbox.setSelected(true);
		morphOpenCheckbox.setSelected(false);
		
		contourMinSizeSpinner.getValueFactory().setValue(0.0);
		
		handThresholdSpinner.getValueFactory().setValue(180.0);
		handDilationSpinner.getValueFactory().setValue(4.0);
		handIterSpinner.getValueFactory().setValue(8.0);

		
		//main preview
		exposureMainCheckbox.setSelected(false); 
		blurMainCheckbox.setSelected(false); 
		cannyMainCheckbox.setSelected(false);
		morphologyMainCheckbox.setSelected(false);
		hullHandGoodsMainCheckbox.setSelected(false);
		hullGoodsMainCheckbox.setSelected(false);
		roiMainCheckbox.setSelected(false);
	}
	
	//getters, setters	

	private Point[] findExtendedHandRegion2(Mat mat) { //FIXME: not working
		int offset = 30;
		Point [] points = new Point [4];
		MatOfPoint mop = new MatOfPoint();
		MatOfPoint2f mop2f = new MatOfPoint2f();
		Core.findNonZero(mat, mop);
		if (mop != null &&  !mop.empty()) {
		mop.convertTo(mop2f, CvType.CV_32FC1);
			RotatedRect rotatedRect = Imgproc.minAreaRect(mop2f);
			rotatedRect.points(points);
			Point bottomLeft = new Point(mat.width(),-200);
			Point topLeft = new Point(mat.width(),mat.height());			
			Point topRight = new Point(-200,mat.height()); 
			Point bottomRight = new Point(-200,-200); 
			
			for (int i = 0; i < points.length; ++i) {
				if (bottomLeft.x >= points[i].x && bottomLeft.y <= points[i].y) 
					bottomLeft = points[i];
				if (topLeft.x >= points[i].x && topLeft.y >= points[i].y) 
					topLeft = points[i];
				if (topRight.x <= points[i].x && topRight.y >= points[i].y) 
					topRight = points[i];
				if (bottomRight.x <= points[i].x && bottomRight.y <= points[i].y) 
					bottomRight = points[i];
			}
//			for (int i = 0; i < points.length; ++i) {
////				if (topLeft.x >= points[i].x && topLeft.y >= points[i].y) 
////					topLeft = points[i];
//			}
//			for (int i = 0; i < points.length; ++i) {
//				if (topRight.x <= points[i].x && topRight.y >= points[i].y) 
//					topRight = points[i];
//			}
//			for (int i = 0; i < points.length; ++i) {
//				if (bottomRight.x <= points[i].x && bottomRight.y <= points[i].y) 
//					bottomRight = points[i];
//			}
//			bottomLeft.x = bottomLeft.x - offset <= 0 ? 0 : bottomLeft.x - offset;
//			bottomLeft.y = bottomLeft.y + offset >= mat.height() ? mat.height() : bottomLeft.y + offset ;
//			topLeft.x = topLeft.x - offset <= 0 ? 0 : topLeft.x - offset;
//			topLeft.y = topLeft.y - offset <= 0 ? 0 : topLeft.y - offset ;
//			topRight.x = topRight.x + offset >= mat.width() ? mat.width()  : topRight.x + offset;
//			topRight.y = topRight.y - offset <= 0 ? 0 : topRight.y - offset ;
//			bottomRight.x = bottomRight.x + offset >= mat.width() ? mat.width()  : bottomRight.x + offset;
//			bottomRight.y = bottomRight.y + offset >= mat.height() ? mat.height() : bottomRight.y + offset ;
			
			points[0] = bottomLeft;
			points[1] = topLeft;
			points[2] = topRight;
			points[3] = bottomRight;
			return points;
		}
		return null;
	}
}
