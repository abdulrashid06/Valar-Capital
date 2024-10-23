package capital.valar.supertrend.application;

import capital.valar.supertrend.service.Strategy;
import capital.valar.supertrend.utils.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;

import static capital.valar.supertrend.application.PropertiesReader.properties;
import static capital.valar.supertrend.utils.KeyValues.setIndexAttribs;

public class ValarTrade {
    public static String keystoreHeading = "";

    public static PrintWriter overAllDayWise,overAllOrderInfo,overAllDetails;

    public static Map<String, String> allFilesMap  = new HashMap<String, String>() {
        {
            put("keystore", properties.getProperty("keystoreFile"));
        }};
    private static List<KeyValues> keyStoresList = new ArrayList<>();

    public static void main(String[] args)throws Exception{
        String time1 = LocalTime.now().toString();

        BufferedReader keyValuesReader = new BufferedReader(new FileReader(properties.getProperty("keystoreFile")));
        String kvLine;
        List<KeyValues> keyValuesList = new ArrayList();
        Set<Integer> kvSerialNos = new HashSet<>();
        ValarTrade valarTrade = new ValarTrade();
        keystoreHeading = keyValuesReader.readLine();
        PrintWriters.loadAllWriters();

        while((kvLine = keyValuesReader.readLine())!=null) {
            KeyValues kv = new KeyValues(kvLine);
            if(!kv.invalidStartTime) {
                keyValuesList.add(kv);
                kvSerialNos.add(kv.sno);
            }
        }

        valarTrade.createDirectories(kvSerialNos);
        for(String inputFile : valarTrade.allFilesMap.values())
            copyFile(inputFile,new File("."),new File("./Outputs/"));


        List<String> keystoreLines = Files.readAllLines(Paths.get(allFilesMap.get("keystore")));

        for(int i = 1;i < keystoreLines.size();i++){
            KeyValues kv = new KeyValues(keystoreLines.get(i));
            keyStoresList.add(kv);
        }


        int[] indexTypes = {0,1};
        
        for(int indexType : indexTypes) {
            	Global.indexType = indexType;
                setIndexAttribs(indexType);
                applyStrategyOnKeystore(indexType, keyStoresList.size());
        }

        PrintWriters.closeAllWriters();

        String time2 = LocalTime.now().toString();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date date1 = format.parse(time1);
        Date date2 = format.parse(time2);
        long difference = date2.getTime() - date1.getTime();
        System.out.println("Total Time Taken(In Seconds) -> "+difference/1000);
    }

    private static void applyStrategyOnKeystore(int bnOrN,int ksSize)throws Exception{
        int runKeystores = Integer.parseInt(properties.getProperty("runKeystores"));
        for(int i = 0;i < ksSize;i++){
            List<KeyValues> runForKeyAttribs = new ArrayList<>();
            int j = i;
            for(; runForKeyAttribs.size() < runKeystores && j < ksSize;j++) {
                KeyValues kv = keyStoresList.get(j);
                if(kv.BnOrN==bnOrN) {
                    System.out.println(kv);
                    runForKeyAttribs.add(kv);
                }
            }
            i = j-1;

            if(runForKeyAttribs.size()!=0) {
                Strategy strategy = new Strategy(runForKeyAttribs);
                strategy.apply();
                strategy.calculateOverAll();
            }
        }
    }

    public void createDirectories(Set<Integer> kvSerialNos){
        String[] dirs = {"./Outputs","./Outputs/serialwise"};
        for(String dir:dirs){
            File outputsDir = new File(dir);
            outputsDir.mkdir();
        }

        for(int sno:kvSerialNos){
            File outputsDir = new File("./Outputs/serialwise/"+sno);
            outputsDir.mkdir();
        }
    }

    public static void copyFile(String fileName,File source, File dest) throws IOException {
        File file = new File(dest+"/"+fileName);
        if(file.exists())file.delete();
        Files.copy(new File(source+"/"+fileName).toPath(),new File(dest+"/"+fileName).toPath());
    }
}
