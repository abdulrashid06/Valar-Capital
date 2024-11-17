package com.valar.accountAttributes;

import com.valar.utils.SymphonyLoginCredentialsLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

public class SymphonyAccountAttributes extends AccountAttributes {
    public String interactiveToken;
    public String url,tokenFile;
    private int urlIndex,accountIndex;
    public SymphonyAccountAttributes(String lotValFile,String broker,String accountName, int index) {
        super(lotValFile,broker,accountName, index);
    }

    public void setLoginCredentials(SymphonyLoginCredentialsLoader slcl){
        url = slcl.url;
        tokenFile = slcl.tokenFile;
        this.accountIndex = slcl.accountIndex;
        this.urlIndex = slcl.urlIndex;
    }

    public String getClientID(){
        return clientID;
    }

    public void checkTokenAndUrlUpdation(List<String> symphonyLoginCredentialsLines){
        checkTokenUpdation();
        checkUrlUpdation(symphonyLoginCredentialsLines);
    }

    private void checkUrlUpdation(List<String> symphonyLoginCredentialsLines){
        url = symphonyLoginCredentialsLines.get(urlIndex).split(",")[accountIndex];
    }

    private void checkTokenUpdation(){
        String filePath = ".\\"+tokenFile;
        File file = new File(filePath);
        if(!file.exists())return;
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            if (lines != null && lines.size() > 0) {
                LocalDate localDate = LocalDate.parse(lines.get(0));
                if (localDate.equals(LocalDate.now()))
                    interactiveToken = lines.get(1);
            }
        }catch (Exception e){}
    }
}
