/*
 * Copyright (c) 2015, Surmon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * - Neither the name of the Surmon project nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package image.domain;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

/**
*
* @author Jakub Novák <jakub.novak@surmon.org>
*/
public class Contour extends MatOfPoint {

    private final MatOfPoint data;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private MatOfPoint convexHull;

    private double area;
    private double perimeter;
    private double convexArea;
    private double convexPerimeter;
    private double maxDiameter;
    private double minDiameter;

    // Not finished descriptors
    private double elongation;
    private double curl;
    private double modificationRatio;

    public Contour(MatOfPoint data) {
        this.data = data;
        calculate();
    }

    private void calculate() {
        area = ShapeDescriptions.area(data);
        perimeter = ShapeDescriptions.perimeter(data);
        convexHull = ShapeDescriptions.convexHull(data);
        convexArea = ShapeDescriptions.area(getConvexHull());
        convexPerimeter = ShapeDescriptions.perimeter(getConvexHull());

        double[] ds = ShapeDescriptions.diameters(data);
        maxDiameter = Math.max(ds[0], ds[1]);
        minDiameter = Math.min(ds[0], ds[1]);
    }

    public MatOfPoint getData() {
        return data;
    }

    public double getLength() {
        return perimeter;
    }

    public double getArea() {
        return area;
    }

    public MatOfPoint getConvexHull() {
        return convexHull;
    }

    public double getConvexLength() {
        return convexPerimeter;
    }

    public double getConvexArea() {
        return convexArea;
    }
    
    public double getFormFactor() {
        return (4 * Math.PI * area) / Math.pow(perimeter, 2);
    }

    public double getRoundness() {
        return (4 * area) / (Math.PI * Math.pow(maxDiameter, 2));
    }

    public double getAspectRatio() {
        return maxDiameter / minDiameter;
    }

    public double getConvexity() {
        return convexPerimeter / perimeter;
    }

    public double getSolidity() {
        return area / convexArea;
    }

    public double getCompactness() {
        return Math.sqrt((4 / Math.PI) * area) / maxDiameter;
    }

    public double getExtent() {
        return area / (maxDiameter * minDiameter);
    }

    public Point getMassCenter() {
        Moments moments = Imgproc.moments(data);

        Point centroid = new Point();
        centroid.x = moments.get_m10() / moments.get_m00();
        centroid.y = moments.get_m01() / moments.get_m00();

        return centroid;
    }    
    
    public double getMaxDiameter() {
		return maxDiameter;
	}

	public double getMinDiameter() {
		return minDiameter;
	}

	/**
     * Creates binary image from this contour.
     *
     * @param useConvexHull If TRUE the convex hull of contour is used instead.
     * @return Binary image that represents contour or its convex hull.
     */
    public Mat segmentImage(Mat source, boolean useConvexHull) {
        Mat mask = Mat.zeros(source.size(), CvType.CV_8U);
        List<MatOfPoint> list = new ArrayList<>(1);

        if (useConvexHull) {
            list.add(getConvexHull());
        } else {
            list.add(data);
        }
        Imgproc.drawContours(mask, list, -1, Scalar.all(255), Core.FILLED);

        Mat result = new Mat();
        source.copyTo(result, mask);
        return result.submat(Imgproc.boundingRect(data));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Length: \t").append(df.format(getLength())).append("; ");
        sb.append("Area: \t").append(df.format(getArea())).append("; ");
        sb.append("Formfactor: \t").append(df.format(getFormFactor())).append("; ");
        sb.append("Roundness: \t").append(df.format(getRoundness())).append("; ");
        sb.append("Aspect Ratio: \t").append(df.format(getAspectRatio())).append("; ");
        sb.append("Convexity: \t").append(df.format(getConvexity())).append("; ");
        sb.append("Solidity: \t").append(df.format(getSolidity())).append("; ");
        sb.append("Compactness: \t").append(df.format(getCompactness())).append("; ");
        sb.append("Extent: \t").append(df.format(getExtent()));
        return sb.toString();
    }
}