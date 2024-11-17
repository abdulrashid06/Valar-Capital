package capital.valar.supertrend.tradeAndDayMetrics;


import capital.valar.supertrend.utils.PrintWriters;

import java.util.List;

public class OverAllMetric extends TradeMetric {
    public float dayMaxProfitTotal,dayMaxProfitPercentTotal,dayMaxProfit,dayMaxLoss;
    public float tradeMaxProfit,tradeMaxLoss=Float.MAX_VALUE;
    public float maxProfitPercent,maxLossPercent=Float.MAX_VALUE;
    public float maxProfit,maxLoss=Float.MAX_VALUE;
    public int tradingDays;
    public float totalHoldingPeriod,calmar;
    
    
    public ProfitLossMetric tradeProfit = new ProfitLossMetric(),
            tradeProfitPercent = new ProfitLossMetric(),
            tradeProfitWithCost = new ProfitLossMetric(),
            tradeProfitPercentWithCost = new ProfitLossMetric();

    public OverAllMetric(){
        super(PrintWriters.overAllPrintWriter);
    }
    

    public void calculateOverAllMetricsAndPrint(String ln,float costPercent){
    	float holdingPeriodAvg=totalHoldingPeriod/totalTrades;

    	orderInfo.write(ln+","+tradingDays + "," + totalTrades + "," + tradeMaxProfit + "," + tradeMaxLoss
                + "," + dayMaxProfit + "," + dayMaxLoss + "," + tradeProfit.getAvgProfit() + "," + tradeProfit.getAvgLoss()
                + "," + tradeProfit.getWinPercent() +","+ tradeProfitPercentWithCost.getWinPercent()+ "," + tradeProfit.getExpectancy()
                + "," + tradeProfitWithCost.getExpectancy() + "," + profit.profit + "," + profitPercent.profit + "," + profit.getAvgProfit()
                + "," + profit.getAvgLoss() + "," + profit.getWinPercent() + "," + profit.getExpectancy() + "," + profitWithCost.profit + "," + profitPercentWithCost.profit
                + "," + profitWithCost.getWinPercent() + "," + profitWithCost.getAvgProfit() + "," + profitWithCost.getAvgLoss()
                + "," + profitWithCost.getExpectancy() +","+holdingPeriodAvg+ ","+calmar+"\n");
//        
//        orderInfo.write(ln+","+tradingDays+","+totalTrades+","+ maxProfit
//        		+","+maxLoss+","+ maxProfitPercent
//        		+","+maxLossPercent+","+dayMaxProfitTotal+","+dayMaxProfitPercentTotal+","+winPercent+","+profit
//        		+","+ profitPercentBn +","+avgProfit+","+avgLoss+","+avgProfitPercent+","+avgLossPercent+","+tradeWinPercent
//        		+","+tradesAvgProfit+","+tradesAvgLoss+","+dayExpectancy+","+tradeExpectancy+","+winPercentWithCost+","+profitWithCost+","+profitPercentWithCost+"\n");

    }

    public void update(List<TradeMetric> daysMetrics){
        for(TradeMetric dayMetric: daysMetrics)
            updateMetric((DayMetric) dayMetric);
    }

    public void updateMetric(DayMetric dayMetric){
        try {
            tradeProfit.copy(dayMetric.profit);
            tradeProfitWithCost.copy(dayMetric.profitWithCost);
            tradeProfitPercent.copy(dayMetric.profitPercent);
            tradeProfitPercentWithCost.copy(dayMetric.profitPercentWithCost);

            profit.add(dayMetric.profit.profit);
            profitWithCost.add(dayMetric.profitWithCost.profit);
            profitPercent.add(dayMetric.profitPercent.profit);
            profitPercentWithCost.add(dayMetric.profitPercentWithCost.profit);

            totalTrades += dayMetric.totalTrades;
            totalHoldingPeriod+=dayMetric.totalHoldingPeriod;
            dayMaxProfitTotal += dayMetric.dayMaxProfit;
            dayMaxProfitPercentTotal += dayMetric.dayMaxProfitPercent;
            maxProfit = Float.max(maxProfit,dayMetric.maxProfit);
            maxLoss = Float.min(maxLoss,dayMetric.maxLoss);
            maxProfitPercent = Float.max(maxProfitPercent,dayMetric.maxProfitPercent);
            maxLossPercent = Float.min(maxLossPercent,dayMetric.maxLossPercent);
            dayMaxProfit=Float.max(dayMaxProfit,dayMetric.profit.profit);
            dayMaxLoss=Float.min(dayMaxLoss,dayMetric.profit.profit);
            tradeMaxProfit = Float.max(tradeMaxProfit,dayMetric.maxProfit);
            tradeMaxLoss = Float.min(tradeMaxLoss,dayMetric.maxLoss);
//        if(dayMetric.profitWithCost>=0)inProfitsWithCost++;
            tradingDays++;
        }catch (Exception e){e.printStackTrace();}
    }

}
