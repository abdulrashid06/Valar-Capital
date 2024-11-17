package com.valar.application;

import com.valar.utils.KeyValues;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.valar.application.ValarTrade.strategy;

public class BOD {
    private boolean testing;
    private boolean orderPlaceOff;

    public BOD(ValarTrade valarTrade){
        try {
            String[] files = getFilesOrDirectories(".\\");
            List<String> filesFound = new ArrayList();
            for(String file:files)
                if(file.startsWith(strategy+"_Lots") && !file.contains("Test"))
                    filesFound.add(file);

            if(!testing && filesFound.size()>1){
                System.out.println("Strangle Lots Files available are...");
                for(int i=0;i<filesFound.size();i++)
                    System.out.println((i+1)+" "+filesFound.get(i));
                Scanner s = new Scanner(System.in);
                System.out.println("Enter your choice...");
                valarTrade.lotFile = filesFound.get(s.nextInt()-1);
            }else {
                if (testing) valarTrade.lotFile = KeyStoreFiles.testLotFile.getFile();
                else valarTrade.lotFile = KeyStoreFiles.lotFile.getFile();
            }

            System.out.println("Selected LotFile "+valarTrade.lotFile);

            for (KeyStoreFiles pv : KeyStoreFiles.values()) {
                if(pv== KeyStoreFiles.testLotFile && !testing)continue;
                BufferedReader br = new BufferedReader(new FileReader(pv.getFile()));
                br.readLine();
                System.out.println(pv.getFile());
                if(pv== KeyStoreFiles.keyStore)
                    new KeyValues(br.readLine());
            }

            if(LocalTime.now().isAfter( LocalTime.parse( "09:15:00" ) )){
                while(true) {
                    System.out.println("Symphony's token could be updated, Are you sure you wanna continue?");
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("\t\tPress 1 to continue and 2 to exit:");
                    int a;
                    try{a = scanner.nextInt();}catch (Exception e){continue;}
                    if(a==1)break;else if(a==2)System.exit(0);
                }

            }

        }catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    public boolean isTesting(){
        return testing;
    }

    public boolean isOrderPlaceOff(){
        return orderPlaceOff;
    }

    public static String[] getFilesOrDirectories(String path){
        File file = new File(path);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current,name).isFile();
            }
        });
        return directories;
    }
}
