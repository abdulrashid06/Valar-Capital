package com.valar.entities;

import com.zerodhatech.models.Depth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class Ohlc {
    private double open;
    private double high;
    private double low=999999;
    private double close;
    public double eventClose;
    private double volume;
    public float volumeSum,volumeCount;


    public Ohlc(){

    }

    public Ohlc(String ln){
        setValues(ln);
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public void setValues(String ln){
        String[] splits = ln.split(",");
        this.open = Double.parseDouble(splits[1]);
        this.high = Double.parseDouble(splits[2]);
        this.low = Double.parseDouble(splits[3]);
        this.close = Double.parseDouble(splits[4]);
        this.eventClose = close;

        volumeSum+=volume;
        volumeCount++;
    }

    public void setValues(Ohlc ohlc2){
        this.open = ohlc2.getOpen();
        this.close = ohlc2.getClose();
        this.high = ohlc2.getHigh();
        this.low = ohlc2.getLow();
        this.volume = ohlc2.getVolume();
        this.eventClose = ohlc2.eventClose;

        volumeSum+=volume;
        volumeCount++;
    }

    public boolean avgVolumeCondition(double avgVol,double volMultiplier){
        return ( (close * (volumeSum/volumeCount)) >= (avgVol * volMultiplier)/375f);
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public String toString(){
        return "Open = "+open+" ,high = "+high+" ,low = "+low+" ,close = "+close+" ,volume = "+volume;
    }
}