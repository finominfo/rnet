package hu.finominfo.rnet.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 *
 * @author User
 */
public class KnockProps {

    private volatile List<Integer> rythms;
    private volatile String knockVoice;
    private volatile String success;
    private volatile String failed;
    private volatile int longWaiting;
    private volatile int shortWaiting;
    private volatile int boundWaiting;

    public KnockProps() {
        Properties prop = PropReader.get().getProperties();
        knockVoice = prop.getProperty("knockVoice", "knock.wav");
        success = prop.getProperty("successMorse", "success_knock.wav");
        failed = prop.getProperty("failedMorse", "failed.wav");
        List<String> myList = Arrays.asList(prop.getProperty("rythms", "1,2,3,2,1,2").split(","));
        List<Integer> intList = myList.stream().map((String s) -> Integer.parseInt(s) - 1).collect(Collectors.toList());
        rythms = new ArrayList<>();
        boolean firstRun = true;
        for (int numOfShortSpaces : intList) {
            if (!firstRun) {
                rythms.add(1);
            } else {
                firstRun = false;
            }
            for (int i = 0; i < numOfShortSpaces; i++) {
                rythms.add(0);
            }
        }
        longWaiting = Integer.valueOf(prop.getProperty("longWaiting", "900"));
        shortWaiting = Integer.valueOf(prop.getProperty("shortWaiting", "300"));
        boundWaiting = Integer.valueOf(prop.getProperty("boundWaiting", "600"));
    }

    public String getSuccess() {
        return success;
    }

    public String getFailed() {
        return failed;
    }

    public List<Integer> getRythms() {
        return rythms;
    }

    public int getLongWaiting() {
        return longWaiting;
    }

    public int getShortWaiting() {
        return shortWaiting;
    }

    public int getBoundWaiting() {
        return boundWaiting;
    }

    public String getKnockVoice() {
        return knockVoice;
    }

    public static void main(String[] args) {
        KnockProps propertiesReader = new KnockProps();
        System.out.println(propertiesReader.getRythms());
    }
}
