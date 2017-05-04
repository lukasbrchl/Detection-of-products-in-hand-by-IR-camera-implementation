package algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
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

import algorithm.domain.DetectionResult;
import algorithm.settings.domain.SettingsWrapper;
import image.MatOperations;

public class MogDetect extends AbstractDetect {

	private static final int MOG_THRESHOLD = 20;
	private static final int MOG_HISTORY = 50;

	private BackgroundSubtractorMOG2 mog = Video.createBackgroundSubtractorMOG2(MOG_HISTORY, MOG_THRESHOLD, true);
	
	private Mat background;

	public MogDetect(SettingsWrapper settings) {
		super(settings);
		background = Mat.zeros(new Size(IMAGE_WIDTH, IMAGE_HEIGHT), CvType.CV_32F);
	}

	@Override
	public void detect() {
		Mat substractedMogMask = new Mat();
		mog.setVarThreshold(20);
		mog.apply(previewMat, substractedMogMask, 0);

		workMat = MatOperations.maskMat(workMat, substractedMogMask);
		handMat = segmentHandBinary(workMat, 180, 9);
		goodsMat = getGoods(workMat, handMat);

		processGoodsMat();
		List <MatOfPoint> filteredGoodsContours = findAndfilterGoodsContours(goodsMat);		 
		drawRectAroundGoods(filteredGoodsContours);
		if (!isBackgroundOnly(previewMat)) 
			goodsContourFeatures(filteredGoodsContours, printInfo);
		refreshBackground(previewMat);
		if (filteredGoodsContours.size() > 0 && isHandInView(previewMat)) result = DetectionResult.HAND_WITH_GOODS;
		else if (isHandInView(previewMat)) result =  DetectionResult.EMPTY_HAND;
		else if (isBackgroundOnly(previewMat)) result = DetectionResult.BACKGROUND;
		else result = DetectionResult.UNDEFINED;
			
	}

	private void drawRectAroundGoods(List <MatOfPoint> goodsContours) {
		for (MatOfPoint mop : goodsContours) {
			RotatedRect rotRect = Imgproc.fitEllipse(new MatOfPoint2f(mop.toArray()));
			Point[] points = new Point[4];
			rotRect.points(points);
			MatOperations.drawMinBoundingRect(goodsMat, points);		
		}
	}

	private void processGoodsMat() {
		splitGoodsFromNoise();
		goodsMat = MatOperations.erode(goodsMat, 4, 3);
		goodsMat = MatOperations.morphClose(goodsMat, 25, 10);
		goodsMat = MatOperations.morphOpen(goodsMat, 5, 5);

	}

	private void splitGoodsFromNoise() {
		MatOfPoint joinedHandContours = getJoinedHandContours();
		if (!joinedHandContours.empty()) {
			MatOfPoint aproxedHand = MatOperations.aproxCurve(joinedHandContours, 0.5, false);
			MatOfInt aproxedHandHull = MatOperations.convexHull(aproxedHand);
			MatOfInt4 convexDefects = MatOperations.convexityDefects(aproxedHand, aproxedHandHull);
			List<Integer> convexDefectsList = convexDefects.toList();
			Point biggestDefect = new Point(), biggestStart = new Point(), biggestEnd = new Point();
			
			int biggestDepth = getBiggestDeffectInfo(convexDefectsList, aproxedHand, biggestDefect, biggestStart, biggestEnd);

			if (convexDefectsList.size() > 0 && biggestDepth > 5) { // TODO
				drawBiggestHandPoints(handMat, biggestDefect, biggestStart, biggestEnd);
				drawHandSplitLines(goodsMat, biggestDefect, biggestStart, biggestEnd);
			}
		}
	}

	private int getBiggestDeffectInfo(List<Integer> convexDefectsList, MatOfPoint aproxedHand, Point biggestDefect,
			Point biggestStart, Point biggestEnd) {
		Point data[] = aproxedHand.toArray();
		int biggestDepth = -1;
		for (int j = 0; j < convexDefectsList.size(); j = j + 4) {
			Point start = data[convexDefectsList.get(j)];
			Point end = data[convexDefectsList.get(j + 1)];
			Point defect = data[convexDefectsList.get(j + 2)];
			int depth = convexDefectsList.get(j + 3) / 256;
			if (biggestDepth < depth) {
				biggestDepth = depth;
				biggestDefect.set(new double [] {defect.x,defect.y});
				biggestStart.set(new double [] {start.x,start.y});
				biggestEnd.set(new double [] {end.x,end.y});
			}
		}
		return biggestDepth;
	}

	private MatOfPoint getJoinedHandContours() {
		List<MatOfPoint> handContours = MatOperations.findContours(handMat, 0, 0);
		Point[] contoursPoints = new Point[0];
		for (int i = 0; i < handContours.size(); ++i)
			contoursPoints = Stream.concat(Arrays.stream(contoursPoints), Arrays.stream(handContours.get(i).toArray()))
					.toArray(Point[]::new);
		return new MatOfPoint(contoursPoints);
	}

	public Mat getHandMask(Mat mat, Rect roiRect, int binaryThreshold, int dilateSize) {
		Mat segmentedHandRoiMask = mat.submat(roiRect);
		Imgproc.threshold(segmentedHandRoiMask, segmentedHandRoiMask, binaryThreshold, 255, Imgproc.THRESH_BINARY);
		Imgproc.dilate(segmentedHandRoiMask, segmentedHandRoiMask,
				Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(dilateSize, dilateSize)));
		return segmentedHandRoiMask;
	}

	public Mat getGoods(Mat substracted, Mat segmentedHandMask) {
		Mat segmentedGoods = Mat.zeros(substracted.size(), substracted.type());
		Mat segmentedHand = MatOperations.invert(segmentedHandMask);
		substracted.copyTo(segmentedGoods, segmentedHand);
		return segmentedGoods;
	}

	public List<MatOfPoint> findAndfilterGoodsContours(Mat segmentedGoods) {
		List<MatOfPoint> goodsContours = MatOperations.findContours(segmentedGoods, 0, 0);
		List<MatOfPoint> filteredContours = new ArrayList<>();

		for (MatOfPoint mop : goodsContours) {
			if (mop.toArray().length < 5) continue; // < 5 is nonsese and will end in error
//			RotatedRect rotRect = Imgproc.fitEllipse(new MatOfPoint2f(mop.toArray()));
//			if (rotRect.size.width < 20 || rotRect.size.height < 20) continue; //TODO		
			filteredContours.add(mop);
		}
		return filteredContours;
	}

	public void drawBiggestHandPoints(Mat handMat, Point biggestDefect, Point biggestStart, Point biggestEnd) {
		Imgproc.circle(handMat, biggestStart, 5, Scalar.all(255), 2);
		Imgproc.circle(handMat, biggestEnd, 5, Scalar.all(255), 2);
		Imgproc.circle(handMat, biggestDefect, 5, Scalar.all(255), 2);
	}

	public void drawHandSplitLines(Mat goodsMat, Point biggestDefect, Point biggestStart, Point biggestEnd) {
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

		Imgproc.line(goodsMat, p3, p4, Scalar.all(0), 3);
		Imgproc.line(goodsMat, p1, biggestStart, Scalar.all(0), 3);
		Imgproc.line(goodsMat, p2, biggestEnd, Scalar.all(0), 3);
	}

	public void refreshBackground(Mat mat) {
		Mat convertedBg = new Mat();
		background.convertTo(convertedBg, CvType.CV_8U);
//		handMat = convertedBg;
		if (isBackgroundOnly(mat) && !isHandInShelf(mat)) {
			Imgproc.accumulateWeighted(mat, background, 0.5);//				
			mog = Video.createBackgroundSubtractorMOG2(MOG_HISTORY, MOG_THRESHOLD, true);
			mog.apply(convertedBg, new Mat());
		}
	}

	public void test() {
		// Mat first = new Mat(segmentedHandRoiMask.size(),
		// segmentedHandRoiMask.type());
		// Mat second = new Mat(segmentedHandRoiMask.size(),
		// segmentedHandRoiMask.type());
		//
		// Imgproc.threshold(segmentedHandRoiMask, first, binaryThreshold , 255,
		// Imgproc.THRESH_BINARY);
		// Imgproc.adaptiveThreshold(segmentedHandRoiMask, second, 255,
		// Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 1555,
		// -210);
		// Core.bitwise_or(first, second, segmentedHandRoiMask);
		// Imgproc.dilate(segmentedHandRoiMask, segmentedHandRoiMask,
		// Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new
		// Size(dilateSize,dilateSize)));
	}

}
