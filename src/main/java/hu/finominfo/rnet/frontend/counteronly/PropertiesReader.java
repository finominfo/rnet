package hu.finominfo.rnet.frontend.counteronly;

import hu.finominfo.rnet.properties.PropReader;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 *
 * @author User
 */
public class PropertiesReader {

    private volatile List<Long> times;
    private final String defaultTimes = "120, 120, 120, 120, 120, 120, 120, 120, 120, 120";
    private volatile List<String> roomNames;
    private final String defaultNames = "ROOM 1, ROOM 2, ROOM 3, ROOM 4, ROOM 5, ROOM 6, ROOM 7, ROOM 8, ROOM 9, ROOM10";
    private volatile List<String> baseAudio;
    private final String defaultBaseAudio = "rainforest1.wav,rainforest2.wav";
    private volatile List<String> animalVoices;
    private final String defaultAnimalVoices = "bird1.wav,chewbacca.wav,frogs.wav,gorilla.wav,monkey.wav";
    private volatile String beep;
    private volatile String success;
    private volatile String failed;
    private final boolean monkeysEnabled;

    public PropertiesReader() {
        Properties prop = PropReader.get().getProperties();
        List<String> myList = Arrays.asList(prop.getProperty("times", defaultTimes).split(","));
        times = myList.stream().map((String s) -> Long.parseLong(s.trim()) * 60_000L).collect(Collectors.toList());
        roomNames = Arrays.asList(prop.getProperty("roomNames", defaultNames).split(","));
        baseAudio = Arrays.asList(prop.getProperty("baseAudio", defaultBaseAudio).split(","));
        animalVoices = Arrays.asList(prop.getProperty("animalVoices", defaultAnimalVoices).split(","));
        beep = prop.getProperty("beep", "beep.wav");
        String monkeys = prop.getProperty("monkeysEnabled", "yes");
        if (monkeys.equalsIgnoreCase("yes") || monkeys.equalsIgnoreCase("true")) {
            monkeysEnabled = true;
        } else {
            monkeysEnabled = false;
        }
        success = prop.getProperty("success", "success.wav");
        failed = prop.getProperty("failed", "failed.wav");
    }

    public boolean isMonkeysEnabled() {
        return monkeysEnabled;
    }

    public String getFailed() {
        return failed;
    }

    public String getSuccess() {
        return success;
    }

    public String getBeep() {
        return beep;
    }

    public List<String> getRoomNames() {
        return roomNames;
    }

    public List<Long> getTimes() {
        return times;
    }

    public List<String> getBaseAudio() {
        return baseAudio;
    }

    public List<String> getAnimalVoices() {
        return animalVoices;
    }

}
