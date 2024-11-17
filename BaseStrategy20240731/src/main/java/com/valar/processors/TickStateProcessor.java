package com.valar.processors;

import com.valar.application.ValarTrade;
import com.valar.entities.TickState;
import com.valar.subscription.TickStateSubscription;
import com.zerodhatech.models.Tick;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

public class TickStateProcessor implements TickToTickStateProcessor {

  private Long instrumentToken;
  private List<TickStateSubscription> subscribers = new ArrayList<>();

  public TickStateProcessor(Long token) {
    this.instrumentToken = token;
  }


  /**
   * Whenever new batch of tick data arrives it's passed to this function by the producer.
   * This function updates tick states by iterating on them.
   */
  @Override
  public void onNext(Tick tick) {
    try {
      TickState tickState = new TickState(tick);
      subscribers.forEach(subscriber -> subscriber.onNext(tickState));
    }catch (Exception e){
      e.printStackTrace();
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionAsString = sw.toString();
      ValarTrade.printError(exceptionAsString+","+e.getCause()+","+e.toString()+","+e.getMessage()+","+e.getStackTrace()+","+e.getLocalizedMessage());
    }
  }

  @Override
  public void onError(Throwable throwable) {
    throwable.printStackTrace();
  }

  @Override
  public void onComplete() {
//    System.out.println(" VWAP completed");
  }

  @Override public Long getInstrumentToken() {
    return instrumentToken;
  }

  @Override public void subscribe(Flow.Subscriber<? super TickState> subscriber) {
    TickStateSubscription subscription = new TickStateSubscription(subscriber);
    subscriber.onSubscribe(subscription);
    subscribers.add(subscription);
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    subscription.request(Long.MAX_VALUE);
  }

  public void printSubscriber(TickStateSubscription subscriber){

  }
}
