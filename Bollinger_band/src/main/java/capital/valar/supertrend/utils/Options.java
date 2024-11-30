package capital.valar.supertrend.utils;

import capital.valar.supertrend.application.PropertiesReader;
import capital.valar.supertrend.state.minute.OptionState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static capital.valar.supertrend.utils.Global.listOfFiles;

public class Options {

    public static ArrayList[] getFiles(String s) {
        ArrayList<String> filesCE = new ArrayList(), filesPE = new ArrayList();
        for (String file : listOfFiles) {
            if (file.startsWith(s)) {
                if (file.contains("PE")) filesPE.add(file);
                else filesCE.add(file);
            }
        }
        return new ArrayList[]{filesPE, filesCE};
    }

    public static long getDateDifference(String dateBeforeString,String dateAfterString){
        LocalDate dateBefore = LocalDate.parse(dateBeforeString);
        LocalDate dateAfter = LocalDate.parse(dateAfterString);

        return  ChronoUnit.DAYS.between(dateBefore, dateAfter);
    }

    public static OptionState getOptionWithStrike(String cOrP,int strike,List<OptionState> allOptionStates){
        for(OptionState os : allOptionStates)
            if(os.peOrCE.equalsIgnoreCase(cOrP) && os.getStrike()==strike)return os;

        return null;
    }

    public static OptionState getOptionWithStrike(int strike,List<OptionState> allOptionStates){
        for(OptionState os : allOptionStates)
            if(os.getStrike()==strike)return os;

        return null;
    }
}