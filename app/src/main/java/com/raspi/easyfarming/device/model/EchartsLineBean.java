package com.raspi.easyfarming.device.model;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dell1 on 2017/5/28.
 */
public class EchartsLineBean {

    public String type;
    public String title;
    public int maxValue;
    public int minValue;
    public String imageUrl;
    public List<String> times;
    public List<String> steps;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getTimes() {
        return times;
    }

    public void setTimes(List<String> times) {
        this.times = times;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return "EchartsLineBean{" +
                "type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", maxValue=" + maxValue +
                ", minValue=" + minValue +
                ", imageUrl='" + imageUrl + '\'' +
                ", times=" + times.toString() +
                ", steps=" + steps.toString() +
                '}';
    }
}
