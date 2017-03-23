package image;

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
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MatOperations {
	
	public static Mat brightnessContrast(Mat mat, double brightness, double contrast) { //CLAHE http://docs.opencv.org/trunk/d5/daf/tutorial_py_histogram_equalization.html
		mat.convertTo(mat, -1, brightness + 1, -contrast);
		return mat;
	}
	
	public static Mat addMult(Mat mat, double add, double mult) {
        Core.add(mat, Scalar.all(add), mat);
        Core.multiply(mat, Scalar.all(mult + 1), mat);
		return mat;
	}
	
	public static Mat blurImage(Mat mat, double size1, double size2, double sigma) {
		if (size1 % 2 == 0) ++size1;		
		if (size2 % 2 == 0) ++size2;
		Mat resultMat = mat.clone();
//		Imgproc.GaussianBlur(mat, resultMat, new Size(size1, size2), sigma);
		Imgproc.bilateralFilter(mat, resultMat, (int) sigma, size1, size2);
		return resultMat;
	}
	
	public static Mat binaryTreshold(Mat mat, double threshold) {
		Mat result = new Mat(mat.size(), mat.type());        
		Imgproc.threshold(mat, result, threshold , 255, Imgproc.THRESH_BINARY);
		return result;
	}
	
	public static Mat otsuThreshold(Mat mat, double thresholdCorrection) {
		Mat result = new Mat(mat.size(), mat.type());        
		Imgproc.threshold(mat, result, Imgproc.threshold(mat, new Mat(), 0, 255, Imgproc.THRESH_OTSU) + thresholdCorrection, 255, Imgproc.THRESH_BINARY);
		return result;
	}	
	
	public static Mat doCannyEdgeDetection(Mat mat, double thresh1, double thresh2) { //todo 2 params
		Mat detectedEdges = new Mat();		
		Imgproc.Canny(mat, mat, thresh1, thresh2);
		Mat dest = new Mat();
		mat.copyTo(dest, detectedEdges);
		
		return dest;
	}
	
	public static List<MatOfPoint> findContours(Mat mat, double minSize) {
		List<MatOfPoint> allContours = new ArrayList<MatOfPoint>();    
		List<MatOfPoint> filteredContours = new ArrayList<MatOfPoint>();    
		
        Imgproc.findContours(mat, allContours,  new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < allContours.size(); i++) {
        	double countourArea = Imgproc.contourArea(allContours.get(i));	      
	        if (countourArea > minSize) filteredContours.add(allContours.get(i));	      
		}        
//        foundContoursLabel.setText(" " + Integer.toString(filteredContours.size()));
        return filteredContours;
	}
	
	public static MatOfPoint findBiggestContour(List<MatOfPoint> contours) {
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
	
	public static Mat drawContours(List <MatOfPoint> contours, int width, int height) {	
		Mat contourImg = new Mat(height, width, CvType.CV_8U, new Scalar(0,0,0));		
		for (int i = 0; i < contours.size(); i++) {
		    Imgproc.drawContours(contourImg, contours, i, new Scalar(255, 0, 0), -1);
		}
		return contourImg;	
	}
	
	public static Mat convexHull(Mat mat, List <MatOfPoint> contours) {
		Mat contoursMat = drawContours(contours, mat.width(), mat.height());
        MatOfPoint points = new MatOfPoint();
        MatOfInt hullTemp = new MatOfInt();
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8U, new Scalar(0,0,0));        
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
	
	public static Mat maskMat(Mat matToMask, Mat mask) {
		Mat result = new Mat(matToMask.size(), matToMask.type(), new Scalar(0,0,0)); 
		matToMask.copyTo(result, mask);
		return result;
	}
	
	public static Mat aproxCurve(Mat mat, List <MatOfPoint> contours) { //TODO: contains bugs
		Mat contoursMat = drawContours(contours, mat.width(), mat.height());
		
		MatOfPoint points = new MatOfPoint();
		MatOfPoint2f thisContour2f = new MatOfPoint2f();
		MatOfPoint approxContour = new MatOfPoint();
		MatOfPoint2f approxContour2f = new MatOfPoint2f();
		
		Core.findNonZero(contoursMat, points);   
		
        if (points == null || points.empty()) return mat;        		
		
        points.convertTo(thisContour2f, CvType.CV_32FC1);
		
		Imgproc.approxPolyDP(thisContour2f, approxContour2f, 2, false);
		
		approxContour2f.convertTo(approxContour, CvType.CV_32S);
		
		
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8U, new Scalar(0,0,0));     
		List<MatOfPoint> cnl = new ArrayList<>();
        cnl.add(approxContour);
        Imgproc.drawContours(result, cnl, -1, Scalar.all(255), Core.FILLED);   
        return result;
	}	
	
	public static Mat invert(Mat mat) {
		Mat result = new Mat(mat.size(), mat.type());
        Mat white = new Mat(mat.size(), mat.type(), Scalar.all(255)); 
        Core.subtract(white, mat, result);
        return result;
	}
	
	public static Mat dilate(Mat mat, double dSize) {
		Mat result = new Mat(mat.size(), mat.type());
		int size = (int) dSize;
		if (size % 2 == 0) ++size;
	    Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(size,size));
	    Imgproc.dilate(mat, result, element);
	    return result;
	}
	
	public static Mat erode(Mat mat, double dSize) {
		Mat result = new Mat(mat.size(), mat.type());
		int size = (int) dSize;
		if (size % 2 == 0) ++size;
	    Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(size,size));
	    Imgproc.erode(mat, result, element);
	    return result;
	}
	
	public static Mat createHistogram(Mat mat) {
		Mat hist = new Mat();
		MatOfInt histSize = new MatOfInt(256);
		Imgproc.calcHist(Arrays.asList(mat), new MatOfInt(0), new Mat(), hist, histSize, new MatOfFloat(0, 256), false);
		int bin_w = (int) Math.round(mat.width() / histSize.get(0, 0)[0]);		
		Mat histImage = new Mat(mat.height(), mat.width(), CvType.CV_8U, new Scalar(0, 0, 0));
		Core.normalize(hist, hist, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
				
		for (int i = 1; i < histSize.get(0, 0)[0]; i++) {
			Imgproc.line(histImage, new Point(bin_w * (i - 1), mat.width() - Math.round(hist.get(i - 1, 0)[0])), new Point(bin_w * (i), mat.height() - Math.round(hist.get(i, 0)[0])), new Scalar(255, 0, 0), 2, 8, 0);			
		}		
		return histImage;		
	}
}
