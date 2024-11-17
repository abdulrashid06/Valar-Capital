package com.valar.processors;

import com.valar.entities.Ohlc;
import com.valar.states.State;
import com.valar.states.StockState;
import com.valar.entities.TickState;
import com.zerodhatech.models.Tick;

import java.time.LocalTime;
import java.util.Locale;

public class TickToMinuteConverter {

    protected Ohlc current = new Ohlc();
    boolean newMin;
    protected double lastVolume, volumeToday;
    protected long token;


    public void loadTick(Tick tick, State stockState) {
        if (newMin) {
            current.setOpen(tick.getLastTradedPrice());
            current.setHigh(tick.getLastTradedPrice());
            current.setLow(tick.getLastTradedPrice());
            newMin = false;

        } else {
            if (current.getHigh() < tick.getLastTradedPrice()) {
                current.setHigh(tick.getLastTradedPrice());
            }
            if (current.getLow() > tick.getLastTradedPrice()) {
                current.setLow(tick.getLastTradedPrice());
            }
            if (current.getOpen() == 0) {
                current.setOpen(tick.getLastTradedPrice());
            }
        }

        current.setClose(tick.getLastTradedPrice());
        volumeToday = tick.getVolumeTradedToday();

        if (stockState != null) {
            stockState.setLtp(tick.getLastTradedPrice());
        }
    }
}


