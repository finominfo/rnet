package hu.finominfo.rnet.communication.tcp.events;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.19..
 */
public enum EventType {
    FILE,
    WAIT,
    STATUS,
    START,
    STOP,
    DIR,
    DEL_FILE,
    MESSAGE,
    CONTROL;


    public byte getNumber() {
        return (byte) ordinal();
    }

    public static EventType get(byte num) {
        for (EventType t : EventType.values()) {
            if (t.getNumber() == num) {
                return t;
            }
        }
        return null;
    }
}
