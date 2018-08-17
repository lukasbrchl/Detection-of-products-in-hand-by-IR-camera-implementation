package data.reciever.domain;


/**
 * Converted image data from raw binary data stream. Filled in {@link FlirDataService} and also might be used in other services.
 * TODO: define own headers with metadata
 *
 * @author Lukas Brchl
 */
public class ImageData {

    private final byte[] data;
    private final String filename;

    //TODO
    //	private int bytesPerPixel;
    //	private int widht;
    //	private int weight;

    public ImageData(byte[] data, String filename) {
        super();
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
