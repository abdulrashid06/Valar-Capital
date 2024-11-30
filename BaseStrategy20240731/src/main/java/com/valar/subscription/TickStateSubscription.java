package com.valar.subscription;

import com.valar.entities.TickState;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;

public class TickStateSubscription implements Flow.Subscription {
  private final Flow.Subscriber<? super TickState> subscriber;
  private final AtomicLong n = new AtomicLong(0);

  public TickStateSubscription(Flow.Subscriber<? super TickState> subscriber) {
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

  public void onNext(TickState tickState) {
    if (n.getAndDecrement() > 0) {
      subscriber.onNext(tickState);
    }
  }
}
