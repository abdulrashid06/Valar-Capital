package capital.valar.supertrend.tradeAndDayMetrics;

import capital.valar.supertrend.utils.PrintWriters;

public class DayMetric extends TradeMetric {
    public float dayMaxProfit,dayMaxProfitPercent;
    public float totalProfitOfProfittableTrades,totalLossOfLosableTrades,totalProfitPercentOfProfittableTrades,totalLossPercentOfLosableTrades;
    public float totalProfitOfProfittableTradesCost,totalLossOfLosableTradesCost, inProfitTradesWithCost, inLossTradesWithCost, totalHoldingPeriodSum;
    private float costPercent,bnCloseAtEntry,cost, psp;
    public int totalTrades;
    String date;

    public DayMetric(String date,float costPercent,float bnCloseAtEntry){
        super(PrintWriters.dayWisePrintWriter);
        this.date = date;
        this.costPercent = costPercent;
        this.bnCloseAtEntry = bnCloseAtEntry;
    }

    public void calculateOverAllMetricsAndPrint(String ln){
        winPercent = (float)inProfits/(float) totalTrades * 100f;
//        winPercentWithCost = (float)inProfitsWithCost/(float)totalTrades * 100f;
        avgProfit = totalProfitOfProfittableTrades/(float)inProfits;
        avgLoss = totalLossOfLosableTrades/(float)inLoss;
        avgProfitPercent = totalProfitPercentOfProfittableTrades/(float)inProfitPercents;
        avgLossPercent = totalLossPercentOfLosableTrades/(float)inLossPercents;

        double tradeExpectancy = (-avgProfit/avgLoss) * (winPercent/(100-winPercent));

        orderInfo.write(ln.split(",")[0]+","+date+","+ totalTrades +","+profit
                +","+profitPercentBn+","+profitCost+","+profitPercentCost+",\n");
        
//        System.out.println(totalHoldingPeriodSum);

    }

    public void updateCostRelatedMetrics(){
        float entriesAndExitsSum = totalSell + totalBuy;
        cost = costPercent/100 * entriesAndExitsSum;
        costPercent = (cost/bnCloseAtEntry)*100;
        profitWithCost = (profit - cost);
        profitPercentWithCost = (profitPercentBn - costPercent);
    }

    private void updateMaxMinProfitsOnTradeExit(TradeMetric tradeMetric){
        maxProfit = Float.max(maxProfit,tradeMetric.profit);
        maxLoss = Float.min(maxLoss,tradeMetric.profit);
        maxProfitPercent = Float.max(maxProfitPercent,tradeMetric.profitPercentBn);
        maxLossPercent = Float.min(maxLossPercent,tradeMetric.profitPercentBn);
        
    }

    public void updateMetric(TradeMetric overAllTradeMetric,float dayMaxProfit,float dayMaxProfitPercent){
        profit += overAllTradeMetric.profit;
        profitCost += overAllTradeMetric.profitCost;
        totalHoldingPeriod += overAllTradeMetric.totalHoldingPeriod;
        cost += overAllTradeMetric.profitWithCost;
        profitPercentCost += overAllTradeMetric.profitPercentCost;
        profitPercentBn += overAllTradeMetric.profitPercentBn;
//        inProfitTradesWithCost += overAllTradeMetric.inProfitTradesCost;
//        inLossTradesCost += overAllTradeMetric.inLossTradesCost;
        

        totalSell += overAllTradeMetric.totalSell;
        totalBuy += overAllTradeMetric.totalBuy;

        this.dayMaxProfit = Float.max(this.dayMaxProfit,dayMaxProfit);
        this.dayMaxProfitPercent = Float.max(this.dayMaxProfitPercent,dayMaxProfitPercent);
        updateMaxMinProfitsOnTradeExit(overAllTradeMetric);
        if(overAllTradeMetric.profit>=0){
            inProfits++;
            totalProfitOfProfittableTrades += overAllTradeMetric.profit;
        }else{
            inLoss++;
            totalLossOfLosableTrades += overAllTradeMetric.profit;
        }
        
        if(overAllTradeMetric.profitCost>=0){
            inProfitTradesCost++;
            totalProfitOfProfittableTradesCost += overAllTradeMetric.profitCost;
        }else{
            inLossTradesCost++;
            totalLossOfLosableTradesCost += overAllTradeMetric.profitCost;
        }

        if(overAllTradeMetric.profitPercentBn >=0){
            inProfitPercents++;
            totalProfitPercentOfProfittableTrades += overAllTradeMetric.profitPercentBn;
        }else{
            inLossPercents++;
            totalLossPercentOfLosableTrades += overAllTradeMetric.profitPercentBn;
        }
        
//        if(totalHoldingPeriod > 0) totalHoldingPeriodSum += totalHoldingPeriod;
//        if(overAllTradeMetric.profitWithCost>=0)inProfitsWithCost++;
        totalTrades++;

    }

}
