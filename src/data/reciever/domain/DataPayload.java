package data.reciever.domain;

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
