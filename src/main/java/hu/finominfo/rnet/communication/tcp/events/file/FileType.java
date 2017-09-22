package hu.finominfo.rnet.communication.tcp.events.file;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.19..
 */
public enum FileType {
    VIDEO((byte) 1),
    AUDIO((byte) 2),
    PICTURE((byte) 3);

    private final byte number;

    FileType(byte b) {
        number = b;
    }

    public byte getNumber() {
        return number;
    }

    public static FileType get(byte num) {
        for (FileType t : FileType.values()) {
            if (t.getNumber() == num) {
                return t;
            }
        }
        return null;
    }
}
