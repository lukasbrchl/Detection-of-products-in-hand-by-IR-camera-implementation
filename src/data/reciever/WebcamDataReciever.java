package data.reciever;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import data.reciever.domain.Status;
import utils.Config;


public class WebcamDataReciever extends DataReciever<Mat>{
	private VideoCapture camera;
	private final int cameraId;
	
	public WebcamDataReciever(int cameraId, int playbackSpeed) {
		super();
		this.cameraId = cameraId;
		this.status = Status.CLOSED;
		this.playbackSpeed = playbackSpeed;
	}

	public void openConnection() {
		camera = new VideoCapture(cameraId);
		if (!camera.isOpened()) initDummyHost();
	}
	
	//do cleanup
	public void closeConnection() {
		camera.release();
	}

	@Override
	protected void initDummyHost() {
		try {
			isDummy = true;
			filesInFolder = Files.walk(Paths.get(Config.getInstance().getValue(Config.WEBCAM_DUMMY_PATH).toString())).filter(Files::isRegularFile).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		fakeStreamCounter = 0;
		setStatus(Status.CONNECTED);
	}

	@Override
	protected Mat getImageFromDummyStream() throws ClosedByInterruptException, InterruptedException {
		if (fakeStreamCounter >= filesInFolder.size()) fakeStreamCounter = 0;		
		Mat data = null;
		try {
			data = Imgcodecs.imread(filesInFolder.get(fakeStreamCounter++).toFile().getAbsolutePath());
		} catch (Exception e) {
			if (e instanceof ClosedByInterruptException) throw new ClosedByInterruptException();
			e.printStackTrace();
		}
		Thread.sleep(playbackSpeed);				
		return data;
	}

	@Override
	protected Mat getImageFromStream() {
		Mat frame = new Mat();
		camera.read(frame);
		return frame;
	}

	@Override
	protected boolean saveImage() {
		String filename = new SimpleDateFormat("MM_dd_HH_mm_ss_SSS").format(new Date()) + ".png";
        Imgcodecs.imwrite(Config.getInstance().getValue(Config.WEBCAM_IMAGE_SAVE) + filename, latest);
		return true;
	}
}
