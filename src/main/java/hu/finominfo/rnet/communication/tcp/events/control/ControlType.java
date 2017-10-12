package hu.finominfo.rnet.communication.tcp.events.control;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.19..
 */
public enum ControlType {
    SHOW_PICTURE,
    PLAY_VIDEO,
    PLAY_AUDIO,
    PLAY_AUDIO_CONTINUOUS,
    STOP_AUDIO,
    RESET_COUNTER,
    START_COUNTER,
    STOP_COUNTER;


    public byte getNumber() {
        return (byte) ordinal();
    }

    public static ControlType get(byte num) {
        for (ControlType t : ControlType.values()) {
            if (t.getNumber() == num) {
                return t;
            }
        }
        return null;
    }
}
