package data.algorithm.settings.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

@XmlRootElement(name="preprocessingSettings")
public class PreprocessingSettings {

	DoubleProperty tempMin;
	DoubleProperty tempMax;
	BooleanProperty scale;
	
	DoubleProperty clacheSize1;
	DoubleProperty clacheSize2;
	DoubleProperty clacheClip;
	DoubleProperty brightnessParam1;
	DoubleProperty brightnessParam2;
	DoubleProperty contrastParam1;
	DoubleProperty contrastParam2;
	
	IntegerProperty medianSize;
	DoubleProperty bilateralSize1;
	DoubleProperty bilateralSize2;
	IntegerProperty bilateralSigma;
	DoubleProperty gaussianSize1;
	DoubleProperty gaussianSize2;
	IntegerProperty gaussianSigma;



	public PreprocessingSettings() {
		this.tempMin = new SimpleDoubleProperty();
		this.tempMax = new SimpleDoubleProperty();
		this.scale = new SimpleBooleanProperty();
		this.clacheSize1 = new SimpleDoubleProperty();
		this.clacheSize2 = new SimpleDoubleProperty();
		this.clacheClip = new SimpleDoubleProperty();
		this.brightnessParam1 = new SimpleDoubleProperty();
		this.brightnessParam2 = new SimpleDoubleProperty();
		this.contrastParam1 = new SimpleDoubleProperty();
		this.contrastParam2 = new SimpleDoubleProperty();
		this.medianSize = new SimpleIntegerProperty();
		this.bilateralSigma = new SimpleIntegerProperty();
		this.bilateralSize2 = new SimpleDoubleProperty();
		this.bilateralSize1 = new SimpleDoubleProperty();
		this.gaussianSize1 = new SimpleDoubleProperty();
		this.gaussianSize2 = new SimpleDoubleProperty();
		this.gaussianSigma = new SimpleIntegerProperty();
	}

//	public PreprocessingSettings(double tempMin, double tempMax, boolean scale, double clacheSize1,
//			double clacheSize2, double clacheClip, double brightnessParam1, double brightnessParam2,
//			double contrastParam1, double contrastParam2, int medianSize, double bilateralSize1, double bilateralSize2, int bilateralSigma,
//			double gaussianSize1, double gaussianSize2, int gaussianSigma) {
//		super();
//		this.tempMin = new SimpleDoubleProperty(tempMin);
//		this.tempMax = new SimpleDoubleProperty(tempMax);
//		this.scale = new SimpleBooleanProperty(scale);
//		this.clacheSize1 = new SimpleDoubleProperty(clacheSize1);
//		this.clacheSize2 = new SimpleDoubleProperty(clacheSize2);
//		this.clacheClip = new SimpleDoubleProperty(clacheClip);
//		this.brightnessParam1 = new SimpleDoubleProperty(brightnessParam1);
//		this.brightnessParam2 = new SimpleDoubleProperty(brightnessParam2);
//		this.contrastParam1 = new SimpleDoubleProperty(contrastParam1);
//		this.contrastParam2 = new SimpleDoubleProperty(contrastParam2);
//		this.medianSize = new SimpleIntegerProperty(medianSize);
//		this.bilateralSize1 = new SimpleDoubleProperty(bilateralSize1);
//		this.bilateralSize2 = new SimpleDoubleProperty(bilateralSize2);
//		this.bilateralSigma = new SimpleIntegerProperty(bilateralSigma);
//		this.gaussianSize1 = new SimpleDoubleProperty(gaussianSize1);
//		this.gaussianSize2 = new SimpleDoubleProperty(gaussianSize2);
//		this.gaussianSigma = new SimpleIntegerProperty(gaussianSigma);
//	}

	public PreprocessingSettings(DoubleProperty tempMin, DoubleProperty tempMax, BooleanProperty scale,
			DoubleProperty clacheSize1, DoubleProperty clacheSize2, DoubleProperty clacheClip,
			DoubleProperty brightnessParam1, DoubleProperty brightnessParam2, DoubleProperty contrastParam1,
			DoubleProperty contrastParam2, IntegerProperty medianSize, DoubleProperty bilateralSize1,
			DoubleProperty bilateralSize2, IntegerProperty bilateralSigma, DoubleProperty gaussianSize1,
			DoubleProperty gaussianSize2, IntegerProperty gaussianSigma) {
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
	
	public DoubleProperty tempMinProperty() {
		return this.tempMin;
	}
	

	public double getTempMin() {
		return this.tempMinProperty().get();
	}
	

	public void setTempMin(final double tempMin) {
		this.tempMinProperty().set(tempMin);
	}
	

	public DoubleProperty tempMaxProperty() {
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
	

	public DoubleProperty clacheSize1Property() {
		return this.clacheSize1;
	}
	

	public double getClacheSize1() {
		return this.clacheSize1Property().get();
	}
	

	public void setClacheSize1(final double clacheSize1) {
		this.clacheSize1Property().set(clacheSize1);
	}
	

	public DoubleProperty clacheSize2Property() {
		return this.clacheSize2;
	}
	

	public double getClacheSize2() {
		return this.clacheSize2Property().get();
	}
	

	public void setClacheSize2(final double clacheSize2) {
		this.clacheSize2Property().set(clacheSize2);
	}
	

	public DoubleProperty clacheClipProperty() {
		return this.clacheClip;
	}
	

	public double getClacheClip() {
		return this.clacheClipProperty().get();
	}
	

	public void setClacheClip(final double clacheClip) {
		this.clacheClipProperty().set(clacheClip);
	}
	

	public DoubleProperty brightnessParam1Property() {
		return this.brightnessParam1;
	}
	

	public double getBrightnessParam1() {
		return this.brightnessParam1Property().get();
	}
	

	public void setBrightnessParam1(final double brightnessParam1) {
		this.brightnessParam1Property().set(brightnessParam1);
	}
	

	public DoubleProperty brightnessParam2Property() {
		return this.brightnessParam2;
	}
	

	public double getBrightnessParam2() {
		return this.brightnessParam2Property().get();
	}
	

	public void setBrightnessParam2(final double brightnessParam2) {
		this.brightnessParam2Property().set(brightnessParam2);
	}
	

	public DoubleProperty contrastParam1Property() {
		return this.contrastParam1;
	}
	

	public double getContrastParam1() {
		return this.contrastParam1Property().get();
	}
	

	public void setContrastParam1(final double contrastParam1) {
		this.contrastParam1Property().set(contrastParam1);
	}
	

	public DoubleProperty contrastParam2Property() {
		return this.contrastParam2;
	}
	

	public double getContrastParam2() {
		return this.contrastParam2Property().get();
	}
	

	public void setContrastParam2(final double contrastParam2) {
		this.contrastParam2Property().set(contrastParam2);
	}
	

	public IntegerProperty medianSizeProperty() {
		return this.medianSize;
	}
	

	public int getMedianSize() {
		return this.medianSizeProperty().get();
	}
	

	public void setMedianSize(final int medianSize) {
		this.medianSizeProperty().set(medianSize);
	}
	

	public DoubleProperty bilateralSize1Property() {
		return this.bilateralSize1;
	}
	

	public double getBilateralSize1() {
		return this.bilateralSize1Property().get();
	}
	

	public void setBilateralSize1(final double bilateralSize1) {
		this.bilateralSize1Property().set(bilateralSize1);
	}
	

	public DoubleProperty bilateralSize2Property() {
		return this.bilateralSize2;
	}
	

	public double getBilateralSize2() {
		return this.bilateralSize2Property().get();
	}
	

	public void setBilateralSize2(final double bilateralSize2) {
		this.bilateralSize2Property().set(bilateralSize2);
	}
	

	public IntegerProperty bilateralSigmaProperty() {
		return this.bilateralSigma;
	}
	

	public int getBilateralSigma() {
		return this.bilateralSigmaProperty().get();
	}
	

	public void setBilateralSigma(final int bilateralSigma) {
		this.bilateralSigmaProperty().set(bilateralSigma);
	}
	

	public DoubleProperty gaussianSize1Property() {
		return this.gaussianSize1;
	}
	

	public double getGaussianSize1() {
		return this.gaussianSize1Property().get();
	}
	

	public void setGaussianSize1(final double gaussianSize1) {
		this.gaussianSize1Property().set(gaussianSize1);
	}
	

	public DoubleProperty gaussianSize2Property() {
		return this.gaussianSize2;
	}
	

	public double getGaussianSize2() {
		return this.gaussianSize2Property().get();
	}
	

	public void setGaussianSize2(final double gaussianSize2) {
		this.gaussianSize2Property().set(gaussianSize2);
	}
	

	public IntegerProperty gaussianSigmaProperty() {
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
