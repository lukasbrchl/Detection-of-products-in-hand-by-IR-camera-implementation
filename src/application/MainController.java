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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import image.ImageConvertor;
import image.service.ImageViewService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import network.DataReciever;
import network.DataReciever.Status;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import utils.Config;
import utils.Utils;

public class MainController {
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 512;
	public static final int IMAGE_CROPPED_WIDTH = 640;
	public static final int IMAGE_CROPPED_HEIGHT = 40;
	public static final int PANEL_IMAGE_WIDTH = 160;
	public static final int PANEL_IMAGE_HEIGHT = 128;

	//Image containers
	@FXML private ImageView mainImage, originalImage, histogramImage, croppedImage;
	//Stream buttons
	@FXML private Button connectToStreamButton, readStreamButton, closeStreamButton, pauseStreamButton;
	@FXML private AnchorPane openPausePane;
	//Overall status
	@FXML private Label streamStatus;
	@FXML private CheckBox saveImagesCheckbox;
	//Scale
	@FXML private Slider minTempSlider, maxTempSlider;	
	@FXML private Spinner<Double> minTempSpinner, maxTempSpinner;
	@FXML private CheckBox scaleTempCheckbox;
	@FXML private AnchorPane minTempPane, maxTempPane;
	//Brightness and contrast
	@FXML private Slider brightnessSlider, contrastSlider;	
	@FXML private Spinner<Double> brightnessSpinner, contrastSpinner;
	//Blur
	@FXML private Slider blurSlider;
	@FXML private Spinner<Double> blurSpinner;
	@FXML private CheckBox blurCheckbox;
	@FXML private ChoiceBox<String> blurChoiceBox;
	@FXML private AnchorPane blurPane;
	//Binary and otsu threshold
	@FXML private Slider binaryThresholdSlider, otsuCorrectionSlider;
	@FXML private Spinner<Double> binaryThresholdSpinner, otsuCorrectionSpinner;
	@FXML private CheckBox binaryThresholdCheckbox, otsuThresholdCheckbox;
	@FXML private AnchorPane binaryThresholdPane, otsuCorrectionPane;	
	//Canny edge
	@FXML private Slider cannyEdge1Slider, cannyEdge2Slider;
	@FXML private Spinner<Double> cannyEdge1Spinner, cannyEdge2Spinner;
	@FXML private CheckBox cannyEdgeCheckbox;
	@FXML private AnchorPane cannyEdge1Pane, cannyEdge2Pane;
	//Dilate and erode
	@FXML private Slider morphSlider;
	@FXML private Spinner<Double> morphSpinner;
	@FXML private CheckBox dilateCheckbox, erodeCheckbox;
	@FXML private AnchorPane morphPane;

	private DoubleProperty minTempDoubleProperty, maxTempDoubleProperty, brightnessDoubleProperty, contrastDoubleProperty, blurDoubleProperty, binaryThresholdDoubleProperty, otsuCorrectionDoubleProperty, 
	cannyEdge1DoubleProperty, cannyEdge2DoubleProperty, morphDoubleProperty;  //prevents GC from cleaning weak listeners
	private ImageViewService imv;
	private ImageConvertor imgConv, imgCroppedConv;
	private DataReciever dataReciever;

	public MainController() {
		imgConv = new ImageConvertor(IMAGE_WIDTH, IMAGE_HEIGHT);
		imgCroppedConv = new ImageConvertor(IMAGE_CROPPED_WIDTH, IMAGE_CROPPED_HEIGHT);
		dataReciever = new DataReciever(Config.getInstance().getValue(Config.SOCKET_HOSTNAME), Integer.parseInt(Config.getInstance().getValue(Config.SOCKET_PORT)), 655360);
	}
	
	 @FXML 
	 public void initialize() {
		 bindSpinnersToSliders();
    }
	 
	//buttons
	@FXML 
	protected void connectToStream(ActionEvent event) {
		dataReciever.openConnection();		
		streamStatus.setText(dataReciever.getStatus().getStrStatus());
		toggleOpenPause();	
	}
	
	@FXML
	protected void readStream(ActionEvent event) throws IOException {
		dataReciever.setStatus(Status.STREAMING);
		toggleOpenPause();		
		if (imv!= null && imv.isRunning()) return;	
		
		imv = new ImageViewService(this, imgConv, dataReciever, IMAGE_WIDTH, IMAGE_HEIGHT);
		imv.valueProperty().addListener((obs, oldValue, newValue) -> { 
			Mat enchancedMat = enchanceMat(newValue.get(0));
//			Mat croppedMat = new Mat(enchancedMat, new Rect(0, 472, 640, 40));
//			Image croppedImg = imgCroppedConv.convertMatToImage(croppedMat);
			Image processedImage = imgConv.convertMatToImage(enchancedMat);
			Image originalImg = imgConv.convertMatToImage(newValue.get(1));
			Image histogramImg = imgConv.convertMatToImage(createHistogram(enchancedMat));			
			Utils.updateFXControl(mainImage.imageProperty(), processedImage);
//			Utils.updateFXControl(croppedImage.imageProperty(), croppedImg);
			Utils.updateFXControl(originalImage.imageProperty(), originalImg);
			Utils.updateFXControl(histogramImage.imageProperty(), histogramImg);
			});
		imv.messageProperty().addListener((obs, oldValue, newValue) -> { 
			streamStatus.setText(newValue);
			});
		imv.start();		
	}

	@FXML
	protected void closeStream(ActionEvent event) {		
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
		if(blurCheckbox.isSelected()) 
			blurPane.setDisable(false);
		else
			blurPane.setDisable(true);
	}
	
	@FXML 
	protected void binaryThresholdCheckboxClicked(ActionEvent event) {
		if(binaryThresholdCheckbox.isSelected()) 
			binaryThresholdPane.setDisable(false);
		else
			binaryThresholdPane.setDisable(true);
	}
	
	@FXML 
	protected void otsuThresholdCheckboxClicked(ActionEvent event) {
		if(otsuThresholdCheckbox.isSelected()) 
			otsuCorrectionPane.setDisable(false);
		else
			otsuCorrectionPane.setDisable(true);
	}
	
	@FXML 
	protected void cannyEdgeCheckboxClicked(ActionEvent event) {
		if(cannyEdgeCheckbox.isSelected())  {
			cannyEdge1Pane.setDisable(false);
			cannyEdge2Pane.setDisable(false);
		}
		else {
			cannyEdge1Pane.setDisable(true);
			cannyEdge2Pane.setDisable(true);

		}
	}
	
	@FXML
	protected void morphCheckboxClicked(ActionEvent event) {
		if (event.getSource().equals(dilateCheckbox)) {
			erodeCheckbox.setSelected(false);
		} else {
			dilateCheckbox.setSelected(false);
		}
		if (dilateCheckbox.isSelected() || erodeCheckbox.isSelected()) 
			morphPane.setDisable(false);
		else
			morphPane.setDisable(true);
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
		 brightnessDoubleProperty = DoubleProperty.doubleProperty(brightnessSpinner.getValueFactory().valueProperty());
		 contrastDoubleProperty = DoubleProperty.doubleProperty(contrastSpinner.getValueFactory().valueProperty());
		 blurDoubleProperty = DoubleProperty.doubleProperty(blurSpinner.getValueFactory().valueProperty());
		 binaryThresholdDoubleProperty = DoubleProperty.doubleProperty(binaryThresholdSpinner.getValueFactory().valueProperty());
		 otsuCorrectionDoubleProperty = DoubleProperty.doubleProperty(otsuCorrectionSpinner.getValueFactory().valueProperty());		
		 cannyEdge1DoubleProperty = DoubleProperty.doubleProperty(cannyEdge1Spinner.getValueFactory().valueProperty());
		 cannyEdge2DoubleProperty = DoubleProperty.doubleProperty(cannyEdge2Spinner.getValueFactory().valueProperty());
		 morphDoubleProperty = DoubleProperty.doubleProperty(morphSpinner.getValueFactory().valueProperty());
	
	     minTempSlider.valueProperty().bindBidirectional(minTempDoubleProperty);
	     maxTempSlider.valueProperty().bindBidirectional(maxTempDoubleProperty);
	     brightnessSlider.valueProperty().bindBidirectional(brightnessDoubleProperty);
	     contrastSlider.valueProperty().bindBidirectional(contrastDoubleProperty);
	     blurSlider.valueProperty().bindBidirectional(blurDoubleProperty);
	     binaryThresholdSlider.valueProperty().bindBidirectional(binaryThresholdDoubleProperty);
	     otsuCorrectionSlider.valueProperty().bindBidirectional(otsuCorrectionDoubleProperty);        
	     cannyEdge1Slider.valueProperty().bindBidirectional(cannyEdge1DoubleProperty);
	     cannyEdge2Slider.valueProperty().bindBidirectional(cannyEdge2DoubleProperty);
	     morphSlider.valueProperty().bindBidirectional(morphDoubleProperty);
	     
	     //focus
	     pauseStreamButton.visibleProperty().addListener((observable, oldValue, newValue) -> {
	    	    if (newValue.equals(false)) readStreamButton.requestFocus();
	    	});
	     readStreamButton.visibleProperty().addListener((observable, oldValue, newValue) -> {
	    	 	if (newValue.equals(false)) pauseStreamButton.requestFocus();
	 	});
	}
	 
	//Mat operations		
	private Mat enchanceMat(Mat mat) {
		if (brightnessDoubleProperty.getValue() != 0 || contrastDoubleProperty.getValue() != 0) doBrightnessContrast(mat);
		if (blurCheckbox.isSelected()) doBlurImage(mat);
		if (binaryThresholdCheckbox.isSelected()) doBinaryTreshold(mat);
		if (otsuThresholdCheckbox.isSelected())	doOtsuTreshold(mat);
		if (cannyEdgeCheckbox.isSelected()) mat = doCannyEdgeDetection(mat);
		if (dilateCheckbox.isSelected()) dilate(mat);
		if (erodeCheckbox.isSelected()) erode(mat);
		return mat;
	}
	
	private Mat doBrightnessContrast(Mat mat) {
		mat.convertTo(mat, -1, brightnessDoubleProperty.getValue() + 1, -contrastDoubleProperty.getValue());
		return mat;
	}
	
	private Mat doBlurImage(Mat mat) {
		int size = (int) blurSlider.getValue();
		if (size % 2 == 0) ++size;
		Imgproc.GaussianBlur(mat, mat, new Size(size, size), 2);
		return mat;
	}
	
	private Mat doBinaryTreshold(Mat mat) {
		Imgproc.threshold(mat, mat, binaryThresholdSlider.getValue(), 255, Imgproc.THRESH_BINARY);
		return mat;
	}
	
	private Mat doOtsuTreshold(Mat mat) {
		Imgproc.threshold(mat, mat, Imgproc.threshold(mat, new Mat(), 0, 255, Imgproc.THRESH_OTSU) + otsuCorrectionSlider.getValue(), 255, Imgproc.THRESH_BINARY);
		return mat;
	}	
	
	private Mat doCannyEdgeDetection(Mat mat) {
		Mat detectedEdges = new Mat();		
		Imgproc.Canny(mat, mat, cannyEdge1Slider.getValue(), cannyEdge2Slider.getValue());
		Mat dest = new Mat();
		mat.copyTo(dest, detectedEdges);

	    Point shift=new Point(150,0);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();    

        Imgproc.findContours(dest, contours,  new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        double[] cont_area =new double[contours.size()]; 
		Mat result = new Mat();
//		
//		  for(int i=0; i< contours.size();i++){
//		        System.out.println(Imgproc.contourArea(contours.get(i)));
//		        if (Imgproc.contourArea(contours.get(i)) > 50 ){
//		            Rect rect = Imgproc.boundingRect(contours.get(i));
//		            System.out.println(rect.height);
//		            if (rect.height > 28){
//		            //System.out.println(rect.x +","+rect.y+","+rect.height+","+rect.width);
////		            	Imgproc.rectangle(dest, new Point(rect.x,rect.height), new Point(rect.y,rect.width),new Scalar(0,0,255));
//		            	Imgproc.rectangle(dest, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height),new Scalar(0,0,255));
//		            }
//		        }
//		    }
		
        
//        for(int i=0; i< contours.size();i++){
//            if (Imgproc.contourArea(contours.get(i)) > 50 ){
//                Rect rect = Imgproc.boundingRect(contours.get(i));
//                cont_area[i]=Imgproc.contourArea(contours.get(i));
//
//                if (rect.height > 25){
//                	Imgproc.rectangle(result, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height),new Scalar(0,0,255));
//
//                    System.out.println(rect.x +"-"+ rect.y +"-"+ rect.height+"-"+rect.width);
//                }
//            }
//        }
        
//		Mat temp = new Mat(), out = new Mat();
//		dest.copyTo(temp);
//		Mat im_floodfill = temp;
//	    Imgproc.floodFill(im_floodfill, new Mat(), new Point(0,0), new Scalar(255));
////	    Mat im_floodfill_inv = new Mat();
////	    Core.bitwise_not(im_floodfill, im_floodfill_inv);		   
////	    Core.bitwise_or(dest, im_floodfill_inv, out);
		
		return dest;
	}
	
	private void dilate(Mat mat) {
		int size = (int) morphSlider.getValue();
		if (size % 2 == 0) ++size;
	    Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(size,size));
	    Imgproc.dilate(mat, mat, element);
	}
	
	private void erode(Mat mat) {
		int size = (int) morphSlider.getValue();
		if (size % 2 == 0) ++size;
	    Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(size,size));
	    Imgproc.erode(mat, mat, element);
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
	public final Slider getMinTempSlider() {
		return minTempSlider;
	}

	public final void setMinTempSlider(Slider minTempSlider) {
		this.minTempSlider = minTempSlider;
	}

	public final Slider getMaxTempSlider() {
		return maxTempSlider;
	}

	public final void setMaxTempSlider(Slider maxTempSlider) {
		this.maxTempSlider = maxTempSlider;
	}

	public final CheckBox getScaleTempCheckbox() {
		return scaleTempCheckbox;
	}

	public final void setScaleTempCheckbox(CheckBox scaleTempCheckbox) {
		this.scaleTempCheckbox = scaleTempCheckbox;
	}

}
