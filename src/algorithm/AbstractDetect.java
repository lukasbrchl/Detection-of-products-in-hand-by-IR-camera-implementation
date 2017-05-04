package algorithm;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import algorithm.domain.DetectionResult;
import algorithm.settings.domain.PreprocessingSettings;
import algorithm.settings.domain.PreviewSettings;
import algorithm.settings.domain.SettingsWrapper;
import application.MainController;
import data.reciever.domain.ImageData;
import image.MatOperations;
import image.domain.Contour;

public abstract class AbstractDetect {

	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 150;
	public static final int PANEL_IMAGE_WIDTH = 160;
	public static final int PANEL_IMAGE_HEIGHT = 37;

	protected Mat originalMat, scaledMat, previewMat;
	protected Mat workMat, handMat, goodsMat;
	protected SettingsWrapper settings;
	protected boolean printInfo;
	protected ImageData data;
	protected DetectionResult result;
	
	public AbstractDetect(SettingsWrapper settings) {
		this.settings = settings;
	}
	
	public abstract void detect();

	//mats
	public void initMats(ImageData data) {
		this.data = data;
		byte [] byteArray = data.getData();
		originalMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, false);
		if ( ((PreprocessingSettings) settings.getByCls(PreprocessingSettings.class)).isScale())
			scaledMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, ((PreprocessingSettings) settings.getByCls(PreprocessingSettings.class)).getTempMin(), ((PreprocessingSettings) settings.getByCls(PreprocessingSettings.class)).getTempMax());
		else
			scaledMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, true);

		previewMat = preprocessPreviewMat(scaledMat);
		workMat = preprocessWorkMat(scaledMat);
		handMat = goodsMat = Mat.zeros(originalMat.size(), originalMat.type());
	}
	
	public Mat preprocessPreviewMat(Mat mat) {
		PreviewSettings pws = ((PreviewSettings) settings.getByCls(PreviewSettings.class));
		PreprocessingSettings pgs = ((PreprocessingSettings) settings.getByCls(PreprocessingSettings.class));
		Mat result = mat.clone();		
		if (pws.isExposure()) {
			result = MatOperations.clache(result, pgs.getClacheSize1(), pgs.getClacheSize2(), pgs.getClacheClip());		
			result = MatOperations.brightnessContrast(result, pgs.getBrightnessParam1(), pgs.getContrastParam1());
			result = MatOperations.addMult(result, pgs.getBrightnessParam2(), pgs.getContrastParam1());
		}
		if (pws.isBlur()) {
			result = MatOperations.medianBlur(result, pgs.getMedianSize());
			result = MatOperations.bilateralBlur(result, pgs.getBilateralSize1(), pgs.getBilateralSize2(), pgs.getBilateralSigma());
			result = MatOperations.gaussianBlur(result, pgs.getGaussianSize1(), pgs.getGaussianSize2(), pgs.getGaussianSigma());
		}
		if (pws.isCanny()) result = MatOperations.doCannyEdgeDetection(result, 20, 40); 
		//TODO
//		if (morphologyMainCheckbox.isSelected()) result = MatOperations.morphology(result, morphOpenCheckbox.isSelected(), morphCloseCheckbox.isSelected(), erodeSpinner.getValue(), dilateSpinner.getValue(), morphIterSpinner.getValue());		
//		if (hullHandGoodsMainCheckbox.isSelected())	result = hullHandWithGoodsRegion(result, untouched);
//		if (hullGoodsMainCheckbox.isSelected()) result = hullGoodsRegion(result, untouched);		
		return result; 
	}
	
	public Mat preprocessWorkMat(Mat mat) {
		PreprocessingSettings pgs = ((PreprocessingSettings) settings.getByCls(PreprocessingSettings.class));
		Mat result = mat.clone();		
		result =  MatOperations.clache(result, pgs.getClacheSize1(), pgs.getClacheSize2(), pgs.getClacheClip());
		result = MatOperations.brightnessContrast(result, pgs.getBrightnessParam1(), pgs.getContrastParam1());
		result = MatOperations.addMult(result, pgs.getBrightnessParam2(), pgs.getContrastParam1());
		result = MatOperations.medianBlur(result, pgs.getMedianSize());
		result = MatOperations.bilateralBlur(result, pgs.getBilateralSize1(), pgs.getBilateralSize2(), pgs.getBilateralSigma());
		result = MatOperations.gaussianBlur(result, pgs.getGaussianSize1(), pgs.getGaussianSize2(), pgs.getGaussianSigma());
		return result;
	}		
	
	public Mat segmentHandBinary(Mat mat, double threshold) {
		return segmentHandBinary(mat, threshold, 1);
	}
	
	public Mat segmentHandBinary(Mat mat, double threshold, int dilateSize) {
		Mat result = new Mat(mat.size(), mat.type());	    
		result =  MatOperations.binaryTreshold(mat, 220);
		Imgproc.dilate(result, result, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(dilateSize,dilateSize)));
		return result;
	}
	
	public boolean isBackgroundOnly(Mat mat) {		
		Mat items = MatOperations.doCannyEdgeDetection(mat, 30, 50);
		List <MatOfPoint> contoursCanny = MatOperations.findContours(items, 5, 5);	
		return contoursCanny.size() == 0 && !isHandInView(mat);
	}
	
	public boolean isHandInView(Mat mat) {
		Mat something = segmentHandBinary(mat, 200);
		List <MatOfPoint> contoursThreshold = MatOperations.findContours(something, 5, 5);	
		return !(contoursThreshold.size() == 0);
	}
	
	public boolean isHandInShelf(Mat mat) {
		Mat segmentedHand = segmentHandBinary(mat, 220);
		segmentedHand = MatOperations.erode(segmentedHand, 25, 10);
		MatOfPoint mop = new MatOfPoint();
		Core.findNonZero(segmentedHand, mop);
		if (Imgproc.boundingRect(mop).height >= IMAGE_HEIGHT) return true;
		return false;
	}
	
	public void goodsContourFeatures(List <MatOfPoint> contours, boolean printInfo) {
		if (!printInfo) return;
		MatOfPoint biggest = MatOperations.findBiggestContour(contours);
		//biggest
		float length = 0, area = 0, minDiameter = 0, maxDiameter = 0, convexLength = 0, convexArea = 0, formFactor = 0,
				roundness = 0, aspectRatio = 0, convexity = 0, solidity = 0, compactness = 0, extent = 0;	
		if (biggest != null && biggest.toArray().length>0) {
			Contour contour = new Contour(biggest);
			length = (float) contour.getLength();
			area = (float) contour.getArea();
			minDiameter = (float) contour.getMinDiameter();
			maxDiameter = (float) contour.getMaxDiameter();
			convexLength = (float) contour.getConvexLength();
			convexArea = (float) contour.getConvexArea();
			formFactor = (float) contour.getFormFactor();
			roundness = (float) contour.getRoundness();
			aspectRatio = (float) contour.getAspectRatio();
			convexity = (float) contour.getConvexity();
			solidity = (float) contour.getSolidity();
			compactness = (float) contour.getCompactness();
			extent = (float) contour.getExtent();
		}
		System.out.print(data.getFilename() + ";");
		System.out.print(length + ";" +  area + ";" + minDiameter + ";" + maxDiameter + ";" + convexLength + ";" + convexArea + ";" + formFactor
				+ ";" + roundness + ";" + aspectRatio + ";" + convexity + ";" + solidity + ";" + compactness + ";" + extent + ";");
		
		//contours overall
		int contoursCount = contours.size();
		float areaSum=0, areaMin = Float.MAX_VALUE, areaMax = 0, areaAvg = 0;
		float lengthSum=0, lengthMin = Float.MAX_VALUE, lengthMax = 0, lengthAvg = 0;
		for (MatOfPoint mop : contours) {
			area = (float) Imgproc.contourArea(mop);
			length = (float) Imgproc.arcLength(new MatOfPoint2f(mop.toArray()), false);
			areaSum += area;
			lengthSum += length;
			if (areaMin > area) areaMin = area;
			if (areaMax < area) areaMax = area;
			if (lengthMin > length) lengthMin = length;
			if (lengthMax < length) lengthMax = length;
		}
		areaAvg = contoursCount != 0 ? areaSum/contoursCount : 0 ;
		lengthAvg = contoursCount != 0 ? lengthSum/contoursCount : 0;
		System.out.print(contoursCount + ";" +  lengthMin + ";" + lengthMax + ";" + lengthSum + ";" + lengthAvg + ";" + areaMin + ";" + areaMax + ";" + areaSum + ";" + areaAvg + ";");
//		System.out.print("empty_hand");
		System.out.print("hand_with_goods");
		System.out.println();
	}
	
	
	
	
	
	
	
	
	
	
	//main mat helper drawers
		private Mat hullHandWithGoodsRegion(Mat mat, Mat untouched) {
			Mat result = mat.clone();
//			Mat workmat = untouched.clone();
//			workmat = preprocessMat(workmat);
//			Mat handRegionPreprocessed = workmat.clone();
	//
//			workmat = MatOperations.doCannyEdgeDetection(workmat, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
//			workmat = MatOperations.dilate(workmat, dilateSpinner.getValue(), 1); //try to link them	
//			List<MatOfPoint> contours = MatOperations.findContours(workmat, contourMinSizeSpinner.getValue());
//			MatOfPoint hullPoints  = MatOperations.convexHull(workmat, contours);
//			RotatedRect rotRect = Imgproc.minAreaRect(new MatOfPoint2f(hullPoints.toArray()));
////			if (print)
////			System.out.print(Imgproc.contourArea(hullPoints) + ";" + 
////			Imgproc.arcLength(new MatOfPoint2f(hullPoints.toArray()), true) + ";" +
////			rotRect.size.area() + ";" );
//			Imgproc.drawContours(handRegionPreprocessed, Arrays.asList(hullPoints), -1, Scalar.all(255), 2);        
//			
//			if (!handRegionPreprocessed.equals(workmat)) result = handRegionPreprocessed;
			return result;
		}	
			
		private Mat hullGoodsRegion(Mat mat, Mat untouched) {
			Mat result = mat.clone();
//			Mat handRegionPreprocessed = mat.clone();
//			Mat workmat = untouched.clone();
//			workmat = preprocessMat(workmat);
//			Mat withoutHand = new Mat(workmat.size(), workmat.type()); 
	//
//			workmat = MatOperations.doCannyEdgeDetection(workmat, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
////			workmat = MatOperations.morphology(workmat, morphOpenCheckbox.isSelected(), morphCloseCheckbox.isSelected(), erodeSpinner.getValue(), dilateSpinner.getValue(), morphIterSpinner.getValue());
//			List <MatOfPoint> contours = MatOperations.findContours(workmat, contourMinSizeSpinner.getValue()); //find contours + filter them
//			MatOfPoint hullPoints = MatOperations.convexHull(workmat, contours); //draw convex hull mask
//			Mat convexHull = new Mat(workmat.size(),workmat.type(), Scalar.all(0));
//			Imgproc.drawContours(convexHull, Arrays.asList(hullPoints), -1, Scalar.all(255), Core.FILLED);        
	//
//			Mat segmentedHand = AlgHelper.segmentHandBinary(handRegionPreprocessed, 220); //find hand mask
//			
//			Core.subtract(convexHull, segmentedHand, withoutHand); //Mat without hand but with noise
	//
//			List <MatOfPoint> contours2 = MatOperations.findContours(withoutHand, 0);
//			MatOfPoint mop = MatOperations.findBiggestContour(contours2); //find biggest contour - might be a goods only
//			hullPoints = MatOperations.convexHull(withoutHand,Arrays.asList(mop));
//			if (hullPoints == null) hullPoints = new MatOfPoint();
//			RotatedRect rotRect = Imgproc.minAreaRect(new MatOfPoint2f(hullPoints.toArray()));
////			if (print)
////			System.out.print(Imgproc.contourArea(hullPoints) + ";" + 
////					Imgproc.arcLength(new MatOfPoint2f(hullPoints.toArray()), true) + ";" +
////					rotRect.size.area() + ";" );
//			Imgproc.drawContours(handRegionPreprocessed, Arrays.asList(hullPoints), -1, Scalar.all(255), 2);        
//			
//			if (!withoutHand.equals(handRegionPreprocessed)) result = handRegionPreprocessed;		
			
			return result;
		}	
		
		
		//testing purpose
		
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
			
			List<MatOfPoint> contours = MatOperations.findContours(result, 100, 0);
			result2 = MatOperations.drawContours(contours, mat.width(), mat.height(), -1);
			result2 = MatOperations.dilate(result2, 3, 5); 
			return result2;
		}
		
		private Mat segmentHand3(Mat mat) {
			Mat result = new Mat(mat.size(),mat.type());
			Imgproc.medianBlur(mat, result, 1);
			Imgproc.adaptiveThreshold(result, result, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 3, 2);
//			Mat firstRow = result.row(0);
//			MatOfPoint mop = new MatOfPoint();
//			result = MatOperations.dilate(result, 4, 5); 
//			result = MatOperations.erode(result, 4, 5); 
	//
	//
//			Core.findNonZero(firstRow, mop);
//			for (int a = 0; a < mop.toArray().length - 1; a++)
//				Imgproc.line(result, mop.toArray()[a], mop.toArray()[a+1], Scalar.all(255),2);
//			
//			List<MatOfPoint> contours = MatOperations.findContours(result, 100);
//			result = MatOperations.drawContours(contours, mat.width(), mat.height(), -1);
//			result = MatOperations.dilate(result, 3, 5); 
//			result = MatOperations.dilate(result, 3, 10); 
//			result = MatOperations.erode(result, 4, 15); 
			return result;
		}
		
		private Mat segmentGoods(Mat original, Mat mat, double cannyThresh1, double cannyThresh2, boolean open, boolean close, double erode, double dilate, double handThreshold, double contourMinSize) {
			Mat result = new Mat(mat.size(), mat.type());
//			Mat withoutHand = new Mat(mat.size(), mat.type()); 
	//
//			result = MatOperations.doCannyEdgeDetection(mat, cannyThresh1 , cannyThresh2); //detect edges
////			result = MatOperations.morphology(result, open, close, erode, dilate, morphIterSpinner.getValue());
////			result = MatOperations.morphClose(edges, 3, 5);
	//
//			
//			List <MatOfPoint> contours = MatOperations.findContours(result, contourMinSize); //find contours + filter them
//			MatOfPoint hullPoints = MatOperations.convexHull(result,contours); //draw convex hull mask
//			Mat convexHull = new Mat(mat.size(),mat.type(), Scalar.all(0));
//			Imgproc.drawContours(convexHull, Arrays.asList(hullPoints), -1, Scalar.all(255), Core.FILLED);        
//			
//			Mat segmentedHandMask = AlgHelper.segmentHandBinary(original, handThreshold); //find hand mask
//			
//			Core.subtract(convexHull, segmentedHandMask, withoutHand); //Mat without hand but with noise
//			result = MatOperations.maskMat(original, withoutHand);  //masked original image - only goods + noise remains	
//			Mat noiseGoods = result.clone();
//			
//			List <MatOfPoint> contours2 = MatOperations.findContours(noiseGoods, 0);
//			MatOfPoint mop = MatOperations.findBiggestContour(contours2); //find biggest contour - might be a goods only
//			hullPoints = MatOperations.convexHull(noiseGoods, Arrays.asList(mop));
//			Imgproc.drawContours(result, Arrays.asList(hullPoints), -1, Scalar.all(255), Core.FILLED);        
//			result = MatOperations.maskMat(noiseGoods, result); //mask biggest contour - only goods should remain				
			return result;
		}
		
		private Mat segmentGoods2(Mat workMat, Rect roiRect) {
//			Mat roi = workMat.submat(roiRect);
//			Mat edges = new Mat(roi.size(), roi.type());
//			Mat handMask = AlgHelper.segmentHandBinary(roi, handThresholdSlider.getValue());
//			
//			handMask = MatOperations.morphOpen(handMask, 5, 10);
//			
//			handMask = MatOperations.invert(handMask);
//			edges = MatOperations.doCannyEdgeDetection(roi, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
////			edges = MatOperations.dilate(edges, 3, 2);
////			edges = MatOperations.erode(edges, 6, 1);
//			edges = MatOperations.morphClose(edges, 3, 5);
	//
//			Core.bitwise_and(handMask, edges, edges);	
////			edges = MatOperations.morphology(edges, false, true, erodeSpinner.getValue(), dilateSpinner.getValue(), morphIterSpinner.getValue());
	//
//			List<MatOfPoint> contours = MatOperations.findContours(edges, 20);
//		    Imgproc.drawContours(roi, contours, -1, new Scalar(255, 0, 0), 2);
//			AlgHelper.goodsContourFeatures(contours,printInfo);
			return workMat;
		}

		public Mat getOriginalMat() {
			return originalMat;
		}

		public void setOriginalMat(Mat originalMat) {
			this.originalMat = originalMat;
		}

		public Mat getScaledMat() {
			return scaledMat;
		}

		public void setScaledMat(Mat scaledMat) {
			this.scaledMat = scaledMat;
		}

		public Mat getPreviewMat() {
			return previewMat;
		}

		public void setPreviewMat(Mat previewMat) {
			this.previewMat = previewMat;
		}

		public Mat getWorkMat() {
			return workMat;
		}

		public void setWorkMat(Mat workMat) {
			this.workMat = workMat;
		}

		public Mat getHandMat() {
			return handMat;
		}

		public void setHandMat(Mat handMat) {
			this.handMat = handMat;
		}

		public Mat getGoodsMat() {
			return goodsMat;
		}

		public void setGoodsMat(Mat goodsMat) {
			this.goodsMat = goodsMat;
		}

		public SettingsWrapper getSettings() {
			return settings;
		}

		public void setSettings(SettingsWrapper settings) {
			this.settings = settings;
		}

		public boolean isPrintInfo() {
			return printInfo;
		}

		public void setPrintInfo(boolean printInfo) {
			this.printInfo = printInfo;
		}

		public DetectionResult getResult() {
			return result;
		}

		public void setResult(DetectionResult result) {
			this.result = result;
		}
}
