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
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import utils.ImageConvertor;

public class MatOperations {
	
	public static Mat createMat(byte [] byteArray, int width, int height, boolean scale, float min, float max, float interval) {		
		Mat mat;
		if (scale) mat = ImageConvertor.convertBinaryToMat(byteArray, width, height, min, max);
		else if (interval > 0) mat = ImageConvertor.convertBinaryToMat(byteArray, width, height, interval);
		else mat = ImageConvertor.convertBinaryToMat(byteArray, width, height);
		return mat;
	}
	
	public static Mat clache(Mat mat, double value1, double value2, double value3) {
		Mat result = new Mat(mat.size(), mat.type()); 
		CLAHE clahe = Imgproc.createCLAHE(value3, new Size(value1, value2));
		clahe.apply(mat, result);
		return result;
	}
	
	public static Mat brightnessContrast(Mat mat, double brightness, double contrast) { //CLAHE http://docs.opencv.org/trunk/d5/daf/tutorial_py_histogram_equalization.html
        double inverse_gamma = 1.0 / 3;

        Mat lut = new Mat(1, 256, CvType.CV_8UC1);
        int data;
        for (int i = 0; i < 256; i++) {
            data = (int) (Math.pow((double) i / 255.0, inverse_gamma) * 255.0);
            lut.put(0, i, data);
        }		
		Core.LUT(mat, lut, mat);
		
		mat.convertTo(mat, -1, brightness + 1, -contrast);
		return mat;
	}
	
	public static Mat addMult(Mat mat, double add, double mult) {
        Core.add(mat, Scalar.all(add), mat);
        Core.multiply(mat, Scalar.all(mult + 1), mat);
		return mat;
	}
	
	public static Mat gamma(Mat mat, double gamma) {
        Mat lut = new Mat(1, 256, CvType.CV_8U);
        int data;
        for (int i = 0; i < 256; i++)
           lut.put(0, i, (int) (Math.pow((double) i / 255.0, 1 / gamma) * 255.0));		
        Core.LUT(mat, lut, mat);
        return mat;
	}
	
	public static Mat blurImage(Mat mat, double size1, double size2, double sigma) {
		if (size1 % 2 == 0) ++size1;		
		if (size2 % 2 == 0) ++size2;
		Mat resultMat = new Mat();
		Mat medianMat = new Mat();
		
		
//		Imgproc.GaussianBlur(mat, resultMat, new Size(size1, size2), sigma);
		Imgproc.medianBlur(mat, medianMat,7);
		Imgproc.GaussianBlur(medianMat, resultMat, new Size(size1, size2), sigma);

//		Imgproc.bilateralFilter(medianMat, resultMat, (int) sigma, size1, size2);
		return resultMat;
	}
	
	public static Mat binaryTreshold(Mat mat, double threshold) {
		Mat result = new Mat(mat.size(), mat.type());        
		Imgproc.threshold(mat, result, threshold , 255, Imgproc.THRESH_BINARY);
//		if ((int) (threshold % 2) == 0) threshold ++;
//		Imgproc.adaptiveThreCshold(mat, result, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, (int) threshold, 50);
//		th2 = cv2.adaptiveThreshold(img,255,cv2.ADAPTIVE_THRESH_MEAN_C,\
//	            cv2.THRESH_BINARY,11,2)
//	th3 = cv2.adaptiveThreshold(img,255,cv2.ADAPTIVE_THRESH_GAUSSIAN_C,\
//	            cv2.THRESH_BINARY,11,2)
		return result;
	}
	
	public static Mat otsuThreshold(Mat mat, double thresholdCorrection) {
		Mat result = new Mat(mat.size(), mat.type());        
		Imgproc.threshold(mat, result, Imgproc.threshold(mat, new Mat(), 0, 255, Imgproc.THRESH_OTSU) + thresholdCorrection, 255, Imgproc.THRESH_BINARY);
		return result;
	}	
	
	public static Mat doCannyEdgeDetection(Mat mat, double thresh1, double thresh2) { //todo 2 params
		Mat result = new Mat();		
		Imgproc.Canny(mat, result, thresh1, thresh2);
		return result;
	}
	
	public static List<MatOfPoint> findContours(Mat mat, double minSize) {
		List<MatOfPoint> allContours = new ArrayList<MatOfPoint>();    
		List<MatOfPoint> filteredContours = new ArrayList<MatOfPoint>();    
		
        Imgproc.findContours(mat, allContours,  new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < allContours.size(); i++) {
        	double countourArea = Imgproc.contourArea(allContours.get(i));	      
	        if (countourArea > minSize) filteredContours.add(allContours.get(i));	      
		}        
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
	
	public static Mat convexHull(Mat mat, Mat drawOn, List <MatOfPoint> contours, boolean fill) {
		Mat contoursMat = drawContours(contours, mat.width(), mat.height());
        MatOfPoint points = new MatOfPoint();
        MatOfInt hullTemp = new MatOfInt();
        Mat result;
        if (drawOn == null) result = new Mat(mat.height(), mat.width(), CvType.CV_8U, new Scalar(0,0,0));    
        else result = drawOn.clone();
        
        Core.findNonZero(contoursMat, points);   
        if (points == null || points.empty()) return mat;        
		
        Imgproc.convexHull(points, hullTemp);	
//		System.out.println(Imgproc.contourArea(hullTemp));
//        System.out.println(hullTemp.dump());

        
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
        
		System.out.println(Imgproc.contourArea(mopOut));

        
        List<MatOfPoint> cnl = new ArrayList<>();
        cnl.add(mopOut);
        int thickness =  fill ? Core.FILLED : 2;
        Imgproc.drawContours(result, cnl, -1, Scalar.all(255), thickness);        
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

    public static Point getMassCenter(MatOfPoint mop, Mat mat) {
//    	Imgproc.minAreaRect(points)
        Moments moments = Imgproc.moments(mop);

        Point center = new Point();
        center.x = moments.get_m10() / moments.get_m00();
        center.y = moments.get_m01() / moments.get_m00();

        if (center.x >= 0 && center.x <= mat.cols() && center.y >= 0 && center.y <= mat.rows())        	
        	return center;
        return new Point(0,0);
    }
	
	public static Mat maskMat(Mat matToMask, Mat mask) {
		Mat result = new Mat(matToMask.size(), matToMask.type(), new Scalar(0,0,0)); 
		matToMask.copyTo(result, mask);
		return result;
	}
	
	public static Mat invert(Mat mat) {
		Mat result = new Mat(mat.size(), mat.type());
        Mat white = new Mat(mat.size(), mat.type(), Scalar.all(255)); 
        Core.subtract(white, mat, result);
        return result;
	}
	
	public static Mat morphology(Mat mat, boolean open, boolean close, double erodeValue, double dilateValue, double iterations) {
		Mat result = mat.clone();
		if ((int) erodeValue % 2 == 0) erodeValue++;
		if ((int) dilateValue % 2 == 0) dilateValue++;

//	    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(erodeValue,erodeValue));
	    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilateValue,dilateValue));
		for (int i = 0; i < (int) iterations; ++i) {
		    if (!close) {
			    Imgproc.morphologyEx(result, result, Imgproc.MORPH_OPEN, dilateElement);
	//			result = MatOperations.erode(mat, erodeValue);
	//			result = MatOperations.dilate(result, dilateValue);
			} else {
			    Imgproc.morphologyEx(result, result, Imgproc.MORPH_CLOSE, dilateElement);
	//			result = MatOperations.dilate(mat, dilateValue);
	//			result = MatOperations.erode(result, erodeValue);
			}
		}
		return result;
	}
	
	public static Mat dilate(Mat mat, double dSize, double iterations) {
//		Mat result = new Mat(mat.size(), mat.type());
		Mat result = mat.clone();

		int size = (int) dSize;
		if (size % 2 == 0) ++size;
	    Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(size,size));
//	    Imgproc.dilate(mat, result, element, new Point(), (int) iterations);
	    for (int i = 0; i < (int) iterations; ++i)
	    	Imgproc.dilate(result, result, element);

	    return result;
	}
	
	public static Mat erode(Mat mat, double dSize, int iterations) {
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
	
	public static Mat drawMinBoundingRect(Mat mat, Point [] points) {
		Mat result = mat.clone();
		if (points != null) {
			for (int i = 0; i < points.length; ++i) {
				Imgproc.line(result, points[i], points[(i+1)%4], Scalar.all(255), 3);
			}
		}
		return result;
	}
	
	public static void replaceMatArea(Mat mat, Mat small, int offsetX, int offsetY) { 
		byte [] data = new byte[small.channels()*small.cols()*small.rows()];
		small.get(0, 0, data);
		mat.put(offsetY, offsetX, data);		
	}
	
	
}