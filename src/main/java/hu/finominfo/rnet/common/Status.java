package hu.finominfo.rnet.common;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.14.
 */
public class Status {
    private volatile String counter = null;
    private volatile String audio = null;
    private volatile String video = null;
    private volatile String picture = null;
    private volatile String message = null;

    public void setCounter(String counter) {
        this.counter = counter;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCurrent() {
        StringBuilder builder = new StringBuilder();
        if (valueIsOk(counter)) {
            builder.append("Counter: ").append(counter).append("\n");
        }
        if (valueIsOk(audio)) {
            builder.append("Audio: ").append(audio).append("\n");
        }
        if (valueIsOk(video)) {
            builder.append("Video: ").append(video).append("\n");
        }
        if (valueIsOk(picture)) {
            builder.append("Picture: ").append(picture).append("\n");
        }
        if (valueIsOk(message)) {
            builder.append("Message: ").append(message).append("\n");
        }
        String result = builder.toString();
        return result.isEmpty() ? " " : result;
    }

    public boolean valueIsOk(String value) {
        return value != null && !value.isEmpty();
    }

}
