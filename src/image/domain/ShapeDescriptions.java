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

import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
*
* @author Jakub Novák <jakub.novak@surmon.org>
*/
public class ShapeDescriptions {
	 public static double area(MatOfPoint contour) {
	        return Imgproc.contourArea(contour);
	    }

	    public static double perimeter(MatOfPoint contour) {
	        return Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
	    }

	    public static double[] diameters(MatOfPoint contour) {
	        Size s = enclosingRectangle(contour).size;
	        return new double[]{s.width, s.height};
	    }

	    /**
	     * @deprecated It can be substitute by
	     * ShapeDescriptions.area(ShapeDescriptions.convexHull())
	     * @param contour
	     * @return area of convex hull
	     */
	    public static double convexArea(MatOfPoint contour) {
	        return area(convexHull(contour));
	    }

	    /**
	     * @deprecated It can be substitute by
	     * ShapeDescriptions.perimeter(ShapeDescriptions.convexHull())
	     * @param contour
	     * @return length of convex hull
	     */
	    public static double convexPerimeter(MatOfPoint contour) {
	        return perimeter(convexHull(contour));
	    }

	    public static double[] convexDiameters(MatOfPoint contour) {
	        return diameters(convexHull(contour));
	    }

	    // Convex Hull
	    public static MatOfPoint convexHull(MatOfPoint contour) {
	        MatOfInt indexes = new MatOfInt();
	        Imgproc.convexHull(contour, indexes);
	        Point[] convexPoint = new Point[indexes.rows()];

	        double x, y;
	        int index;
	        for (int i = 0; i < indexes.rows(); i++) {
	            index = (int) indexes.get(i, 0)[0];
	            x = contour.get(index, 0)[0];
	            y = contour.get(index, 0)[1];
	            convexPoint[i] = new Point(x, y);
	        }

	        return new MatOfPoint(convexPoint);
	    }

	    public static RotatedRect enclosingRectangle(MatOfPoint contour) {
	        return Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
	    }
}
