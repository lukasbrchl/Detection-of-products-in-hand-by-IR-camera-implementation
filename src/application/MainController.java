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

import image.ImageConvertor;
import image.service.DataRecieverService;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import network.DataReciever;
import network.DataReciever.Status;
import utils.Config;
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
	@FXML private ImageView mainImageView, handImageView, goodsImageView, originalImageView, histogramImageView, originalCroppedImageView;
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

	private DoubleProperty minTempDoubleProperty, maxTempDoubleProperty,  binaryThresholdDoubleProperty, otsuCorrectionDoubleProperty, morphDoubleProperty;  //prevents GC from cleaning weak listeners
	private DataRecieverService imv;
	private DataReciever dataReciever;

	public MainController() {
		dataReciever = new DataReciever(Config.getInstance().getValue(Config.SOCKET_HOSTNAME), Integer.parseInt(Config.getInstance().getValue(Config.SOCKET_PORT)), 655360);
	}
	
	 @FXML 
	 public void initialize() {
		 bindSpinnersToSliders();
		 playbackSpeedSpinner.valueProperty().addListener((obs, oldValue, newValue) -> { 
			 dataReciever.setPlaybackSpeed(newValue.intValue());
		 });
    }
	 
	//buttons
	@FXML 
	protected void connectToStream(ActionEvent event) {
		dataReciever.openConnection();		
		streamStatusLabel.setText(dataReciever.getStatus().getStrStatus());
		toggleOpenPause();	
	}
	
	@FXML
	protected void readStream(ActionEvent event) throws IOException {
		dataReciever.setStatus(Status.STREAMING);
		toggleOpenPause();		
		if (imv!= null && imv.isRunning()) return;	
		
		imv = new DataRecieverService(dataReciever);
		imv.valueProperty().addListener((obs, oldValue, newValue) -> { 
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
			Image histogramImage = ImageConvertor.convertMatToImage(createHistogram(workMat));	
			Image originalCroppedImage = ImageConvertor.convertMatToImage(originalCroppedMat);
			Utils.updateFXControl(originalImageView.imageProperty(), originalImage);
			Utils.updateFXControl(histogramImageView.imageProperty(), histogramImage);
			Utils.updateFXControl(originalCroppedImageView.imageProperty(), originalCroppedImage);
		});
		imv.messageProperty().addListener((obs, oldValue, newValue) -> { 
			streamStatusLabel.setText(newValue);
			});
		imv.start();		
	}

	
	private Mat processMainMat(byte[] byteArray) {
		Mat mat;
		if (scaleMainCheckbox.isSelected()) mat = createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, minTempDoubleProperty.getValue().floatValue(), maxTempDoubleProperty.getValue().floatValue());
		else mat = createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT);
		if (clache1Spinner.getValue() != 0 && clache2Spinner.getValue() != 0 ) {
			CLAHE clahe = Imgproc.createCLAHE(clacheClipSpinner.getValue(), new Size(clache1Spinner.getValue(), clache2Spinner.getValue()));
			clahe.apply(mat, mat);
		}
		if ((brightnessSpinner.getValue() != 0 || contrastSpinner.getValue() != 0) && exposureMainCheckbox.isSelected()) brightnessContrast(mat, brightnessSpinner.getValue(), contrastSpinner.getValue());
		if ((addSpinner.getValue() != 0 || multSpinner.getValue() != 0) && exposureMainCheckbox.isSelected()) addMult(mat, addSpinner.getValue(), multSpinner.getValue());
		if (blurCheckbox.isSelected() && blurMainCheckbox.isSelected()) mat = blurImage(mat, blur1Spinner.getValue(), blur2Spinner.getValue(), blurSigmaSpinner.getValue());	
		if (cannyMainCheckbox.isSelected()) mat = doCannyEdgeDetection(mat, cannyEdge1Spinner.getValue(), cannyEdge2Spinner.getValue());
		if (dilateMainCheckbox.isSelected()) mat = dilate(mat, dilateSpinner.getValue());
		
		
		return mat;
	}

	private Mat createMat(byte [] byteArray, int width, int height, float min, float max) {				
		return ImageConvertor.convertBinaryToMat(byteArray, width, height, min, max);
	}
	
	private Mat segmentGoods(Mat original, Mat mat, double cannyThresh1, double cannyThresh2, double dilate, double handThreshold, double contourMinSize) {
		Mat result = new Mat(mat.size(), mat.type());
		Mat withoutHand = new Mat(mat.size(), mat.type()); 

		result = doCannyEdgeDetection(mat, cannyThresh1 , cannyThresh2); //detect edges
		result = dilate(mat, dilate); //try to link them	
		
		List <MatOfPoint> contours = findContours(result, contourMinSize); //find contours + filter them
		Mat convexHull = convexHull(result, contours); //draw convex hull mask
		Mat segmentedHand = segmentHand(original, handThreshold); //find hand mask
		
		Core.subtract(convexHull, segmentedHand, withoutHand); //Mat without hand but with noise
		result = maskMat(original, withoutHand);  //masked original image - only goods + noise remains	
		Mat noiseGoods = result.clone();
		
		List <MatOfPoint> contours2 = findContours(noiseGoods, 0);
		MatOfPoint mop = findBiggestContour(contours2); //find biggest contour - might be a goods only
		result = convexHull(noiseGoods, Arrays.asList(mop));
		result = maskMat(noiseGoods, result); //mask biggest contour - only goods should remain				
		return result;
	}
	
	
	private Mat segmentHand(Mat mat, double threshold) {
		Mat result = binaryTreshold(mat, threshold);
		return result;
	}
		
	private void detectHand(Mat mat) {
		List <MatOfPoint> mopList = findContours(mat, 0);
		MatOfPoint mopHand = findBiggestContour(mopList);
		double countourArea = Imgproc.contourArea(mopHand);
	}	

	@FXML
	private void closeStream(ActionEvent event) {		
		dataReciever.closeConnection();
		imv.cancel();
		toggleOpenPause();
	}
	
	
	@FXML
	protected void pauseStream(ActionEvent event) {
		dataReciever.setStatus(Status.PAUSED);
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
		if (saveImagesCheckbox.isSelected()) dataReciever.setSaveImages(true);
		else dataReciever.setSaveImages(false);
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
		if (brightnessSpinner.getValue() != 0 || contrastSpinner.getValue() != 0) brightnessContrast(mat, brightnessSpinner.getValue(), contrastSpinner.getValue());
		if (addSpinner.getValue() != 0 || multSpinner.getValue() != 0) addMult(mat, addSpinner.getValue(), multSpinner.getValue());
		if (blurCheckbox.isSelected()) mat = blurImage(mat, blur1Spinner.getValue(), blur2Spinner.getValue(), blurSigmaSpinner.getValue());	
		return mat;
	}
	

	private Mat brightnessContrast(Mat mat, double brightness, double contrast) { //CLAHE http://docs.opencv.org/trunk/d5/daf/tutorial_py_histogram_equalization.html
		mat.convertTo(mat, -1, brightness + 1, -contrast);
		return mat;
	}
	private Mat addMult(Mat mat, double add, double mult) {
        Core.add(mat, Scalar.all(add), mat);
        Core.multiply(mat, Scalar.all(mult + 1), mat);
		return mat;
	}
	private Mat blurImage(Mat mat, double size1, double size2, double sigma) {
		if (size1 % 2 == 0) ++size1;		
		if (size2 % 2 == 0) ++size2;
		Mat resultMat = mat.clone();
		Imgproc.GaussianBlur(mat, resultMat, new Size(size1, size2), sigma);
//		Imgproc.bilateralFilter(mat, resultMat, blurSigmaSpinner.getValue().intValue(), blur1Spinner.getValue(), blur2Spinner.getValue());
		return resultMat;
	}
	private Mat binaryTreshold(Mat mat, double threshold) {
		Mat result = new Mat(mat.size(), mat.type());        
		Imgproc.threshold(mat, result, threshold , 255, Imgproc.THRESH_BINARY);
		return result;
	}
	private Mat otsuThreshold(Mat mat, double thresholdCorrection) {
		Mat result = new Mat(mat.size(), mat.type());        
		Imgproc.threshold(mat, result, Imgproc.threshold(mat, new Mat(), 0, 255, Imgproc.THRESH_OTSU) + thresholdCorrection, 255, Imgproc.THRESH_BINARY);
		return result;
	}	
	private Mat doCannyEdgeDetection(Mat mat, double thresh1, double thresh2) { //todo 2 params
		Mat detectedEdges = new Mat();		
		Imgproc.Canny(mat, mat, thresh1, thresh2);
		Mat dest = new Mat();
		mat.copyTo(dest, detectedEdges);
		
		return dest;
	}
	private List<MatOfPoint> findContours(Mat mat, double minSize) {
		List<MatOfPoint> allContours = new ArrayList<MatOfPoint>();    
		List<MatOfPoint> filteredContours = new ArrayList<MatOfPoint>();    
		
        Imgproc.findContours(mat, allContours,  new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < allContours.size(); i++) {
        	double countourArea = Imgproc.contourArea(allContours.get(i));	      
	        if (countourArea > minSize) filteredContours.add(allContours.get(i));	      
		}        
        foundContoursLabel.setText(" " + Integer.toString(filteredContours.size()));
        return filteredContours;
	}
	
	private MatOfPoint findBiggestContour(List<MatOfPoint> contours) {
		MatOfPoint max = new MatOfPoint();
		double maxArea = 0;
		for (MatOfPoint mop : contours) {
        	double countourArea = Imgproc.contourArea(mop);	     
        	if (countourArea > maxArea) {
        		maxArea = countourArea;
        		max = mop;
        	} 
		}
		return max;
	}
	
	private Mat drawContours(List <MatOfPoint> contours) {	
		Mat contourImg = new Mat(IMAGE_CROPPED_HEIGHT, IMAGE_CROPPED_WIDTH, CvType.CV_8U, new Scalar(0,0,0));		
		for (int i = 0; i < contours.size(); i++) {
		    Imgproc.drawContours(contourImg, contours, i, new Scalar(255, 0, 0), -1);
		}
		return contourImg;	
	}
	
	private Mat convexHull(Mat mat, List <MatOfPoint> contours) {
		Mat contoursMat = drawContours(contours);
        MatOfPoint points = new MatOfPoint();
        MatOfInt hullTemp = new MatOfInt();
		Mat result = new Mat(IMAGE_CROPPED_HEIGHT, IMAGE_CROPPED_WIDTH, CvType.CV_8U, new Scalar(0,0,0));        
        Core.findNonZero(contoursMat, points);   
        if (points == null || points.empty()) return mat;        
		
        Imgproc.convexHull(points, hullTemp);		
        
        MatOfPoint mopOut = new MatOfPoint();
        mopOut.create((int)hullTemp.size().height,1,CvType.CV_32SC2);

        for(int i = 0; i < hullTemp.size().height ; i++)
        {
            int index = (int)hullTemp.get(i, 0)[0];
            double[] point = new double[] {
            		points.get(index, 0)[0], points.get(index, 0)[1]
            };
            mopOut.put(i, 0, point);
        }        
        
        List<MatOfPoint> cnl = new ArrayList<>();
        cnl.add(mopOut);
        Imgproc.drawContours(result, cnl, -1, Scalar.all(255), Core.FILLED);        
        return result;
	}
	private Mat maskMat(Mat matToMask, Mat mask) {
		Mat result = new Mat(matToMask.size(), matToMask.type(), new Scalar(0,0,0)); 
		matToMask.copyTo(result, mask);
		return result;
	}
	private Mat aproxCurve(Mat mat, List <MatOfPoint> contours) { //TODO: contains bugs
		Mat contoursMat = drawContours(contours);
		
		MatOfPoint points = new MatOfPoint();
		MatOfPoint2f thisContour2f = new MatOfPoint2f();
		MatOfPoint approxContour = new MatOfPoint();
		MatOfPoint2f approxContour2f = new MatOfPoint2f();
		
		Core.findNonZero(contoursMat, points);   
		
        if (points == null || points.empty()) return mat;        		
		
        points.convertTo(thisContour2f, CvType.CV_32FC1);
		
		Imgproc.approxPolyDP(thisContour2f, approxContour2f, 2, false);
		
		approxContour2f.convertTo(approxContour, CvType.CV_32S);
		
		
		Mat result = new Mat(IMAGE_CROPPED_HEIGHT, IMAGE_CROPPED_WIDTH, CvType.CV_8U, new Scalar(0,0,0));     
		List<MatOfPoint> cnl = new ArrayList<>();
        cnl.add(approxContour);
        Imgproc.drawContours(result, cnl, -1, Scalar.all(255), Core.FILLED);   
        return result;
	}	
	private Mat invert(Mat mat) {
		Mat result = new Mat(mat.size(), mat.type());
        Mat white = new Mat(mat.size(), mat.type(), Scalar.all(255)); 
        Core.subtract(white, mat, result);
        return result;
	}
	private Mat dilate(Mat mat, double dSize) {
		Mat result = new Mat(mat.size(), mat.type());
		int size = (int) dSize;
		if (size % 2 == 0) ++size;
	    Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(size,size));
	    Imgproc.dilate(mat, result, element);
	    return result;
	}
	private Mat erode(Mat mat, double dSize) {
		Mat result = new Mat(mat.size(), mat.type());
		int size = (int) dSize;
		if (size % 2 == 0) ++size;
	    Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(size,size));
	    Imgproc.erode(mat, result, element);
	    return result;
	}
	private Mat createHistogram(Mat mat) {
		Mat hist = new Mat();
		MatOfInt histSize = new MatOfInt(256);
		Imgproc.calcHist(Arrays.asList(mat), new MatOfInt(0), new Mat(), hist, histSize, new MatOfFloat(0, 256), false);
		int bin_w = (int) Math.round(IMAGE_WIDTH / histSize.get(0, 0)[0]);		
		Mat histImage = new Mat(IMAGE_HEIGHT, IMAGE_WIDTH, CvType.CV_8U, new Scalar(0, 0, 0));
		Core.normalize(hist, hist, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
				
		for (int i = 1; i < histSize.get(0, 0)[0]; i++) {
			Imgproc.line(histImage, new Point(bin_w * (i - 1), IMAGE_HEIGHT - Math.round(hist.get(i - 1, 0)[0])), new Point(bin_w * (i), IMAGE_HEIGHT - Math.round(hist.get(i, 0)[0])), new Scalar(255, 0, 0), 2, 8, 0);			
		}		
		return histImage;		
	}
	
	//other helpers
	private void toggleOpenPause() {
		if (dataReciever.getStatus().equals(Status.PAUSED)) {
			connectToStreamButton.setVisible(false);
			readStreamButton.setVisible(true);
			pauseStreamButton.setVisible(false);
		} else if (dataReciever.getStatus().equals(Status.CONNECTED)){
			connectToStreamButton.setVisible(false);
			readStreamButton.setVisible(true);
			pauseStreamButton.setVisible(false);
		} else if (dataReciever.getStatus().equals(Status.CLOSED)){
			connectToStreamButton.setVisible(true);
			readStreamButton.setVisible(false);
			pauseStreamButton.setVisible(false);
		} else if (dataReciever.getStatus().equals(Status.STREAMING)){
			connectToStreamButton.setVisible(false);
			readStreamButton.setVisible(false);
			pauseStreamButton.setVisible(true);
		}
	}	
	
	//getters, setters	

}
