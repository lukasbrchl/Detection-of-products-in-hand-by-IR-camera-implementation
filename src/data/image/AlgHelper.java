package data.image;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

import application.MainController;
import data.image.domain.Contour;

public final class AlgHelper {
	
	public static Mat segmentHandBinary(Mat mat, double threshold) {
		Mat result = new Mat(mat.size(), mat.type());	    
		result =  MatOperations.binaryTreshold(mat, threshold);
		return result;
	}
	
	public static boolean isBackgroundOnly(Mat mat) {
		Mat items = MatOperations.doCannyEdgeDetection(mat, 30, 60);
		List <MatOfPoint> contours = MatOperations.findContours(items, 25);	
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
	
}
