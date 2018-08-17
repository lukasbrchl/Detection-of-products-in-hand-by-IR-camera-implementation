package algorithm.detector;

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

import algorithm.detector.domain.DetectionResult;
import algorithm.settings.domain.MogSettings;
import algorithm.settings.domain.SettingsWrapper;
import image.MatOperations;

/**
* Class containing methods for successfully detecting goods on the scene using background substract alghoritm.
* 
* @author Lukas Brchl
*/
public class MogDetector extends AbstractDetector {

	private BackgroundSubtractorMOG2 bcgSubstractor;
	
	private Mat background;
	private MogSettings mog;	

	public MogDetector(SettingsWrapper settings) {
		super(settings);
		mog =  (MogSettings) settings.getByCls(MogSettings.class);	
		Video.createBackgroundSubtractorMOG2(mog.getMogHistory(), mog.getMogThreshold(), true);
		background = Mat.zeros(new Size(IMAGE_WIDTH, IMAGE_HEIGHT), CvType.CV_32F);
	}

	@Override
	public DetectionResult detect() {
		if (isBackgroundOnly(previewMat, mog.getHandThreshold(), mog.getBcgCanny1(), mog.getBcgCanny2())) {
			refreshBackground(previewMat);
			return DetectionResult.BACKGROUND;
		}

		Mat substractedMogMask = new Mat();
		bcgSubstractor.setVarThreshold(mog.getMogThreshold());
		bcgSubstractor.apply(previewMat, substractedMogMask, 0);

		workMat = MatOperations.maskMat(workMat, substractedMogMask);
		handMat = segmentHandBinary(workMat, mog.getHandThreshold(), mog.getHandMaskDilate());
		goodsMat = getGoods(workMat, handMat);

		processGoodsMat();
		List <MatOfPoint> filteredGoodsContours = findAndfilterGoodsContours(goodsMat);		 
//		drawRectAroundGoods(filteredGoodsContours);
		if (printInfo) System.out.println(goodsContourFeatures(filteredGoodsContours));
		
		if (filteredGoodsContours.size() > 0 && isHandInView(previewMat, mog.getHandThreshold())) return DetectionResult.HAND_WITH_GOODS;
		else if (isHandInView(previewMat, mog.getHandThreshold())) return DetectionResult.EMPTY_HAND;
		else return DetectionResult.UNDEFINED;
			
	}
	
	/**
	* Updates stored background with input mat.
	* 
	* @param mat	input background mat
	*/
	public void refreshBackground(Mat mat) {
		Mat convertedBg = new Mat();
		background.convertTo(convertedBg, CvType.CV_8U);
		Imgproc.accumulateWeighted(mat, background, mog.getBcgLearningRate());//				
		bcgSubstractor = Video.createBackgroundSubtractorMOG2(mog.getMogHistory(), mog.getMogThreshold(), true);
		bcgSubstractor.apply(convertedBg, new Mat());
	}
	
	/**
	* Combines input mat with input mask creating separate goods mat.
	* 
	* @param substracted			substracted hang with goods from background
	* @param segmentedHandMask		hand mask
	* @return mat containing only goods or noise
	*/
	public Mat getGoods(Mat substracted, Mat segmentedHandMask) {
		Mat segmentedGoods = Mat.zeros(substracted.size(), substracted.type());
		Mat segmentedHand = MatOperations.invert(segmentedHandMask);
		substracted.copyTo(segmentedGoods, segmentedHand);
		return segmentedGoods;
	}

	/**
	* Remove noise and artifacts from goods mat.
	*/
	private void processGoodsMat() {
		splitGoodsFromNoise();
		goodsMat = MatOperations.erode(goodsMat, mog.getGoodsErodeSize(), mog.getGoodsErodeIter());
		goodsMat = MatOperations.morphClose(goodsMat, mog.getGoodsCloseSize(), mog.getGoodsCloseIter());
		goodsMat = MatOperations.morphOpen(goodsMat, mog.getGoodsOpenSize(), mog.getGoodsOpenIter());
	}

	/**
	* Draws lines around biggest convex points of hand to split hand glow from goods.
	*/
	private void splitGoodsFromNoise() {
		MatOfPoint joinedHandContours = getJoinedHandContours();
		if (!joinedHandContours.empty()) {
			MatOfPoint aproxedHand = MatOperations.aproxCurve(joinedHandContours, 0.5, false);
			MatOfInt aproxedHandHull = MatOperations.convexHull(aproxedHand);
			MatOfInt4 convexDefects = MatOperations.convexityDefects(aproxedHand, aproxedHandHull);
			List<Integer> convexDefectsList = convexDefects.toList();
			Point biggestDefect = new Point(), biggestStart = new Point(), biggestEnd = new Point();
			
			int biggestDepth = getBiggestDeffectInfo(convexDefectsList, aproxedHand.toArray(), biggestDefect, biggestStart, biggestEnd);

			if (convexDefectsList.size() > 0 && biggestDepth > mog.getHandMinConvexDepth()) {
				drawBiggestHandPoints(handMat, biggestDefect, biggestStart, biggestEnd);
				drawHandSplitLines(goodsMat, biggestDefect, biggestStart, biggestEnd);
			}
		}
	}

	/**
	* Finds biggest convex depth and corresponding points.
	* 
	* @param convexDefectsList	list of convex defects
	* @param data 				array of hand aproximed hand points
	* @param biggestDefect		output param
	* @param biggestStart		output param
	* @param biggestEnd		output param
	* @return biggest convex depth
	*/
	private int getBiggestDeffectInfo(List<Integer> convexDefectsList, Point data[], Point biggestDefect, Point biggestStart, Point biggestEnd) {
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

	/**
	* Joins hand contours into single MatOfPoint.
	* 
	* @return joined contours
	*/
	private MatOfPoint getJoinedHandContours() {
		List<MatOfPoint> handContours = MatOperations.findContours(handMat, 0, 0);
		Point[] contoursPoints = new Point[0];
		for (int i = 0; i < handContours.size(); ++i)
			contoursPoints = Stream.concat(Arrays.stream(contoursPoints), Arrays.stream(handContours.get(i).toArray()))
					.toArray(Point[]::new);
		return new MatOfPoint(contoursPoints);
	}

	/**
	* Find and filter out uninteresting contours from goods mat.
	* 
	* @param segmentedGoods		Mat containing only goods
	* @return found and filtered goods contours
	*/
	public List<MatOfPoint> findAndfilterGoodsContours(Mat segmentedGoods) {
		List<MatOfPoint> goodsContours = MatOperations.findContours(segmentedGoods, 0, 0);
		List<MatOfPoint> filteredContours = new ArrayList<>();

		for (MatOfPoint mop : goodsContours) {
			if (mop.toArray().length < 5) continue; // < 5 is nonsese and will end in error
			filteredContours.add(mop);
		}
		return filteredContours;
	}
	
	/**
	* Draws biggest convex points on the input mat.
	* 
	* @param handMat		Mat to draw on
	* @param biggestDefect	convexity deffect point
	* @param biggestStart	convexity deffect point
	* @param biggestEnd		convexity deffect point
	*/
	public void drawBiggestHandPoints(Mat handMat, Point biggestDefect, Point biggestStart, Point biggestEnd) {
		Imgproc.circle(handMat, biggestStart, 5, Scalar.all(255), 2);
		Imgproc.circle(handMat, biggestEnd, 5, Scalar.all(255), 2);
		Imgproc.circle(handMat, biggestDefect, 5, Scalar.all(255), 2);
	}

	/**
	* Draws parallel split lines around hand to separate goods from hand glow
	* 
	* @param goodsMat		Mat to draw on
	* @param biggestDefect	convexity deffect point
	* @param biggestStart	convexity deffect point
	* @param biggestEnd		convexity deffect point
	*/
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
	
	/**
	* Draws min bounding rect around input contours.
	* 
	* @param goodsContours	contours which will be highlighted
	*/
	private void drawRectAroundGoods(List <MatOfPoint> goodsContours) {
		for (MatOfPoint mop : goodsContours) {
			RotatedRect rotRect = Imgproc.fitEllipse(new MatOfPoint2f(mop.toArray()));
			Point[] points = new Point[4];
			rotRect.points(points);
			MatOperations.drawMinBoundingRect(goodsMat, points);		
		}
	}

	public Mat getBackground() {
		Mat bcg = new Mat();
		background.convertTo(bcg, CvType.CV_8U);
		return bcg;
	}

	public void setBackground(Mat background) {
		this.background = background;
	}

	
}
