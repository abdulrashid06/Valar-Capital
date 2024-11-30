package capital.valar.supertrend.entities;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import capital.valar.supertrend.state.minute.State;
import capital.valar.supertrend.tradeAndDayMetrics.TradeMetric;
import capital.valar.supertrend.utils.KeyValues;
import capital.valar.supertrend.utils.PrintAttribs;

public class TradeEntity {
    public float indexCloseAtEntry;
    public PrintAttribs printAttribs = new PrintAttribs();
    private KeyValues kv;
    public boolean canEnter;
    private int tradeId;
    public boolean tradeSquared;
    private State indexState;
    private Ohlc indexohlc;
    private List<TradeAttrib> tradeAttribs = new ArrayList(2);
    private ProfitMetric profit = new ProfitMetric(),
    profitPercent = new ProfitMetric(),profitPercentBN = new ProfitMetric(),
    profitCost = new ProfitMetric(), profitCostPercent = new ProfitMetric();
    
    private double upperBand, lowerBand, sma, currentSma;
    private double[] bandAttribs;
    public int holdingPeriod, entryParser;

    public TradeMetric overAllTradeMetric = new TradeMetric();
    private List<TradeMetric> tradeMetrics = new ArrayList<>();
    
    public boolean isTriggered = false;
    private float maxProfitPercent = 0.0f;
    
    private String entryTime = "23";

    class ProfitMetric{
        public float currentProfit;
        public float profitBooked;
        public float getTotalProfit(){
            return currentProfit + profitBooked;
        }

        public void resetCurrentProfit(){
            currentProfit = 0;
        }
    }
    
//    private void calculateHoldingPeriod() {
//        if (entryTime != null && indexohlc.date != null) {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy"); // Define your date format
//            LocalDate entryLocalDate = LocalDate.parse(entryTime, formatter);
//            LocalDate indexOhlcDate = LocalDate.parse(indexohlc.date, formatter);
//            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(entryLocalDate, indexOhlcDate);
//            holdingPeriod = (int) daysBetween;
//        }
//    }

    public class TradeAttrib {
        State os;
        public char lOrS;
        float entryPrice;
        public Ohlc ohlcAtEntry,ohlc;
        public  TradeMetric tradeMetric;
        public TradeAttrib(State os,char lOrS){
            this.lOrS = lOrS;
            set(os);
        }

        public void lookForShift(){
            boolean shiftCondition = false;
            if(shiftCondition){
                shiftTrade(this,"","");
                set(null);;
            }
        }

        private void set(State os){
            this.os = os;
            ohlc = os.ohlc;
            entryPrice = ohlc.close;
            ohlcAtEntry = new Ohlc(ohlc.ln);
            tradeMetric = new TradeMetric(new Ohlc(os.ohlc),lOrS);
            tradeMetrics.add(tradeMetric);
            TradeEntity.this.entryTime = tradeMetric.entryOhlc.date;
        }



        public float getProfit(){
            if(lOrS =='l')return (os.ohlc.close - entryPrice);
            else return (entryPrice - os.ohlc.close);
        }

        public float getProfitPercent(){
            if(lOrS =='l')return (os.ohlc.close - entryPrice)/entryPrice * 100;
            else return (entryPrice - os.ohlc.close)/entryPrice * 100;
        }

        public float getProfitPercentBN(){
            if(lOrS =='l')return (os.ohlc.close - entryPrice)/indexohlc.close * 100;
            else return (entryPrice - os.ohlc.close)/indexohlc.close * 100;
        }
        
        public float getProfitCost() {
        	float entryCostDeduct = (entryPrice / 100) * kv.costPercent;
        	float exitCostDeduct = (os.ohlc.close / 100) * kv.costPercent;
        	
        	if(lOrS =='l') { 
        		return ((os.ohlc.close - exitCostDeduct) - (entryPrice + entryCostDeduct));
        	}else {
        		return ((entryPrice - entryCostDeduct) - (os.ohlc.close + exitCostDeduct));
        	}
        }
        
        public float getProfitCostPercent() {
            float profitCost = getProfitCost(); 
            return (profitCost / entryPrice) * 100; 
        }


        public String[] getInfo(){
            return new String[]{ohlcAtEntry.dnt + "," + ohlcAtEntry.close + "," + os.name};
        }
    }

    public TradeEntity(int tradeId,KeyValues kv,State indexState, double currSma ){
        this.tradeId = tradeId;
        this.kv = kv;
        this.indexState = indexState;
        this.currentSma = currSma;
        entryParser = indexState.parser;
        indexohlc = indexState.ohlc;
    }
    
    public void updateSma(double currentSma) {
        this.currentSma = currentSma; // Update the SMA in every iteration
    }


    
    public TradeEntity setAdditionalMetrics(double... bandAttribs) {
        this.upperBand = bandAttribs[1];
        this.lowerBand = bandAttribs[2];
        this.sma = bandAttribs[3];
        this.bandAttribs = bandAttribs;
        loadAttribs();
        return this;
        
    }

    // loading all the attributes to take new trade
    private TradeEntity loadAttribs(){
    	tradeAttribs.add(new TradeAttrib(indexState,kv.tradeType.charAt(0)));
        indexCloseAtEntry = indexohlc.close;
        printAttribs.setVariablesAtEntry(bandAttribs);
        canEnter = true;
        return this;
    }

    
    public float getBNInTermsOfDistance(float distancePercent,char cOrP){
        float indexClose = indexohlc.close;
        if(cOrP=='c') indexClose = indexClose + (distancePercent / 100 * indexClose);
        else indexClose = indexClose - (distancePercent/100 * indexClose);

        return indexClose;
    }

    // updating the profits in all the form
    public void updateProfit(){
        profit.resetCurrentProfit();profitPercent.resetCurrentProfit();profitPercentBN.resetCurrentProfit();profitCost.resetCurrentProfit();profitCostPercent.resetCurrentProfit();
        for(TradeAttrib tradeAttrib : tradeAttribs){
            profit.currentProfit += tradeAttrib.getProfit();
            profitPercent.currentProfit += tradeAttrib.getProfitPercent();
            profitPercentBN.currentProfit += tradeAttrib.getProfitPercentBN();
            profitCost.currentProfit += tradeAttrib.getProfitCost();
            profitCostPercent.currentProfit += tradeAttrib.getProfitCostPercent();
        }
        
    }

    public float getTotalProfitPercent(){
        return profitPercent.getTotalProfit();
    }

    public float getTotalProfit(){
        return profit.getTotalProfit();
    }
    
    public void exitByDayExit(String reason,String reasonInfo) {
        exitTrade(reason,reasonInfo);
    }

    // checking all the conditions for trade exit
    public boolean checkExitAndIsToBeExited(){
        updateProfit();
        
        
        maxProfitPercent = Float.max(maxProfitPercent, profitPercent.getTotalProfit());
        
        if(!isTriggered) {
        	isTriggered = profitPercent.getTotalProfit() >= kv.triggerBN;
//        	System.out.println(indexState.ohlc.dnt+ "   "+ profitPercent.getTotalProfit());
        }

        if(profitPercent.getTotalProfit() <= -kv.tradeSLBN) {
            exitTrade( "Trade SL", "trade SL ");
    	}
        else if(profitPercent.getTotalProfit() >= kv.targetBN) {
            exitTrade( "Trade Target", " trade target is achieved");
        }
        else if(isTriggered && (maxProfitPercent - profitPercent.getTotalProfit()) >= kv.fallBackBN) {
            exitTrade( "Trigger Fallback", "max : " + maxProfitPercent );
        }
        else if(checkSmaExit() && kv.SmaExit) {
        	exitTrade("SMA Exit", "SMA Exit");
        }else if ((!kv.positional&& indexohlc.mins >= kv.endTime) || indexState.finished) {
            exitTrade("EndTime", "Trade exited at the day end");
        }
            /* else if (checkDaySL()) {
                exitTrade("Day SL", "Day SL");
            } */
      
        
        /*else if(false){
            Optional<OptionState> shiftOption = Optional.empty();
            if(shiftOption.isPresent()) {
                OptionState shiftToOption = shiftOption.get();
                boolean canSwitch = !shiftToOption.equals(tradedOption);
                if(canSwitch) {
                    exitTrade(ca,true, "Shift", "ShiftProfit% " + shiftProfitPercent +" ShiftProfit%BN " + profitPercentBN+" Price% "+pricePercent, niftyClose);
                    tradedOption = shiftToOption;
                    ohlcAtEntry = new Ohlc(tradedOption.ohlc.ln);
                    indexCloseAtEntry = niftyClose;
                }
            }
        }*/

        return tradeSquared;
    }
    
    
  public boolean checkSmaExit() {
	boolean flag = false;
	if(kv.tradeType.equalsIgnoreCase("l")) {
		if(indexohlc.close < currentSma) {
			flag= true;
		}
	}else if(kv.tradeType.equalsIgnoreCase("s")) {
		if(indexohlc.close > currentSma) {
//			System.out.println(kv.sno+"   " + indexohlc.dnt + "    "+ indexohlc.close + "    " + currentSma+"   "+sma);
			flag= true;
		}
	}
	return flag;
}
    

  // this overallTradeMetric is beign used for the calculation of the day metric, and is updated on exit of trade
    private void updateOverAll(){
        for(TradeMetric tradeMetric:tradeMetrics)
            if(tradeMetric.exitOhlc!=null)
                overAllTradeMetric.updateOverAll(tradeMetric);
    }

    // used to shift the trade in options and the amount of profit is booked
    private void shiftTrade(TradeAttrib ta,String reason,String reasonInfo){
        ta.tradeMetric.calculateOverAllMetricsAndPrint(profitPercentBN.currentProfit,profitCost.currentProfit, reason, reasonInfo, tradeId,holdingPeriod, kv, new Ohlc(ta.os.ohlc), indexohlc.close, ta.os.name, printAttribs);
        profit.profitBooked += ta.getProfit();
        profitPercent.profitBooked += ta.getProfitPercent();
        profitPercentBN.profitBooked += ta.getProfitPercentBN();
    }

    
    // used to exit trade and to print the tradeInfo in orderbook
    private void exitTrade(String reason,String reasonInfo){
    	holdingPeriod = indexState.parser - entryParser;
    	printAttribs.setProfitVariables(profitCost.currentProfit, profitCostPercent.currentProfit);
        for(TradeAttrib ta : tradeAttribs){
//        	System.out.println(ta);
            ta.tradeMetric.calculateOverAllMetricsAndPrint(profitPercent.currentProfit, profitCost.currentProfit, reason, reasonInfo, tradeId,holdingPeriod, kv, new Ohlc(ta.os.ohlc), indexohlc.close, ta.os.name, printAttribs);
        }
        updateOverAll();
        tradeSquared = true;
    }
}
