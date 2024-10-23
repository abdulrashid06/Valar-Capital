package capital.valar.supertrend.entities;


/**
 * This class stores intermediate states for calculating EMA50 and EMA100.
 * It also saves current EMA50 and EMA100. It can be modified easily to contain other EMAs.
 */
public class EMAEntity {

    private float alphaS;
    float ema;
    float firstSClose;
    int count;
    private int period;

    public EMAEntity(int period){
        this.period = period;
        alphaS = 2f/((float)period+1f);
    }

    public void calculateEMA(Ohlc ohlc) {
        if (count< period){
            count++;
            firstSClose+=ohlc.close;
            if(count== period) ema = firstSClose/ period;
        }else ema = ((ohlc.close- ema) * alphaS + ema);
    }

    @Override
    public String toString() {
        return "EMAEntity{" +
                ", emaS=" + ema +
                '}';
    }


    public float getEMA(){
        return ema;
    }
}