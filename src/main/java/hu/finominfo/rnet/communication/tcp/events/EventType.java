package hu.finominfo.rnet.communication.tcp.events;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.19..
 */
public enum EventType {
    FILE(1),
    WAIT(2),
    ADDRESS(3),
    START(4),
    STOP(5),
    DIR(6),
    DEL_FILE(7);

    private final byte number;

    EventType(int b) {
        number = (byte) b;
    }

    public byte getNumber() {
        return number;
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
