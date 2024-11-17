package com.valar.entities;

import static com.valar.application.ValarTrade.allAccountAttributes;
import static com.valar.application.ValarTrade.lock;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import com.valar.accountAttributes.AccountAttributes;
import com.valar.application.ValarTrade;
import com.valar.indices.Index;
import com.valar.orderPlacer.OrderPlacer;
import com.valar.orderPlacer.SymphonyOrderPlacer;
import com.valar.orderPlacer.ZerodhaOrderPlacer;
import com.valar.states.State;
import com.valar.states.StockState;
import com.valar.utils.KeyValues;



public class Strategy {
    public ValarTrade valarTrade;
    public KeyValues kv;
    private int tradeId;
    public boolean dayExited,dayAtrConditionChecked;
    private Index index;

    public List<TradeEntity> tradeEntities = new ArrayList<>();
    public List<OrderPlacer> orderPlacers;
    public HashMap<Long, StockState> stockStates;
    public String tag_Sno;
    
	private float dayMaxProfit, dayMaxProfitPercent;
	private int unSquaredTrades;
	private State indexState;
//	private DayState indexDayState;
	private int parserAtLastTrade;
	private int lastTradedMins;
//	private Map<String, DayMetric> dayMetricsMap;
	private double lastDayAtr;
	private boolean dayATRConditionSatisfied;
	private double bandRangePercent,bandChangePercent;
	private Map<Integer,Boolean> indicatorUpdatedForPeriodMap;

    public Strategy(ValarTrade valarTrade,KeyValues kv, State indexState,HashMap<Long,StockState> stockStates){
        this.valarTrade = valarTrade;
        this.kv = kv;
        this.indexState = indexState;
        this.stockStates = stockStates;
        tag_Sno = kv.tag +"_"+ String.format("%03d", kv.sno);
        index = ValarTrade.indexMap.get(kv.indexType);
		indicatorUpdatedForPeriodMap = index.indicatorUpdatedForPeriodMap;


        orderPlacers = new ArrayList();

        for(AccountAttributes accountAttributes:allAccountAttributes){
            if(accountAttributes.broker.equalsIgnoreCase("z")) {
                orderPlacers.add(new ZerodhaOrderPlacer(accountAttributes,kv.sno,accountAttributes.id, valarTrade, accountAttributes.kiteConnect));
            }else{
                orderPlacers.add(new SymphonyOrderPlacer(accountAttributes,kv.sno,accountAttributes.id, valarTrade, accountAttributes.clientID));
            }
        }

        Timer timer = new Timer();
        int delay = (90 - LocalDateTime.now().getSecond()) * 1000;
        long intevalPeriod = 1000;
        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                valarTrade.checkQuantityEvery30Seconds(kv.indexType,kv.sno,orderPlacers,tradeEntities);
            }
        };
        timer.scheduleAtFixedRate(task2, delay, intevalPeriod);
    }



    public void checkCondition(int mins){
        synchronized (lock) {
//			System.out.println(LocalTime.now()+" "+kv.sno+" "+kv.tradeType);

			if(!indicatorUpdatedForPeriodMap.containsKey(kv.candlePeriod) || !indicatorUpdatedForPeriodMap.get(kv.candlePeriod))return;
//			else indicatorUpdatedForPeriodMap.put(kv.candlePeriod,false);

            boolean isExitTime = mins >= kv.endTime;
//			System.out.println(index.dayAtrIndicatorsMap.get(kv.atrPeriod) +"  "+ index.stockState.getClose() + "  "+lastDayAtr);
			if(!dayAtrConditionChecked) {
				double lastDayAtr = index.dayAtrIndicatorsMap.get(kv.atrPeriod) / index.stockState.getClose() * 100;
				if (lastDayAtr >= 0 && (lastDayAtr < kv.atrFrom || lastDayAtr > kv.atrTo)) {
					dayExited = true;
					return;
				}
				dayAtrConditionChecked = true;
				System.out.println(index.dayAtrIndicatorsMap.get(kv.atrPeriod) +"  "+ index.stockState.getClose() + "  "+lastDayAtr);
			}

            checkDaySLAndExitsInEnteredTrades(isExitTime, false);
            List<StockState> stockStateList = new ArrayList<>(stockStates.values());

//            System.out.println(LocalTime.now()+" "+index.symbol+" "+index.stockState.getClose()+" "
//                    +index.futureState.getSymbol()+" "+index.futureState.getClose());
//            for(StockState ss: stockStateList)
//                System.out.println(LocalTime.now()+" "+ss.getSymbol()+" "+ss.getLtp()+" "+ss.getClose());
//            System.out.println("\n");


    		if (mins >= kv.startTime) {
//    			checkForExitsInEnteredTrades();

    			// Handling Optional and null checks for the BollingerBand values
    			Optional<Map<BollingerBand.BBType, Double>> stValues = index.getBollingerBandValues(kv.candlePeriod,kv.period, kv.sd);

    			if (stValues.isPresent()) {
    			    Map<BollingerBand.BBType, Double> bbMap = stValues.get();
    			    
    			    Double upperBand = bbMap.get(BollingerBand.BBType.UB);
    			    Double lowerBand = bbMap.get(BollingerBand.BBType.LB);
    			    Double sma = bbMap.get(BollingerBand.BBType.MB);

    			    if (upperBand != null && lowerBand != null && sma != null) {
    			        // Update currentSma for every trade entity in each iteration
    			        for (TradeEntity trade : tradeEntities) {
    			            trade.updateSma(sma);  // Update current SMA for ongoing trades
    			            
    			        }
    			    }

					System.out.println(kv.tradeType+"      "+upperBand+" >> "+ lowerBand +" >> " + sma + " >> "+index.stockState.getClose() +" <<<< " + index.futureState.getClose()  + "  ::  " + LocalTime.now().minusMinutes(1));
    			    boolean entryConditionSatisfied = mins >= kv.startTime && dayAtrConditionChecked
    				            && checkBollingerBandCondition() 
    			    		    && mins - lastTradedMins >= kv.tradeGap
    				            && unSquaredTrades <= kv.maxOverLap
    				            && breakoutConditionChecker(mins, index.stockState.getClose(), upperBand, lowerBand)
    				            && (kv.positional || mins <= kv.cutOffTime);

					System.out.println(kv.tradeType+"    "+"start= "+(mins >= kv.startTime) + " ATR="+dayAtrConditionChecked + " bollinger ="+checkBollingerBandCondition()
					+ " tradegap ="+(mins - lastTradedMins >= kv.tradeGap) + " overlaps ="+(unSquaredTrades <= kv.maxOverLap)
							+ " breakout ="+(breakoutConditionChecker(mins, index.stockState.getClose(), upperBand, lowerBand)) + " pOrC = "+(kv.positional || mins <= kv.cutOffTime));
					System.out.println();
    			    		    
    			        if (entryConditionSatisfied) {
    			        	TradeEntity tradeEntity = new TradeEntity(sma,kv,tradeId,valarTrade,orderPlacers,kv.tradeType.charAt(0),tag_Sno);
//    			            if (tradeEntity.canEnter) {
    			        	tradeEntities.add(tradeEntity);
    		                synchronized (Strategy.class) {
    		                    valarTrade.tradeEntities.add(tradeEntity);
    		                }
    			                lastTradedMins = mins;
//    			            }
    			        }
//    			    }
    			} else {
    				System.err.println("BollingerBand values are not present.");
    			}
    		}
       }
    }

    boolean exitConditionSatisfiedLastTick;
    double profitAtLastTick = -Double.MAX_VALUE;
    public void checkDaySLAndExitsInEnteredTrades(boolean isExitTime,boolean isIntraMin){
//        synchronized (lock) {
//            float totalProfitPercent = 0, totalProfit = 0;
//
//            for (TradeEntity tradeEntity : tradeEntities) {
//                if (!tradeEntity.isExited) tradeEntity.updateProfitMetricsAndMaxMin(isIntraMin);
//                totalProfitPercent += tradeEntity.getTotalProfitPercent();
//                totalProfit += tradeEntity.getTotalProfit();
//                unSquaredTrades++;
//            }
////
//            boolean daySLConditionSatisfied = totalProfitPercent <= -kv.daySLBN && tradeEntities.size() != 0;
////            if (isIntraMin) {
////                if (!(exitConditionSatisfiedLastTick && totalProfit < profitAtLastTick)) {
////                    daySLConditionSatisfied = false;
////                }
////                profitAtLastTick = totalProfit;
////                exitConditionSatisfiedLastTick = totalProfitPercent <= -kv.daySL && tradeEntities.size() != 0;
////            }
////
//            if (daySLConditionSatisfied) {
//                dayExited = true;
//                for (TradeEntity tradeEntity : tradeEntities) {
//                    if (!tradeEntity.isExited) {
//                        tradeEntity.placeExitOrder("DaySL", "totalProfitPercent " + totalProfitPercent, true);
//                    }
//                }
//            }
//
            if (!dayExited) {
                for (TradeEntity tradeEntity : tradeEntities) {
                    if (!tradeEntity.isExited) tradeEntity.checkExits(isExitTime);
                }
            }
//        }
    }

//    public boolean entryConditionsFulfilled(int mins){
//    	boolean entryConditionSatisfied ;
//    	
//        if(entryConditionSatisfied) entered = true;
//
//        return entryConditionSatisfied;
//    }

    public boolean checkBollingerBandCondition() {
	    Optional<Map<BollingerBand.BBType, Double>> stValues = index.getBollingerBandValues(kv.candlePeriod,kv.period, kv.sd),
	            stValuesAtLb = index.getBollingerBandValuesAtLb(kv.candlePeriod,kv.lookBackDuration, kv.period, kv.sd);

	    boolean bbEntryCondition = false;

	    if (stValues.isPresent() && stValuesAtLb.isPresent()) {
	        Map<BollingerBand.BBType, Double> bbMap = stValues.get(), bbLbMap = stValuesAtLb.get();

	        // Null checks to prevent NullPointerException
	        Double upperBand = bbMap.get(BollingerBand.BBType.UB);
	        Double lowerBand = bbMap.get(BollingerBand.BBType.LB);
	        Double sma = bbMap.get(BollingerBand.BBType.SMA);

	        Double prevUpperBand = bbLbMap.get(BollingerBand.BBType.UB);
	        Double prevLowerBand = bbLbMap.get(BollingerBand.BBType.LB);

	        // Ensure all required values are non-null before proceeding
	        if (upperBand != null && lowerBand != null && sma != null && prevUpperBand != null && prevLowerBand != null) {
	            double bandRange = upperBand - lowerBand;
	            bandRangePercent = (bandRange / sma) * 100;

	            double prevRange = prevUpperBand - prevLowerBand;
	            double currentRange = upperBand - lowerBand;
	            bandChangePercent = ((currentRange - prevRange) / prevRange) * 100;

	            bbEntryCondition = bandRangePercent >= kv.minBandRange && bandRangePercent <= kv.maxBandRange
	                    && bandChangePercent >= kv.minBandChange && bandChangePercent <= kv.maxBandChange;
				System.out.println(bandChangePercent+"   "+bandRangePercent);
	            
	        } else {
	            // Handle missing or null values if necessary
//	            System.out.println("Null values in BollingerBand calculation: UB, LB, or SMA might be missing.");
	        }
	    }

	    return bbEntryCondition;
	}


	public boolean breakoutConditionChecker(int mins, double price, double upperBand, double lowerBand) {
		boolean tradeSignal = false;

		double upperRange = ((price - upperBand) / upperBand) * 100; // Lower bound for the buy range
		double lowerRange = ((lowerBand - price) / lowerBand) * 100; // Upper bound for the buy range
		System.out.println(upperRange+" ::  "+lowerRange);
		
//		if(indexState.ohlc.dnt.equals("29-04-22 14:25"))System.out.println("upperRange "+upperRange+" "+lowerRange);
		if(mins < kv.cutOffTime) {
			if (!kv.positional) {
				if (kv.tradeType.equalsIgnoreCase("l") && upperRange > kv.minBreakout && upperRange < kv.maxBreakout) {
					tradeSignal = true;
				} else if (kv.tradeType.equalsIgnoreCase("s") && lowerRange > kv.minBreakout && lowerRange < kv.maxBreakout) {
					tradeSignal = true;
				}
			}else {
//				System.out.println("in positional");
				if (kv.tradeType.equalsIgnoreCase("l") && upperRange > kv.minBreakout && upperRange < kv.maxBreakout) {
					tradeSignal = true;
//					System.out.println("tradeSignal "+tradeSignal + "    tradeType "+kv.tradeType+"    upperRange "+upperRange);
				} else if (kv.tradeType.equalsIgnoreCase("s") && lowerRange > kv.minBreakout && lowerRange < kv.maxBreakout) {
					tradeSignal = true;
//					System.out.println("tradeSignal "+tradeSignal + "    tradeType "+kv.tradeType+"     lowerRange "+lowerRange);
				}
			}
		}

		return tradeSignal;
	}



	private List<StockState> getOSWithStrike(int strike){
        List<StockState> osList = new ArrayList<>(2);
        for(StockState ss : stockStates.values()){
            if(ss.getStrike()==strike)
                osList.add(ss);
        }

        return osList;
    }

}
