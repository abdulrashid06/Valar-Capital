package com.valar.application;

public enum KeyStoreFiles {
    lotFile("DeltaHedging_Lots.csv"),testLotFile("DeltaHedging_Lots_Test.csv"),
     keyStore("DeltaHedging_20240223_Keystore.csv"), accounts("Accounts_Server.csv"),;
    private String fileName;
    KeyStoreFiles(String fileName){
        this.fileName = fileName;
    }

    public String getFile(){
        return fileName;
    }

}
