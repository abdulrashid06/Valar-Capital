package capital.valar.supertrend.entities;

import org.ta4j.core.BarSeries;

import java.util.HashMap;
import java.util.Map;

public class BollingerBand {
    private final Map<BBType, Double> bbValues = new HashMap<>();
    private final Map<BBType, Double> bbValuesAtLb = new HashMap<>();
    private final int bbPeriod;
    private final int bbSd;
    private final BarSeries series;

    public enum BBType {
        UB, MB, LB, SMA
    }

    public BollingerBand(BarSeries series, int bbPeriod, int bbSd) {
        this.series = series;
        this.bbPeriod = bbPeriod;
        this.bbSd = bbSd;
    }

    // Calculate SMA manually
    private double calculateSMA(int endIndex) {
        if (endIndex >= series.getBarCount() || endIndex < 0) {
        	return Double.NaN;
        }

        int startIndex = Math.max(0, endIndex - bbPeriod + 1);
        int actualPeriod = endIndex - startIndex + 1; // Actual period might be less than bbPeriod
        if (actualPeriod < bbPeriod) {
            return Double.NaN; // Or any default value indicating insufficient data
        }

        double sum = 0.0;
        for (int i = startIndex; i <= endIndex; i++) {
            sum += series.getBar(i).getClosePrice().doubleValue();
        }

        return sum / actualPeriod; // Ensure correct averaging
    }

    private double calculateStandardDeviation(int endIndex, double sma) {
        if (endIndex >= series.getBarCount() || endIndex < 0) {
        	return Double.NaN;
        }

        int startIndex = Math.max(0, endIndex - bbPeriod + 1);
        int actualPeriod = endIndex - startIndex + 1; // Same as in calculateSMA

        if (actualPeriod < bbPeriod) {
            return Double.NaN; // Or any default value indicating insufficient data
        }

        double sumSquaredDiffs = 0.0;
        for (int i = startIndex; i <= endIndex; i++) {
            double closePrice = series.getBar(i).getClosePrice().doubleValue();
            sumSquaredDiffs += Math.pow(closePrice - sma, 2);
        }
        return Math.sqrt(sumSquaredDiffs / actualPeriod);
    }



    public Map<BBType, Double> getBbValues(boolean belongsToLb, int parser) {
        if (parser >= series.getBarCount() || parser < 0) {
            return new HashMap<>();
        }

        Map<BBType, Double> values = belongsToLb ? bbValuesAtLb : bbValues;

        double sma = calculateSMA(parser);
        double sd = calculateStandardDeviation(parser, sma);

        double upperBand = sma + (bbSd * sd);
        double lowerBand = sma - (bbSd * sd);

        values.put(BBType.UB, upperBand);
        values.put(BBType.MB, sma);
        values.put(BBType.LB, lowerBand);
        values.put(BBType.SMA, sma);

        return values;
    }


    public String getBbValuesInString(int parser) {
//        double sma = calculateSMA(parser);
//        double sd = calculateStandardDeviation(parser, sma);

//        double upperBand = sma + (bbSd * sd);
//        double lowerBand = sma - (bbSd * sd);

        return "";
    }
}
