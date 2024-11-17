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
	    public ProfitLossMetric profit = new ProfitLossMetric(),
	    profitPercent = new ProfitLossMetric(),
	    profitWithCost = new ProfitLossMetric(),
	    profitPercentWithCost = new ProfitLossMetric();

	    protected float totalSell,totalBuy;
	    PrintWriter orderInfo;
	    public float totalHoldingPeriod;


	    public char lOrS;
	    public Ohlc entryOhlc,exitOhlc;
	    public TradeMetric(){}

	    public TradeMetric(PrintWriter orderInfo){
	        this.orderInfo = orderInfo;
	    }

	    public TradeMetric(Ohlc entryOhlc,char lOrS){
	        this.entryOhlc = entryOhlc;
	        this.lOrS = lOrS;
	        this.orderInfo = PrintWriters.orderInfoPrintWriter;
	    }

    public void calculateOverAllMetricsAndPrint(float profitPercent,float profitWithCost,float profitPercentWithCost,String reason, String reasonInfo, int id,int holdingPeriod, KeyValues kv, Ohlc exitOhlc,float indexClose, String option, PrintAttribs printAttribs){
        this.exitOhlc = exitOhlc;
        if(lOrS =='l') this.profit.add(exitOhlc.close - entryOhlc.close);
        else this.profit.add(entryOhlc.close - exitOhlc.close);
        this.totalHoldingPeriod+=holdingPeriod;

        this.profitPercent.add(profitPercent);
        this.profitWithCost.add(profitWithCost);
        this.profitPercentWithCost.add(profitPercentWithCost);

        totalTrades = 1;

        orderInfo.write(kv.sno+","+entryOhlc.date+","+kv.tradeType+","+id+","+holdingPeriod+"," +entryOhlc.date+"," +entryOhlc.time+","+entryOhlc.close
                +","+exitOhlc.date+","+exitOhlc.time+","+exitOhlc.close+","+reason/*+","+reasonInfo*/+","+profit.profit+","+ profitPercent +","+this.profitWithCost.profit+","+profitPercentWithCost+","+printAttribs+"\n");
    }

    // used for calculation of the day metric ( profit, loss, etc...)
    public void updateOverAll(TradeMetric tradeMetric){
        profit.copy(tradeMetric.profit);
        profitWithCost.copy(tradeMetric.profitWithCost);
        profitPercent.copy(tradeMetric.profitPercent);
        profitPercentWithCost.copy(tradeMetric.profitPercentWithCost);

        totalHoldingPeriod+=tradeMetric.totalHoldingPeriod;

        if(tradeMetric.lOrS =='l') {
            totalBuy += tradeMetric.entryOhlc.close;
            totalSell += tradeMetric.exitOhlc.close;
        }else{
            totalBuy += tradeMetric.exitOhlc.close;
            totalSell += tradeMetric.entryOhlc.close;
        }

        totalTrades = 1;
    }

    public void closePrinter(){
        orderInfo.close();
    }

}
