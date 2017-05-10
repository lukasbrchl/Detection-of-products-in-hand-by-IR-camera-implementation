package algorithm.settings.domain;

import javax.xml.bind.annotation.XmlRootElement;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
* This class contains properties that affects algorithm inputs of mog detection method.
* 
* @author Lukas Brchl
*/
@XmlRootElement()
public class MogSettings extends Settings {
	
	ObjectProperty<Integer> mogThreshold;
	ObjectProperty<Integer> mogHistory;	
	
	ObjectProperty<Integer> bcgCanny1;
	ObjectProperty<Integer> bcgCanny2;
	ObjectProperty<Double> bcgLearningRate;
	
	ObjectProperty<Integer> handThreshold;
	ObjectProperty<Integer> handMaskDilate;	
	ObjectProperty<Integer> handMinConvexDepth;
	
	ObjectProperty<Integer> goodsErodeSize;
	ObjectProperty<Integer> goodsErodeIter;
	ObjectProperty<Integer> goodsCloseSize;
	ObjectProperty<Integer> goodsCloseIter;
	ObjectProperty<Integer> goodsOpenSize;
	ObjectProperty<Integer> goodsOpenIter;
	
	
	public MogSettings() {
		this.mogThreshold = new SimpleObjectProperty<Integer>(0);
		this.mogHistory = new SimpleObjectProperty<Integer>(0);
		this.bcgCanny1 = new SimpleObjectProperty<Integer>(0);
		this.bcgCanny2 = new SimpleObjectProperty<Integer>(0);
		this.bcgLearningRate = new SimpleObjectProperty<Double>(0.0);
		this.handThreshold = new SimpleObjectProperty<Integer>(0);
		this.handMaskDilate = new SimpleObjectProperty<Integer>(0);
		this.handMinConvexDepth = new SimpleObjectProperty<Integer>(0);
		this.goodsErodeSize = new SimpleObjectProperty<Integer>(0);
		this.goodsErodeIter = new SimpleObjectProperty<Integer>(0);
		this.goodsCloseSize = new SimpleObjectProperty<Integer>(0);
		this.goodsCloseIter = new SimpleObjectProperty<Integer>(0);
		this.goodsOpenSize = new SimpleObjectProperty<Integer>(0);
		this.goodsOpenIter = new SimpleObjectProperty<Integer>(0);
	}
	
	public MogSettings(ObjectProperty<Integer> mogThreshold, ObjectProperty<Integer> mogHistory,
			ObjectProperty<Integer> bcgCanny1, ObjectProperty<Integer> bcgCanny2,
			ObjectProperty<Double> bcgLearningRate, ObjectProperty<Integer> handThreshold,
			ObjectProperty<Integer> handMaskDilate, ObjectProperty<Integer> handMinConvexDepth,
			ObjectProperty<Integer> goodsErodeSize, ObjectProperty<Integer> goodsErodeIter,
			ObjectProperty<Integer> goodsCloseSize, ObjectProperty<Integer> goodsCloseIter,
			ObjectProperty<Integer> goodsOpenSize, ObjectProperty<Integer> goodsOpenIter) {
		super();
		this.mogThreshold = mogThreshold;
		this.mogHistory = mogHistory;
		this.bcgCanny1 = bcgCanny1;
		this.bcgCanny2 = bcgCanny2;
		this.bcgLearningRate = bcgLearningRate;
		this.handThreshold = handThreshold;
		this.handMaskDilate = handMaskDilate;
		this.handMinConvexDepth = handMinConvexDepth;
		this.goodsErodeSize = goodsErodeSize;
		this.goodsErodeIter = goodsErodeIter;
		this.goodsCloseSize = goodsCloseSize;
		this.goodsCloseIter = goodsCloseIter;
		this.goodsOpenSize = goodsOpenSize;
		this.goodsOpenIter = goodsOpenIter;
	}

	public ObjectProperty<Integer> mogThresholdProperty() {
		return this.mogThreshold;
	}
	
	public Integer getMogThreshold() {
		return this.mogThresholdProperty().get();
	}
	
	public void setMogThreshold(final Integer mogThreshold) {
		this.mogThresholdProperty().set(mogThreshold);
	}
	
	public ObjectProperty<Integer> mogHistoryProperty() {
		return this.mogHistory;
	}
	
	public Integer getMogHistory() {
		return this.mogHistoryProperty().get();
	}
	
	public void setMogHistory(final Integer mogHistory) {
		this.mogHistoryProperty().set(mogHistory);
	}
	
	public ObjectProperty<Integer> bcgCanny1Property() {
		return this.bcgCanny1;
	}
	
	public Integer getBcgCanny1() {
		return this.bcgCanny1Property().get();
	}
	
	public void setBcgCanny1(final Integer bcgCanny1) {
		this.bcgCanny1Property().set(bcgCanny1);
	}
	
	public ObjectProperty<Integer> bcgCanny2Property() {
		return this.bcgCanny2;
	}
	
	public Integer getBcgCanny2() {
		return this.bcgCanny2Property().get();
	}
	
	public void setBcgCanny2(final Integer bcgCanny2) {
		this.bcgCanny2Property().set(bcgCanny2);
	}
	
	public ObjectProperty<Double> bcgLearningRateProperty() {
		return this.bcgLearningRate;
	}
	
	public Double getBcgLearningRate() {
		return this.bcgLearningRateProperty().get();
	}
	
	public void setBcgLearningRate(final Double bcgLearningRate) {
		this.bcgLearningRateProperty().set(bcgLearningRate);
	}
	
	public ObjectProperty<Integer> handThresholdProperty() {
		return this.handThreshold;
	}
	
	public Integer getHandThreshold() {
		return this.handThresholdProperty().get();
	}
	
	public void setHandThreshold(final Integer handThreshold) {
		this.handThresholdProperty().set(handThreshold);
	}
	
	public ObjectProperty<Integer> handMaskDilateProperty() {
		return this.handMaskDilate;
	}
	
	public Integer getHandMaskDilate() {
		return this.handMaskDilateProperty().get();
	}
	
	public void setHandMaskDilate(final Integer handMaskDilate) {
		this.handMaskDilateProperty().set(handMaskDilate);
	}
	
	public ObjectProperty<Integer> handMinConvexDepthProperty() {
		return this.handMinConvexDepth;
	}
	
	public Integer getHandMinConvexDepth() {
		return this.handMinConvexDepthProperty().get();
	}
	
	public void setHandMinConvexDepth(final Integer handMinConvexDepth) {
		this.handMinConvexDepthProperty().set(handMinConvexDepth);
	}
	
	public ObjectProperty<Integer> goodsErodeSizeProperty() {
		return this.goodsErodeSize;
	}
	
	public Integer getGoodsErodeSize() {
		return this.goodsErodeSizeProperty().get();
	}
	
	public void setGoodsErodeSize(final Integer goodsErodeSize) {
		this.goodsErodeSizeProperty().set(goodsErodeSize);
	}
	
	public ObjectProperty<Integer> goodsErodeIterProperty() {
		return this.goodsErodeIter;
	}
	
	public Integer getGoodsErodeIter() {
		return this.goodsErodeIterProperty().get();
	}
	
	public void setGoodsErodeIter(final Integer goodsErodeIter) {
		this.goodsErodeIterProperty().set(goodsErodeIter);
	}
	
	public ObjectProperty<Integer> goodsCloseSizeProperty() {
		return this.goodsCloseSize;
	}
	
	public Integer getGoodsCloseSize() {
		return this.goodsCloseSizeProperty().get();
	}
	
	public void setGoodsCloseSize(final Integer goodsCloseSize) {
		this.goodsCloseSizeProperty().set(goodsCloseSize);
	}
	
	public ObjectProperty<Integer> goodsCloseIterProperty() {
		return this.goodsCloseIter;
	}
	
	public Integer getGoodsCloseIter() {
		return this.goodsCloseIterProperty().get();
	}
	
	public void setGoodsCloseIter(final Integer goodsCloseIter) {
		this.goodsCloseIterProperty().set(goodsCloseIter);
	}
	
	public ObjectProperty<Integer> goodsOpenSizeProperty() {
		return this.goodsOpenSize;
	}
	
	public Integer getGoodsOpenSize() {
		return this.goodsOpenSizeProperty().get();
	}
	
	public void setGoodsOpenSize(final Integer goodsOpenSize) {
		this.goodsOpenSizeProperty().set(goodsOpenSize);
	}
	
	public ObjectProperty<Integer> goodsOpenIterProperty() {
		return this.goodsOpenIter;
	}
	
	public Integer getGoodsOpenIter() {
		return this.goodsOpenIterProperty().get();
	}
	
	public void setGoodsOpenIter(final Integer goodsOpenIter) {
		this.goodsOpenIterProperty().set(goodsOpenIter);
	}
	
	
	
	
}
