package algorithm.detector;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import algorithm.settings.domain.PreprocessingSettings;
import algorithm.settings.domain.SettingsWrapper;
import image.MatOperations;

public class EdgeDetector extends AbstractDetector {
	
	public EdgeDetector(SettingsWrapper settings) {
		super(settings);
	}


	@Override
	public void detect() {
		PreprocessingSettings pgs = ((PreprocessingSettings) settings.getByCls(PreprocessingSettings.class));
		Mat handMat = segmentHandBinary(workMat, 180); //TODO
		List <MatOfPoint> contours = MatOperations.findContours(handMat, 10, 0); //TODO
		MatOfPoint biggest = MatOperations.findBiggestContour(contours);
		Mat edgesMat = new Mat(workMat.size(), workMat.type(), Scalar.all(0));
		
		if (contours.size() > 0 && Imgproc.contourArea(biggest) > 100) {
			Rect roiRect = findExtendedRegion(handMat);
	
			if (roiRect.width != 0 && roiRect.height != 0)
				edgesMat = segmentGoodsUsingEdges(handMat, workMat, roiRect);
			
//			Imgproc.rectangle(handMat, new Point(roiRect.x, roiRect.y), new Point(roiRect.x + roiRect.width, roiRect.y + roiRect.height), Scalar.all(255));

		}		
	}
	
	
	private Mat segmentGoodsUsingEdges(Mat handMat, Mat workMat, Rect roiRect) {
		PreprocessingSettings pgs = ((PreprocessingSettings) settings.getByCls(PreprocessingSettings.class));
		Mat roi = workMat.submat(roiRect);
		Mat edges = new Mat(roi.size(), roi.type());
		Mat handMatRoi = handMat.submat(roiRect);

		Imgproc.morphologyEx(handMatRoi, handMatRoi, Imgproc.MORPH_DILATE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7,7)));

		handMatRoi = MatOperations.invert(handMatRoi);
//		
		edges = MatOperations.doCannyEdgeDetection(roi, 20 , 30); //TODO
//		edges = MatOperations.dilate(edges, 1, 2);
//		edges = MatOperations.erode(edges, 3, 1);
//		edges = MatOperations.morphClose(edges, 3, 5);
//
		Core.bitwise_and(handMatRoi, edges, edges);	
//
		List<MatOfPoint> contours = MatOperations.findContours(edges, 25, 0);
	    Imgproc.drawContours(roi, contours, -1, new Scalar(255, 0, 0), 2);
		goodsContourFeatures(contours,printInfo);
		return workMat;
	}
	
	public static Rect findExtendedRegion(Mat mat) {
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
