package com.valar.publishers;

import com.valar.processors.TickToTickStateProcessor;
import com.valar.subscription.TickSubscription;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnError;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Flow;

public class HistoricalPublisher  implements Flow.Publisher<Tick> {

    public static final String FROM_DATE = "2018-10-03";
    private Map<Long, TickSubscription> subscribers = new HashMap<>();

    /**
     * Opens a web socket and streams tick data for given tokens. It publishes the received tick data batch
     * to all the subscribers. Right now we only have one subscriber i.e VWAPProcessor.
     */
    public void streamTicks(KiteConnect kiteConnect, ArrayList<Long> tokens) throws KiteException {
//        final KiteTicker tickerProvider = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());
//
//        tickerProvider.setOnConnectedListener(() -> {
//            tickerProvider.subscribe(tokens);
//            tickerProvider.setMode(tokens, KiteTicker.modeQuote);
//        });
//
//        tickerProvider.setOnOrderUpdateListener(order -> System.out.println("order update " + order.orderId));
//
//        tickerProvider.setOnErrorListener(new OnError() {
//            @Override
//            public void onError(Exception exception) {
//                exception.printStackTrace();
//            }
//
//            @Override
//            public void onError(KiteException kiteException) {
//                kiteException.printStackTrace();
//            }
//        });
//
//        tickerProvider.setOnTickerArrivalListener(ticks ->
//                ticks.forEach(tick -> subscribers.get(tick.getInstrumentToken()).onNext(tick)));
//
//        tickerProvider.setTryReconnection(true);
//        tickerProvider.setMaximumRetries(10);
//        tickerProvider.setMaximumRetryInterval(30);
//
//        tickerProvider.connect();
//
//        boolean isConnected = tickerProvider.isConnectionOpen();
//        System.out.println(isConnected);
        HistoricalData hdata = null;
        try {
            try {
                hdata = kiteConnect
                        .getHistoricalData(converToDate(LocalDate.parse(FROM_DATE)), converToDate(LocalDate.now()), tokens + "",
                                "minute", false);
                System.out.println(hdata.dataArrayList.get(hdata.dataArrayList.size() - 1).timeStamp+
                        " Close-> "+hdata.dataArrayList.get(hdata.dataArrayList.size() - 1).close);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch(KiteException e){}

    }

    @Override
    public void subscribe(Flow.Subscriber<? super Tick> subscriber) {
        TickSubscription subscription = new TickSubscription(subscriber);
        subscriber.onSubscribe(subscription);
        if (subscriber instanceof TickToTickStateProcessor) {
            TickToTickStateProcessor processor = (TickToTickStateProcessor) subscriber;
            subscribers.put(processor.getInstrumentToken(), subscription);
        } else {
            throw new RuntimeException("Subscriber need to TickToTickStateProcessor in order to return instrument token");
        }
    }

    public static Date converToDate(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atTime(LocalTime.MAX)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

}
