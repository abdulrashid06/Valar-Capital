package capital.valar.supertrend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import capital.valar.supertrend.entities.BollingerBand;
import capital.valar.supertrend.entities.TradeEntity;
import capital.valar.supertrend.state.day.DayState;
import capital.valar.supertrend.state.minute.State;
import capital.valar.supertrend.tradeAndDayMetrics.DayMetric;
import capital.valar.supertrend.utils.KeyValues;

public class StrategyImpl {
	private int tradeId;
	private float dayMaxProfit, dayMaxProfitPercent;
	public KeyValues kv;
	public boolean dayExited;
	private int unSquaredTrades;
	private List<TradeEntity> tradeEntities = new ArrayList<>();
	private State indexState;
	private DayState indexDayState;
	private int parserAtLastTrade;
	private Map<String, DayMetric> dayMetricsMap;
	private double lastDayAtr;
	private boolean dayATRConditionSatisfied;
	private double bandRangePercent,bandChangePercent;

	public StrategyImpl(KeyValues kv, DayState indexDayState, State indexState, Map<String, DayMetric> dayMetricsMap) {
		this.kv = kv;
		this.indexState = indexState;
		this.indexDayState = indexDayState;
		this.dayMetricsMap = dayMetricsMap;
	}

	public void iterate(int mins, String dNT) {
		if (mins == kv.startTime) {
			if(!kv.positional && !indexDayState.atrMap.containsKey(indexState.ohlc.date)) {
				dayExited = true;
				return;
			}
            lastDayAtr = indexDayState.getLastDayAtr(indexState.ohlc.date);
            dayATRConditionSatisfied = lastDayAtr >= kv.atrFrom && lastDayAtr <= kv.atrTo;
            if (!dayATRConditionSatisfied && !kv.positional) {
                dayExited = true;
                return;
            }
//            System.out.println(indexState.ohlc.date +"  "+ lastDayAtr);
		}

		if (mins >= kv.startTime) {
			checkForExitsInEnteredTrades();

			// Handling Optional and null checks for the BollingerBand values
			Optional<Map<BollingerBand.BBType, Double>> stValues = indexState.getBollingerBandValues(kv.period, kv.sd);

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
//			    System.out.println(indexState.ohlc.dnt +"   "+tradeEntities.size());
			    
			    boolean entryConditionSatisfied = mins >= kv.startTime && dayATRConditionSatisfied
				            && checkBollingerBandCondition() 
			    		    && indexState.parser - parserAtLastTrade >= kv.tradeGap
				            && unSquaredTrades <= kv.maxOverLap
				            && toleranceChecker(mins, indexState.ohlc.close, upperBand, lowerBand)
				            && (kv.positional || mins <= kv.cutOffTime);

			        if (entryConditionSatisfied) {
			            // Only set the sma at entry
			            TradeEntity tradeEntity = new TradeEntity(tradeId, kv, indexState,sma);
			            tradeEntity.setAdditionalMetrics(lastDayAtr, upperBand, lowerBand, sma, bandRangePercent, bandChangePercent);
			            if (tradeEntity.canEnter) {
			                tradeEntities.add(tradeEntity);
			                tradeId++;
			                parserAtLastTrade = indexState.parser;
			            }
			        }
//			    }
			} else {
				System.err.println("BollingerBand values are not present.");
			}
		}
	}

	public boolean checkBollingerBandCondition() {
	    Optional<Map<BollingerBand.BBType, Double>> stValues = indexState.getBollingerBandValues(kv.period, kv.sd),
	            stValuesAtLb = indexState.getBollingerBandValuesAtLb(kv.lookBackDuration, kv.period, kv.sd);

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
	            
	            // Debugging info (optional)
//	            if (kv.sno == 4 && indexState.ohlc.date.equals("29-03-23")) {
//	                System.out.println(kv.sno + " " + indexState.ohlc.dnt + " lower: " + lowerBand
//	                        + " upper: " + upperBand + " sma: " + sma
//	                        + " bandRangePercent: " + bandRangePercent
//	                        + " bandChangePercent: " + bandChangePercent);
//	            }
	        } else {
	            // Handle missing or null values if necessary
	            System.out.println("Null values in BollingerBand calculation: UB, LB, or SMA might be missing.");
	        }
	    }

	    return bbEntryCondition;
	}


	public boolean toleranceChecker(int mins, float price, double upperBand, double lowerBand) {
		boolean tradeSignal = false;

//		double tolerancePointsLower = lowerBand * (kv.tolerance / 100);
//		double tolerancePointsUpper = upperBand * (kv.tolerance / 100);

		double upperRange = ((price - upperBand) / upperBand) * 100; // Lower bound for the buy range
		double lowerRange = ((lowerBand - price) / lowerBand) * 100; // Upper bound for the buy range
//
//		double upperRange1 = upperBand - tolerancePointsUpper; // Lower bound for the sell range
//		double upperRange2 = upperBand + tolerancePointsUpper; // Upper bound for the sell range

		if (mins < kv.cutOffTime && !kv.positional) {
			if (kv.tradeType.equalsIgnoreCase("l") && upperRange > kv.minBreakout && upperRange < kv.maxBreakout) {
				tradeSignal = true;
			} else if (kv.tradeType.equalsIgnoreCase("s") && lowerRange > kv.minBreakout && lowerRange < kv.maxBreakout) {
				tradeSignal = true;
			}
		}else {
			if (kv.tradeType.equalsIgnoreCase("l") && upperRange > kv.minBreakout && upperRange < kv.maxBreakout) {
				tradeSignal = true;
			} else if (kv.tradeType.equalsIgnoreCase("s") && lowerRange > kv.minBreakout && lowerRange < kv.maxBreakout) {
				tradeSignal = true;
			}
		}

		return tradeSignal;
	}

	public void checkForExitsInEnteredTrades() {
		float totalProfitPercent = 0, totalProfit = 0;
		unSquaredTrades = 0;
		for (TradeEntity tradeEntity : tradeEntities) {
			if (!tradeEntity.tradeSquared && tradeEntity.checkExitAndIsToBeExited()) {
				onTradeExit(indexState.ohlc.date, tradeEntity);
			}
			if (!tradeEntity.tradeSquared)
				unSquaredTrades++;

			totalProfitPercent += tradeEntity.getTotalProfitPercent();
			totalProfit += tradeEntity.getTotalProfit();
		}

		dayMaxProfit = Float.max(dayMaxProfit, totalProfit);
		dayMaxProfitPercent = Float.max(dayMaxProfitPercent, totalProfitPercent);

		if (totalProfitPercent <= -kv.daySLBN && !kv.positional) {
			for (TradeEntity tradeEntity : tradeEntities) {
				if (!tradeEntity.tradeSquared) {
					tradeEntity.exitByDayExit("DaySL",
							"DayProfit " + totalProfit + " ProfitPercent " + totalProfitPercent);
					onTradeExit(indexState.ohlc.date, tradeEntity);
				}
				dayExited = true; 
			}
		}
	}

	
	
	
	public void onTradeExit(String date, TradeEntity tradeEntity) {
		DayMetric dayMetric;
		if (dayMetricsMap.containsKey(date))
			dayMetric = dayMetricsMap.get(date);
		else {
			dayMetric = new DayMetric(date, kv.costPercent, tradeEntity.indexCloseAtEntry);
			dayMetricsMap.put(date, dayMetric);
		}

		dayMetric.updateMetric(tradeEntity.overAllTradeMetric, dayMaxProfit, dayMaxProfitPercent);
	}


}
