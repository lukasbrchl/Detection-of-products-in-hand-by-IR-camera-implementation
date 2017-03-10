import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import image.ImageConvertor;
import image.ImageViewer;
import network.DataReciever;

public class Main {
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 512;
	public static final int RAW_IMAGE_SIZE = IMAGE_WIDTH * IMAGE_HEIGHT * 2; //size in bytes
	public static final String FILE_PREFIX = "test_";
	public static final String TIFF_POSTFIX = ".tiff";
	
	public static void main(String[] args) {
		try {
			DataReciever dataReciever = new DataReciever(DataReciever.DEFAULT_HOST_NAME, DataReciever.DEFAULT_PORT_NUMBER, RAW_IMAGE_SIZE);
			ImageConvertor imageCovnertor = new ImageConvertor(IMAGE_WIDTH, IMAGE_HEIGHT);
			ImageViewer imageViewer = new ImageViewer();
			
			dataReciever.initSocket();
			
			int imgCounter = 0;
			byte [] binaryImage = dataReciever.getImageFromStream();
			
			while (binaryImage != null && binaryImage.length != 0) {
				BufferedImage bufferedImage = imageCovnertor.convertToImage(binaryImage);		
				imageViewer.loadImage(bufferedImage);				
				ImageIO.write(bufferedImage, "TIFF", new File(FILE_PREFIX + imgCounter++ + TIFF_POSTFIX));					
				binaryImage = dataReciever.getImageFromStream();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
//
//		Mat mat = new Mat(IMAGE_WIDTH, IMAGE_HEIGHT, CvType.CV_8UC1);
//		mat.put(0, 0, data);

	}
}
