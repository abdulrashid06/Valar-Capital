package com.valar.processors;

import com.valar.entities.TickState;
import com.zerodhatech.models.Tick;
import java.util.concurrent.Flow;

public interface TickToTickStateProcessor extends Flow.Processor<Tick, TickState> {

  public Long getInstrumentToken();
}
