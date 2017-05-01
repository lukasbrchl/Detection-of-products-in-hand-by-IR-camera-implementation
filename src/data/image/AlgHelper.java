package data.image;

import org.opencv.core.Mat;

public final class AlgHelper {
	
	public static Mat segmentHandBinary(Mat mat, double threshold) {
		Mat result = new Mat(mat.size(), mat.type());	    
		result =  MatOperations.binaryTreshold(mat, threshold);
		return result;
	}
	
//	public static Mat extendBinaryMask(Mat mat, int iterations, int size) {
//		
//	}
	
}
