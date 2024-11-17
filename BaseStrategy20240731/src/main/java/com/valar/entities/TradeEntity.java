package com.valar.entities;

import com.valar.orderPlacer.OrderPlacer;
import com.valar.application.ValarTrade;
import com.valar.indices.Index;
import com.valar.states.State;
import com.valar.states.StockState;
import com.valar.utils.*;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class TradeEntity {

    private KeyValues kv;
    private double totalProfit,profit,totalProfitted, totalProfitPercent;
    public String dayExitReason = "";
    public boolean enteredThisMin;
    public boolean isExited;
    private String ids;
    private ValarTrade valarTrade;
    public List<OrderPlacer> orderPlacers;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduler;
    private double indexFutureAtTradeEntry;
    private int tradesCounter;
    private int tradeId;
    private Index index;
    public String tag_Sno;
    public int maxOverlap = 2;

    public State indexState;
    public char lOrS;
    private double entryPrice;
    private double profitPercent;
	private Double currentSma;

	public boolean isTriggered = false;
    private float maxProfitPercent = 0.0f;
    private static final DecimalFormat dfZero = new DecimalFormat("0.00");
    
    
    public TradeEntity(Double sma,KeyValues kv,int tradeId, ValarTrade valarTrade, List<OrderPlacer> orderPlacers,char lOrS,String tag_Sno){
        this.kv = kv;
        this.tradeId = tradeId;
        this.valarTrade = valarTrade;
        this.orderPlacers = orderPlacers;
        this.tag_Sno = tag_Sno;
        this.lOrS = lOrS;
        index = ValarTrade.indexMap.get(kv.indexType);
this.currentSma=sma;
        this.executorService = valarTrade.executorService;
        this.scheduler = valarTrade.scheduler;
        ids = kv.sno +","+tradeId;
        enteredThisMin = true;

        indexState = index.futureState;

        indexFutureAtTradeEntry = indexState.getClose();
        entryPrice = indexState.getOhlc().getClose();
        InitializeTradeEntry();
    }


    public void updateSma(Double currentSma) {
            this.currentSma = currentSma;
    }


    public double getProfit(boolean isIntraMin){
        double price = indexState.getOhlc().getClose();
        if(isIntraMin)price = indexState.getLtp();

        if (lOrS == 'l') profit = price - entryPrice;
        else profit = entryPrice - price;
        profitPercent = (profit / indexFutureAtTradeEntry) * 100;

        return profit;
    }

    public double getProfitPercent(){
        return profitPercent;
    }

    public String getTagKey(){
        return kv.sno +":"+tradeId;
    }

    public double getIndexPrice(){
        return index.stockState.getClose();
    }

    public int getKeyStoreID(){
        return kv.sno;
    }

    public KeyValues getKv(){
        return kv;
    }

    public String getIds(){
        return ids;
    }

    private void InitializeTradeEntry(){
        placeOrder(true,true,"","");
    }

    private String getStrategyTag(int counter){

        return tag_Sno+"_"
                + String.format("%03d", tradeId)+"_"+String.format("%04d",counter);
    }

//    private TradedAttribs shiftTradeAttrib(TradedAttribs tradedAttribs,StockState shiftTo,String reason, String reasonInfo){
//        placeOrderForTradeAttrib(tradedAttribs,false,reason,reasonInfo);
//
//        int index = tradedAttribsList.indexOf(tradedAttribs);
//
//        tradedAttribs = new TradedAttribs(tradedAttribs.la,'s',shiftTo,true);
//        tradedAttribsList.set(index,tradedAttribs);
//        placeOrderForTradeAttrib(tradedAttribs,true,reason,reasonInfo);
//
//        return tradedAttribs;
//    }

    public void addTag(){
        tradesCounter++;
        indexState.addTag(getTagKey(), getStrategyTag(tradesCounter));
    }

    public synchronized void placeOrder(boolean isDayFirstEntry,boolean isEntry,String reason,String reasonInfo){

        tradesCounter++;
        indexState.addTag(getTagKey(), getStrategyTag(tradesCounter));

        valarTrade.executorService.submit(() -> {
            orderPlacers.parallelStream().forEach(
                (orderPlacer) -> {
                schedule(orderPlacer, isEntry, indexState, reason, reasonInfo, lOrS);
            });
        });
    }

    public void schedule(OrderPlacer orderPlacer,boolean isEntry
    ,State ss,String reason,String reasonInfo,char lOrS){
        if(ss==null)return;

        PrintAttribs printAttribs = new PrintAttribs(kv.sno,tradeId,this,isEntry,lOrS);

        orderPlacer.scheduleLimitOrdersSplits(kv.indexType,ids,lOrS,false, this, ss,isEntry,false);
        int index = orderPlacers.indexOf(orderPlacer);
        if(index==0) {
            String event;
            if (isEntry) event = "Entry";
            else{
                event = "Exit";
                if(lOrS=='l')lOrS = 'l';
                else lOrS = 's';
            }
            printAttribs.time = LocalTime.now().minusMinutes(1).toString();
            printAttribs.idAndTime = ids + "," + printAttribs.time;
            printAttribs.tradeAndEvent = lOrS + "," + event;
            printAttribs.reason = reason;
            printAttribs.reasonInfo = reasonInfo;
            printAttribs.pro = profit;
            printAttribs.proBN = totalProfitPercent;
            ss.printOrderInfo(getTagKey(), printAttribs);
        }
    }

    public void placeExitOrder(String reason,String reasonInfo,boolean isExited){
        if(!this.isExited)this.isExited = isExited;
        dayExitReason = reason;
        placeOrder(false,false,reason,reasonInfo);
    }

    private void updateProfitMetrics(boolean isIntraMin){
        profit = getProfit(isIntraMin);
        totalProfit = profit + totalProfitted;
        totalProfitPercent = (totalProfit/ indexFutureAtTradeEntry) * 100;
    }

    public void updateProfitMetricsAndMaxMin(boolean isIntraMin){
        updateProfitMetrics(isIntraMin);
    }

    
    public void checkExits(boolean isExitTime) {
        updateProfitMetrics(false);

        maxProfitPercent = Float.max(maxProfitPercent, (float) profitPercent);

        // Trigger check to see if profit percent meets trigger threshold
        if (!isTriggered) {
            isTriggered = getProfitPercent() >= kv.triggerBN;
        }

        // Check various exit conditions
        if (profitPercent <= -kv.tradeSLBN) {
        	placeExitOrder("Trade SL", "Stop loss reached", true);
        } else if (profitPercent >= kv.targetBN) {
        	placeExitOrder("Trade Target", "Profit target reached", true);
        } else if (isTriggered && (maxProfitPercent - profitPercent) >= kv.fallBackBN) {
        	placeExitOrder("Trigger Fallback", "Trigger fallback", true);
        } else if (checkSmaExit() && kv.SmaExit) {
        	placeExitOrder("SMA Exit", "Exited due to SMA condition"+currentSma+" ", true);
        } else if (!kv.positional && isExitTime) {
            placeExitOrder("EndTime", "End of trading day", true);
        }
    }


    public double getTotalProfit(){
        return totalProfit;
    }

    public int getTradeId(){
        return tradeId;
    }

    public double getTotalProfitPercent(){
        return totalProfitPercent;
    }
    
    
    public boolean checkSmaExit() {
    	boolean flag = false;
    	if(kv.tradeType.equalsIgnoreCase("l")) {
    		if(index.stockState.getOhlc().getClose() < currentSma) {
    			flag= true;
    		}
    	}else if(kv.tradeType.equalsIgnoreCase("s")) {
//            System.out.println("sma exit S : " + "  "+index.stockState.getOhlc().getClose() +"     "+ currentSma);
    		if(index.stockState.getOhlc().getClose() > currentSma) {
//    			System.out.println(kv.sno+"   " + indexohlc.dnt + "    "+ indexohlc.close + "    " + currentSma+"   "+sma);
    			flag= true;
//                System.out.println(flag + "sma exitied");
    		}
    	}
    	return flag;
    }

}