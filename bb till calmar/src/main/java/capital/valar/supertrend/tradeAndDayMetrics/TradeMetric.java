package capital.valar.supertrend.tradeAndDayMetrics;



import capital.valar.supertrend.entities.Ohlc;
import capital.valar.supertrend.utils.KeyValues;
import capital.valar.supertrend.utils.PrintAttribs;
import capital.valar.supertrend.utils.PrintWriters;

import java.io.PrintWriter;

public class TradeMetric {
    public int totalTrades;
    public float maxProfit = -Float.MAX_VALUE,maxLoss = Float.MAX_VALUE
            ,maxProfitPercent = -Float.MAX_VALUE,maxLossPercent = Float.MAX_VALUE;
    public float profit, profitPercent, profitPercentBn, profitCost, profitPercentCost;
//    public float profitWithCost,profitPercentWithCost;
    public int inProfits/*,inProfitsWithCost*/,inLoss,inProfitPercents,inLossPercents, inProfitTradesCost, inLossTradesCost;
    float avgProfit,avgLoss,avgProfitPercent,avgLossPercent, totalHoldingPeriod;
    public float winPercent/*,winPercentWithCost*/;
    protected float totalSell,totalBuy;
    PrintWriter orderInfo;

    public char lOrS;
    public Ohlc entryOhlc,exitOhlc;

    public float profitWithCost,profitPercentWithCost;

    public TradeMetric(){}

    public TradeMetric(PrintWriter orderInfo){
        this.orderInfo = orderInfo;
    }

    public TradeMetric(Ohlc entryOhlc,char lOrS){
        this.entryOhlc = entryOhlc;
        this.lOrS = lOrS;
        this.orderInfo = PrintWriters.orderInfoPrintWriter;
    }

    public void calculateOverAllMetricsAndPrint(float profitPercentBn,float profitCost,String reason, String reasonInfo, int id,int holdingPeriod, KeyValues kv, Ohlc exitOhlc,float indexClose, String option, PrintAttribs printAttribs){
        this.exitOhlc = exitOhlc;
        if(lOrS =='l') profit = exitOhlc.close - entryOhlc.close;
        else profit = entryOhlc.close - exitOhlc.close;

        this.profitPercentBn += profitPercentBn;
//        this.profitWithCost += printAttribs.profitCost;
        this.profitCost = profitCost;
        this.totalHoldingPeriod = holdingPeriod;
        this.profitPercentCost = printAttribs.profitCostPercent;
        if(profit>=0) inProfits++;
        else inLoss++;
        
        if(profitCost >=0) inProfitTradesCost++;
        else inLossTradesCost++;

        totalTrades = 1;

        orderInfo.write(kv.sno+","+entryOhlc.date+","+kv.tradeType+","+id+","+holdingPeriod+"," +entryOhlc.date+"," +entryOhlc.time+","+entryOhlc.close
                +","+exitOhlc.date+","+exitOhlc.time+","+exitOhlc.close+","+reason/*+","+reasonInfo*/+","+profit+","+ this.profitPercentBn+"," +printAttribs+"\n");
    }

    // used for calculation of the day metric ( profit, loss, etc...)
    public void updateOverAll(TradeMetric tradeMetric){
        profit += tradeMetric.profit;
        profitCost += tradeMetric.profitCost;
        totalHoldingPeriod += tradeMetric.totalHoldingPeriod;
        profitPercentCost += tradeMetric.profitPercentCost;
        profitPercentBn += tradeMetric.profitPercentBn;
        
//        System.out.println(totalHoldingPeriod);

        if(tradeMetric.lOrS =='l') {
            totalBuy += tradeMetric.entryOhlc.close;
            totalSell += tradeMetric.exitOhlc.close;
        }else{
            totalBuy += tradeMetric.exitOhlc.close;
            totalSell += tradeMetric.entryOhlc.close;
        }

        if(profit>=0) inProfits++;
        else inLoss++;
        
        if(profitCost > 0) inProfitTradesCost++;
        else inLossTradesCost++;

        totalTrades = 1;
    }

    public void closePrinter(){
        orderInfo.close();
    }

}
