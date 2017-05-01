package data.algorithm.params;

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
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PreprocessingSettings {

	DoubleProperty tempMin;
	DoubleProperty tempMax;
	BooleanProperty scale;
	DoubleProperty clacheParam1;
	DoubleProperty clacheParam2;
	DoubleProperty clacheParam3;
	DoubleProperty brightnessParam1;
	DoubleProperty brightnessParam2;
	DoubleProperty contrastParam1;
	DoubleProperty contrastParam2;
	IntegerProperty median;
	IntegerProperty bilateralSigma;
	IntegerProperty bilateralParam1;
	IntegerProperty bilateralParam2;

	public PreprocessingSettings() {
		this.tempMin = new SimpleDoubleProperty();
		this.tempMax = new SimpleDoubleProperty();
		this.scale = new SimpleBooleanProperty();
		this.clacheParam1 = new SimpleDoubleProperty();
		this.clacheParam2 = new SimpleDoubleProperty();
		this.clacheParam3 = new SimpleDoubleProperty();
		this.brightnessParam1 = new SimpleDoubleProperty();
		this.brightnessParam2 = new SimpleDoubleProperty();
		this.contrastParam1 = new SimpleDoubleProperty();
		this.contrastParam2 = new SimpleDoubleProperty();
		this.median = new SimpleIntegerProperty();
		this.bilateralSigma = new SimpleIntegerProperty();
		this.bilateralParam1 = new SimpleIntegerProperty();
		this.bilateralParam2 = new SimpleIntegerProperty();
	}

	public PreprocessingSettings(double tempMin, double tempMax, boolean scale, double clacheParam1,
			double clacheParam2, double clacheParam3, double brightnessParam1, double brightnessParam2,
			double contrastParam1, double contrastParam2, int median, int bilateralSigma, int bilateralParam1,
			int bilateralParam2) {
		super();
		this.tempMin = new SimpleDoubleProperty(tempMin);
		this.tempMax = new SimpleDoubleProperty(tempMax);
		this.scale = new SimpleBooleanProperty(scale);
		this.clacheParam1 = new SimpleDoubleProperty(clacheParam1);
		this.clacheParam2 = new SimpleDoubleProperty(clacheParam2);
		this.clacheParam3 = new SimpleDoubleProperty(clacheParam3);
		this.brightnessParam1 = new SimpleDoubleProperty(brightnessParam1);
		this.brightnessParam2 = new SimpleDoubleProperty(brightnessParam2);
		this.contrastParam1 = new SimpleDoubleProperty(contrastParam1);
		this.contrastParam2 = new SimpleDoubleProperty(contrastParam2);
		this.median = new SimpleIntegerProperty(median);
		this.bilateralSigma = new SimpleIntegerProperty(bilateralSigma);
		this.bilateralParam1 = new SimpleIntegerProperty(bilateralParam1);
		this.bilateralParam2 = new SimpleIntegerProperty(bilateralParam2);
	}

	public DoubleProperty getTempMinProp() {
		return tempMin;
	}
	
	public double getTempMin() {
		return tempMin.get();
	}

	public void setTempMin(double tempMin) {
		this.tempMin.set(tempMin);
	}

	public DoubleProperty getTempMaxProp() {
		return tempMax;
	}
	
	public double getTempMax() {
		return tempMax.get();
	}

	public void setTempMax(double tempMax) {
		this.tempMax.set(tempMax);
	}

	public BooleanProperty IsScaleProp() {
		return scale;
	}
	
	public boolean isScale() {
		return scale.get();
	}

	public void setScale(boolean scale) {
		this.scale.set(scale);
	}

	public DoubleProperty getClacheParam1Prop() {
		return clacheParam1;
	}
	
	public double getClacheParam1() {
		return clacheParam1.get();
	}

	public void setClacheParam1(double clacheParam1) {
		this.clacheParam1.set(clacheParam1);
	}

	public DoubleProperty getClacheParam2Prop() {
		return clacheParam2;
	}
	
	public double getClacheParam2() {
		return clacheParam2.doubleValue();
	}

	public void setClacheParam2(double clacheParam2) {
		this.clacheParam2.set(clacheParam2);
	}
	
	public DoubleProperty getClacheParam3Prop() {
		return clacheParam3;
	}

	public double getClacheParam3() {
		return clacheParam3.doubleValue();
	}

	public void setClacheParam3(double clacheParam3) {
		this.clacheParam3.set(clacheParam3);
	}

	public DoubleProperty getBrightnessParam1Prop() {
		return brightnessParam1;
	}
	
	public double getBrightnessParam1() {
		return brightnessParam1.doubleValue();
	}

	public void setBrightnessParam1(double brightnessParam1) {
		this.brightnessParam1.set(brightnessParam1);
	}

	public DoubleProperty getBrightnessParam2Prop() {
		return brightnessParam2;
	}
	
	public double getBrightnessParam2() {
		return brightnessParam2.doubleValue();
	}

	public void setBrightnessParam2(double brightnessParam2) {
		this.brightnessParam2.set(brightnessParam2);
	}

	public DoubleProperty getContrastParam1Prop() {
		return contrastParam1;
	}
	
	public double getContrastParam1() {
		return contrastParam1.doubleValue();
	}

	public void setContrastParam1(double contrastParam1) {
		this.contrastParam1.set(contrastParam1);
	}
	
	public DoubleProperty getContrastParam2Prop() {
		return contrastParam2;
	}

	public double getContrastParam2() {
		return contrastParam2.doubleValue();
	}

	public void setContrastParam2(double contrastParam2) {
		this.contrastParam2.set(contrastParam2);
	}
	
	public IntegerProperty getMedianProp() {
		return median;
	}

	public int getMedian() {
		return median.intValue();
	}

	public void setMedian(int median) {
		this.median.set(median);
	}
	
	public IntegerProperty getBilateralSigmaProp() {
		return bilateralSigma;
	}

	public int getBilateralSigma() {
		return bilateralSigma.intValue();
	}

	public void setBilateralSigma(int bilateralSigma) {
		this.bilateralSigma.set(bilateralSigma);
	}

	public IntegerProperty getBilateralParam1Prop() {
		return bilateralParam1;
	}
	
	public int getBilateralParam1() {
		return bilateralParam1.intValue();
	}

	public void setBilateralParam1(int bilateralParam1) {
		this.bilateralParam1.set(bilateralParam1);
	}
	
	public IntegerProperty getBilateralParam2Prop() {
		return bilateralParam2;
	}

	public int getBilateralParam2() {
		return bilateralParam2.intValue();
	}

	public void setBilateralParam2(int bilateralParam2) {
		this.bilateralParam2.set(bilateralParam2);
	}	
	
	

}
