package capital.valar.supertrend.utils;

import static capital.valar.supertrend.application.PropertiesReader.properties;
import static capital.valar.supertrend.utils.Global.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class KeyValues {
	public String tradeType, ln;
	public int sno, BnOrN, startTime, cutOffTime, endTime;
	public int period, sd, lookBackDuration, maxOverLap, tradeGap;
	public float costPercent, atrFrom, atrTo, vixFrom, vixTo;
	public float minBandRange, maxBandRange, minBandChange, maxBandChange, minBreakout, maxBreakout;
	public float targetBN, triggerBN, fallBackBN, tradeSLBN, daySLBN;
	
	public boolean invalidStartTime, SmaExit, positional;
	
    private int iter;
    private List<String> timeFrames;
    
    public KeyValues(String ln) {
        this.ln = ln;
        String[] splits = ln.split(",");

        // Initialize iterator to zero before assigning values
        iter = 0;
        
        // Mapping each split value to the respective fields
        sno = Integer.parseInt(splits[iter++]); // First field: sno
        tradeType = splits[iter++]; // TradeType (l/s)
        costPercent = Float.parseFloat(splits[iter++]); // Cost percent

        // Time fields are converted to minutes using Global.getInMinutes()
        startTime = Global.getInMinutes(splits[iter++]); 
        cutOffTime = Global.getInMinutes(splits[iter++]); 
        endTime = Global.getInMinutes(splits[iter++]);

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
    }


    public static List<String> getTimeFrames(int timeFrame){
        int marketClosingTime = getInMinutes(15,29);

        List<String> timeFrames = new ArrayList();
        int hr = 9,min = 15;
        String lastSavedTime = null;
        int periodCounter = 0;
        String time;
        while(hr < 15 || min < 31){
            if(hr<10) time = "0"+hr+":";
            else time = hr+":";
            if(min<10)time += "0"+min;
            else time += min;

            periodCounter++;
            if((periodCounter-1)%timeFrame==0){
                if(lastSavedTime!=null)timeFrames.add(lastSavedTime);
                lastSavedTime = time;
            }

            min++;
            if(min>59){
                hr++;
                min=0;
            }
        }

        if(!lastSavedTime.equalsIgnoreCase(timeFrames.get(timeFrames.size()-1)) &&
        getInMinutes(lastSavedTime) <= marketClosingTime) timeFrames.add(lastSavedTime);

        return timeFrames;
    }

    private String getCorrectTimeForm(String s){
        int h = Integer.parseInt(s.split(":")[0]);
        if(h<10 && !s.startsWith("0"))
            s = "0"+s;
        return s;
    }

    private void throwErrorOnInvalidTimeFrame(String time,String name){
        if(!timeFrames.contains(time)) {
            System.err.println(name+" "+time+" doesn't fall in given candlePeriod for keystore "+sno
                    +"\nPossible time frames are : "+timeFrames);
            invalidStartTime = true;
        }
    }

    @Override
    public String toString() {
    	return "KeyValues [sno=" + sno + ", tradeType=" + tradeType+ ", positional=" + positional + ", BnOrN=" + BnOrN + ", startTime="
				+ startTime + ", cutOffTime=" + cutOffTime + ", endTime=" + endTime + ", period=" + period + ", sd="
				+ sd + ", lookBackDuration=" + lookBackDuration + ", maxOverLap=" + maxOverLap + ", tradeGap="
				+ tradeGap + ", costPercent=" + costPercent + ", atrFrom=" + atrFrom + ", atrTo=" + atrTo + ", vixFrom="
				+ vixFrom + ", vixTo=" + vixTo + ", minBandRange=" + minBandRange + ", maxBandRange=" + maxBandRange
				+ ", minBandChange=" + minBandChange + ", maxBandChange=" + maxBandChange + ", minBreakout=" + minBreakout
				+ ", maxBreakout=" + maxBreakout + ", sma=" + SmaExit
				+ ", targetBN=" + targetBN + ", triggerBN=" + triggerBN + ", fallBackBN=" + fallBackBN + ", tradeSLBN="
				+ tradeSLBN + ", daySLBN=" + daySLBN + ", invalidStartTime=" + invalidStartTime + ", timeFrames="
				+ timeFrames + "]";
    }

    public static void setIndexAttribs(int BNOrN){
        if(BNOrN==0) {
            indexFile = properties.getProperty("bankNifty1Min");
            indexDayFile = properties.getProperty("bankNiftyDay");
            tradingDatesFile = properties.getProperty("bankNiftyTradingDates");
            expiryDatesFile = properties.getProperty("bankNiftyExpiryDates");
            strikePlus = 100;
            dataAvailableFromYr = 16;
            optionsDataPath = properties.getProperty("BNOptionFilesPath");
            try{indexLines = Files.readAllLines(Paths.get(indexFile));}catch (Exception e){e.printStackTrace();}
        }else{
            indexFile = properties.getProperty("nifty1Min");
            indexDayFile = properties.getProperty("niftyDay");
            tradingDatesFile = properties.getProperty("niftyTradingDates");
            expiryDatesFile = properties.getProperty("niftyExpiryDates");
            strikePlus = 50;
            dataAvailableFromYr = 19;
            optionsDataPath = properties.getProperty("NiftyOptionFilesPath");
            try{indexLines = Files.readAllLines(Paths.get(indexFile));}catch (Exception e){e.printStackTrace();}
        }
//        listOfFiles = getAllFilesOfFolder();
    }
}
