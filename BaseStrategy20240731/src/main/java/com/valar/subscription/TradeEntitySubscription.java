package com.valar.subscription;

import com.valar.entities.TradeEntity;

import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;

public class TradeEntitySubscription implements Flow.Subscription {
  private final Flow.Subscriber<? super TradeEntity> subscriber;
  private final AtomicLong n = new AtomicLong(0);

  public TradeEntitySubscription(Flow.Subscriber<? super TradeEntity> subscriber) {
    this.subscriber = subscriber;
  }

  @Override
  public void request(long n) {
    this.n.set(n);
  }

  @Override
  public void cancel() {
    this.n.set(0);
  }

  public void onNext(TradeEntity tradeEntity) {
    if (n.getAndDecrement() > 0) {
      subscriber.onNext(tradeEntity);
    }
  }
}
