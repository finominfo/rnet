package hu.finominfo.rnet.communication.tcp.events;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.19..
 */
public enum EventType {
    RECEIVED(1),
    NOT_RECEIVED(2),
    ADDRESS(3),
    FILE(4),
    START(5),
    STOP(6),
    FOLDERS(7);

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
