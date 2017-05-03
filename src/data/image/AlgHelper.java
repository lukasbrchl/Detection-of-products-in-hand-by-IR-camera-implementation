package data.image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import application.MainController;
import data.algorithm.settings.domain.PreprocessingSettings;
import data.image.domain.Contour;

public final class AlgHelper {
	
	
	public static void createBasicMats(PreprocessingSettings ps) {
		
	}
	
	public static Mat preprocessMat(Mat mat, PreprocessingSettings ps) {
		Mat result = mat.clone();		
		result =  MatOperations.clache(result, ps.getClacheSize1(), ps.getClacheSize2(), ps.getClacheClip());
		result = MatOperations.brightnessContrast(result, ps.getBrightnessParam1(), ps.getContrastParam1());
		result = MatOperations.addMult(result, ps.getBrightnessParam2(), ps.getContrastParam1());
		result = MatOperations.medianBlur(result, ps.getMedianSize());
		result = MatOperations.bilateralBlur(result, ps.getBilateralSize1(), ps.getBilateralSize2(), ps.getBilateralSigma());
		result = MatOperations.gaussianBlur(result, ps.getGaussianSize1(), ps.getGaussianSize2(), ps.getGaussianSigma());
		return result;
	}
	
	public static Mat preprocessMainMat(Mat mat, PreprocessingSettings ps, boolean exposure, boolean blur, boolean canny) {
		Mat result = mat.clone();		
		if (exposure) {
			result = MatOperations.clache(result, ps.getClacheSize1(), ps.getClacheSize2(), ps.getClacheClip());		
			result = MatOperations.brightnessContrast(result, ps.getBrightnessParam1(), ps.getContrastParam1());
			result = MatOperations.addMult(result, ps.getBrightnessParam2(), ps.getContrastParam1());
		}
		if (blur) {
			result = MatOperations.medianBlur(result, ps.getMedianSize());
			result = MatOperations.bilateralBlur(result, ps.getBilateralSize1(), ps.getBilateralSize2(), ps.getBilateralSigma());
			result = MatOperations.gaussianBlur(result, ps.getGaussianSize1(), ps.getGaussianSize2(), ps.getGaussianSigma());
		}
		if (canny) result = MatOperations.doCannyEdgeDetection(result, 20, 40); //TODO
//		if (morphologyMainCheckbox.isSelected()) result = MatOperations.morphology(result, morphOpenCheckbox.isSelected(), morphCloseCheckbox.isSelected(), erodeSpinner.getValue(), dilateSpinner.getValue(), morphIterSpinner.getValue());		
//		if (hullHandGoodsMainCheckbox.isSelected())	result = hullHandWithGoodsRegion(result, untouched);
//		if (hullGoodsMainCheckbox.isSelected()) result = hullGoodsRegion(result, untouched);		
		return result;
	}
	
	public static Mat segmentHandBinary(Mat mat, double threshold) {
		Mat result = new Mat(mat.size(), mat.type());	    
		result =  MatOperations.binaryTreshold(mat, threshold);
		return result;
	}
	
	public static boolean isBackgroundOnly(Mat mat) {
		Mat items = MatOperations.doCannyEdgeDetection(mat, 40, 80);
		List <MatOfPoint> contours = MatOperations.findContours(items, 25, 0);	
		return contours.size() == 0;
	}
	
	public static boolean isHandInShelf(Mat mat) {
		Mat segmentedHand = AlgHelper.segmentHandBinary(mat, 220);
		segmentedHand = MatOperations.erode(segmentedHand, 25, 10);
		MatOfPoint mop = new MatOfPoint();
		Core.findNonZero(segmentedHand, mop);
		if (Imgproc.boundingRect(mop).height >= MainController.IMAGE_HEIGHT) return true;
		return false;
	}
	
	
	public static void goodsContourFeatures(List <MatOfPoint> contours, boolean printInfo) {
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
//		System.out.print(";" + flirDummyFolder.getFileName().toString());
	}
	
	
	//MOG variant#################### 
	
	public static Mat roiHandExtendedMask(Mat mat, Rect roiRect, int binaryThreshold, int dilateSize) {
		Mat segmentedHandRoiMask = mat.submat(roiRect);
		Mat first = new Mat(segmentedHandRoiMask.size(), segmentedHandRoiMask.type());
		Mat second = new Mat(segmentedHandRoiMask.size(), segmentedHandRoiMask.type());
		
		Imgproc.threshold(segmentedHandRoiMask, first, binaryThreshold , 255, Imgproc.THRESH_BINARY);
		Imgproc.adaptiveThreshold(segmentedHandRoiMask, second, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 1555, -210);
		Core.bitwise_or(first, second, segmentedHandRoiMask);
		Imgproc.dilate(segmentedHandRoiMask, segmentedHandRoiMask, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(dilateSize,dilateSize)));
		
		return segmentedHandRoiMask;
	}
	
	public static Mat getGoods(Mat substracted, Mat segmentedHandMask, Mat segmentedHandRoiMask, Rect roiRect) {
		Mat segmentedGoods = Mat.zeros(substracted.size(), substracted.type()); 
		
		List <MatOfPoint> substractedContours = MatOperations.findContours(substracted, 0, 0);
		Rect boundingRect = Imgproc.boundingRect(MatOperations.findBiggestContour(substractedContours));
		
		roiRect.x = boundingRect.x;
		roiRect.y = boundingRect.y;
		
		if (roiRect.width > 0 && roiRect.height > 0) {
			segmentedHandRoiMask = AlgHelper.roiHandExtendedMask(segmentedHandMask, roiRect, 220, 9);
		}
		
		Mat segmentedHand = MatOperations.invert(segmentedHandMask);
		substracted.copyTo(segmentedGoods, segmentedHand);	
		return segmentedGoods;
	}
	
	public static List <MatOfPoint> findAndfilterGoodsContours(Mat segmentedGoods) {
		List <MatOfPoint> segmentedGoodsContours = MatOperations.findContours(segmentedGoods, 0, 0);
		List <MatOfPoint> filteredContours = new ArrayList<>();
		
		for (MatOfPoint mop : segmentedGoodsContours) {
			if (mop.toArray().length < 5) continue;
			RotatedRect rotRect = Imgproc.fitEllipse(new MatOfPoint2f(mop.toArray()));
			if (rotRect.size.width < 20 || rotRect.size.height < 20) continue;
			Point [] points = new Point [4];
			rotRect.points(points);
			MatOperations.drawMinBoundingRect(segmentedGoods, points);
			filteredContours.add(mop);
		}
		return filteredContours;
	}
	
	public static void drawBiggestHandPoints(Mat segmentedHandRoiMask, Point biggestDefect, Point biggestStart, Point biggestEnd) {	  
		Imgproc.circle(segmentedHandRoiMask, biggestStart, 5, Scalar.all(255), 2);
        Imgproc.circle(segmentedHandRoiMask, biggestEnd, 5, Scalar.all(255), 2);
        Imgproc.circle(segmentedHandRoiMask, biggestDefect, 5, Scalar.all(255), 2);
	}
	
	public static void drawHandSplitLines(Mat segmentedGoods, Rect roiRect, Point biggestDefect, Point biggestStart, Point biggestEnd) {
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
			        
		p1.x += roiRect.x; p1.y += roiRect.y;p2.x += roiRect.x;p2.y += roiRect.y; 
		p3.x += roiRect.x;p3.y += roiRect.y;p4.x += roiRect.x;p4.y += roiRect.y;
		biggestStart.x += roiRect.x; biggestStart.y += roiRect.y;
		biggestEnd.x += roiRect.x; biggestEnd.y += roiRect.y;
		         
		Imgproc.line(segmentedGoods, p3, p4, Scalar.all(0),3);
		Imgproc.line(segmentedGoods, p1, biggestStart, Scalar.all(0),3);
		Imgproc.line(segmentedGoods, p2, biggestEnd, Scalar.all(0),3);
	}
	
	public static  void refreshBackground(BackgroundSubtractorMOG2 mog, Mat mat, boolean printInfo) {
		if (AlgHelper.isBackgroundOnly(mat)) {
			if (printInfo) System.out.println("background");
			mog = Video.createBackgroundSubtractorMOG2(51, 200, true);
			mog.apply(mat, new Mat());
		}
	}
	
	
	//Edge detect variant#################### 
	
	public static Rect findExtendedRegion(Mat mat) {
		MatOfPoint mop = new MatOfPoint();
		Core.findNonZero(mat, mop);
		Rect rect = Imgproc.boundingRect(mop);
		rect.height = MainController.IMAGE_HEIGHT;
		int enlargeSideBy = 20;
		if (rect.x < enlargeSideBy) {
			rect.width += rect.x + enlargeSideBy ;
			rect.x = 0;
		} else if (rect.x + rect.width >= MainController.IMAGE_WIDTH - enlargeSideBy) {
			rect.width += MainController.IMAGE_WIDTH - (rect.x + rect.width) + enlargeSideBy ;
			rect.x -= enlargeSideBy;
		} else {
			rect.width += enlargeSideBy*2;
			rect.x -= enlargeSideBy;
		}
		return rect;
	}
	
	
	//main mat helper drawers
	private Mat hullHandWithGoodsRegion(Mat mat, Mat untouched) {
		Mat result = mat.clone();
//		Mat workmat = untouched.clone();
//		workmat = preprocessMat(workmat);
//		Mat handRegionPreprocessed = workmat.clone();
//
//		workmat = MatOperations.doCannyEdgeDetection(workmat, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
//		workmat = MatOperations.dilate(workmat, dilateSpinner.getValue(), 1); //try to link them	
//		List<MatOfPoint> contours = MatOperations.findContours(workmat, contourMinSizeSpinner.getValue());
//		MatOfPoint hullPoints  = MatOperations.convexHull(workmat, contours);
//		RotatedRect rotRect = Imgproc.minAreaRect(new MatOfPoint2f(hullPoints.toArray()));
////		if (print)
////		System.out.print(Imgproc.contourArea(hullPoints) + ";" + 
////		Imgproc.arcLength(new MatOfPoint2f(hullPoints.toArray()), true) + ";" +
////		rotRect.size.area() + ";" );
//		Imgproc.drawContours(handRegionPreprocessed, Arrays.asList(hullPoints), -1, Scalar.all(255), 2);        
//		
//		if (!handRegionPreprocessed.equals(workmat)) result = handRegionPreprocessed;
		return result;
	}	
		
	private Mat hullGoodsRegion(Mat mat, Mat untouched) {
		Mat result = mat.clone();
//		Mat handRegionPreprocessed = mat.clone();
//		Mat workmat = untouched.clone();
//		workmat = preprocessMat(workmat);
//		Mat withoutHand = new Mat(workmat.size(), workmat.type()); 
//
//		workmat = MatOperations.doCannyEdgeDetection(workmat, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
////		workmat = MatOperations.morphology(workmat, morphOpenCheckbox.isSelected(), morphCloseCheckbox.isSelected(), erodeSpinner.getValue(), dilateSpinner.getValue(), morphIterSpinner.getValue());
//		List <MatOfPoint> contours = MatOperations.findContours(workmat, contourMinSizeSpinner.getValue()); //find contours + filter them
//		MatOfPoint hullPoints = MatOperations.convexHull(workmat, contours); //draw convex hull mask
//		Mat convexHull = new Mat(workmat.size(),workmat.type(), Scalar.all(0));
//		Imgproc.drawContours(convexHull, Arrays.asList(hullPoints), -1, Scalar.all(255), Core.FILLED);        
//
//		Mat segmentedHand = AlgHelper.segmentHandBinary(handRegionPreprocessed, 220); //find hand mask
//		
//		Core.subtract(convexHull, segmentedHand, withoutHand); //Mat without hand but with noise
//
//		List <MatOfPoint> contours2 = MatOperations.findContours(withoutHand, 0);
//		MatOfPoint mop = MatOperations.findBiggestContour(contours2); //find biggest contour - might be a goods only
//		hullPoints = MatOperations.convexHull(withoutHand,Arrays.asList(mop));
//		if (hullPoints == null) hullPoints = new MatOfPoint();
//		RotatedRect rotRect = Imgproc.minAreaRect(new MatOfPoint2f(hullPoints.toArray()));
////		if (print)
////		System.out.print(Imgproc.contourArea(hullPoints) + ";" + 
////				Imgproc.arcLength(new MatOfPoint2f(hullPoints.toArray()), true) + ";" +
////				rotRect.size.area() + ";" );
//		Imgproc.drawContours(handRegionPreprocessed, Arrays.asList(hullPoints), -1, Scalar.all(255), 2);        
//		
//		if (!withoutHand.equals(handRegionPreprocessed)) result = handRegionPreprocessed;		
		
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
	
	private Mat segmentGoods(Mat original, Mat mat, double cannyThresh1, double cannyThresh2, boolean open, boolean close, double erode, double dilate, double handThreshold, double contourMinSize) {
		Mat result = new Mat(mat.size(), mat.type());
//		Mat withoutHand = new Mat(mat.size(), mat.type()); 
//
//		result = MatOperations.doCannyEdgeDetection(mat, cannyThresh1 , cannyThresh2); //detect edges
////		result = MatOperations.morphology(result, open, close, erode, dilate, morphIterSpinner.getValue());
////		result = MatOperations.morphClose(edges, 3, 5);
//
//		
//		List <MatOfPoint> contours = MatOperations.findContours(result, contourMinSize); //find contours + filter them
//		MatOfPoint hullPoints = MatOperations.convexHull(result,contours); //draw convex hull mask
//		Mat convexHull = new Mat(mat.size(),mat.type(), Scalar.all(0));
//		Imgproc.drawContours(convexHull, Arrays.asList(hullPoints), -1, Scalar.all(255), Core.FILLED);        
//		
//		Mat segmentedHandMask = AlgHelper.segmentHandBinary(original, handThreshold); //find hand mask
//		
//		Core.subtract(convexHull, segmentedHandMask, withoutHand); //Mat without hand but with noise
//		result = MatOperations.maskMat(original, withoutHand);  //masked original image - only goods + noise remains	
//		Mat noiseGoods = result.clone();
//		
//		List <MatOfPoint> contours2 = MatOperations.findContours(noiseGoods, 0);
//		MatOfPoint mop = MatOperations.findBiggestContour(contours2); //find biggest contour - might be a goods only
//		hullPoints = MatOperations.convexHull(noiseGoods, Arrays.asList(mop));
//		Imgproc.drawContours(result, Arrays.asList(hullPoints), -1, Scalar.all(255), Core.FILLED);        
//		result = MatOperations.maskMat(noiseGoods, result); //mask biggest contour - only goods should remain				
		return result;
	}
	
	private Mat segmentGoods2(Mat workMat, Rect roiRect) {
//		Mat roi = workMat.submat(roiRect);
//		Mat edges = new Mat(roi.size(), roi.type());
//		Mat handMask = AlgHelper.segmentHandBinary(roi, handThresholdSlider.getValue());
//		
//		handMask = MatOperations.morphOpen(handMask, 5, 10);
//		
//		handMask = MatOperations.invert(handMask);
//		edges = MatOperations.doCannyEdgeDetection(roi, cannyEdge1Spinner.getValue() , cannyEdge2Spinner.getValue()); //detect edges
////		edges = MatOperations.dilate(edges, 3, 2);
////		edges = MatOperations.erode(edges, 6, 1);
//		edges = MatOperations.morphClose(edges, 3, 5);
//
//		Core.bitwise_and(handMask, edges, edges);	
////		edges = MatOperations.morphology(edges, false, true, erodeSpinner.getValue(), dilateSpinner.getValue(), morphIterSpinner.getValue());
//
//		List<MatOfPoint> contours = MatOperations.findContours(edges, 20);
//	    Imgproc.drawContours(roi, contours, -1, new Scalar(255, 0, 0), 2);
//		AlgHelper.goodsContourFeatures(contours,printInfo);
		return workMat;
	}	
	
	
	
}
