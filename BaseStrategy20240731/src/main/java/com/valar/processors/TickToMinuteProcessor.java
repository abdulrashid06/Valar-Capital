package com.valar.processors;

import com.valar.application.ValarTrade;
import com.valar.entities.Ohlc;
import com.valar.states.IndexState;
import com.valar.states.State;
import com.valar.entities.TickState;
import com.valar.subscription.TickStateSubscription;
import com.zerodhatech.models.Tick;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TickToMinuteProcessor extends TickToMinuteConverter implements Flow.Processor<TickState, TickState>  {
    private List<TickStateSubscription> subscribers = new ArrayList<>();
    private ValarTrade valarTrade;
    private int indexType;
    private State stockState;

    private Set<Integer> candlePeriods;
    private long token;
    private Map<Integer,CandleOhlc> candleMap = new HashMap<>();

    class CandleOhlc{
        int period;
        boolean newMin;
        boolean firstMinSkipped;
        Ohlc ohlc = new Ohlc();
        public CandleOhlc(int period){
            this.period = period;
        }
    }

    public TickToMinuteProcessor(ValarTrade valarTrade, State stockState, long token, int indexType){
        this.valarTrade = valarTrade;
        this.stockState = stockState;
        this.token = token;
        this.indexType = indexType;
        candlePeriods = new HashSet<>();
        candlePeriods.add(1);
        createSchedulers();
    }

    public TickToMinuteProcessor(ValarTrade valarTrade, State stockState, long token, int indexType, Set<Integer> candlePeriods){
        this.valarTrade = valarTrade;
        this.stockState = stockState;
        this.token = token;
        this.indexType = indexType;
        this.candlePeriods = candlePeriods;
       this. candlePeriods.add(1);
        createSchedulers();
    }

    private void createSchedulers(){
//        System.out.println(LocalTime.now()+" Tick2Min "+stockState.getToken()+" "+stockState.getId());
        for(int candlePeriod : candlePeriods) {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            int initialDelay = 59 - LocalDateTime.now().getSecond();

            System.out.println("initialDelay "+initialDelay);
            initialDelay = Math.max(initialDelay, 0);
            candleMap.put(candlePeriod,new CandleOhlc(candlePeriod));

            scheduler.scheduleAtFixedRate(() -> {
                updateIndexAfterOneMinute(candlePeriod);
            }, initialDelay, 60L * candlePeriod, TimeUnit.SECONDS);
        }
    }


    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(TickState tickState) {
        try {
//        System.out.println(LocalTime.now()+" "+stockState.getId()+" "+
//                tickState.getTick().getInstrumentToken()+" "+tickState.getTick().getLastTradedPrice());
            Tick tick = tickState.getTick();
            for(CandleOhlc candleOhlc : candleMap.values()) {
                Ohlc current = candleOhlc.ohlc;
                if (candleOhlc.newMin) {
                    current.setOpen(tick.getLastTradedPrice());
                    current.setHigh(tick.getLastTradedPrice());
                    current.setLow(tick.getLastTradedPrice());
                    candleOhlc.newMin = false;

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
            }
            volumeToday = tick.getVolumeTradedToday();

            if(stockState!=null) {
                stockState.setLtp(tick.getLastTradedPrice());
            }
        }catch (Exception e){
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            valarTrade.printError(exceptionAsString+","+e.getCause()+","+e.toString()+","+e.getMessage()+","+e.getStackTrace()+","+e.getLocalizedMessage());
        }
    }

     public void updateIndexAfterOneMinute(int candlePeriod){
        try {
            TickState tickState = new TickState(candlePeriod);
            CandleOhlc candleOhlc = candleMap.get(candlePeriod);
            Ohlc current = candleOhlc.ohlc;
            current.setVolume(volumeToday - lastVolume);
            lastVolume = volumeToday;
            if (candleOhlc.firstMinSkipped) {
                tickState.setOhlc(current);
                candleOhlc.newMin = true;
            } else {
                candleOhlc.newMin = true;
                System.out.println("Skipped");
            }

            if(candlePeriod==1)stockState.setOhlc(current);
            else ((IndexState)stockState).setOhlc(current,candlePeriod);

            if (candleOhlc.firstMinSkipped) {
//                System.out.println(LocalTime.now() + " Sending Tick2Min for " + stockState.getId());
                subscribers.forEach(subscriber -> subscriber.onNext(tickState));
            } else candleOhlc.firstMinSkipped = true;
        }catch (Exception e){
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            valarTrade.printError(exceptionAsString+","+e.getCause()+","+e.toString()+","+e.getMessage()+","+e.getStackTrace()+","+e.getLocalizedMessage());
        }
     }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {}


    @Override public void subscribe(Flow.Subscriber<? super TickState> subscriber) {
        TickStateSubscription subscription = new TickStateSubscription(subscriber);
        subscriber.onSubscribe(subscription);
        subscribers.add(subscription);
    }
}