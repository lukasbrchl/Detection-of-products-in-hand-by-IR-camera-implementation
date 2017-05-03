package data.algorithm.settings.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

@XmlRootElement(name="preprocessingSettings")
public class PreprocessingSettings {

	ObjectProperty<Double> tempMin;
	ObjectProperty<Double> tempMax;
	BooleanProperty scale;
	
	ObjectProperty<Double> clacheSize1;
	ObjectProperty<Double> clacheSize2;
	ObjectProperty<Double> clacheClip;
	ObjectProperty<Double> brightnessParam1;
	ObjectProperty<Double> brightnessParam2;
	ObjectProperty<Double> contrastParam1;
	ObjectProperty<Double> contrastParam2;
	
	ObjectProperty<Integer> medianSize;
	ObjectProperty<Double> bilateralSize1;
	ObjectProperty<Double> bilateralSize2;
	ObjectProperty<Integer> bilateralSigma;
	ObjectProperty<Double> gaussianSize1;
	ObjectProperty<Double> gaussianSize2;
	ObjectProperty<Integer> gaussianSigma;



	public PreprocessingSettings() {
		this.tempMin = new SimpleObjectProperty<Double>(0.0);
		this.tempMax = new SimpleObjectProperty<Double>(0.0);
		this.scale = new SimpleBooleanProperty();
		this.clacheSize1 = new SimpleObjectProperty<Double>(0.0);
		this.clacheSize2 = new SimpleObjectProperty<Double>(0.0);
		this.clacheClip = new SimpleObjectProperty<Double>(0.0);
		this.brightnessParam1 = new SimpleObjectProperty<Double>(0.0);
		this.brightnessParam2 = new SimpleObjectProperty<Double>(0.0);
		this.contrastParam1 = new SimpleObjectProperty<Double>(0.0);
		this.contrastParam2 = new SimpleObjectProperty<Double>(0.0);
		this.medianSize = new SimpleObjectProperty<Integer>(0);
		this.bilateralSigma = new SimpleObjectProperty<Integer>(0);
		this.bilateralSize2 = new SimpleObjectProperty<Double>(0.0);
		this.bilateralSize1 = new SimpleObjectProperty<Double>(0.0);
		this.gaussianSize1 = new SimpleObjectProperty<Double>(0.0);
		this.gaussianSize2 = new SimpleObjectProperty<Double>(0.0);
		this.gaussianSigma = new SimpleObjectProperty<Integer>(0);
	}

//	public PreprocessingSettings(double tempMin, double tempMax, boolean scale, double clacheSize1,
//			double clacheSize2, double clacheClip, double brightnessParam1, double brightnessParam2,
//			double contrastParam1, double contrastParam2, int medianSize, double bilateralSize1, double bilateralSize2, int bilateralSigma,
//			double gaussianSize1, double gaussianSize2, int gaussianSigma) {
//		super();
//		this.tempMin = new SimpleObjectProperty(tempMin);
//		this.tempMax = new SimpleObjectProperty(tempMax);
//		this.scale = new SimpleBooleanProperty(scale);
//		this.clacheSize1 = new SimpleObjectProperty(clacheSize1);
//		this.clacheSize2 = new SimpleObjectProperty(clacheSize2);
//		this.clacheClip = new SimpleObjectProperty(clacheClip);
//		this.brightnessParam1 = new SimpleObjectProperty(brightnessParam1);
//		this.brightnessParam2 = new SimpleObjectProperty(brightnessParam2);
//		this.contrastParam1 = new SimpleObjectProperty(contrastParam1);
//		this.contrastParam2 = new SimpleObjectProperty(contrastParam2);
//		this.medianSize = new SimpleObjectProperty(medianSize);
//		this.bilateralSize1 = new SimpleObjectProperty(bilateralSize1);
//		this.bilateralSize2 = new SimpleObjectProperty(bilateralSize2);
//		this.bilateralSigma = new SimpleObjectProperty(bilateralSigma);
//		this.gaussianSize1 = new SimpleObjectProperty(gaussianSize1);
//		this.gaussianSize2 = new SimpleObjectProperty(gaussianSize2);
//		this.gaussianSigma = new SimpleObjectProperty(gaussianSigma);
//	}

	public PreprocessingSettings(ObjectProperty<Double> tempMin, ObjectProperty<Double> tempMax, BooleanProperty scale,
			ObjectProperty<Double> clacheSize1, ObjectProperty<Double> clacheSize2, ObjectProperty<Double> clacheClip,
			ObjectProperty<Double> brightnessParam1, ObjectProperty<Double> brightnessParam2, ObjectProperty<Double> contrastParam1,
			ObjectProperty<Double> contrastParam2, ObjectProperty<Integer> medianSize, ObjectProperty<Double> bilateralSize1,
			ObjectProperty<Double> bilateralSize2, ObjectProperty<Integer> bilateralSigma, ObjectProperty<Double> gaussianSize1,
			ObjectProperty<Double> gaussianSize2, ObjectProperty<Integer> gaussianSigma) {
		super();
		this.tempMin = tempMin;
		this.tempMax = tempMax;
		this.scale = scale;
		this.clacheSize1 = clacheSize1;
		this.clacheSize2 = clacheSize2;
		this.clacheClip = clacheClip;
		this.brightnessParam1 = brightnessParam1;
		this.brightnessParam2 = brightnessParam2;
		this.contrastParam1 = contrastParam1;
		this.contrastParam2 = contrastParam2;
		this.medianSize = medianSize;
		this.bilateralSize1 = bilateralSize1;
		this.bilateralSize2 = bilateralSize2;
		this.bilateralSigma = bilateralSigma;
		this.gaussianSize1 = gaussianSize1;
		this.gaussianSize2 = gaussianSize2;
		this.gaussianSigma = gaussianSigma;
	}
	
	
	
	// getter, setters
	
	public ObjectProperty<Double> tempMinProperty() {
		return this.tempMin;
	}
	

	public double getTempMin() {
		return this.tempMinProperty().get();
	}
	

	public void setTempMin(final double tempMin) {
		this.tempMinProperty().set(tempMin);
	}
	

	public ObjectProperty<Double> tempMaxProperty() {
		return this.tempMax;
	}
	

	public double getTempMax() {
		return this.tempMaxProperty().get();
	}
	

	public void setTempMax(final double tempMax) {
		this.tempMaxProperty().set(tempMax);
	}
	

	public BooleanProperty scaleProperty() {
		return this.scale;
	}
	

	public boolean isScale() {
		return this.scaleProperty().get();
	}
	

	public void setScale(final boolean scale) {
		this.scaleProperty().set(scale);
	}
	

	public ObjectProperty<Double> clacheSize1Property() {
		return this.clacheSize1;
	}
	

	public double getClacheSize1() {
		return this.clacheSize1Property().get();
	}
	

	public void setClacheSize1(final double clacheSize1) {
		this.clacheSize1Property().set(clacheSize1);
	}
	

	public ObjectProperty<Double> clacheSize2Property() {
		return this.clacheSize2;
	}
	

	public double getClacheSize2() {
		return this.clacheSize2Property().get();
	}
	

	public void setClacheSize2(final double clacheSize2) {
		this.clacheSize2Property().set(clacheSize2);
	}
	

	public ObjectProperty<Double> clacheClipProperty() {
		return this.clacheClip;
	}
	

	public double getClacheClip() {
		return this.clacheClipProperty().get();
	}
	

	public void setClacheClip(final double clacheClip) {
		this.clacheClipProperty().set(clacheClip);
	}
	

	public ObjectProperty<Double> brightnessParam1Property() {
		return this.brightnessParam1;
	}
	

	public double getBrightnessParam1() {
		return this.brightnessParam1Property().get();
	}
	

	public void setBrightnessParam1(final double brightnessParam1) {
		this.brightnessParam1Property().set(brightnessParam1);
	}
	

	public ObjectProperty<Double> brightnessParam2Property() {
		return this.brightnessParam2;
	}
	

	public double getBrightnessParam2() {
		return this.brightnessParam2Property().get();
	}
	

	public void setBrightnessParam2(final double brightnessParam2) {
		this.brightnessParam2Property().set(brightnessParam2);
	}
	

	public ObjectProperty<Double> contrastParam1Property() {
		return this.contrastParam1;
	}
	

	public double getContrastParam1() {
		return this.contrastParam1Property().get();
	}
	

	public void setContrastParam1(final double contrastParam1) {
		this.contrastParam1Property().set(contrastParam1);
	}
	

	public ObjectProperty<Double> contrastParam2Property() {
		return this.contrastParam2;
	}
	

	public double getContrastParam2() {
		return this.contrastParam2Property().get();
	}
	

	public void setContrastParam2(final double contrastParam2) {
		this.contrastParam2Property().set(contrastParam2);
	}
	

	public ObjectProperty<Integer> medianSizeProperty() {
		return this.medianSize;
	}
	

	public int getMedianSize() {
		return this.medianSizeProperty().get();
	}
	

	public void setMedianSize(final int medianSize) {
		this.medianSizeProperty().set(medianSize);
	}
	

	public ObjectProperty<Double> bilateralSize1Property() {
		return this.bilateralSize1;
	}
	

	public double getBilateralSize1() {
		return this.bilateralSize1Property().get();
	}
	

	public void setBilateralSize1(final double bilateralSize1) {
		this.bilateralSize1Property().set(bilateralSize1);
	}
	

	public ObjectProperty<Double> bilateralSize2Property() {
		return this.bilateralSize2;
	}
	

	public double getBilateralSize2() {
		return this.bilateralSize2Property().get();
	}
	

	public void setBilateralSize2(final double bilateralSize2) {
		this.bilateralSize2Property().set(bilateralSize2);
	}
	

	public ObjectProperty<Integer> bilateralSigmaProperty() {
		return this.bilateralSigma;
	}
	

	public int getBilateralSigma() {
		return this.bilateralSigmaProperty().get();
	}
	

	public void setBilateralSigma(final int bilateralSigma) {
		this.bilateralSigmaProperty().set(bilateralSigma);
	}
	

	public ObjectProperty<Double> gaussianSize1Property() {
		return this.gaussianSize1;
	}
	

	public double getGaussianSize1() {
		return this.gaussianSize1Property().get();
	}
	

	public void setGaussianSize1(final double gaussianSize1) {
		this.gaussianSize1Property().set(gaussianSize1);
	}
	

	public ObjectProperty<Double> gaussianSize2Property() {
		return this.gaussianSize2;
	}
	

	public double getGaussianSize2() {
		return this.gaussianSize2Property().get();
	}
	

	public void setGaussianSize2(final double gaussianSize2) {
		this.gaussianSize2Property().set(gaussianSize2);
	}
	

	public ObjectProperty<Integer> gaussianSigmaProperty() {
		return this.gaussianSigma;
	}
	

	public int getGaussianSigma() {
		return this.gaussianSigmaProperty().get();
	}
	

	public void setGaussianSigma(final int gaussianSigma) {
		this.gaussianSigmaProperty().set(gaussianSigma);
	}

	@Override
	public String toString() {
		return "PreprocessingSettings [tempMin=" + tempMin.get() + ", tempMax=" + tempMax.get()  + ", scale=" + scale.get() 
				+ ", clacheSize1=" + clacheSize1.get()  + ", clacheSize2=" + clacheSize2.get()  + ", clacheClip=" + clacheClip.get() 
				+ ", brightnessParam1=" + brightnessParam1.get()  + ", brightnessParam2=" + brightnessParam2.get() 
				+ ", contrastParam1=" + contrastParam1.get()  + ", contrastParam2=" + contrastParam2.get()  + ", medianSize="
				+ medianSize.get()  + ", bilateralSize1=" + bilateralSize1.get()  + ", bilateralSize2=" + bilateralSize2.get() 
				+ ", bilateralSigma=" + bilateralSigma.get()  + ", gaussianSize1=" + gaussianSize1.get()  + ", gaussianSize2="
				+ gaussianSize2.get()  + ", gaussianSigma=" + gaussianSigma.get()  + "]";
	}
	

	

	
	
}
