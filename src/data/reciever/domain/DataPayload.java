package data.reciever.domain;

/**
* A payload of raw binary data recieved directly from socket stream.
* 
* @author Lukas Brchl
*/
public class DataPayload {
	private final byte [] data;
	private final String filename;
	
	public DataPayload(byte[] data, String filename) {
		this.data = data;
		this.filename = filename;
	}

	public byte[] getData() {
		return data;
	}
	
	public String getFilename() {
		return filename;
	}

}
