package data.reciever;

import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Path;
import java.util.List;

import org.opencv.videoio.VideoCapture;

import data.reciever.domain.Status;

public abstract class DataReciever<T> {
	public static final int DEFAULT_PLAYBACK_SPEED = 33;
	
	protected Status status;
	protected boolean saveImages;
	protected boolean isDummy;
	protected int playbackSpeed;
	protected List<Path> filesInFolder;
	protected int fakeStreamCounter;
	protected T latest;
	
	protected abstract void openConnection();	
	public abstract void initDummyHost(Path path);
	protected abstract void closeConnection(); //do cleanup
	
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
	
	protected abstract T getImageFromDummyStream() throws InterruptedException, ClosedByInterruptException;
	protected abstract T getImageFromStream();
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
