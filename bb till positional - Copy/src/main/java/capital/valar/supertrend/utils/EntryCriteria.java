package capital.valar.supertrend.utils;



import capital.valar.supertrend.state.minute.OptionState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntryCriteria {
    String dNT,date,time;
    double indexClose;
//    Map<Double, OptionState> ranksMapPE, ranksMapCE;
    KeyValues kv;
    List<OptionState> allOptionsStatePE,allOptionsStateCE;

    public EntryCriteria(KeyValues kv, List<OptionState> allOptionsStatePE, List<OptionState> allOptionsStateCE/*, Map<Double, OptionState> ranksMapPE, Map<Double, OptionState> ranksMapCE*/) {
        this.kv = kv;
        this.allOptionsStatePE = allOptionsStatePE;
        this.allOptionsStateCE = allOptionsStateCE;
//        this.ranksMapPE = ranksMapPE;
//        this.ranksMapCE = ranksMapCE;
    }

    public void setIndexClose(String dNT,String date, String time,double indexClose){
        this.dNT = dNT;
        this.date = date;
        this.time = time;
        this.indexClose = indexClose;
    }

    public void createRanksMap(List<OptionState> optionStates){
        float premium = -Float.MAX_VALUE;//premiums of pe and ce
        OptionState osWithMaxPremium = null;

        for (OptionState os : optionStates) {
            float tempPremium = os.premium;
            if (premium < tempPremium) {
                premium = tempPremium;
                osWithMaxPremium = os;
            }
        }

        Map<Double, OptionState> rankMap = new HashMap();
        rankMap.put(0d, osWithMaxPremium);

        for(OptionState os : optionStates) {
            if(os==osWithMaxPremium)continue;;
            double val = os.getStrike();
            double rank = (Math.abs(val - osWithMaxPremium.getStrike())) / 100;
            if (os.peOrCE.equalsIgnoreCase("PE") && osWithMaxPremium.getStrike() < val) rank *= -1;
            else if(os.peOrCE.equalsIgnoreCase("CE") && osWithMaxPremium.getStrike() > val) rank *= -1;
            rankMap.put(rank, os);
        }
    }

    public double getRankOf(OptionState f, Map<Double, OptionState> ranksMap) {
        for (double rank : ranksMap.keySet()) {
            if (ranksMap.get(rank) == f)
                return rank;
        }

        return -1000;
    }

    /*public Object[] getOptionAccToSDOrDistance() {
        boolean skipTheDayOrMin = false;
        float bnForPE = getBNInTermsOfDistanceOrATR(dNT,date, indexClose, "PE",kv.pOrdOrA,kv.priceOrDistanceOrAtrMultiplier),
                bnForCE = getBNInTermsOfDistanceOrATR(dNT,date, indexClose, "CE",kv.pOrdOrA,kv.priceOrDistanceOrAtrMultiplier);

        int bnForPERounded = (int) bnForPE, bnForCERounded = (int) bnForCE, bnMod100PE = (int) (bnForPE % 100), bnMod100CE = (int) (bnForCE % 100);
        bnForPERounded -= bnMod100PE;
        bnForCERounded -= bnMod100CE;
        if (bnMod100PE >= KeyValues.strikePlus/2) bnForPERounded += KeyValues.strikePlus;
        if (bnMod100CE >= KeyValues.strikePlus/2) bnForCERounded += KeyValues.strikePlus;

        OptionState optionPE = Filter.getOptionWithStrike(allOptionsStatePE, bnForPERounded),
                optionCE = Filter.getOptionWithStrike(allOptionsStateCE, bnForCERounded);

        int skipped = 0;
        while (optionPE==null || optionCE==null || !optionPE.minMaxConditionSatisfied || !optionCE.minMaxConditionSatisfied) {
            bnForPERounded += KeyValues.strikePlus;
            bnForCERounded -= KeyValues.strikePlus;
            optionPE = Filter.getOptionWithStrike(allOptionsStatePE, bnForPERounded);
            optionCE = Filter.getOptionWithStrike(allOptionsStateCE, bnForCERounded);
            skipped++;

            if (skipped > 50) {
                skipTheDayOrMin = true;
                break;
            }
        }
        return new Object[]{skipTheDayOrMin || optionPE==null || optionCE==null,optionPE, optionCE};
    }*/

    public Object[] getOptionAccToRank(OptionState optionPE, OptionState optionCE, double rank) {
        /*while (optionPE == null || optionCE == null || !optionPE.minMaxConditionSatisfied || !optionCE.minMaxConditionSatisfied || optionPE.ohlc.lastMinClose < (kv.rankMinClosePercent * indexClose) || optionCE.ohlc.lastMinClose < (kv.rankMinClosePercent * indexClose)) {
            rank--;
            if (rank <= -5) {
                skipTheDayOrMin = true;
                System.err.println("Error because of PremiumRank on date " + date + " " + time + " rank " + rank + " ranksMap:\nranksMapPE " + ranksMapPE + "\n ranksMapCE " + ranksMapCE
                        + " \n rank " + rank + " \t " + ranksMapPE.get(rank));
            }
            if (skipTheDayOrMin) break;
            optionPE = ranksMapPE.get(rank);
            optionCE = ranksMapCE.get(rank);
        }
        return new Object[]{optionPE, optionCE, skipTheDayOrMin, rank};*/
        return null;
    }

    public OptionState getOptionAccToPrice(String peOrCE){
        /*double price = kv.priceOrDistanceOrAtrMultiplier/100 * indexClose;
        double distance = Double.MAX_VALUE;
        OptionState res = null;
        List<OptionState> allOptionsState;
        if(peOrCE.equalsIgnoreCase("PE"))allOptionsState = allOptionsStatePE;
        else allOptionsState = allOptionsStateCE;
        for(OptionState os:allOptionsState){
            if(os.peOrCE.equalsIgnoreCase(peOrCE) && os.minMaxConditionSatisfied) {
                double close = os.ohlc.close,
                        dist = Math.abs(price - close);
                if (distance > dist) {
                    distance = dist;
                    res = os;
                }
            }
        }

        return res;*/
        return null;
    }

    public static float getBNInTermsOfDistance(float priceOrRankOrDistance, float niftyClose, String type){

        if(type.equals("CE")) niftyClose = niftyClose + (priceOrRankOrDistance / 100 * niftyClose);
        else niftyClose = niftyClose - (priceOrRankOrDistance/100 * niftyClose);

        return niftyClose;
    }

    public static float getBNInTermsOfDistanceOrATR(String dNT,String date,double niftyClose,String type,char pOrdOrA,float priceOrDistanceOrAtrMultiplier){
        /*float distanceOrATRMultiplier = 0;
        boolean isSD = pOrdOrA=='s';
        if(pOrdOrA=='d') distanceOrATRMultiplier = priceOrDistanceOrAtrMultiplier/100 * (float)niftyClose;
        else if(pOrdOrA=='a') distanceOrATRMultiplier = bnDayATRMap.get(date).floatValue() * priceOrDistanceOrAtrMultiplier;
        else if(isSD) distanceOrATRMultiplier = getSD(dNT);

//        System.out.println(dNT+" b4 sd "+distanceOrATRMultiplier+" "+niftyClose+" "+type);

        if(type.equals("CE")){
            if (isSD) niftyClose = niftyClose + (distanceOrATRMultiplier * priceOrDistanceOrAtrMultiplier);
            else niftyClose += distanceOrATRMultiplier;
        } else{
            if(isSD)niftyClose = niftyClose - (distanceOrATRMultiplier * priceOrDistanceOrAtrMultiplier);
            else niftyClose -= distanceOrATRMultiplier;
        }
//        System.out.println(dNT+" after sd "+distanceOrATRMultiplier+" "+niftyClose+" "+type+" "+priceOrDistanceOrAtrMultiplier);

        return (float)niftyClose;*/

        return 0;
    }

    public static float getSD(String dnt) {
        /*float sum = 0,standardDeviation = 0,mean = 0,res = 0,sq = 0;
        List<Float> arr = new ArrayList<>();
        if(!indexStoredPricesForSD.closeList.containsKey(dnt))return 0;
        String[] elements = indexStoredPricesForSD.closeList.get(dnt).split(",");
        for (String i:elements) {
            float v = Float.parseFloat(i);
            arr.add(v);
            sum = sum + v;
        }

        int n = arr.size();

        mean = sum / (n);

        for(float i:arr)
            standardDeviation = (float)(standardDeviation + Math.pow((i - mean), 2));

        sq = standardDeviation / n;
        res = (float)Math.sqrt(sq);

        return res;*/

        return 0;
    }
}
