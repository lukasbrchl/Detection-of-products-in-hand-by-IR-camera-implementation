package data.reciever;

import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Path;
import java.util.List;

import org.opencv.videoio.VideoCapture;

import data.reciever.domain.Status;

/**
* An abstract generic class for stream data reciever.
* 
* @author Lukas Brchl
*/
public abstract class DataReciever<T> {
	protected Status status;
	protected boolean saveImages;
	protected boolean isDummy;
	protected int playbackSpeed;
	protected List<Path> filesInFolder;
	protected int fakeStreamCounter;
	protected T latest;
	
	/**
	 *  Connects to specified hostname and port.
	 *  
	 * @return the success of the operation
	 */
	protected abstract boolean openConnection();
	
	/**
	 *  Initializes dummy stream.
	 *  
	 * @return the success of the operation
	 */
	protected abstract boolean initDummyHost(Path path);
	
	/**
	 *  Do cleanup, close all resources.
	 */
	protected abstract void closeConnection(); //do cleanup
	
	/**
	 *  Decides on returning latest or actual recieved image from stream. Depending on saveImage flag might store recieved images.
	 *  
	 * @return the image of type T
	 */	
	public T getImage() throws ClosedByInterruptException, InterruptedException {
		if (status.equals(Status.PAUSED)) return latest;
		if (isDummy) 
			latest = getImageFromDummyStream();
		else {
			latest = getImageFromStream();	
			if (saveImages) saveImage();
		}
		return latest;
	}
	
	/**
	 *  Gets image from initialized dummy stream.
	 *  
	 * @return the image of type T
	 */	
	protected abstract T getImageFromDummyStream() throws InterruptedException, ClosedByInterruptException;
	
	/**
	 *  Gets image from connected real stream.
	 *  
	 * @return the image of type T
	 */
	protected abstract T getImageFromStream();
	
	/**
	 *  Saves recieved images to harddrive.
	 *  
	 * @return the success of the operation
	 */
	protected abstract boolean saveImage();

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isSaveImages() {
		return saveImages;
	}

	public void setSaveImages(boolean saveImages) {
		this.saveImages = saveImages;
	}

	public boolean isDummy() {
		return isDummy;
	}

	public void setDummy(boolean isDummy) {
		this.isDummy = isDummy;
	}

	public int getPlaybackSpeed() {
		return playbackSpeed;
	}

	public void setPlaybackSpeed(int playbackSpeed) {
		this.playbackSpeed = playbackSpeed;
	}

	public int getFakeStreamCounter() {
		return fakeStreamCounter;
	}

	public void setFakeStreamCounter(int fakeStreamCounter) {
		this.fakeStreamCounter = fakeStreamCounter;
	}

	public T getLatest() {
		return latest;
	}

	public void setLatest(T latest) {
		this.latest = latest;
	}
	
	
}
