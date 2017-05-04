package data.reciever.service;

import java.nio.channels.ClosedByInterruptException;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Mat;

import application.MainController;
import data.reciever.FlirDataReciever;
import data.reciever.domain.DataPayload;
import data.reciever.domain.ImageData;
import image.ImageConvertor;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import utils.Utils;

public class FlirDataService extends Service<ImageData> {
	
	private final FlirDataReciever dataReciever;

	public FlirDataService(FlirDataReciever dataReciever) {
		this.dataReciever = dataReciever;
	}
	
	@Override
	protected Task<ImageData> createTask() {
		
		return new Task<ImageData>() {
			@Override protected ImageData call() {				
				DataPayload payload;				
				try {
					payload = dataReciever.getImage();
					while (payload != null && payload.getData().length != 0 && !isCancelled()) {
						ImageData data = new ImageData(payload.getData(), payload.getFilename());
						updateValue(data);
						updateMessage(dataReciever.getStatus().getStrStatus());
						payload = dataReciever.getImage();						
					}	
					
				} catch (InterruptedException | ClosedByInterruptException ex ) { //catch Thread.sleep()
				}
				updateMessage(dataReciever.getStatus().getStrStatus());
				return null;
			}
		};
	}
}
