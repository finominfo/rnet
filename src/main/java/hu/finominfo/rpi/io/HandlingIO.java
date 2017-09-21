package hu.finominfo.rpi.io;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.apache.log4j.Logger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.17..
 */
public abstract class HandlingIO {
    private final static Logger logger = Logger.getLogger(HandlingIO.class);

    private static final Pin START_PIN = RaspiPin.GPIO_09;
    private final GpioController gpio = GpioFactory.getInstance();
    private final GpioPinDigitalInput stopButton = gpio.provisionDigitalInputPin(START_PIN, PinPullResistance.PULL_UP);


    public HandlingIO() {
        stopButton.setShutdownOptions(true);
        stopButton.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
            if (event.getPin().getPin().equals(START_PIN) && event.getState().equals(PinState.LOW)) {
                if (checkAfterAWhile() && checkAfterAWhile() && checkAfterAWhile() && checkAfterAWhile() && checkAfterAWhile()) {
                    logger.info("Pressed stop button");
                    stopButtonPressed();
                }
            }
        });
    }

    private boolean checkAfterAWhile() {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() == start) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return stopButton.getState().equals(PinState.LOW);
    }
    
    public abstract void stopButtonPressed();

}
