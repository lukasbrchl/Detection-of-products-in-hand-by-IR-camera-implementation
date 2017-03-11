import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import image.ImageConvertor;
import image.ImageViewer;

public class Main {
	public static final float MIN_TEMP = 26.0f;
	public static final float MAX_TEMP = 36.0f;
	
	public static void main(String[] args) throws IOException, InterruptedException {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        ImageConvertor imageConvertor = new ImageConvertor(640, 512);
        ImageViewer imViewer1 = new ImageViewer(); ImageViewer imViewer2 = new ImageViewer(); ImageViewer imViewer3 = new ImageViewer(); ImageViewer imViewer4 = new ImageViewer(); ImageViewer imViewer5 = new ImageViewer();
        ImageViewer imViewer6 = new ImageViewer(); ImageViewer imViewer7 = new ImageViewer(); ImageViewer imViewer8 = new ImageViewer(); ImageViewer imViewer9 = new ImageViewer(); ImageViewer imViewer10 = new ImageViewer();

		Path backgroundPath = Paths.get("img/binary/background.bin");
		Path keysPath = Paths.get("img/binary/keys.bin");
		byte[] backgroundData = Files.readAllBytes(backgroundPath);
		byte[] keysData = Files.readAllBytes(keysPath);

		Mat keys = imageConvertor.convertBinaryToMat(keysData, MIN_TEMP,MAX_TEMP);

		imViewer1.loadImage(imageConvertor.convertMatToImage(keys));
        keys.convertTo(keys, -1, 2.7, -300);
	    imViewer2.loadImage(imageConvertor.convertMatToImage(keys));
	    Imgproc.adaptiveThreshold(keys, keys, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 501, -55);

		imViewer3.loadImage(imageConvertor.convertMatToImage(keys));
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(4,4));
        Imgproc.dilate(keys, keys, element);
 		imViewer4.loadImage(imageConvertor.convertMatToImage(keys));





	}
	
	
}
