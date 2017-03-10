package network.domain;

public class ImageData {
	
	private final byte [] data;
	//define own header with metadata
//	private int bytesPerPixel;
//	private int widht;
//	private int weight;
	
	public ImageData(byte[] data) {
		super();
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}	
}
