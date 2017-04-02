package utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;


public class ImageConvertor {

	public static BufferedImage convertBinaryToBufferedImage(byte[] byteArray, int imageWidth, int imageHeight) {
		float min = bytesToCelsius(getMin(byteArray));
		float max = bytesToCelsius(getMax(byteArray));
		return convertBinaryToBufferedImage(byteArray, imageWidth, imageHeight, min, max);
	}
	
	public static BufferedImage convertBinaryToBufferedImage(byte[] byteArray, int imageWidth, int imageHeight, float interval) {
		float origMin = bytesToCelsius(getMin(byteArray));
		float origMax = bytesToCelsius(getMax(byteArray));
		float avg = (origMin + origMax)/2 - 3.5f;
		float min = avg - interval/2;
		float max = avg + interval/2;
		if (origMax - origMin < 5)
			return convertBinaryToBufferedImage(byteArray, imageWidth, imageHeight);
		return convertBinaryToBufferedImage(byteArray, imageWidth, imageHeight, min, max);
	}
	
	
	public static BufferedImage convertBinaryToBufferedImage(byte[] byteArray, int imageWidth, int imageHeight, float min, float max) {
		try {
			BufferedImage outputImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_GRAY);
			int counter = 0;
			WritableRaster raster = outputImage.getRaster();
			for (int y = 0; y < outputImage.getHeight(); ++y) {
				for (int x = 0; x < outputImage.getWidth(); ++x) {
					float temp  = bytesToCelsius(readTwoBytes(byteArray, counter));
					int normalizedVal = (int) (normalizeToByte(temp, min, max));
//					Color color = new Color(normalizedVal, normalizedVal, normalizedVal);
//					outputImage.setRGB(x, y, color.getRGB());
					raster.setPixel(x, y, new int[] {normalizedVal});
					counter += 2;
				} 
				
			}
			return outputImage;			
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return null;
	}
	
	public static Image convertBinaryToImage(byte[] byteArray, int imageWidth, int imageHeight) {
		float min = bytesToCelsius(getMin(byteArray));
		float max = bytesToCelsius(getMax(byteArray));
		return convertBinaryToImage(byteArray, imageWidth, imageHeight, min, max);
	}
	
	public static Image convertBinaryToImage(byte[] byteArray, int imageWidth, int imageHeight, float min, float max) {
	    WritableImage image = new WritableImage(imageWidth, imageHeight);
	    PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteRgbInstance();
	    byte [] pixels = new byte[imageWidth * imageHeight * 3]; // * 3 because Image needs 3 bytes per pixel even if grayscale

	    for (int i = 0, cnt = 0; i < byteArray.length; i += 2, cnt += 3) {
	    	float value = normalizeToByte(bytesToCelsius(readTwoBytes(byteArray, i)), min, max);
	    	pixels[cnt] = (byte) value;
	    	pixels[cnt + 1] = (byte) value;
	    	pixels[cnt + 2] = (byte) value;
	    }	    
	    image.getPixelWriter().setPixels(0, 0, imageWidth, imageHeight, pixelFormat, pixels, 0, imageWidth * 3);
	    return image;
	}
		
	public static Mat convertBinaryToMat(byte[] byteArray, int imageWidth, int imageHeight) {
		byte [] pixels = ((DataBufferByte) convertBinaryToBufferedImage(byteArray, imageWidth, imageHeight).getRaster().getDataBuffer()).getData(); //TODO: lot ineffective			
		Mat mat = new Mat(imageHeight, imageWidth, CvType.CV_8U);
		mat.put(0, 0, pixels);
		return mat;
	}	
	
	public static Mat convertBinaryToMat(byte[] byteArray, int imageWidth, int imageHeight, float interval) {
		byte [] pixels = ((DataBufferByte) convertBinaryToBufferedImage(byteArray, imageWidth, imageHeight, interval).getRaster().getDataBuffer()).getData();			
		Mat mat = new Mat( imageHeight, imageWidth, CvType.CV_8U);
		mat.put(0, 0, pixels);
		return mat;
	}
	
	public static Mat convertBinaryToMat(byte[] byteArray, int imageWidth, int imageHeight, float min, float max) {
		byte [] pixels = ((DataBufferByte) convertBinaryToBufferedImage(byteArray, imageWidth, imageHeight, min, max).getRaster().getDataBuffer()).getData();			
		Mat mat = new Mat( imageHeight, imageWidth, CvType.CV_8U);
		mat.put(0, 0, pixels);
		return mat;
	}
	
	public static BufferedImage convertMatToBufferedImage(Mat m){
 	    byte [] buffer = new byte[m.channels()*m.cols()*m.rows()];
 	    m.get(0, 0, buffer); 
 	    BufferedImage image = new BufferedImage(m.cols(), m.rows(), BufferedImage.TYPE_BYTE_GRAY);
 	    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
 	    System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);  
 	    
 	    return image;
 	}
	 
	public static Image convertMatToImage(Mat m) {
		WritableImage image = new WritableImage(m.width(), m.height());
		byte [] data = new byte[m.cols() * m.rows() * 3]; // * 3 because Image needs 3 bytes per pixel even if grayscale
		Mat rgbMat = new Mat();  
		if (m.channels() > 2)
			rgbMat = m;
		else 
			Imgproc.cvtColor(m, rgbMat, Imgproc.COLOR_GRAY2RGB); //TODO: inefficient, but is there other way? SwingFXUtils.toFXImage = slow
		rgbMat.get(0, 0, data);
	    image.getPixelWriter().setPixels(0, 0, m.width(), m.height(), PixelFormat.getByteRgbInstance(), data, 0, m.width()*3); 
 	    return image;
	 	}
	
	 //helper methods
	public static float bytesToCelsius(int value) {
		return ((float) value * 0.04f) - 273.15f; // * 0.04 FLIR Ax5 constant + kelvin to celsius
	}

	public static int unsignedToSigned(byte a) {
		return a & 0xFF;
	}
	
	public static float normalizeToByte(float value, float min, float max) {
		float newMin = 0.0f, newMax = 255.0f;
		return normalize(value, min, max, newMin, newMax);
	}

	public static float normalize(float value, float min, float max, float newMin, float newMax) {
		if (value > max) value = max;
		if (value < min) value = min;
		float normalized = (newMax - newMin) / (max - min) * (value - max) + newMax;
		return normalized;
	}

	//TODO: inefficient
	public static int getMax(byte[] inputArray) {
		int maxValue = 0;
		for (int i = 0; i < inputArray.length; i += 2) {
			int value = readTwoBytes(inputArray, i);
			if (value > maxValue) maxValue = value;
		}
		return maxValue;
	}

	public static int getMin(byte[] inputArray) {
		int minValue = Integer.MAX_VALUE;
		for (int i = 0; i < inputArray.length; i += 2) {
			int value = readTwoBytes(inputArray, i);
			if (value < minValue) minValue = value;
		}
		return minValue;
	}
	
	public static int getAvg(byte [] inputArray) {
		int counter = 0;
		int sum = 0;
		for (int i = 0; i < inputArray.length; i += 2) {
			sum += readTwoBytes(inputArray, i);
			++counter;
		}
		return sum/counter;
	}
	
	public static int getMedian(byte[] inputArray) {
		int[] convertedArray = new int[inputArray.length/2];
		int cnt = 0;
		for (int i = 0; i < inputArray.length; i += 2) {
			convertedArray[cnt++] = readTwoBytes(inputArray, i);
		}
		Arrays.sort(convertedArray);
		int middle = convertedArray.length/2;
		int medianValue = 0; //declare variable 
		if (convertedArray.length%2 == 1) 
		    medianValue = convertedArray[middle];
		else
		   medianValue = (convertedArray[middle-1] + convertedArray[middle]) / 2;
		return medianValue;
	}

	public static int readTwoBytes(byte[] inputArray, int index) {
		int firstByte = unsignedToSigned(inputArray[index]);
		int secondByte = unsignedToSigned(inputArray[index + 1]);
		return (secondByte << 8) | firstByte;
	}
	
	//debugging purpose
	public void saveAsAsciiPgm(byte[] byteArray, int imageWidth, int imageHeight, String fileName) {
		try {
			float min = bytesToCelsius(getMin(byteArray));
			float max = bytesToCelsius(getMax(byteArray));
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
			writer.write("P2 " + imageWidth + " " + imageHeight + " " + 255 + "\n");

			for (int i = 0; i < byteArray.length; i += 2) {
				String str = Integer.toString((int) normalizeToByte(bytesToCelsius(readTwoBytes(byteArray, i)), min, max));
				writer.write(str + " ");
				if (i % (640 * 2) == 0 && i != 0) writer.write("\n"); // 2 bytes = 1 pixel
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
