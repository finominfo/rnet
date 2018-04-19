package hu.finominfo.rnet.communication.tcp.events.dir.media;

/**
 * Created by kks on 2018.04.13..
 */
public enum TimeOrder {
    BEFORE("*B*"),
    DURING("*D*"),
    SUCCESS("*S*"),
    FAILED("*F*");

    private final String sign;

    TimeOrder(String sign) {
        this.sign = sign;
    }

    public String getSign() {
        return sign;
    }
}
