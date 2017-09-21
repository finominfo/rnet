package hu.finominfo.common;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class Globals {
    public static final String VIDEO_FOLDER = "./video";
    public static final String AUDIO_FOLDER = "./audio";
    public static final String PICTURE_FOLDER = "./picture";
    public static final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(4);
}
