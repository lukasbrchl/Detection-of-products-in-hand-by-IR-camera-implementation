package image.service;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import image.ImageConvertor;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import network.domain.ImageData;
import utils.Config;

public class WebcamService extends Service<Mat> {
	
	private boolean saveImages;
	
	@Override
	protected Task<Mat> createTask() {
		
		return new Task<Mat>() {
			@Override protected Mat call() {		
				VideoCapture camera = new VideoCapture(1);
		        Mat frame = new Mat();
		        
		        if(!camera.isOpened()){
		            System.out.println("Error");
		        }
		        else {                  
		            while(camera.read(frame)) {
		            	Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2GRAY);
				        updateValue(frame);
				        if (saveImages) {
				        	String filename = new SimpleDateFormat("MM_dd_HH_mm_ss_SSS").format(new Date()) + ".png";
//				        	File outputfile = new File(Config.getInstance().getValue(Config.WEBCAM_IMAGE_SAVE) + filename);
				            Imgcodecs.imwrite(Config.getInstance().getValue(Config.WEBCAM_IMAGE_SAVE) + filename, frame);
//				        	try {
//								ImageIO.write(ImageConvertor.convertMatToBufferedImage(frame), "png", outputfile);
//							} catch (IOException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
				        }
				        frame = new Mat();
		            }   
		        }
		        camera.release();
		        return null;
			}
		};
	}

	public boolean isSaveImages() {
		return saveImages;
	}

	public void setSaveImages(boolean saveImages) {
		this.saveImages = saveImages;
	}

}
