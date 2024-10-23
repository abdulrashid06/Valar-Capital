package capital.valar.supertrend.utils;

import capital.valar.supertrend.entities.SuperTrendEntity;
import capital.valar.supertrend.state.day.DayState;
import capital.valar.supertrend.state.minute.State;

public class PrintAttribs {
    private double dayAtrPercent;
    private double upperBand, lowerBand, sma;
    private double bandChangePercent, bandRangePercent, currentPrice;
    public float profitCost, profitCostPercent;


    public void setVariablesAtEntry(double... inputs){
        this.dayAtrPercent = inputs[0];
        this.upperBand = inputs[1];
        this.lowerBand = inputs[2];
        this.sma = inputs[3];
        this.bandRangePercent = inputs[4];
        this.bandChangePercent = inputs[5];
    }
    
    public void setProfitVariables(float profitCost,float profitCostPercent) {
        this.profitCost = profitCost;
        this.profitCostPercent = profitCostPercent;
    }



    public String toString(){
        return profitCost+","+profitCostPercent+","+dayAtrPercent+","+upperBand +","+ lowerBand +","+sma+","+bandRangePercent+","+bandChangePercent;
    }
}
