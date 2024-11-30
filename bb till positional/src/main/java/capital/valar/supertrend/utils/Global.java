package capital.valar.supertrend.utils;

import capital.valar.supertrend.application.PropertiesReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.*;

import static capital.valar.supertrend.utils.Options.getDateDifference;

public class Global {

    public final static List<String> skipDates = new ArrayList(Arrays.asList(new String[]{"10-07-17","08-08-17","20-12-16","19-10-17"}));
    public static int indexType;
    public static String indexFile,indexDayFile, tradingDatesFile,expiryDatesFile;
    public static int strikePlus;
    public static String optionsDataPath;
    public static int dataAvailableFromYr;
    public static List<String> indexLines;
    public static List<String> listOfFiles;

    public static boolean printOrderInfoAndSerialWise = true;

    public static Map<String,String> dayAtrMap = new HashMap<>(),minAtrMap = new HashMap<>();
    public static Map<String,String> dayEMAMap = new HashMap<>(),minEMAMap = new HashMap<>();
    public static Map<String,Float> lastDayCloseMap = new HashMap<>();

    public static void addOrRemove(List<Double> list, int period, double add){
        if(list.size()>=period){
            list.remove(0);
        }list.add(add);
    }

    public static int getInMinutes(String time){
        String[] timeSplits = time.split(":");
        return (Integer.parseInt(timeSplits[0]) * 60) + Integer.parseInt(timeSplits[1]);
    }

    public static int getInMinutes(int hr,int min){
        return (hr * 60) + min;
    }

//    public static List<String> getAllFilesOfFolder() {
//        List<String> files = new ArrayList<>();
//        int len = optionsDataPath.length(),
//                tillYr = Integer.parseInt(optionsDataPath.substring(len - 15,len - 13));
//
//        for(int i = dataAvailableFromYr;i<=tillYr;i++){
//            String path = optionsDataPath+"20"+i;
//            File file = new File(path);
//            String[] directories = file.list(new FilenameFilter() {
//                @Override
//                public boolean accept(File current, String name) {
//                    return new File(current, name).isFile();
//                }
//            });
//            files.addAll(Arrays.asList(directories));
//        }
//
//        return files;
//    }

    public static String yrMonthDate(String date){
        String day = date.split("-")[0],month = date.split("-")[1],yr = date.split("-")[2];
        return "20"+yr+"-"+month+"-"+day;
    }

    public static String getDateByComparing(boolean nextExpiry,String date){
        double val=10;
        String dat = yrMonthDate(date);
        String sout="",next="";
        try{
            FileReader fileReader = new FileReader(expiryDatesFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String s,ss="";

            boolean updateNext=false;
            while((s=reader.readLine())!=null){
                ss = s;
                s = yrMonthDate(s.split(" ")[0]);
                double diff = getDateDifference(s,dat);
                if(updateNext){next = ss;updateNext = false;}

                if(diff==0){
                    sout = ss;
                    next = reader.readLine();
                    break;
                }else if(diff<0){
                    if(val>0 || val<diff) {
                        sout = ss;
                        val = diff;
                        updateNext = true;
                    }
                }
            }

        }catch(Exception e){e.printStackTrace();}

        if(nextExpiry)return next;
        return sout;
    }

    public static ArrayList[] getFiles(String s){
        ArrayList<String> filesCE = new ArrayList(),filesPE = new ArrayList();
        for (String file:listOfFiles) {
            if(file.startsWith(s)) {
                if(file.contains("PE"))
                    filesPE.add(file);
                else
                    filesCE.add(file);
            }
        }
        return new ArrayList[]{filesPE,filesCE};
    }

    public static String getInitPath(String file){
        try {
            int yr;;

            if (indexType == 0) yr = Integer.parseInt(file.substring(14, 16));
            else yr = Integer.parseInt(file.substring(10, 12));

            int diff = yr - dataAvailableFromYr;

            return optionsDataPath + "20" + (dataAvailableFromYr + diff) + "/";
        }catch (Exception e){return "";}
    }
}
