package data.reciever.service;

import java.nio.channels.ClosedByInterruptException;

import org.opencv.core.Mat;

import data.reciever.WebcamDataReciever;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
* Service for WebcamDataReciever.
* 
* TODO: Not finished
* @author Lukas Brchl
*/
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
