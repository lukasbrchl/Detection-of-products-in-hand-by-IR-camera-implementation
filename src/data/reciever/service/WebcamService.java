package data.reciever.service;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import data.reciever.FlirDataReciever;
import data.reciever.WebcamDataReciever;
import data.reciever.domain.ImageData;
import image.ImageConvertor;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import utils.Config;

//TODO: not completed

public class WebcamService extends Service<Mat> {
	
	private final WebcamDataReciever dataReciever;

	public WebcamService(WebcamDataReciever dataReciever) {
		this.dataReciever = dataReciever;
	}
	
		
	@Override
	protected Task<Mat> createTask() {		
		return new Task<Mat>() {
			@Override protected Mat call() {	
				Mat mat;				
				try {
					mat = dataReciever.getImage();
					while (mat != null  && !isCancelled()) {
						updateValue(mat);
						updateMessage(dataReciever.getStatus().getStrStatus());
						mat = dataReciever.getImage();						
					}					
				} catch (InterruptedException | ClosedByInterruptException ex ) { //catch Thread.sleep()
				}				
		        return null;
			}
		};
	}
}
