package algorithm.detector;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import algorithm.detector.domain.DetectionResult;
import algorithm.settings.domain.PreprocessingSettings;
import algorithm.settings.domain.SettingsWrapper;
import image.MatOperations;

/**
* Class containing methods for successfully detecting goods on the scene using edge detection method.

*
* @author Lukas Brchl
*/
public class EdgeDetector extends AbstractDetector {
	
	public EdgeDetector(SettingsWrapper settings) {
		super(settings);
	}


	@Override
	public DetectionResult detect() {
		PreprocessingSettings pgs = ((PreprocessingSettings) settings.getByCls(PreprocessingSettings.class));
		Mat handMat = segmentHandBinary(workMat, 180); //TODO
		List <MatOfPoint> contours = MatOperations.findContours(handMat, 10, 0); //TODO
		MatOfPoint biggest = MatOperations.findBiggestContour(contours);
		Mat edgesMat = new Mat(workMat.size(), workMat.type(), Scalar.all(0));
		
		if (contours.size() > 0 && Imgproc.contourArea(biggest) > 100) {
			Rect roiRect = findExtendedRegion(handMat);
	
			if (roiRect.width != 0 && roiRect.height != 0)
				edgesMat = findGoodsEdges(handMat, workMat, roiRect);
			
//			Imgproc.rectangle(handMat, new Point(roiRect.x, roiRect.y), new Point(roiRect.x + roiRect.width, roiRect.y + roiRect.height), Scalar.all(255));		
		}	
		return DetectionResult.UNDEFINED;
	}
	
	
	/**
	* Tries to find goods edges on ROI of input preprocessed mat and process them.  
	* 
	* @param handMask	segmented hand mask
	* @param workMat	preprocessed mat
	* @param roiRect	working area
	*/
	private Mat findGoodsEdges(Mat handMask, Mat workMat, Rect roiRect) {
		PreprocessingSettings pgs = ((PreprocessingSettings) settings.getByCls(PreprocessingSettings.class));
		Mat roi = workMat.submat(roiRect);
		Mat edges = new Mat(roi.size(), roi.type());
		Mat handMaskRoi = handMask.submat(roiRect);

		Imgproc.morphologyEx(handMaskRoi, handMaskRoi, Imgproc.MORPH_DILATE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7,7)));

		handMaskRoi = MatOperations.invert(handMaskRoi);
//		
		edges = MatOperations.doCannyEdgeDetection(roi, 20 , 30); //TODO
//		edges = MatOperations.dilate(edges, 1, 2);
//		edges = MatOperations.erode(edges, 3, 1);
//		edges = MatOperations.morphClose(edges, 3, 5);
//
		Core.bitwise_and(handMaskRoi, edges, edges);	
//
		List<MatOfPoint> contours = MatOperations.findContours(edges, 25, 0);
	    Imgproc.drawContours(roi, contours, -1, new Scalar(255, 0, 0), 2);
		if (printInfo) System.out.println(goodsContourFeatures(contours));
		return workMat;
	}
	
	/**
	* Finds and extends region based on non zero mat values.
	* 
	* @param mat	Mat to find on
	* @return region of interest rectangle
	*/
	private Rect findExtendedRegion(Mat mat) {
		MatOfPoint mop = new MatOfPoint();
		Core.findNonZero(mat, mop);
		Rect rect = Imgproc.boundingRect(mop);
		rect.height = IMAGE_HEIGHT;
		int enlargeSideBy = 20;
		if (rect.x < enlargeSideBy) {
			rect.width += rect.x + enlargeSideBy ;
			rect.x = 0;
		} else if (rect.x + rect.width >= IMAGE_WIDTH - enlargeSideBy) {
			rect.width += IMAGE_WIDTH - (rect.x + rect.width) + enlargeSideBy ;
			rect.x -= enlargeSideBy;
		} else {
			rect.width += enlargeSideBy*2;
			rect.x -= enlargeSideBy;
		}
		return rect;
	}	
}
