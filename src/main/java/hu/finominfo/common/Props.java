package hu.finominfo.common;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21.
 */
public class Props {

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
    private final boolean controller;
    private final int port;

    private static Props ourInstance = new Props();

    public static Props get() {
        return ourInstance;
    }

    private Props() {
        Properties prop = PropReader.get().getProperties();
        List<String> myList = Arrays.asList(prop.getProperty("times", defaultTimes).split(","));
        times = myList.stream().map((String s) -> Long.parseLong(s.trim()) * 60_000L).collect(Collectors.toList());
        roomNames = Arrays.asList(prop.getProperty("roomNames", defaultNames).split(","));
        baseAudio = Arrays.asList(prop.getProperty("baseAudio", defaultBaseAudio).split(","));
        animalVoices = Arrays.asList(prop.getProperty("animalVoices", defaultAnimalVoices).split(","));
        beep = prop.getProperty("beep", "beep.wav");
        success = prop.getProperty("success", "success.wav");
        failed = prop.getProperty("failed", "failed.wav");
        controller = prop.getProperty("node", "servant").equalsIgnoreCase("controller");
        port = Integer.valueOf(prop.getProperty("port", "10000"));
    }

    public int getPort() {
        return port;
    }

    public boolean isController() {
        return controller;
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