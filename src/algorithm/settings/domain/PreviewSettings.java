package algorithm.settings.domain;

import javax.xml.bind.annotation.XmlRootElement;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

@XmlRootElement()
public class PreviewSettings extends Settings {
	
	BooleanProperty exposure;
	
	BooleanProperty blur;
	
	ObjectProperty<Double> cannyThresh1;
	ObjectProperty<Double> cannyThresh2;
	BooleanProperty canny;
	
	ObjectProperty<Integer> playbackSpeed;
	
	public PreviewSettings() {
		this.exposure = new SimpleBooleanProperty();
		this.blur = new SimpleBooleanProperty();
		this.cannyThresh1 = new SimpleObjectProperty<Double>(0.0);
		this.cannyThresh2 = new SimpleObjectProperty<Double>(0.0);
		this.canny = new SimpleBooleanProperty();
		this.playbackSpeed = new SimpleObjectProperty<Integer>(0);
	}

	public PreviewSettings(BooleanProperty exposure, BooleanProperty blur, ObjectProperty<Double> cannyThresh1,
			ObjectProperty<Double> cannyThresh2, BooleanProperty canny, ObjectProperty<Integer> playbackSpeed) {
		super();
		this.exposure = exposure;
		this.blur = blur;
		this.cannyThresh1 = cannyThresh1;
		this.cannyThresh2 = cannyThresh2;
		this.canny = canny;
		this.playbackSpeed = playbackSpeed;
	}

	public BooleanProperty exposureProperty() {
		return this.exposure;
	}
	

	public boolean isExposure() {
		return this.exposureProperty().get();
	}
	

	public void setExposure(final boolean exposure) {
		this.exposureProperty().set(exposure);
	}
	

	public BooleanProperty blurProperty() {
		return this.blur;
	}
	

	public boolean isBlur() {
		return this.blurProperty().get();
	}
	

	public void setBlur(final boolean blur) {
		this.blurProperty().set(blur);
	}
	

	public ObjectProperty<Double> cannyThresh1Property() {
		return this.cannyThresh1;
	}
	

	public Double getCannyThresh1() {
		return this.cannyThresh1Property().get();
	}
	

	public void setCannyThresh1(final Double cannyThresh1) {
		this.cannyThresh1Property().set(cannyThresh1);
	}
	

	public ObjectProperty<Double> cannyThresh2Property() {
		return this.cannyThresh2;
	}
	

	public Double getCannyThresh2() {
		return this.cannyThresh2Property().get();
	}
	

	public void setCannyThresh2(final Double cannyThresh2) {
		this.cannyThresh2Property().set(cannyThresh2);
	}
	

	public BooleanProperty cannyProperty() {
		return this.canny;
	}
	

	public boolean isCanny() {
		return this.cannyProperty().get();
	}
	

	public void setCanny(final boolean canny) {
		this.cannyProperty().set(canny);
	}
	

	public ObjectProperty<Integer> playbackSpeedProperty() {
		return this.playbackSpeed;
	}
	

	public Integer getPlaybackSpeed() {
		return this.playbackSpeedProperty().get();
	}
	

	public void setPlaybackSpeed(final Integer playbackSpeed) {
		this.playbackSpeedProperty().set(playbackSpeed);
	}

	@Override
	public String toString() {
		return "PreviewSettings [exposure=" + exposure + ", blur=" + blur + ", cannyThresh1=" + cannyThresh1
				+ ", cannyThresh2=" + cannyThresh2 + ", canny=" + canny + ", playbackSpeed=" + playbackSpeed + "] \n";
	}	
	
}
