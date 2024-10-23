/*
package capital.valar.supertrend.entities;


import capital.valar.supertrend.utils.KeyValues;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static capital.valar.supertrend.utils.Global.printOrderInfoAndSerialWise;

public class KeyAttribs {
//    public Attribs overAllAttribs,wingsOverAllAttribs;
    public KeyValues kv;
    public PrintWriter pw,pw2;
//    public ATREntity atrEntity,dayAtrEntity;
//    public EMAEntity emaEntity,dayEmaEntity;
    public int index;
    private List<Float> closes = new ArrayList<>();
//    public Trading trading;

    public KeyAttribs(KeyValues kv, int index)throws Exception{
        this.kv = kv;
        this.index = index;
//        overAllAttribs = new Attribs();
//        wingsOverAllAttribs = new Attribs();
//        dayAtrEntity = new ATREntity(kv.dayATRPeriod);
//        dayEmaEntity = new EMAEntity(kv.dayEMAPeriod);
//        atrEntity = new ATREntity(kv.atrPeriod);
//        emaEntity = new EMAEntity(kv.emaPeriod);
        loadPrintWriters();
    }

    private void loadPrintWriters()throws Exception{
        if(printOrderInfoAndSerialWise) {
            pw = new PrintWriter(new File("./Outputs/serialwise/" + kv.sno + "/DayWise.csv"));
            pw2 = new PrintWriter(new File("./Outputs/serialwise/" + kv.sno + "/OrderInfo.csv"));
            pw.write("date,totalTrades,winPercent,maxProfit,maxloss,profit,\n");
            pw2.write("Date,Event,Time,Close,Event,Time,Close,Reason,ReasonInfo,File,Profit,ProfitPercent,\n");
        }
    }

    public void closeReader(){
        if(printOrderInfoAndSerialWise) {
            pw.close();
            pw2.close();
        }
    }

    */
/*public float getSD(Ohlc ohlc){
        if(kv.sdPeriod!=0 && (kv.entryPOrROrDOrS=='s')) {
            closes.add(ohlc.close);
            if (closes.size() > kv.sdPeriod)
                closes.remove(0);

            return getSD();
        }

        return 0;
    }

    private float getSD() {
        float sum = 0,standardDeviation = 0,mean = 0,res = 0,sq = 0;
        List<Float> arr = new ArrayList<>();

        for (float v:closes) {
            arr.add(v);
            sum = sum + v;
        }

        int n = arr.size();

        mean = sum / (n);

        for(float i:arr)
            standardDeviation = (float)(standardDeviation + Math.pow((i - mean), 2));

        sq = standardDeviation / n;
        res = (float)Math.sqrt(sq);

//        System.out.println(arr.size()+" "+arr+" \tSD "+res);

        return res;
    }*//*


//    public void calculateAttribsAtDayEnd(){
//        if (trading.isTraded) trading.calculateOverAllTradeAttribs(kv,overAllAttribs);
//    }
}
*/
