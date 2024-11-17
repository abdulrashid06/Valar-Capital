package com.valar.publishers;

import com.valar.processors.TickToTickStateProcessor;
import com.valar.subscription.TickSubscription;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnError;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;

/**
 * @author ansh
 */
public class TickPublisher implements Flow.Publisher<Tick> {

//  public static Map<Long, TickSubscription> subscribers = new HashMap<>();
  public static Map<Long, List<TickSubscription>> subscribers = new HashMap<>();

  /**
   * Opens a web socket and streams tick data for given tokens. It publishes the received tick data batch
   * to all the subscribers. Right now we only have one subscriber i.e VWAPProcessor.
   */
  public void streamTicks(KiteConnect kiteConnect, ArrayList<Long> tokens) throws KiteException {
    final KiteTicker tickerProvider = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());

    tickerProvider.setOnConnectedListener(() -> {
      tickerProvider.subscribe(tokens);
      tickerProvider.setMode(tokens, KiteTicker.modeFull);
    });

//    tickerProvider.setOnOrderUpdateListener(order -> System.out.println("order update " + order.orderId));

    tickerProvider.setOnErrorListener(new OnError() {
      @Override
      public void onError(Exception exception) {
        exception.printStackTrace();
      }

      @Override
      public void onError(KiteException kiteException) {
        kiteException.printStackTrace();
      }
    });

    tickerProvider.setOnTickerArrivalListener(ticks ->
      ticks.forEach(tick -> subscribers.get(tick.getInstrumentToken()).forEach(subscription ->subscription.onNext(tick))));

    tickerProvider.setTryReconnection(true);
    tickerProvider.setMaximumRetries(10);
    tickerProvider.setMaximumRetryInterval(30);

    tickerProvider.connect();

    boolean isConnected = tickerProvider.isConnectionOpen();
    System.out.println(isConnected);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("Disconnecting... the socket");
      tickerProvider.disconnect();
    }));

  }

  @Override
  public void subscribe(Flow.Subscriber<? super Tick> subscriber) {
    TickSubscription subscription = new TickSubscription(subscriber);
    subscriber.onSubscribe(subscription);
    if (subscriber instanceof TickToTickStateProcessor) {
      TickToTickStateProcessor processor = (TickToTickStateProcessor) subscriber;

      if(subscribers.containsKey(processor.getInstrumentToken())){
        subscribers.get(processor.getInstrumentToken()).add(subscription);
      }else{
        List<TickSubscription> subscriptions = new ArrayList();
        subscriptions.add(subscription);
        subscribers.put(processor.getInstrumentToken(),subscriptions);
      }

    } else {
      throw new RuntimeException("Subscriber need to TickToTickStateProcessor in order to return instrument token");
    }
  }

//  @Override
//  public void subscribe(Flow.Subscriber<? super Tick> subscriber) {
//    TickSubscription subscription = new TickSubscription(subscriber);
//    subscriber.onSubscribe(subscription);
//    if (subscriber instanceof TickToTickStateProcessor) {
//      TickToTickStateProcessor processor = (TickToTickStateProcessor) subscriber;
//      subscribers.put(processor.getInstrumentToken(), subscription);
//    } else {
//      throw new RuntimeException("Subscriber need to TickToTickStateProcessor in order to return instrument token");
//    }
//  }


}
