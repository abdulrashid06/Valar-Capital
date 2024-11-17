package com.valar.entities;

import com.zerodhatech.models.Tick;

public class TickState {
  long token;
  Ohlc ohlc = new Ohlc();
  double vwap;
  Tick tick;
  public int belongsToCandlePeriod;

  public TickState(Tick tick){
    this.tick = tick;
  }

  public TickState(int belongsToCandlePeriod){
    this.belongsToCandlePeriod = belongsToCandlePeriod;
  }

  public void setVwap(double vwap){
    this.vwap = vwap;
  }

  public double getVwap(){return vwap;}

  public Ohlc getOhlc(){
    return ohlc;
  }

  public void setOhlc(Ohlc ohlc){
    this.ohlc.setValues(ohlc);
  }

  public long getToken() {
    return token;
  }

  public void setToken(long token) {
    this.token = token;
  }

  public Tick getTick() {
    return tick;
  }

  public void setTick(Tick tick) {
    this.tick = tick;
  }
}
