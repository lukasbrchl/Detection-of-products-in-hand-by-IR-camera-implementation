package application;

import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.Size;
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
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import utils.Utils;

public class MainController {
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 512;

	//Stream buttons
	@FXML private Button openStreamButton, closeStreamButton, pauseStreamButton;
	@FXML private AnchorPane openPausePane;
	//Image container
	@FXML private ImageView imageView;
	//Overall status
	@FXML private Label streamStatus;
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
	private ImageConvertor imgConv;
	private DataReciever dataReciever;

	public MainController() {
		imgConv = new ImageConvertor(IMAGE_WIDTH, IMAGE_HEIGHT);
		dataReciever = new DataReciever(DataReciever.DUMMY_HOST, 0, 0);
	}
	
	 @FXML 
	 public void initialize() {
		 bindSpinnersToSliders();
    }
	 
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
        	    if (newValue.equals(false)) {
        	    	openStreamButton.requestFocus();
        	    }
        	});
         openStreamButton.visibleProperty().addListener((observable, oldValue, newValue) -> {
     	    if (newValue.equals(false)) {
     	    	pauseStreamButton.requestFocus();
     	    }
     	});
	 }
	
	@FXML 
	public void focusHere() {
		 openPausePane.requestFocus();
	 }
	 
	@FXML
	protected void openStream(ActionEvent event) throws IOException {
		dataReciever.setPaused(false);
		toggleOpenPause();		
		if (imv!= null && imv.isRunning()) return;	
		
		imv = new ImageViewService(this, imgConv, dataReciever, IMAGE_WIDTH, IMAGE_HEIGHT);
		imv.valueProperty().addListener((obs, oldValue, newValue) -> { 
			Mat enchancedMat = enchanceMat(newValue);
			Image img = imgConv.convertMatToImage(enchancedMat);
			Utils.updateFXControl(imageView.imageProperty(), img);
			});
		streamStatus.textProperty().bind(imv.messageProperty());
		imv.start();		
	}
	
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
		mat.convertTo(mat, -1, brightnessDoubleProperty.getValue() + 1, contrastDoubleProperty.getValue());
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
	
	@FXML
	protected void closeStream(ActionEvent event) {		
		imv.cancel();
		dataReciever.setPaused(true);
		toggleOpenPause();
	}
	
	@FXML
	protected void pauseStream(ActionEvent event) {
		dataReciever.setPaused(true);
		toggleOpenPause();
	}
	
	private void toggleOpenPause() {
		if (dataReciever.isPaused()) {
			openStreamButton.setVisible(true);
			pauseStreamButton.setVisible(false);
		} else {
			openStreamButton.setVisible(false);
			pauseStreamButton.setVisible(true);
		}
	}
	
	
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
	protected void setClosed() {
		// close socket
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
