package com.valar.states;

import com.valar.entities.Ohlc;
import com.valar.indicators.ATREntity;
import com.valar.indicators.EMAEntity;

import java.util.HashMap;
import java.util.Map;

public class IndexState extends State{

    private double dayEMA,dayATR,lastDayClose;
    private ATREntity atrEntity;
    private EMAEntity emaEntity;
    private Map<Integer, Ohlc> ohlcMap = new HashMap<>();

    public IndexState(long token, long symphonyToken,String symbol){
        super(true,token,symphonyToken,"",symbol);
    }

    public Ohlc getOhlc(int period){
        if(ohlcMap.containsKey(period)) return ohlcMap.get(period);
        else return new Ohlc();
    }

    public void setOhlc(Ohlc ohlc,int period){
        if(ohlcMap.containsKey(period)) ohlcMap.get(period).setValues(ohlc);
        else{
            Ohlc newOhlc = new Ohlc();
            ohlc.setValues(newOhlc);
            ohlcMap.put(period, newOhlc);
        }
    }
}