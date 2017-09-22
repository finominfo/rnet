package hu.finominfo.rnet.communication.tcp.events;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.19..
 */
public enum EventType {
    RECEIVED((byte) 1),
    NOT_RECEIVED((byte) 2),
    ADDRESS((byte) 3),
    FILE((byte) 4),
    START((byte) 5),
    STOP((byte) 6);

    private final byte number;

    EventType(byte b) {
        number = b;
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
