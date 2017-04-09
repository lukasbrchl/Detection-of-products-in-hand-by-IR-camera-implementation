package image;

import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

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
