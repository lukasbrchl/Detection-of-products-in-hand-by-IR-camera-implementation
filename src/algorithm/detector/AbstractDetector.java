package algorithm.detector;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import algorithm.detector.domain.DetectionResult;
import algorithm.settings.domain.PreprocessingSettings;
import algorithm.settings.domain.PreviewSettings;
import algorithm.settings.domain.SettingsWrapper;
import application.MainController;
import data.reciever.domain.ImageData;
import image.MatOperations;
import image.domain.Contour;

/**
 * Superclass for all detector algorithms.
 * Contains necceseary methods like hand segmentation, background detection for other subclass algorithms.
 *
 * @author Lukas Brchl
 */
public abstract class AbstractDetector {

    public static final int IMAGE_WIDTH = 640;
    public static final int IMAGE_HEIGHT = 150;
    public static final int PANEL_IMAGE_WIDTH = 160;
    public static final int PANEL_IMAGE_HEIGHT = 37;

    protected Mat originalMat, scaledMat, previewMat;
    protected Mat workMat, handMat, goodsMat;
    protected SettingsWrapper settings;
    protected boolean printInfo;
    protected ImageData data;

    public AbstractDetector(SettingsWrapper settings) {
        this.settings = settings;
    }

    /**
     * Starts detection alghoritm on created mats.
     *
     * @return result of detection
     */
    public abstract DetectionResult detect();

    /**
     * Intitialize mats needed for alghoritm run.
     *
     * @param data data needed for creating Mat object
     */
    public void initMats(ImageData data) {
        this.data = data;
        byte[] byteArray = data.getData();
        originalMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, false);
        if (((PreprocessingSettings) settings.getByCls(PreprocessingSettings.class)).isScale())
            scaledMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, ((PreprocessingSettings) settings.getByCls(PreprocessingSettings.class)).getTempMin(), ((PreprocessingSettings) settings.getByCls(PreprocessingSettings.class)).getTempMax());
        else
            scaledMat = MatOperations.createMat(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT, true);

        previewMat = preprocessPreviewMat(scaledMat);
        workMat = preprocessWorkMat(scaledMat);
        handMat = goodsMat = Mat.zeros(originalMat.size(), originalMat.type());
    }

    /**
     * Preproccess input mat based on options selected in preview tab.
     *
     * @param mat input mat
     * @return preprocessed mat
     */
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

    /**
     * Preproccess input mat based on options selected in preprocess tab.
     *
     * @param mat input mat
     * @return preprocessed mat
     */
    public Mat preprocessWorkMat(Mat mat) {
        PreprocessingSettings pgs = ((PreprocessingSettings) settings.getByCls(PreprocessingSettings.class));
        Mat result = mat.clone();
        result = MatOperations.clache(result, pgs.getClacheSize1(), pgs.getClacheSize2(), pgs.getClacheClip());
        result = MatOperations.brightnessContrast(result, pgs.getBrightnessParam1(), pgs.getContrastParam1());
        result = MatOperations.addMult(result, pgs.getBrightnessParam2(), pgs.getContrastParam1());
        result = MatOperations.medianBlur(result, pgs.getMedianSize());
        result = MatOperations.bilateralBlur(result, pgs.getBilateralSize1(), pgs.getBilateralSize2(), pgs.getBilateralSigma());
        result = MatOperations.gaussianBlur(result, pgs.getGaussianSize1(), pgs.getGaussianSize2(), pgs.getGaussianSigma());
        return result;
    }

    /**
     * Segments input mat with binary threshold operation.
     *
     * @param mat       input mat
     * @param threshold input binary threshold
     * @return hand mask
     */
    public Mat segmentHandBinary(Mat mat, double threshold) {
        return segmentHandBinary(mat, threshold, 1);
    }

    /**
     * Segments input mat with binary threshold operation.
     *
     * @param mat        input mat
     * @param threshold  input binary threshold
     * @param dilateSize mask enlarge
     * @return hand mask
     */
    public Mat segmentHandBinary(Mat mat, double threshold, int dilateSize) {
        Mat result = new Mat(mat.size(), mat.type());
        result = MatOperations.binaryTreshold(mat, threshold);
        Imgproc.dilate(result, result, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilateSize, dilateSize)));
        return result;
    }

    /**
     * Determines if input mat is background only.
     *
     * @param mat          input mat
     * @param handThresh   input binary threshold for hand segmentation
     * @param canny1Thresh first threshold for canny edge alghoritm
     * @param canny2Thresh second threshold for canny edge alghoritm
     * @return true if input mat is background
     */
    public boolean isBackgroundOnly(Mat mat, int handThresh, int canny1Thresh, int canny2Thresh) {
        Mat items = MatOperations.doCannyEdgeDetection(mat, canny1Thresh, canny2Thresh);
        List<MatOfPoint> contoursCanny = MatOperations.findContours(items, 5, 5);
        return contoursCanny.size() == 0 && !isHandInView(mat, handThresh);
    }

    /**
     * Determines if input mat contains hand.
     *
     * @param mat        input mat
     * @param handThresh input binary threshold for hand segmentation
     * @return true if input mat contains hand
     */
    public boolean isHandInView(Mat mat, int handThresh) {
        Mat something = segmentHandBinary(mat, 200);
        List<MatOfPoint> contoursThreshold = MatOperations.findContours(something, 5, 5);
        return !(contoursThreshold.size() == 0);
    }

    /**
     * Determines if input mat contains hand and if hand is inside shelf.
     *
     * @param mat        input mat
     * @param handThresh input binary threshold for hand segmentation
     * @return true if input mat has hand which is inside shelf
     */
    public boolean isHandInShelf(Mat mat, int handThresh) {
        if (!isHandInView(mat, handThresh)) return false;
        Mat segmentedHand = segmentHandBinary(mat, 220);
        segmentedHand = MatOperations.erode(segmentedHand, 25, 10);
        MatOfPoint mop = new MatOfPoint();
        Core.findNonZero(segmentedHand, mop);
        if (Imgproc.boundingRect(mop).height >= IMAGE_HEIGHT) return true;
        return false;
    }

    /**
     * Calculates predefined atributes on input contours.
     *
     * @param contours input contours
     * @return csv formated string with contour features
     */
    public String goodsContourFeatures(List<MatOfPoint> contours) {
        StringBuilder sb = new StringBuilder();
        MatOfPoint biggest = MatOperations.findBiggestContour(contours);
        //biggest
        float length = 0, area = 0, minDiameter = 0, maxDiameter = 0, convexLength = 0, convexArea = 0, formFactor = 0,
                roundness = 0, aspectRatio = 0, convexity = 0, solidity = 0, compactness = 0, extent = 0;
        if (biggest != null && biggest.toArray().length > 0) {
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
        sb.append(data.getFilename() + ";");
        sb.append(length + ";" + area + ";" + minDiameter + ";" + maxDiameter + ";" + convexLength + ";" + convexArea + ";" + formFactor
                + ";" + roundness + ";" + aspectRatio + ";" + convexity + ";" + solidity + ";" + compactness + ";" + extent + ";");

        //contours overall
        int contoursCount = contours.size();
        float areaSum = 0, areaMin = Float.MAX_VALUE, areaMax = 0, areaAvg = 0;
        float lengthSum = 0, lengthMin = Float.MAX_VALUE, lengthMax = 0, lengthAvg = 0;
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
        areaAvg = contoursCount != 0 ? areaSum / contoursCount : 0;
        lengthAvg = contoursCount != 0 ? lengthSum / contoursCount : 0;

        // Write feature values
//		sb.append(contoursCount + ";" +  lengthMin + ";" + lengthMax + ";" + lengthSum + ";" + lengthAvg + ";" + areaMin + ";" + areaMax + ";" + areaSum + ";" + areaAvg + ";");
//		sb.append(contoursCount + ";" +  lengthMin + ";" + lengthSum + ";" + lengthAvg + ";" + areaMin + ";"  + areaSum + ";" + areaAvg + ";");

        //Category has to be choosen and written at the end
//		sb.append("empty_hand");
//		sb.append("hand_with_goods");

        return sb.toString();
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

}
