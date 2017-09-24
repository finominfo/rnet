package hu.finominfo.rnet.communication.tcp.events.file;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.19..
 */
public enum FileType {
    VIDEO(1),
    AUDIO(2),
    PICTURE(3),
    MAIN(4);

    private final byte number;

    FileType(int b) {
        number = (byte)b;
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
