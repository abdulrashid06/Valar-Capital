package com.valar.utils;

import com.valar.entities.Strategy;

import java.util.List;

public class SLChecker extends Thread {
    public List<Strategy> strategies;
    public SLChecker(List<Strategy> strategies){
        this.strategies = strategies;
    }

    @Override
    public void run() {
        while(true) {
            for (Strategy strategy : strategies) {
                if (!strategy.dayExited) {
                    strategy.checkDaySLAndExitsInEnteredTrades(false,true);
                }
            }
        }
    }
}
