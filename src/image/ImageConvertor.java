package image;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
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

	public BufferedImage convertToImage(byte[] byteArray) {
		try {
			float min = bytesToCelsius(getMin(byteArray));
			float max = bytesToCelsius(getMax(byteArray));
			BufferedImage outputImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
			int counter = 0;
			
			for (int y = 0; y < outputImage.getHeight(); ++y) {
				for (int x = 0; x < outputImage.getWidth(); ++x) {
					float temp  = bytesToCelsius(readTwoBytes(byteArray, counter));
					int normalizedVal = (int) (normalizeToByte(temp, min, max));
					Color color = new Color(normalizedVal, normalizedVal, normalizedVal);
					outputImage.setRGB(x, y, color.getRGB());
					counter += 2;
				} 
			}	
			return outputImage;			
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return null;
	}
	
	private static float bytesToCelsius(int value) {
		return ((float) value * 0.04f) - 273.15f; // * 0.04 FLIR Ax5 constant + kelvin to celsius
	}

	private static int unsignedToSigned(byte a) {
		return a & 0xFF;
	}

	private static float normalizeToByte(float value, float min, float max) {
		float newMin = 0.0f, newMax = 255.0f;
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
