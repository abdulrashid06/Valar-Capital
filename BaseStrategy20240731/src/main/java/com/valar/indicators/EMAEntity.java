package com.valar.indicators;


import com.valar.entities.Ohlc;

/**
 * This class stores intermediate states for calculating EMA50 and EMA100.
 * It also saves current EMA50 and EMA100. It can be modified easily to contain other EMAs.
 */
public class EMAEntity {

    private double alphaS;
    double ema;
    double firstSClose;
    int count;
    private int period;

    public EMAEntity(int period){
        this.period = period;
        alphaS = 2f/((float)period+1f);
    }

    public void calculateEMA(double close){
        if (count< period){
            count++;
            firstSClose+=close;
            if(count== period) ema = firstSClose/ period;
        }else ema = ((close- ema) * alphaS + ema);
    }

    public void calculateEMA(Ohlc ohlc) {
        if (count< period){
            count++;
            firstSClose+=ohlc.getClose();
            if(count== period) ema = firstSClose/ period;
        }else ema = ((ohlc.getClose()- ema) * alphaS + ema);
    }

    @Override
    public String toString() {
        return "EMAEntity{" +
                ", emaS=" + ema +
                '}';
    }


    public double getEMA(){
        return ema;
    }
}