package com.valar.subscription;

import com.zerodhatech.models.Tick;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;

public class TickSubscription implements Flow.Subscription {

  private final Flow.Subscriber<? super Tick> subscriber;
  private final AtomicLong n = new AtomicLong(0);

  public TickSubscription(Flow.Subscriber<? super Tick> subscriber) {
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

  public void onNext(Tick tick) {
    if (n.getAndDecrement() > 0) {
      subscriber.onNext(tick);
    }
  }
}
