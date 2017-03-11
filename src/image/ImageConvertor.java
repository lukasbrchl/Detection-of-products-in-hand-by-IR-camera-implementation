package image;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.color.ColorSpace;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class ImageConvertor {
	private final int imageWidth, imageHeight;	
	
	public ImageConvertor(int width,int height) {
		imageWidth = width;
		imageHeight = height;
	}
	
	//debugging purpose
	public void saveAsAsciiPgm(byte[] byteArray, String fileName) {
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

	public BufferedImage convertBinaryToImage(byte[] byteArray) {
		float min = bytesToCelsius(getMin(byteArray));
		float max = bytesToCelsius(getMax(byteArray));
		return convertBinaryToImage(byteArray, min, max);
	}
	
	public BufferedImage convertBinaryToImage(byte[] byteArray, float min, float max) {
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
	
	public Mat convertBinaryToMat(byte[] byteArray) {
		byte [] pixels = ((DataBufferByte) convertBinaryToImage(byteArray).getRaster().getDataBuffer()).getData(); //TODO: lot ineffective			
		Mat mat = new Mat(imageWidth, imageHeight, CvType.CV_8U);
		mat.put(0, 0, pixels);
		return mat;
	}
	
	public Mat convertBinaryToMat(byte[] byteArray, float min, float max) {
		byte [] pixels = ((DataBufferByte) convertBinaryToImage(byteArray, min, max).getRaster().getDataBuffer()).getData();			
		Mat mat = new Mat( imageHeight, imageWidth, CvType.CV_8U);
		mat.put(0, 0, pixels);
		return mat;
	}
	
	 public BufferedImage convertMatToImage(Mat m){
 	    byte [] buffer = new byte[m.channels()*m.cols()*m.rows()];
 	    m.get(0,0,buffer); 
 	    BufferedImage image = new BufferedImage(m.cols(), m.rows(), BufferedImage.TYPE_BYTE_GRAY);
 	    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
 	    System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);  
 	    return image;

 	}
	
	private static float bytesToCelsius(int value) {
		return ((float) value * 0.04f) - 273.15f; // * 0.04 FLIR Ax5 constant + kelvin to celsius
	}

	private static int unsignedToSigned(byte a) {
		return a & 0xFF;
	}
	
	private static float normalizeToByte(float value, float min, float max) {
		float newMin = 0.0f, newMax = 255.0f;
		return normalize(value, min, max, newMin, newMax);
	}

	private static float normalize(float value, float min, float max, float newMin, float newMax) {
		if (value > max) value = max;
		if (value < min) value = min;
		float normalized = (newMax - newMin) / (max - min) * (value - max) + newMax;
		return normalized;
	}

	//TODO: inefficient
	private static int getMax(byte[] inputArray) {
		int maxValue = 0;
		for (int i = 0; i < inputArray.length; i += 2) {
			int value = readTwoBytes(inputArray, i);
			if (value > maxValue) maxValue = value;
		}
		return maxValue;
	}

	private static int getMin(byte[] inputArray) {
		int minValue = Integer.MAX_VALUE;
		for (int i = 0; i < inputArray.length; i += 2) {
			int value = readTwoBytes(inputArray, i);
			if (value < minValue) minValue = value;
		}
		return minValue;
	}

	private static int readTwoBytes(byte[] inputArray, int index) {
		int firstByte = unsignedToSigned(inputArray[index]);
		int secondByte = unsignedToSigned(inputArray[index + 1]);
		return (secondByte << 8) | firstByte;
	}
	
}
