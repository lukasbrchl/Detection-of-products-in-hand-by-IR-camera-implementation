package network;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import utils.Config;

public class FlirDataReciever {
	public enum Status {
		CONNECTED("Connected"),
		STREAMING("Streaming"),
		PAUSED("Paused"),
		CLOSED("Closed");
		private final String strStatus;
		private Status (String strStatus) {
			this.strStatus = strStatus;
		}
		public String getStrStatus() {
			return strStatus;
		}
	}
	public static final String DUMMY_HOST = "dummy";
	public static final int DEFAULT_PLAYBACK_SPEED = 33;
	
	private final int BUFFER_SIZE = 8192; // or 4096, or more
	private Socket socket; 
	private InputStream inputStream;
	private final int bytesToRecieve; //shouldn't change while recieving
	private String hostName;
	private final int port;
	private List<Path> filesInFolder;
	private int fakeStreamCounter;
	private byte[] latestBuffer;
	private Status status;
	private boolean saveImages;
	private int playbackSpeed;
	
	public FlirDataReciever (String hostName, int port, int bytesToRecieve) {
		this.bytesToRecieve = bytesToRecieve;
		this.hostName = hostName;
		this.port = port;
		this.status = Status.CLOSED;
		playbackSpeed = DEFAULT_PLAYBACK_SPEED;
	}
	
	public void openConnection() {
		try {
			if (hostName.equals(DUMMY_HOST)) { //debugging purpose
				initDummyHost();
				return;
			}
			socket = new Socket(hostName, port);
			inputStream =  socket.getInputStream();
			status = Status.CONNECTED;
			
		} catch (Exception e) {
			initDummyHost();
		}	
	}
	
	//do cleanup
	public void closeConnection() {
		status = Status.CLOSED;
		if (hostName.equals(DUMMY_HOST)) return;
		try {
			if (!socket.isClosed())
				socket.close();
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public byte[] getImageFromStream() throws ClosedByInterruptException, InterruptedException {
		if (status.equals(Status.PAUSED)) return latestBuffer;
		if (hostName.equals(DUMMY_HOST)) 
			latestBuffer = getImageFromDummyStream();
		else {
			latestBuffer = getImageFromSocketStream();	
			if (saveImages) saveBinaryData();
		}
		return latestBuffer;
	}
	
	private byte[] getImageFromSocketStream() {
		byte [] file = new byte [bytesToRecieve];	
		byte [] buffer = new byte[BUFFER_SIZE]; 
		int readCount, bytesWritten = 0;
		
		try {
			while ((readCount = inputStream.read(buffer)) > 0) {
				System.arraycopy(buffer, 0, file, bytesWritten, readCount); 
				bytesWritten += readCount;
				if (bytesWritten >= bytesToRecieve) return file;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return file;
	}
	
	private byte[] getImageFromDummyStream() throws InterruptedException, ClosedByInterruptException {		
		if (fakeStreamCounter >= filesInFolder.size()) fakeStreamCounter = 0;		
		byte[] data = null;
		try {
			data = Files.readAllBytes(filesInFolder.get(fakeStreamCounter++));
		} catch (Exception e) {
			if (e instanceof ClosedByInterruptException) throw new ClosedByInterruptException();
			e.printStackTrace();
		}
		Thread.sleep(playbackSpeed);				
		return data;
	}
	
	private void saveBinaryData() {
		try {
			String filename = new SimpleDateFormat("MM_dd_HH_mm_ss_SSS").format(new Date());
			DataOutputStream os = new DataOutputStream(new FileOutputStream(Config.getInstance().getValue(Config.FLIR_IMAGE_SAVE)+ filename + ".bin"));		 
			os.write(latestBuffer, 0, bytesToRecieve);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		 
	}

	private void initDummyHost() {
		try {
			hostName = DUMMY_HOST;
			filesInFolder = Files.walk(Paths.get(Config.getInstance().getValue(Config.DUMMY_PATH).toString())).filter(Files::isRegularFile).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		fakeStreamCounter = 0;
		setStatus(Status.CONNECTED);
	}

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

	public int getPlaybackSpeed() {
		return playbackSpeed;
	}

	public void setPlaybackSpeed(int playbackSpeed) {
		this.playbackSpeed = playbackSpeed;
	}
	
}
