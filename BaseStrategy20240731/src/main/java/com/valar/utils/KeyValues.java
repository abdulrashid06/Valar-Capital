package com.valar.utils;

import com.valar.application.ValarTrade;
import static  com.valar.application.ValarTrade.getInMinutes;

public class KeyValues{
    public int sno,day,indexType;
    public String label,tag,tradeType;
    private int iter;
    public int startTimeMins, exitTimeMins;
    public int atrPeriod=14;
    
    public int startTime, cutOffTime, endTime, candlePeriod;
	public int period, sd, lookBackDuration, maxOverLap, tradeGap;
	public float  atrFrom, atrTo, vixFrom, vixTo;
	public float minBandRange, maxBandRange, minBandChange, maxBandChange, minBreakout, maxBreakout;
	public float targetBN, triggerBN, fallBackBN, tradeSLBN, daySLBN;
	
	public boolean invalidStartTime, SmaExit, positional;

    public KeyValues(String out){

        String[] splits = out.split(",");

        sno = Integer.parseInt(splits[iter++]);
        label = splits[iter++];
        tag = getSevenDigitesTag(splits[iter++]);

        day = Integer.parseInt(splits[iter++]);
        indexType = Integer.parseInt(splits[iter++]);
        tradeType = splits[iter++]; // TradeType (l/s)
        positional = Boolean.parseBoolean(splits[iter++]);

        // Time fields are converted to minutes using Global.getInMinutes()
        startTime = getInMinutes(splits[iter++]); 
        cutOffTime = getInMinutes(splits[iter++]); 
        endTime = getInMinutes(splits[iter++]);
        candlePeriod = Integer.parseInt(splits[iter++]); 

        // Parse ATRFrom%, ATRTo%, VixFrom%, VixTo%
        atrFrom = Float.parseFloat(splits[iter++]);
        atrTo = Float.parseFloat(splits[iter++]);
        vixFrom = Float.parseFloat(splits[iter++]);
        vixTo = Float.parseFloat(splits[iter++]);

        // Parse Period, noOfSD, Band Range, LookbackDuration, and other fields
        period = Integer.parseInt(splits[iter++]); 
        sd = Integer.parseInt(splits[iter++]);
        minBandRange = Float.parseFloat(splits[iter++]); 
        maxBandRange = Float.parseFloat(splits[iter++]);
        lookBackDuration = Integer.parseInt(splits[iter++]);

        // Band Change and Breakout percentages
        minBandChange = Float.parseFloat(splits[iter++]);
        maxBandChange = Float.parseFloat(splits[iter++]);
        minBreakout = Float.parseFloat(splits[iter++]);
        maxBreakout = Float.parseFloat(splits[iter++]);

        // Exit at SMA
        SmaExit = Boolean.parseBoolean(splits[iter++]);

        // Target, Trigger, Fallback BN values
        targetBN = Float.parseFloat(splits[iter++]);
        triggerBN = Float.parseFloat(splits[iter++]);
        fallBackBN = Float.parseFloat(splits[iter++]);
        tradeSLBN = Float.parseFloat(splits[iter++]);
        daySLBN = Float.parseFloat(splits[iter++]);

        // MaxOverlap and TradeGap
        maxOverLap = Integer.parseInt(splits[iter++]);
        tradeGap = Integer.parseInt(splits[iter++]);



        System.out.println(this);
    }

    private String getSevenDigitesTag(String tagPrefix){
        int length = tagPrefix.length();
        if (length < 7) {
            while (length < 7) {
                tagPrefix += "x";
                length++;
            }
            return tagPrefix;
        } else if (length > 7) {
            return tagPrefix.substring(0, 7);
        } else {
            return tagPrefix;
        }
    }

    private String getCorrectTimeForm(String s){
        int h = Integer.parseInt(s.split(":")[0]);
        if(h<10 && !s.startsWith("0"))
            s = "0"+s;
        return s;
    }

}