package network;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import image.ImageConvertor;
import network.domain.ImageData;

public class DataReciever {
	public static final String DEFAULT_HOST_NAME = "localhost";
	public static final int DEFAULT_PORT_NUMBER = 27015;
	private final int BUFFER_SIZE = 8192; // or 4096, or more
	private Socket socket; 
	private InputStream inputStream;
	private final int bytesToRecieve; //shouldn't change while recieving
	private final String hostName;
	private final int port;
	
	
	public DataReciever (String hostName, int port, int bytesToRecieve) {
		this.bytesToRecieve = bytesToRecieve;
		this.hostName = hostName;
		this.port = port;
	}
	
	public void initSocket() {
		try {
			socket = new Socket(hostName, port);
			inputStream =  socket.getInputStream();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	//do cleanup
	public void finish() {
		try {
			if (!socket.isClosed())
				socket.close();
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List <ImageData> recieveData(boolean showImage, boolean saveImage) {
		List <ImageData> result = new LinkedList<>();
		byte [] binaryImage = getImageFromStream();
		
//		if (binaryImage != null || binaryImage.length != 0) return result;
		
		while (binaryImage != null && binaryImage.length != 0) {
			result.add(new ImageData(binaryImage));
			binaryImage = getImageFromStream();
		}
		return result;
	}
	
	public byte [] getImageFromStream() {
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
}
