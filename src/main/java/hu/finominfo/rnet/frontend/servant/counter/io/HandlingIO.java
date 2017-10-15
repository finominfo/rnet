package hu.finominfo.rnet.frontend.servant.counter.io;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import hu.finominfo.rnet.common.Utils;
import org.apache.log4j.Logger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.17..
 */
public abstract class HandlingIO {
    private final static Logger logger = Logger.getLogger(HandlingIO.class);

    private static final Pin START_PIN = RaspiPin.GPIO_09;
    private volatile GpioPinDigitalInput stopButton = null;


    public HandlingIO() {
        try {
            stopButton = GpioFactory.getInstance().provisionDigitalInputPin(START_PIN, PinPullResistance.PULL_UP);
            stopButton.setShutdownOptions(true);
            stopButton.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
                if (event.getPin().getPin().equals(START_PIN) && event.getState().equals(PinState.LOW)) {
                    if (checkAfterAWhile() && checkAfterAWhile() && checkAfterAWhile() && checkAfterAWhile() && checkAfterAWhile()) {
                        logger.info("Pressed stop button");
                        stopButtonPressed();
                    }
                }
            });
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }

    private boolean checkAfterAWhile() {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() == start) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage());
            }
        }
        return stopButton.getState().equals(PinState.LOW);
    }

    public abstract void stopButtonPressed();

}
