package capital.valar.supertrend.utils;

import capital.valar.supertrend.application.ValarTrade;

import java.io.PrintWriter;

public class PrintWriters {
    public static PrintWriter orderInfoPrintWriter,dayWisePrintWriter,overAllPrintWriter;

    public static void loadAllWriters()throws Exception{
        orderInfoPrintWriter = new PrintWriter("./Outputs/OrderBook.csv");
        orderInfoPrintWriter.write("S.no,Date,TradeType,TradeId,EntryTime,EntryPrice,ExitTime,ExitPrice,Reason,Profit,Profit%,ProfitCost,ProfitCost%,DayATR,UpperBand,LowerBand,SMA,BandRangePercent,BandChangePercent,\n");

        dayWisePrintWriter = new PrintWriter("./Outputs/DayWise.csv");
        dayWisePrintWriter.write("S.no,Date,TotalTrades,Profit,Profit%,Cost,Cost%,\n");

        overAllPrintWriter = new PrintWriter("./Outputs/OverAllDetails.csv");
        overAllPrintWriter.write(ValarTrade.keystoreHeading+",TradingDays,TotalTrades,TradeMaxProfit,TradeMaxLoss" +
        	    ",DayMaxProfit,DayMaxLoss,TradeAverageProfit,TradeAverageLoss,TradeWinPercent,TradeExpectancy,Profit" +
        	    ",DayAverageProfit,DayAverageLoss,DayWinPercent,DayExpectancy,Profit(Cost),DayWinPercent(Cost)" +
        	    ",DayAverageProfit(Cost),DayAverageLoss(Cost),DayExpectancy(Cost),\n");
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
