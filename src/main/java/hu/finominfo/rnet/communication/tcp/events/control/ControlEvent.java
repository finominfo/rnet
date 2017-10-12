package hu.finominfo.rnet.communication.tcp.events.control;

import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventType;
import hu.finominfo.rnet.communication.tcp.events.control.objects.ShowPicture;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

import java.io.File;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class ControlEvent extends Event {

    private final ControlType controlType;
    private final ControlObject controlObject;

    public ControlEvent(ControlType controlType, ControlObject controlObject) {
        super(EventType.CONTROL);
        this.controlType = controlType;
        this.controlObject = controlObject;
    }

    public ControlEvent(ControlType controlType) {
        super(EventType.CONTROL);
        this.controlType = controlType;
        this.controlObject = null;
    }

    public ControlType getControlType() {
        return controlType;
    }

    public ControlObject getControlObject() {
        return controlObject;
    }

    @Override
    public void getRemainingData(ByteBuf buf) {
        buf.writeByte(controlType.getNumber());
        if (controlObject != null) {
            controlObject.getData(buf);
        }
    }

    public static ControlEvent create(ByteBuf msg) {
        ControlType controlType = ControlType.get(msg.readByte());
        switch (controlType) {
            case SHOW_PICTURE:
                return new ControlEvent(controlType, ShowPicture.create(msg));
            case PLAY_VIDEO:
                break;
            case PLAY_AUDIO:
                break;
            case PLAY_AUDIO_CONTINUOUS:
                break;
            case STOP_AUDIO:
                break;
            case RESET_COUNTER:
                break;
            case START_COUNTER:
                break;
            case STOP_COUNTER:
                break;
        }
        return new ControlEvent(controlType);
    }


}
