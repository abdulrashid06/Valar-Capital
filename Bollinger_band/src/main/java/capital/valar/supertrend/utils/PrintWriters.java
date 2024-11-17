package capital.valar.supertrend.utils;

import capital.valar.supertrend.application.ValarTrade;

import java.io.PrintWriter;

public class PrintWriters {
    public static PrintWriter orderInfoPrintWriter,dayWisePrintWriter,overAllPrintWriter;

    public static void loadAllWriters()throws Exception{
        orderInfoPrintWriter = new PrintWriter("./Outputs/OrderBook.csv");
        orderInfoPrintWriter.write("S.no,Date,TradeType,TradeId,HoldingPeriod,EntryDate,EntryTime,EntryPrice,ExitDate,ExitTime,ExitPrice,Reason,Profit,Profit%,ProfitCost,ProfitCost%,DayATR,UpperBand,LowerBand,SMA,BandRangePercent,BandChangePercent,\n");

        dayWisePrintWriter = new PrintWriter("./Outputs/DayWise.csv");
        dayWisePrintWriter.write("S.no,Date,TotalTrades,Profit,Profit%,ProfitCost,ProfitCost%,\n");

        overAllPrintWriter = new PrintWriter("./Outputs/OverAllDetails1.csv");
        overAllPrintWriter.write(ValarTrade.keystoreHeading+",TradingDays,TotalTrades,TradeMaxProfit,TradeMaxLoss" +
                ",DayMaxProfit,DayMaxLoss,TradeAverageProfit,TradeAverageLoss,TradeWinPercent,TradeWinPercent(Cost),TradeExpectancy,TradeExpectancy(Cost),Profit,Profit %" +
                ",DayAverageProfit,DayAverageLoss,DayWinPercent,DayExpectancy,ProfitCost,ProfitCost %,DayWinPercent(Cost)" +
                ",DayAverageProfit(Cost),DayAverageLoss(Cost),DayExpectancy(Cost),HoldingPeriodAvg,Calmar\n");
//        overAllPrintWriter.write(ValarTrade.keystoreHeading+",tradingDays,totalTrades,DayMaxProfit,DayMaxLoss,DayMaxProfit%" +
//        		",DayMaxLoss%,DayTotalMaxProfit,DayTotalMaxProfit%,DayWinPercent,DayProfit" +
//        		",DayProfit%,DayAvgProfit,DayAvgLoss,DayAvgProfit%,DayAvgLoss%" +
//        		",TradesWin%,TradesAvgProfit,TradesAvgLoss,dayExpectancy,TradeExpectancy,DayWinPercent(cost),DayProfit(cost),DayProfit%(cost),\n");

    }

    public static void closeAllWriters(){
        orderInfoPrintWriter.close();
        dayWisePrintWriter.close();
        overAllPrintWriter.close();
    }
}
