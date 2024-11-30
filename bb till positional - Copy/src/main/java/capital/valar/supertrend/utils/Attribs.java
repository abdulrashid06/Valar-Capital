package capital.valar.supertrend.utils;

import java.util.HashMap;
import java.util.Map;

public class Attribs {
    public int totalDaysWithProfit,totalDaysWithLoss;
    public float totalProfitOfProfittableDays,totalLossOfLosableDays;

    public float maxProfit,maxLoss,profit, profitPercentBN,profitWithCost,maxDayProfitTotal;
    public float tradingDays,inProfits,inProfitsWithCost,totalTrades;
    public float bnCloseAtEntry;

    public Map<String,Attribs> dayAttribs = new HashMap<>();

    public Attribs(){

    }

    private Attribs(float bnCloseAtEntry, float profitWithCost){
        this.bnCloseAtEntry = bnCloseAtEntry;
        this.profitWithCost = profitWithCost;
    }

    public synchronized void setDayAttribs(String date,float bnCloseAtEntry,float profitWithCost){
        dayAttribs.put(date,new Attribs(bnCloseAtEntry,profitWithCost));
    }
}
