package com.valar.processors;

import com.valar.application.ValarTrade;
import com.valar.entities.*;
import com.valar.states.StockState;
import com.valar.subscription.TradeEntitySubscription;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

public class DecisionProcessor implements Flow.Processor<TickState, TradeEntity> {

    private StockState stockState; //TODO: initiate stock state
    private List<TradeEntitySubscription> subscribers = new ArrayList<>();

    private ValarTrade valarTrade;
    public DecisionProcessor(ValarTrade valarTrade,StockState stockState) {
        this.valarTrade = valarTrade;
        this.stockState = stockState;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(TickState tickState) {
        try{

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
    public void onComplete() {
//        System.out.println(" VWAP completed");
    }

    @Override
    public void subscribe(Flow.Subscriber<? super TradeEntity> subscriber) {
        TradeEntitySubscription subscription = new TradeEntitySubscription(subscriber);
        subscriber.onSubscribe(subscription);
        subscribers.add(subscription);
    }
}

