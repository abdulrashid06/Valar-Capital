package com.valar.processors;

import com.valar.states.StockState;
import com.valar.entities.TickState;
import com.valar.subscription.TickStateSubscription;
import com.zerodhatech.models.Tick;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TickToMinuteProcessorOptions extends TickToMinuteConverter implements TickToTickStateProcessor {
    private List<TickStateSubscription> subscribers = new ArrayList<>();
    private StockState stockState;
    public TickToMinuteProcessorOptions(long token, StockState stockState){
        this.stockState = stockState;
        this.token = token;

        int initialDelay = 59 - LocalDateTime.now().getSecond();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        initialDelay = Math.max(initialDelay, 0);

        scheduler.scheduleAtFixedRate(() -> {
            afterOneMinute();
        }, initialDelay, 60, TimeUnit.SECONDS);
    }

    @Override
    public Long getInstrumentToken() {
        return token;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super TickState> subscriber) {
        TickStateSubscription subscription = new TickStateSubscription(subscriber);
        subscriber.onSubscribe(subscription);
        subscribers.add(subscription);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Tick item) {
        loadTick(item,stockState);
    }

    @Override
    public void onError(Throwable throwable) {}

    @Override
    public void onComplete() {}

    public void afterOneMinute(){
        current.setVolume(volumeToday - lastVolume);
        lastVolume = volumeToday;
        current.eventClose = current.getClose();
        stockState.setOhlc(current);
    }
}
