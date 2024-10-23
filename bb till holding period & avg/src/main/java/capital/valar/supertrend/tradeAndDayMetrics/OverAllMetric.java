package capital.valar.supertrend.tradeAndDayMetrics;


import capital.valar.supertrend.utils.PrintWriters;

import java.util.List;

public class OverAllMetric extends TradeMetric {
    public float dayMaxProfit,dayMaxLoss;
    public float maxProfitPercent,maxLossPercent=Float.MAX_VALUE;
    public float tradeMaxProfit,tradeMaxLoss=Float.MAX_VALUE;
    public int tradingDays,tradesInProfit,tradesInLoss;
    public float totalProfitOfProfittableDays,totalProfitPercentOfProfittableDays,totalLossOfLosableDays,totalLossPercentOfLosableDays;
    public float totalProfitOfProfittableTrades,totalLossOfLosableTrades;
    public float inProfitsWithCost, inLossWithCost, inProfitWithCostSum, inlossWithCostSum;
    public float profitableTradesCountWithCost, lossTradesCountWithCost, profitableTradesSumWithCost, lossTradesWithSum;
    public float totalHoldingPeriod;

    public OverAllMetric(){
        super(PrintWriters.overAllPrintWriter);
    }

    public void calculateOverAllMetricsAndPrint(String ln,float costPercent){
        winPercent = ((float)inProfits/(float)tradingDays) * 100f;
//        winPercentWithCost = ((float)inProfitsWithCost/(float)tradingDays) * 100f;
        avgProfit = totalProfitOfProfittableDays/(float)inProfits;
        avgLoss = totalLossOfLosableDays/(float)inLoss;
        avgProfitPercent = totalProfitPercentOfProfittableDays/(float)inProfitPercents;
        avgLossPercent = totalLossPercentOfLosableDays/(float)inLossPercents;

        float avgHoldingPeriod = totalHoldingPeriod /(float)totalTrades;

        float tradesAvgProfit = totalProfitOfProfittableTrades/(float)tradesInProfit,
        tradesAvgProfitWithCost = profitableTradesSumWithCost/profitableTradesCountWithCost,
        tradesAvgLoss = totalLossOfLosableTrades/(float)tradesInLoss,
        tradesAvgLossWithCost = lossTradesWithSum/lossTradesCountWithCost,
        tradeWinPercentCost = (profitableTradesCountWithCost/totalTrades) * 100,
        tradeWinPercent = ((float)tradesInProfit/(float)totalTrades) * 100,
        winPercentWithCost = inProfitsWithCost/tradingDays * 100,
        dayAvgProfitWithCost = inProfitWithCostSum/inProfitsWithCost,
        dayAvgLossWithCost = inlossWithCostSum/inLossWithCost,
        dayExpectancyWithCost = (-dayAvgProfitWithCost/dayAvgLossWithCost) * (winPercentWithCost/(100-winPercentWithCost));

        double dayExpectancy = (-avgProfit/avgLoss) * (winPercent/(100-winPercent)),
                tradeExpectancy = (-tradesAvgProfit/tradesAvgLoss) * (tradeWinPercent/(100-tradeWinPercent)),
        tradeExpectancyWithCost = (-tradesAvgProfitWithCost/tradesAvgLossWithCost) * (tradeWinPercentCost/(100-tradeWinPercentCost));
        

        orderInfo.write(ln+","+tradingDays + "," + totalTrades + "," + tradeMaxProfit + "," + tradeMaxLoss 
        	    + "," + dayMaxProfit + "," + dayMaxLoss + "," + tradesAvgProfit + "," + tradesAvgLoss 
        	    + "," + tradeWinPercent + "," + tradeExpectancy+ "," + tradeExpectancyWithCost + "," + profit + "," + avgProfit 
        	    + "," + avgLoss + "," + winPercent + "," + dayExpectancy + "," + profitWithCost 
        	    + "," + winPercentWithCost + "," + dayAvgProfitWithCost + "," + dayAvgLossWithCost 
        	    + "," + dayExpectancyWithCost+ "," + avgHoldingPeriod + "\n");
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
            profit += dayMetric.profit;
            profitPercentBn += dayMetric.profitPercentBn;
            if (dayMetric.profit >= 0) {
                inProfits++;
                totalProfitOfProfittableDays += dayMetric.profit;
            } else {
                inLoss++;
                totalLossOfLosableDays += dayMetric.profit;
            }

            if(dayMetric.profitPercentBn >= 0){
                inProfitPercents++;
                totalProfitPercentOfProfittableDays += dayMetric.profitPercentBn;
            }else{
                inLossPercents++;
                totalLossPercentOfLosableDays += dayMetric.profitPercentBn;
            }

            totalTrades += dayMetric.totalTrades;
            tradesInProfit += dayMetric.inProfits;
            tradesInLoss += dayMetric.inLoss;
            profitableTradesCountWithCost += dayMetric.inProfitTradesCost;
            lossTradesCountWithCost += dayMetric.inLossTradesCost;

            totalProfitOfProfittableTrades += dayMetric.totalProfitOfProfittableTrades;
            totalLossOfLosableTrades += dayMetric.totalLossOfLosableTrades;
            profitableTradesSumWithCost += dayMetric.totalProfitOfProfittableTradesCost;
            lossTradesWithSum += dayMetric.totalLossOfLosableTradesCost;
            
            totalHoldingPeriod += dayMetric.totalHoldingPeriod;

            profitWithCost+= dayMetric.profitWithCost;
            profitPercentWithCost+= dayMetric.profitPercentWithCost;


            if(dayMetric.profitWithCost>=0) {
            	inProfitWithCostSum += dayMetric.profitWithCost;
            	inProfitsWithCost += 1;
            }else if(dayMetric.profitWithCost<=0) {
            	inlossWithCostSum += dayMetric.profitWithCost;
                inLossWithCost += 1;
            }
            dayMaxProfit = Float.max(dayMaxProfit, dayMetric.profit);
            dayMaxLoss = Float.min(dayMaxLoss, dayMetric.profit);
            tradeMaxProfit = Float.max(tradeMaxProfit,dayMetric.maxProfit);
            tradeMaxLoss = Float.min(tradeMaxLoss,dayMetric.maxLoss);
            maxProfitPercent = Float.max(maxProfitPercent,dayMetric.maxProfitPercent);
            maxLossPercent = Float.min(maxLossPercent,dayMetric.maxLossPercent);
//        if(dayMetric.profitWithCost>=0)inProfitsWithCost++;
            tradingDays++;
        }catch (Exception e){e.printStackTrace();}
    }

}
