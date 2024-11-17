package com.valar.utils;

import com.valar.entities.TradeEntity;

import java.time.LocalTime;

public class PrintAttribs {
    public String reason = "",reasonInfo="";
    public int ksId,tradeId;

    public String time = LocalTime.now().toString();
    public String idAndTime,tradeAndEvent;
    public TradeEntity tradeEntity;
    public double pro,proBN;
    public String transactionType;

    public PrintAttribs(int ksId, int tradeId, TradeEntity tradeEntity,boolean isEntry,char lOrS){
        this.ksId = ksId;
        this.tradeId = tradeId;
        this.tradeEntity = tradeEntity;
        this.pro=tradeEntity.getTotalProfit();
        if(isEntry){if(lOrS=='l')transactionType = "Buy"; else transactionType = "Sell";
        }else{ if(lOrS=='l')transactionType = "Sell";else transactionType = "Buy";}
    }
}
