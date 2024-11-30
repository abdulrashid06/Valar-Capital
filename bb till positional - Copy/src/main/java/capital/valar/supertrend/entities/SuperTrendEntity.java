package capital.valar.supertrend.entities;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

public class SuperTrendEntity {
  double ub;
  double lb;
  double m;
  public double st;
  double cm1;
  double stm1;

  public String lastSuperTrendSign,superTrendSign;

  public SuperTrendEntity(double multiplier){
    m = multiplier;
  }

  public double getMultiplier(){
    return m;
  }


  public void update(Ohlc ohlc,double atr) {
    if (atr == 0) {
      cm1 = ohlc.close;
      return;
    }
    double ubb = (ohlc.high + ohlc.low) / 2 + m * atr;
    double lbb = (ohlc.high + ohlc.low) / 2 - m * atr;
    double ubTm1 = ub;
    double lbTm1 = lb;
    if (ub > ubb || ub < cm1) {
      ub = ubb;
    }
    if (lb < lbb || lb > cm1) {
      lb = lbb;
    }
    stm1 = st;
    if (st == ubTm1) {
      if (ohlc.close <= ub) {
        st = ub;
      } else {
        st = lb;
      }
    } else if (st == lbTm1) {
      if (ohlc.close >= lb) {
        st = lb;
      } else {
        st = ub;
      }
    }
    this.cm1 = ohlc.close;

    lastSuperTrendSign = superTrendSign;
    if(st==ub) superTrendSign = "red";
    else if(st==lb) superTrendSign = "green";

  }

  public String print(LocalDateTime d, double ubb, double lbb) {
    DecimalFormat df = new DecimalFormat("#.##");
    return "SuperTrendEntity{" +
      " Date=" + d +
      ", ubb=" + df.format(ubb) +
      ", lbb=" + df.format(lbb) +
      ", ub=" + df.format(ub) +
      ", lb=" + df.format(lb) +
      ", st=" + df.format(st) +
      ", cm1=" + df.format(cm1) +
      '}';
  }

  public boolean exitShort() {
    return stm1 == ub && st == lb;
  }

  public boolean exitLong() {
    return stm1 == lb && st == ub;
  }

  @Override
  public String toString() {
    return "SuperTrendEntity{" +
            "ub=" + ub +
            ", lb=" + lb +
            ", m=" + m +
            ", st=" + st +
            ", cm1=" + cm1 +
            ", stm1=" + stm1 +
            ", superTrendSign='" + superTrendSign + '\'' +
            '}';
  }
}
