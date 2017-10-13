package hu.finominfo.rnet.communication.tcp.events.control;

import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventType;
import hu.finominfo.rnet.communication.tcp.events.control.objects.PlayAudio;
import hu.finominfo.rnet.communication.tcp.events.control.objects.PlayVideo;
import hu.finominfo.rnet.communication.tcp.events.control.objects.ResetCounter;
import hu.finominfo.rnet.communication.tcp.events.control.objects.ShowPicture;
import io.netty.buffer.ByteBuf;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class ControlEvent extends Event {
    private static final Logger logger = Logger.getLogger(ControlEvent.class);
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

    //Kvasz András utca 19 - 6-os kapucsengõ

    public static ControlEvent create(ByteBuf msg) {
        ControlType controlType = ControlType.get(msg.readByte());
        try {
            switch (controlType) {
                case SHOW_PICTURE:
                    return new ControlEvent(controlType, ShowPicture.create(msg));
                case PLAY_VIDEO:
                    return new ControlEvent(controlType, PlayVideo.create(msg));
                case PLAY_AUDIO:
                    return new ControlEvent(controlType, PlayAudio.create(msg));
                case PLAY_AUDIO_CONTINUOUS:
                    return new ControlEvent(controlType, PlayAudio.create(msg));
                case STOP_AUDIO:
                    return new ControlEvent(controlType);
                case RESET_COUNTER:
                    return new ControlEvent(controlType, ResetCounter.create(msg));
                case START_COUNTER:
                    return new ControlEvent(controlType);
                case STOP_COUNTER:
                    return new ControlEvent(controlType);
            }
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
        return new ControlEvent(controlType);
    }


}
