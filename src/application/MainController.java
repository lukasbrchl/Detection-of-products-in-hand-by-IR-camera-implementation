package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

import data.algorithm.params.PreprocessingSettings;
import data.algorithm.params.SettingsManager;
import data.image.AlgHelper;
import data.image.ImageConvertor;
import data.image.MatOperations;
import data.image.domain.Contour;
import data.reciever.FlirDataReciever;
import data.reciever.WebcamDataReciever;
import data.reciever.domain.Status;
import data.reciever.service.FlirDataService;
import data.reciever.service.WebcamService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button ;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
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

	//Main container
	@FXML private BorderPane mainBorderPane;
	//Image containers
	@FXML private ImageView mainImageView, helperImageView, handImageView, goodsImageView, originalImageView, histogramImageView, originalCroppedImageView;
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
	@FXML private MenuItem loadFilesFromFolder, loadPreprocesSettings, storePreprocesSettings;
	
	private DoubleProperty minTempDoubleProperty, maxTempDoubleProperty,  binaryThresholdDoubleProperty;  //prevents GC from cleaning weak listeners
	private FlirDataService fds;
	private FlirDataReciever flirDataReciever;
	private Path flirDummyFolder;
	private boolean printInfo;	
	private BackgroundSubtractorMOG2 mog = Video.createBackgroundSubtractorMOG2(MOG_HISTORY, MOG_THRESHOLD, true);
	
	private static final int MOG_THRESHOLD = 80;
	private static final int MOG_HISTORY = 50;
	
	private PreprocessingSettings ps = new PreprocessingSettings();

	public MainController() {
	}
	
	 @FXML 
	 public void initialize() {
		 bindSpinnersToSliders();
		 mogDefaults();
		 flirDataReciever = new FlirDataReciever(Config.getInstance().getValue(Config.SOCKET_HOSTNAME), Integer.parseInt(Config.getInstance().getValue(Config.SOCKET_PORT)), IMAGE_WIDTH*IMAGE_HEIGHT*2, playbackSpeedSpinner.getValue().intValue());
		 playbackSpeedSpinner.valueProperty().addListener((obs, oldValue, newValue) -> { 
			 flirDataReciever.setPlaybackSpeed(newValue.intValue());
		 });
	 }
	 
	 @FXML 
	 public void loadFilesFromFolderClicked(ActionEvent event) throws IOException {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(new File("D:\\ThesisProjectImages\\"));
		File file = directoryChooser.showDialog(mainBorderPane.getScene().getWindow());
		if (file != null) {
			flirDummyFolder = file.toPath();
			flirDataReciever.initDummyHost(flirDummyFolder);
			readStream(event);			
		}
	 }
	 
	 @FXML 
	 public void loadPreprocesSettingsClicked(ActionEvent event) {
		 if (flirDummyFolder == null) return;
		 ps = SettingsManager.loadPreproc(flirDummyFolder.toAbsolutePath().toString());
		 bindPreprocessSettings();
	 }

	private void bindPreprocessSettings() {
		 DoubleProperty.doubleProperty(minTempSpinner.getValueFactory().valueProperty()).bindBidirectional(ps.getTempMinProp());
		 DoubleProperty.doubleProperty(maxTempSpinner.getValueFactory().valueProperty()).bindBidirectional(ps.getTempMaxProp());
//		 BooleanProperty.booleanProperty().bindBidirectional(ps.IsScaleProp());
		 DoubleProperty.doubleProperty(clache1Spinner.getValueFactory().valueProperty()).bindBidirectional(ps.getClacheParam1Prop());
		 DoubleProperty.doubleProperty(clache2Spinner.getValueFactory().valueProperty()).bindBidirectional(ps.getClacheParam2Prop());
		 DoubleProperty.doubleProperty(clacheClipSpinner.getValueFactory().valueProperty()).bindBidirectional(ps.getClacheParam3Prop());		 
		 DoubleProperty.doubleProperty(brightnessSpinner.getValueFactory().valueProperty()).bindBidirectional(ps.getBrightnessParam1Prop());
		 DoubleProperty.doubleProperty(addSpinner.getValueFactory().valueProperty()).bindBidirectional(ps.getBrightnessParam2Prop());
		 DoubleProperty.doubleProperty(contrastSpinner.getValueFactory().valueProperty()).bindBidirectional(ps.getContrastParam1Prop());
		 DoubleProperty.doubleProperty(multSpinner.getValueFactory().valueProperty()).bindBidirectional(ps.getContrastParam2Prop());		 
//		 IntegerProperty.integerProperty().bindBidirectional(ps.getMedianProp());
//		 IntegerProperty.integerProperty().bindBidirectional(ps.getBilateralParam1Prop());
//		 IntegerProperty.integerProperty().bindBidirectional(ps.getBilateralParam2Prop());
//		 IntegerProperty.integerProperty().bindBidirectional(ps.getBilateralSigmaProp());

	}
	 
	 @FXML 
	 public void storePreprocesSettingsClicked(ActionEvent event) {
		 if (flirDummyFolder == null) return;
		 try {
			SettingsManager.storePreproc(new PreprocessingSettings(minTempDoubleProperty.doubleValue(), maxTempDoubleProperty.doubleValue(), scaleTempCheckbox.isSelected(), 
					 clache1Spinner.getValue(), clache2Spinner.getValue(), clacheClipSpinner.getValue(), brightnessSpinner.getValue(), addSpinner.getValue(),
					 contrastSpinner.getValue(), multSpinner.getValue(), 10, 20, 20, 20),
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
				byte [] byteArray = newValue.getData();
		 	    Mat originalMat, scaledMat, mainMat, workMat;		     

				originalMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, false, minTempSpinner.getValue().floatValue() , maxTempSpinner.getValue().floatValue(), -1);
				if (scaleTempCheckbox.isSelected())
					scaledMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, scaleTempCheckbox.isSelected(), minTempSpinner.getValue().floatValue() , maxTempSpinner.getValue().floatValue(), -1);
				else 
					scaledMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, true, ImageConvertor.bytesToCelsius(ImageConvertor.getMin(byteArray)) , ImageConvertor.bytesToCelsius(ImageConvertor.getMax(byteArray)), -1);

				mainMat = processMainMat(scaledMat);			
				workMat = preprocessMat(scaledMat);				
				
//				detectUsingMog(mainMat, workMat);
				detectUsingEdges(workMat);


				Utils.updateFXControl(mainImageView.imageProperty(), ImageConvertor.convertMatToImage(mainMat));
				Utils.updateFXControl(originalImageView.imageProperty(), ImageConvertor.convertMatToImage(originalMat));
				Utils.updateFXControl(histogramImageView.imageProperty(), ImageConvertor.convertMatToImage(MatOperations.createHistogram(workMat)));
				Utils.updateFXControl(originalCroppedImageView.imageProperty(), ImageConvertor.convertMatToImage(originalMat));
				
				refreshBackground(mainMat, printInfo);
				if(printInfo && !AlgHelper.isBackgroundOnly(workMat)) System.out.print(newValue.getFilename() + ";" );				
			});
			
			fds.messageProperty().addListener((obs, oldValue, newValue) -> { 
				streamStatusLabel.setText(newValue);
				});
			
			fds.start();
		}		
	}
	
	
	private void detectUsingEdges(Mat workMat) {
		Mat handMat = AlgHelper.segmentHandBinary(workMat, handThresholdSlider.getValue());
		List <MatOfPoint> contours = MatOperations.findContours(handMat, contourMinSizeSpinner.getValue());
		MatOfPoint biggest = MatOperations.findBiggestContour(contours);
		Mat edgesMat = new Mat(workMat.size(), workMat.type(), Scalar.all(0));
		
		if (contours.size() > 0 && Imgproc.contourArea(biggest) > 100) {
			Rect roiRect = MatOperations.findExtendedRegion(handMat);
	
			if (roiRect.width != 0 && roiRect.height != 0)
				edgesMat = segmentGoodsUsingEdges(handMat, workMat, roiRect);
			
//			Imgproc.rectangle(handMat, new Point(roiRect.x, roiRect.y), new Point(roiRect.x + roiRect.width, roiRect.y + roiRect.height), Scalar.all(255));

		}		

		Utils.updateFXControl(goodsImageView.imageProperty(), ImageConvertor.convertMatToImage(edgesMat));
		Utils.updateFXControl(handImageView.imageProperty(), ImageConvertor.convertMatToImage(handMat));	
		Utils.updateFXControl(helperImageView.imageProperty(), ImageConvertor.convertMatToImage(workMat));	
	}
	
	private void detectUsingMog(Mat mainMat, Mat workMat) {

		Mat handMat = mainMat.clone();		
		Mat fgmask = new Mat();
		mog.setVarThreshold(80);
		mog.apply(handMat, fgmask, 0);
		
		fgmask = MatOperations.maskMat(workMat, fgmask);
		
		Mat segmentedGoods = Mat.zeros(fgmask.size(), fgmask.type()); 
		Mat segmentedHandSmall = Mat.zeros(fgmask.size(), fgmask.type()); 
		Mat segmentedHandFull = fgmask.clone();
		List <MatOfPoint> contours = MatOperations.findContours(fgmask, 0);
		Rect rect = Imgproc.boundingRect(MatOperations.findBiggestContour(contours));
		if (rect.width > 0 && rect.height > 0) {
			segmentedHandSmall = segmentedHandFull.submat(rect);
			Mat first = new Mat(segmentedHandSmall.size(), segmentedHandSmall.type());
			Mat second = new Mat(segmentedHandSmall.size(), segmentedHandSmall.type());
			Imgproc.threshold(segmentedHandSmall, first, 220 , 255, Imgproc.THRESH_BINARY);
			Imgproc.adaptiveThreshold(segmentedHandSmall, second, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,1555, -210);
			Core.bitwise_or(first, second, segmentedHandSmall);
			Imgproc.dilate(segmentedHandSmall, segmentedHandSmall, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(9,9)));
		}
		
		Mat segmentedHand = MatOperations.invert(segmentedHandFull);
		fgmask.copyTo(segmentedGoods, segmentedHand);	
		
//		segmentedGoods = MatOperations.erode(segmentedGoods, 5, 5);
//		segmentedGoods = MatOperations.morphology(segmentedGoods, true, false, 5, 4, 5);
		
		
		if (!AlgHelper.isHandInShelf(fgmask)) {
			contours = MatOperations.findContours(segmentedHandSmall, 0);
			Point [] contoursPoints = new Point[0];
			for (int i = 0; i < contours.size(); ++i) contoursPoints = Stream.concat(Arrays.stream(contoursPoints), Arrays.stream(contours.get(i).toArray())).toArray(Point[]::new);				
			MatOfPoint joinedContours = new MatOfPoint(contoursPoints);
			if (!joinedContours.empty()) {
				MatOfPoint aprox = MatOperations.aproxCurve(joinedContours, 0.5, false);
				if (!aprox.empty()) { 
					MatOfInt convexHull2 = new MatOfInt();
					Imgproc.convexHull(aprox, convexHull2);
					if (convexHull2.toArray().length > 2) {
						MatOfInt4 convexDef = MatOperations.convexityDefects(aprox, convexHull2);
						List <Integer>cdList = convexDef.toList(); 
						Point data[] = aprox.toArray();

						Point biggestDefect = null, biggestStart = null, biggestEnd = null;
						int biggestSize = -1;
					    for (int j = 0; j < cdList.size(); j = j+4) {
					        Point start = data[cdList.get(j)];
					        Point end = data[cdList.get(j+1)];
					        Point defect = data[cdList.get(j+2)];
					        int depth = cdList.get(j+3)/256;
				            if (biggestSize < depth) {
				            	biggestSize = depth;
				            	biggestDefect = defect;
				            	biggestStart = start;
				            	biggestEnd = end;
				            }
					    }
					    if (cdList.size() > 0 && biggestSize > 20 ) {
					        Imgproc.circle(segmentedHandSmall, biggestStart, 5, Scalar.all(255), 2);
					        Imgproc.circle(segmentedHandSmall, biggestEnd, 5, Scalar.all(255), 2);
					        Imgproc.circle(segmentedHandSmall, biggestDefect, 5, Scalar.all(255), 2);
					        
					        Point p1 = new Point(), p2 = new Point(), p3 = new Point(), p4 = new Point();
					        int length = 150;
						        double angle = Math.atan2(biggestStart.y - biggestEnd.y, biggestStart.x - biggestEnd.x);
			        
					        p1.x = Math.round(biggestEnd.x + length * Math.cos(angle));
					        p1.y = Math.round(biggestEnd.y + length * Math.sin(angle));
					        p2.x = Math.round(biggestStart.x + -length * Math.cos(angle));
					        p2.y = Math.round(biggestStart.y + -length * Math.sin(angle));
					        
					        p3.x = Math.round(biggestDefect.x + -length * Math.cos(angle));
					        p3.y = Math.round(biggestDefect.y + -length * Math.sin(angle));
					        p4.x = Math.round(biggestDefect.x + length * Math.cos(angle));
					        p4.y = Math.round(biggestDefect.y + length * Math.sin(angle));
					        
					        p1.x += rect.x; p1.y += rect.y;p2.x += rect.x;p2.y += rect.y; 
					        p3.x += rect.x;p3.y += rect.y;p4.x += rect.x;p4.y += rect.y;
					        biggestStart.x += rect.x; biggestStart.y += rect.y;
					        biggestEnd.x += rect.x; biggestEnd.y += rect.y;
			                
					        Imgproc.line(segmentedGoods, p3, p4, Scalar.all(0),3);
					        Imgproc.line(segmentedGoods, p1, biggestStart, Scalar.all(0),3);
					        Imgproc.line(segmentedGoods, p2, biggestEnd, Scalar.all(0),3);
					    }
					}
				}
			}
			
			segmentedGoods = MatOperations.erode(segmentedGoods, 3, 2);
			
			contours = MatOperations.findContours(segmentedGoods, 0);
			List <MatOfPoint> filteredContours = new ArrayList<>();
			for (MatOfPoint mop : contours) {
				if (mop.toArray().length < 5) continue;
				RotatedRect rotRect = Imgproc.fitEllipse(new MatOfPoint2f(mop.toArray()));
				if (rotRect.size.width < 20 || rotRect.size.height < 20) continue;
				Point [] points = new Point [4];
				rotRect.points(points);
				MatOperations.drawMinBoundingRect(segmentedGoods, points);
				filteredContours.add(mop);
			}
			if(printInfo && !AlgHelper.isBackgroundOnly(workMat)) {
//				goodsContourFeatures(filteredContours);
//				System.out.println("Hand with goods" );
			}
		}

		Utils.updateFXControl(helperImageView.imageProperty(), ImageConvertor.convertMatToImage(fgmask));			
		Utils.updateFXControl(handImageView.imageProperty(), ImageConvertor.convertMatToImage(segmentedHandFull));
		Utils.updateFXControl(goodsImageView.imageProperty(), ImageConvertor.convertMatToImage(segmentedGoods));			
	}
	
	private void refreshBackground(Mat mat, boolean printInfo) {
		if (AlgHelper.isBackgroundOnly(mat)) {
			if (printInfo) System.out.println("background");
			mog = Video.createBackgroundSubtractorMOG2(51, 200, true);
			mog.apply(mat, new Mat());
		}
	}
	

		
	private Mat hullHandWithGoodsRegion(Mat mat, Mat untouched) {
		Mat result = mat.clone();
		Mat workmat = untouched.clone();
		workmat = preprocessMat(workmat);
		Mat handRegionPreprocessed = workmat.clone();

		workmat = MatOperations.doCannyEdgeDetection(workmat, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
		workmat = MatOperations.dilate(workmat, dilateSpinner.getValue(), 1); //try to link them	
		List<MatOfPoint> contours = MatOperations.findContours(workmat, contourMinSizeSpinner.getValue());
		MatOfPoint hullPoints  = MatOperations.convexHull(workmat, contours);
		RotatedRect rotRect = Imgproc.minAreaRect(new MatOfPoint2f(hullPoints.toArray()));
//		if (print)
//		System.out.print(Imgproc.contourArea(hullPoints) + ";" + 
//		Imgproc.arcLength(new MatOfPoint2f(hullPoints.toArray()), true) + ";" +
//		rotRect.size.area() + ";" );
		Imgproc.drawContours(handRegionPreprocessed, Arrays.asList(hullPoints), -1, Scalar.all(255), 2);        
		
		if (!handRegionPreprocessed.equals(workmat)) result = handRegionPreprocessed;
		return result;
	}	
		
	private Mat hullGoodsRegion(Mat mat, Mat untouched) {
		Mat result = mat.clone();
		Mat handRegionPreprocessed = mat.clone();
		Mat workmat = untouched.clone();
		workmat = preprocessMat(workmat);
		Mat withoutHand = new Mat(workmat.size(), workmat.type()); 

		workmat = MatOperations.doCannyEdgeDetection(workmat, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
//		workmat = MatOperations.morphology(workmat, morphOpenCheckbox.isSelected(), morphCloseCheckbox.isSelected(), erodeSpinner.getValue(), dilateSpinner.getValue(), morphIterSpinner.getValue());
		List <MatOfPoint> contours = MatOperations.findContours(workmat, contourMinSizeSpinner.getValue()); //find contours + filter them
		MatOfPoint hullPoints = MatOperations.convexHull(workmat, contours); //draw convex hull mask
		Mat convexHull = new Mat(workmat.size(),workmat.type(), Scalar.all(0));
		Imgproc.drawContours(convexHull, Arrays.asList(hullPoints), -1, Scalar.all(255), Core.FILLED);        

		Mat segmentedHand = AlgHelper.segmentHandBinary(handRegionPreprocessed, 220); //find hand mask
		
		Core.subtract(convexHull, segmentedHand, withoutHand); //Mat without hand but with noise

		List <MatOfPoint> contours2 = MatOperations.findContours(withoutHand, 0);
		MatOfPoint mop = MatOperations.findBiggestContour(contours2); //find biggest contour - might be a goods only
		hullPoints = MatOperations.convexHull(withoutHand,Arrays.asList(mop));
		if (hullPoints == null) hullPoints = new MatOfPoint();
		RotatedRect rotRect = Imgproc.minAreaRect(new MatOfPoint2f(hullPoints.toArray()));
//		if (print)
//		System.out.print(Imgproc.contourArea(hullPoints) + ";" + 
//				Imgproc.arcLength(new MatOfPoint2f(hullPoints.toArray()), true) + ";" +
//				rotRect.size.area() + ";" );
		Imgproc.drawContours(handRegionPreprocessed, Arrays.asList(hullPoints), -1, Scalar.all(255), 2);        
		
		if (!withoutHand.equals(handRegionPreprocessed)) result = handRegionPreprocessed;		
		
		return result;
	}	
	
	private Mat segmentGoods(Mat original, Mat mat, double cannyThresh1, double cannyThresh2, boolean open, boolean close, double erode, double dilate, double handThreshold, double contourMinSize) {
		Mat result = new Mat(mat.size(), mat.type());
		Mat withoutHand = new Mat(mat.size(), mat.type()); 

		result = MatOperations.doCannyEdgeDetection(mat, cannyThresh1 , cannyThresh2); //detect edges
//		result = MatOperations.morphology(result, open, close, erode, dilate, morphIterSpinner.getValue());
//		result = MatOperations.morphClose(edges, 3, 5);

		
		List <MatOfPoint> contours = MatOperations.findContours(result, contourMinSize); //find contours + filter them
		MatOfPoint hullPoints = MatOperations.convexHull(result,contours); //draw convex hull mask
		Mat convexHull = new Mat(mat.size(),mat.type(), Scalar.all(0));
		Imgproc.drawContours(convexHull, Arrays.asList(hullPoints), -1, Scalar.all(255), Core.FILLED);        
		
		Mat segmentedHandMask = AlgHelper.segmentHandBinary(original, handThreshold); //find hand mask
		
		Core.subtract(convexHull, segmentedHandMask, withoutHand); //Mat without hand but with noise
		result = MatOperations.maskMat(original, withoutHand);  //masked original image - only goods + noise remains	
		Mat noiseGoods = result.clone();
		
		List <MatOfPoint> contours2 = MatOperations.findContours(noiseGoods, 0);
		MatOfPoint mop = MatOperations.findBiggestContour(contours2); //find biggest contour - might be a goods only
		hullPoints = MatOperations.convexHull(noiseGoods, Arrays.asList(mop));
		Imgproc.drawContours(result, Arrays.asList(hullPoints), -1, Scalar.all(255), Core.FILLED);        
		result = MatOperations.maskMat(noiseGoods, result); //mask biggest contour - only goods should remain				
		return result;
	}
	
	private Mat segmentGoods2(Mat workMat, Rect roiRect) {
		Mat roi = workMat.submat(roiRect);
		Mat edges = new Mat(roi.size(), roi.type());
		Mat handMask = AlgHelper.segmentHandBinary(roi, handThresholdSlider.getValue());
		
		handMask = MatOperations.morphOpen(handMask, 5, 10);
		
		handMask = MatOperations.invert(handMask);
		edges = MatOperations.doCannyEdgeDetection(roi, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
//		edges = MatOperations.dilate(edges, 3, 2);
//		edges = MatOperations.erode(edges, 6, 1);
		edges = MatOperations.morphClose(edges, 3, 5);

		Core.bitwise_and(handMask, edges, edges);	
//		edges = MatOperations.morphology(edges, false, true, erodeSpinner.getValue(), dilateSpinner.getValue(), morphIterSpinner.getValue());

		List<MatOfPoint> contours = MatOperations.findContours(edges, 20);
	    Imgproc.drawContours(roi, contours, -1, new Scalar(255, 0, 0), 2);
		AlgHelper.goodsContourFeatures(contours,printInfo);
		return workMat;
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
		List<MatOfPoint> contours = MatOperations.findContours(edges, 25);
	    Imgproc.drawContours(roi, contours, -1, new Scalar(255, 0, 0), 2);
		AlgHelper.goodsContourFeatures(contours,printInfo);
		return workMat;
	}	
		
	private Mat segmentHand2(Mat mat, int i, int j, int k, int l) {
		Mat result = new Mat(mat.size(), mat.type());
		Mat result2 =  new Mat(mat.size(), mat.type(), Scalar.all(0));
		Mat blurred = new Mat();
		Imgproc.medianBlur(mat, blurred, 25);		
		Imgproc.bilateralFilter(blurred, result, 6, 15, 15);
		result = MatOperations.doCannyEdgeDetection(result, 10,30); 
		result = MatOperations.dilate(result, 3, 10); 
		result = MatOperations.erode(result, 4, 15); 
		
		Mat firstRow = result.row(0);
		MatOfPoint mop = new MatOfPoint();
		Core.findNonZero(firstRow, mop);
		for (int a = 0; a < mop.toArray().length - 1; a++)
			Imgproc.line(result, mop.toArray()[a], mop.toArray()[a+1], Scalar.all(255),2);
		
		List<MatOfPoint> contours = MatOperations.findContours(result, 100);
		result2 = MatOperations.drawContours(contours, mat.width(), mat.height(), -1);
		result2 = MatOperations.dilate(result2, 3, 5); 
		return result2;
	}
	
	private Mat segmentHand3(Mat mat) {
		Mat result = new Mat(mat.size(),mat.type());
		Imgproc.medianBlur(mat, result, 1);
		Imgproc.adaptiveThreshold(result, result, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 3, 2);
//		Mat firstRow = result.row(0);
//		MatOfPoint mop = new MatOfPoint();
//		result = MatOperations.dilate(result, 4, 5); 
//		result = MatOperations.erode(result, 4, 5); 
//
//
//		Core.findNonZero(firstRow, mop);
//		for (int a = 0; a < mop.toArray().length - 1; a++)
//			Imgproc.line(result, mop.toArray()[a], mop.toArray()[a+1], Scalar.all(255),2);
//		
//		List<MatOfPoint> contours = MatOperations.findContours(result, 100);
//		result = MatOperations.drawContours(contours, mat.width(), mat.height(), -1);
//		result = MatOperations.dilate(result, 3, 5); 
//		result = MatOperations.dilate(result, 3, 10); 
//		result = MatOperations.erode(result, 4, 15); 
		return result;
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
		if (saveImagesCheckbox.isSelected()) flirDataReciever.setSaveImages(true);
		else flirDataReciever.setSaveImages(false);		
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
		if (cannyMainCheckbox.isSelected()) result = MatOperations.doCannyEdgeDetection(result, cannyEdge1Spinner.getValue(), cannyEdge2Spinner.getValue());
//		if (morphologyMainCheckbox.isSelected()) result = MatOperations.morphology(result, morphOpenCheckbox.isSelected(), morphCloseCheckbox.isSelected(), erodeSpinner.getValue(), dilateSpinner.getValue(), morphIterSpinner.getValue());
		
		if (hullHandGoodsMainCheckbox.isSelected())	result = hullHandWithGoodsRegion(result, untouched);
		if (hullGoodsMainCheckbox.isSelected()) result = hullGoodsRegion(result, untouched);
		
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
	 

	private void thermalCameraSegmentationDefaults() {
		saveImagesCheckbox.setSelected(false);
		playbackSpeedSpinner.getValueFactory().setValue(100.0);
		minTempSpinner.getValueFactory().setValue(29.0);
		maxTempSpinner.getValueFactory().setValue(33.0);
		scaleTempCheckbox.setSelected(true);
		clache1Spinner.getValueFactory().setValue(0.0); 
		clache2Spinner.getValueFactory().setValue(0.0); 
		clacheClipSpinner.getValueFactory().setValue(0.0); 
		brightnessSpinner.getValueFactory().setValue(-0.0); 
		addSpinner.getValueFactory().setValue(0.0); 
		contrastSpinner.getValueFactory().setValue(0.0); 
		multSpinner.getValueFactory().setValue(0.0);
		blur1Spinner.getValueFactory().setValue(6.0); 
		blur2Spinner.getValueFactory().setValue(6.0); 
		blurSigmaSpinner.getValueFactory().setValue(7.0);
		blurCheckbox.setSelected(true);
		cannyEdge1Spinner.getValueFactory().setValue(15.0); 
		cannyEdge2Spinner.getValueFactory().setValue(35.0);
		dilateSpinner.getValueFactory().setValue(9.0);
		erodeSpinner.getValueFactory().setValue(2.0);
		morphIterSpinner.getValueFactory().setValue(3.0);
		morphCloseCheckbox.setSelected(true);
		morphOpenCheckbox.setSelected(false);
		
		contourMinSizeSpinner.getValueFactory().setValue(0.0);
		
		handThresholdSpinner.getValueFactory().setValue(180.0);
		handDilationSpinner.getValueFactory().setValue(6.0);
		handIterSpinner.getValueFactory().setValue(4.0);

		
		//main preview
		exposureMainCheckbox.setSelected(false); 
		blurMainCheckbox.setSelected(false); 
		cannyMainCheckbox.setSelected(false);
		morphologyMainCheckbox.setSelected(false);
		hullHandGoodsMainCheckbox.setSelected(false);
		hullGoodsMainCheckbox.setSelected(false);
		roiMainCheckbox.setSelected(false);
	}
	
	private void mogDefaults() {
		saveImagesCheckbox.setSelected(false);
		playbackSpeedSpinner.getValueFactory().setValue(100.0);
		minTempSpinner.getValueFactory().setValue(28.0);
		maxTempSpinner.getValueFactory().setValue(38.0);
		scaleTempCheckbox.setSelected(true);
		clache1Spinner.getValueFactory().setValue(1.0); 
		clache2Spinner.getValueFactory().setValue(1.0); 
		clacheClipSpinner.getValueFactory().setValue(1.0); 
		brightnessSpinner.getValueFactory().setValue(0.0); 
		addSpinner.getValueFactory().setValue(-30.0); 
		contrastSpinner.getValueFactory().setValue(0.0); 
		multSpinner.getValueFactory().setValue(0.8);
		blur1Spinner.getValueFactory().setValue(3.0); 
		blur2Spinner.getValueFactory().setValue(3.0); 
		blurSigmaSpinner.getValueFactory().setValue(3.0);
		blurCheckbox.setSelected(true);
		cannyEdge1Spinner.getValueFactory().setValue(15.0); 
		cannyEdge2Spinner.getValueFactory().setValue(35.0);
		dilateSpinner.getValueFactory().setValue(9.0);
		erodeSpinner.getValueFactory().setValue(2.0);
		morphIterSpinner.getValueFactory().setValue(3.0);
		morphCloseCheckbox.setSelected(true);
		morphOpenCheckbox.setSelected(false);
		
		contourMinSizeSpinner.getValueFactory().setValue(0.0);
		
		handThresholdSpinner.getValueFactory().setValue(170.0);
		handDilationSpinner.getValueFactory().setValue(6.0);
		handIterSpinner.getValueFactory().setValue(4.0);

		
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


}
