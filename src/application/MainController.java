package application;

import java.io.File;
import java.io.IOException;
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
import utils.ImageConvertor;
import utils.Utils;


public class MainController {
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 150;
	public static final int IMAGE_CROPPED_WIDTH = 640;
	public static final int IMAGE_CROPPED_HEIGHT = 150;
	public static final int PANEL_IMAGE_WIDTH = 160;
	public static final int PANEL_IMAGE_HEIGHT = 128;
	public static final int PANEL_CROPPED_IMAGE_WIDTH = 160;
	public static final int PANEL_CROPPED_IMAGE_HEIGHT = 37;
	
	public static final int CROP_OFFSET_X = 0;
	public static final int CROP_OFFSET_Y = 0;

	@FXML private BorderPane mainBorderPane;
	//Image containers
	@FXML private ImageView mainImageView, mogImageView, handImageView, goodsImageView, originalImageView, histogramImageView, originalCroppedImageView;
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
	@FXML private MenuItem loadFromFolder;
	
	private DoubleProperty minTempDoubleProperty, maxTempDoubleProperty,  binaryThresholdDoubleProperty;  //prevents GC from cleaning weak listeners
	private FlirDataService fds;
	private FlirDataReciever flirDataReciever;
	private Path flirDummyFolder;
	private WebcamService wcs;
	private WebcamDataReciever webcamDataReciever;
	private boolean print;
	private BackgroundSubtractorMOG2 mog;
	public MainController() {
	}
	
	 @FXML 
	 public void initialize() {
		 bindSpinnersToSliders();
		 mogDefaults();
		 flirDataReciever = new FlirDataReciever(Config.getInstance().getValue(Config.SOCKET_HOSTNAME), Integer.parseInt(Config.getInstance().getValue(Config.SOCKET_PORT)), IMAGE_WIDTH*IMAGE_HEIGHT*2, playbackSpeedSpinner.getValue().intValue());
		 webcamDataReciever = new WebcamDataReciever(1, playbackSpeedSpinner.getValue().intValue());
		 playbackSpeedSpinner.valueProperty().addListener((obs, oldValue, newValue) -> { 
			 flirDataReciever.setPlaybackSpeed(newValue.intValue());
			 webcamDataReciever.setPlaybackSpeed(newValue.intValue());
		 });
		mog = Video.createBackgroundSubtractorMOG2(51, 200, true);
	 }
	 
	 @FXML 
	 public void loadFromFolderClicked(ActionEvent event) throws IOException {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(new File("D:\\ThesisProjectImages\\"));
		File file = directoryChooser.showDialog(mainBorderPane.getScene().getWindow());
		if (file != null) {
			flirDummyFolder = file.toPath();
			flirDataReciever.initDummyHost(flirDummyFolder);
			readStream(event);

		}
		mog = Video.createBackgroundSubtractorMOG2(51, 200, true);
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
				print = false;
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
//
//				Mat handMat = workCroppedMat.clone();				
//				handMat = segmentHand(handMat, handThresholdSlider.getValue(), handDilationSpinner.getValue(), handIterSpinner.getValue());
//				List <MatOfPoint> contours = MatOperations.findContours(handMat, contourMinSizeSpinner.getValue());
//				MatOfPoint biggest = MatOperations.findBiggestContour(contours);
				
//				if (contours.size() > 0 && Imgproc.contourArea(biggest) > 100) {
//					Mat croppedEdgesMat = workCroppedMat.clone();						
//					Rect rect = findExtendedHandRegion(handMat);
//					Imgproc.rectangle(handMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), Scalar.all(255));
//	
//					if (rect.width != 0 && rect.height != 0) {
//						croppedEdgesMat = segmentGoods2(croppedEdgesMat,rect);
//						Image goodsImage = ImageConvertor.convertMatToImage(croppedEdgesMat);
//						Utils.updateFXControl(goodsImageView.imageProperty(), goodsImage);			
//					}
//				}

				if (print) System.out.println(";" + newValue.getFilename());
				Mat handMat = new Mat(mainMat, new Rect(CROP_OFFSET_X, CROP_OFFSET_Y, IMAGE_CROPPED_WIDTH, IMAGE_CROPPED_HEIGHT));				
				Mat fgmask = new Mat();
				mog.setVarThreshold(40);
				mog.apply(handMat, fgmask, 0);
				
				fgmask = MatOperations.maskMat(workCroppedMat, fgmask);
				
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
				}
				
				Mat segmentedHand = MatOperations.invert(segmentedHandFull);
				fgmask.copyTo(segmentedGoods, segmentedHand);	
				
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
						    if (cdList.size() > 0) {
						        Imgproc.circle(segmentedHandSmall, biggestStart, 5, Scalar.all(255), 2);
						        Imgproc.circle(segmentedHandSmall, biggestEnd, 5, Scalar.all(255), 2);
						        Imgproc.circle(segmentedHandSmall, biggestDefect, 5, Scalar.all(255), 2);
						        MatOfPoint2f mop = new MatOfPoint2f(biggestStart, biggestEnd, biggestDefect);
//						        Imgproc.rectangle(segmentedHandSmall, biggestDefect, biggestEnd, Scalar.all(255));
//						        RotatedRect rotRect = Imgproc.minAreaRect(mop);
//						        Point [] points = new Point[4];
//						        rotRect.points(points);
//						        MatOperations.drawMinBoundingRect(segmentedHandSmall, points);
						    }
						}
					}
				}
				
				segmentedGoods = MatOperations.erode(segmentedGoods, 5, 5);
				segmentedGoods = MatOperations.morphology(segmentedGoods, true, false, 5, 6, 5);
//
				//center
				Image mainImage = ImageConvertor.convertMatToImage(mainMat);
				Image mogImage = ImageConvertor.convertMatToImage(fgmask);
				Image handImage = ImageConvertor.convertMatToImage(segmentedHandFull);
				Image goodsImage = ImageConvertor.convertMatToImage(segmentedGoods);
				Utils.updateFXControl(mainImageView.imageProperty(), mainImage);
				Utils.updateFXControl(mogImageView.imageProperty(), mogImage);			
				Utils.updateFXControl(handImageView.imageProperty(), handImage);
				Utils.updateFXControl(goodsImageView.imageProperty(), goodsImage);			

//				//panel
				Image originalImage = ImageConvertor.convertMatToImage(originalMat);
				Image histogramImage = ImageConvertor.convertMatToImage(MatOperations.createHistogram(workMat));	
				Image originalCroppedImage = ImageConvertor.convertMatToImage(originalCroppedMat);
				Utils.updateFXControl(originalImageView.imageProperty(), originalImage);
				Utils.updateFXControl(histogramImageView.imageProperty(), histogramImage);
				Utils.updateFXControl(originalCroppedImageView.imageProperty(), originalCroppedImage);
				
				refreshBackground(workCroppedMat);
			});
			fds.messageProperty().addListener((obs, oldValue, newValue) -> { 
				streamStatusLabel.setText(newValue);
				});
			fds.start();
		}		
	}
	
	private void refreshBackground(Mat mat) {
		Mat items = MatOperations.doCannyEdgeDetection(mat, 20, 40);
		List <MatOfPoint> contours = MatOperations.findContours(items, 25);	
		if (contours.size() == 0) {
//			System.out.println("background");
			mog = Video.createBackgroundSubtractorMOG2(51, 200, true);
			mog.apply(mat, new Mat());
		}
	}
	
	private Mat drawRegionOfInterestRect(Mat mat, Mat preprocessed) {
		Mat result = mat.clone();		
		Mat workmat = preprocessed.submat(CROP_OFFSET_Y, CROP_OFFSET_Y + IMAGE_CROPPED_HEIGHT, CROP_OFFSET_X, CROP_OFFSET_X +  IMAGE_CROPPED_WIDTH);
		Mat smallResult = mat.submat(CROP_OFFSET_Y, CROP_OFFSET_Y + IMAGE_CROPPED_HEIGHT, CROP_OFFSET_X, CROP_OFFSET_X +  IMAGE_CROPPED_WIDTH);
		workmat = segmentHand(workmat, handThresholdSpinner.getValue(), handDilationSpinner.getValue(), handIterSpinner.getValue());
		Rect rect = MatOperations.findExtendedRegion(workmat);
		Imgproc.rectangle(smallResult, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), Scalar.all(255), 2 );

		MatOperations.replaceMatArea(result, smallResult, CROP_OFFSET_X, CROP_OFFSET_Y);
		
		return result;
	}
	
	private Mat hullHandWithGoodsRegion(Mat mat, Mat untouched) {
		Mat result = mat.clone();
		Mat workmat = untouched.submat(CROP_OFFSET_Y, CROP_OFFSET_Y + IMAGE_CROPPED_HEIGHT, CROP_OFFSET_X, CROP_OFFSET_X +  IMAGE_CROPPED_WIDTH);
		workmat = preprocessMat(workmat);
		Mat handRegionPreprocessed = workmat.clone();

		workmat = MatOperations.doCannyEdgeDetection(workmat, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
		workmat = MatOperations.dilate(workmat, dilateSpinner.getValue(), 1); //try to link them	
		List<MatOfPoint> contours = MatOperations.findContours(workmat, contourMinSizeSpinner.getValue());
		MatOfPoint hullPoints  = MatOperations.convexHull(workmat, contours);
		RotatedRect rotRect = Imgproc.minAreaRect(new MatOfPoint2f(hullPoints.toArray()));
		if (print)
		System.out.print(Imgproc.contourArea(hullPoints) + ";" + 
		Imgproc.arcLength(new MatOfPoint2f(hullPoints.toArray()), true) + ";" +
		rotRect.size.area() + ";" );
		Imgproc.drawContours(handRegionPreprocessed, Arrays.asList(hullPoints), -1, Scalar.all(255), 2);        
		
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
		MatOfPoint hullPoints = MatOperations.convexHull(workmat, contours); //draw convex hull mask
		Mat convexHull = new Mat(workmat.size(),workmat.type(), Scalar.all(0));
		Imgproc.drawContours(convexHull, Arrays.asList(hullPoints), -1, Scalar.all(255), Core.FILLED);        

		Mat segmentedHand = segmentHand(handRegionPreprocessed, 220, 4, 3); //find hand mask
		
		Core.subtract(convexHull, segmentedHand, withoutHand); //Mat without hand but with noise

		List <MatOfPoint> contours2 = MatOperations.findContours(withoutHand, 0);
		MatOfPoint mop = MatOperations.findBiggestContour(contours2); //find biggest contour - might be a goods only
		hullPoints = MatOperations.convexHull(withoutHand,Arrays.asList(mop));
		if (hullPoints == null) hullPoints = new MatOfPoint();
		RotatedRect rotRect = Imgproc.minAreaRect(new MatOfPoint2f(hullPoints.toArray()));
		if (print)
		System.out.print(Imgproc.contourArea(hullPoints) + ";" + 
				Imgproc.arcLength(new MatOfPoint2f(hullPoints.toArray()), true) + ";" +
				rotRect.size.area() + ";" );
		Imgproc.drawContours(handRegionPreprocessed, Arrays.asList(hullPoints), -1, Scalar.all(255), 2);        
		
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
		MatOfPoint hullPoints = MatOperations.convexHull(result,contours); //draw convex hull mask
		Mat convexHull = new Mat(mat.size(),mat.type(), Scalar.all(0));
		Imgproc.drawContours(convexHull, Arrays.asList(hullPoints), -1, Scalar.all(255), Core.FILLED);        
		
		Mat segmentedHandMask = segmentHand(original, handThreshold, handDilationSpinner.getValue(), handIterSpinner.getValue()); //find hand mask
		
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
	
	private Mat segmentGoods2(Mat mat, Rect rect) {
		Mat roi = mat.submat(rect);
		Mat edges = new Mat(roi.size(), roi.type());
		Mat handMask = segmentHand(roi, handThresholdSlider.getValue(), handDilationSpinner.getValue(), handIterSpinner.getValue());
		
		handMask = MatOperations.invert(handMask);
		edges = MatOperations.doCannyEdgeDetection(roi, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
//		edges = MatOperations.dilate(edges, 3, 2);
//		edges = MatOperations.erode(edges, 6, 1);
		edges = MatOperations.morphology(edges, false, true, 3, 3, 5);

		Core.bitwise_and(handMask, edges, edges);	
//		edges = MatOperations.morphology(edges, false, true, erodeSpinner.getValue(), dilateSpinner.getValue(), morphIterSpinner.getValue());

		List<MatOfPoint> contours = MatOperations.findContours(edges, 20);
	    Imgproc.drawContours(roi, contours, -1, new Scalar(255, 0, 0), 2);
		goodsContourFeatures(contours);
		return mat;
	}	
	
	private Mat segmentHand(Mat mat, double threshold, double dilation, double iterations) {
		Mat result = new Mat(mat.size(), mat.type());	    
//		result = MatOperations.dilate(mat, dilation, (int) iterations);		
		result =  MatOperations.binaryTreshold(mat, threshold);
//		result = MatOperations.erode(result, dilation, (int) iterations);
//		result = MatOperations.otsuThreshold(result, threshold);
		return result;
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
	
	private void goodsContourFeatures(List <MatOfPoint> mopList) {
		if (!print) return;
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
		System.out.print(";" + flirDummyFolder.getFileName().toString());
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
		if (roiMainCheckbox.isSelected()) result = drawRegionOfInterestRect(result, preprocessed);
		
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
