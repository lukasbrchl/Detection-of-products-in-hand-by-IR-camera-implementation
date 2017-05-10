package data.reciever;

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
import java.util.Arrays;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import data.reciever.domain.DataPayload;
import data.reciever.domain.Status;
import utils.Config;

/**
* Data reciever object, customized to recieve data throught socket stream. 
* Supports dummy host - loading files from predefined folder.
*
* @author Lukas Brchl
*/
public class FlirDataReciever extends DataReciever<DataPayload> {
	
//	private final int BUFFER_SIZE = 8192; // or 4096, or more\
	private final int BUFFER_SIZE = 8000; // or 4096, or more

	private Socket socket; 
	private InputStream inputStream;
	private final int bytesToRecieve; //shouldn't change while recieving
	private String hostName;
	private final int port;
	
	public FlirDataReciever (String hostName, int port, int bytesToRecieve, int playbackSpeed) {
		super();
		this.bytesToRecieve = bytesToRecieve;
		this.hostName = hostName;
		this.port = port;
		this.status = Status.CLOSED;
		this.playbackSpeed = playbackSpeed;
	}
	
	public boolean openConnection() {
		try {
			socket = new Socket(hostName, port);
			inputStream =  socket.getInputStream();
			status = Status.CONNECTED;			
		} catch (Exception e) {
			return false;
		}	
		return true;
	}
	
	public void closeConnection() {
		status = Status.CLOSED;
		if (!isDummy) {
			try {
				if (socket != null && !socket.isClosed())
					socket.close();
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		isDummy = false;
	}
	
	protected DataPayload getImageFromStream() {
		byte [] file = new byte [bytesToRecieve];	
		byte [] buffer = new byte[BUFFER_SIZE]; 
		int readCount, bytesWritten = 0;
		
		try {
			while ((readCount = inputStream.read(buffer)) > 0) {
				System.arraycopy(buffer, 0, file, bytesWritten, readCount); 
				bytesWritten += readCount;
				if (bytesWritten >= bytesToRecieve) return new DataPayload(file, null);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return new DataPayload(file, null);
	}
	
	boolean findIfArrayIsASubset(int[] main, int[] sub) { //TODO: currently not using
		int count = 0;
		for (int i = 0; i < main.length; i++) {
		    for (int j = 0; j < sub.length; j++) {
		        if (main[i] == sub[j]) {
		            main[i] = -1;
		            count++;
		            break;
		        }
		    }
		}
		if (count == sub.length)
			return true;
		return false;
	}

	protected DataPayload getImageFromDummyStream() throws InterruptedException, ClosedByInterruptException {		
//		if (fakeStreamCounter >= filesInFolder.size()) fakeStreamCounter = 0;
		if (fakeStreamCounter >= filesInFolder.size()) throw new RuntimeException("At the end of the stream");		

		String filename = null;
		byte[] data = null;
		try {
			filename = filesInFolder.get(fakeStreamCounter).getFileName().toString();
			data = Files.readAllBytes(filesInFolder.get(fakeStreamCounter++));
		} catch (Exception e) {
			if (e instanceof ClosedByInterruptException) throw new ClosedByInterruptException();
			e.printStackTrace();
		}
		Thread.sleep(playbackSpeed);				
		return new DataPayload(data, filename);
	}
	
	protected boolean saveImage() {
		try {
			String filename = new SimpleDateFormat("MM_dd_HH_mm_ss_SSS").format(new Date());
			DataOutputStream os = new DataOutputStream(new FileOutputStream(Config.getInstance().getValue(Config.FLIR_IMAGE_SAVE)+ filename + ".bin"));		 
			os.write(latest.getData(), 0, bytesToRecieve);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}		 
		return true;
	}

	public boolean initDummyHost(Path path) {
		try {
			isDummy = true;
			filesInFolder = Files.walk(path).filter(Files::isRegularFile).filter(f -> f.getFileName().toString().endsWith(".bin")).collect(Collectors.toList());		
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		fakeStreamCounter = 0;
		setStatus(Status.CONNECTED);
		return true;
	}
	
}
