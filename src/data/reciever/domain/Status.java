package data.reciever.domain;

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