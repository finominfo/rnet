package hu.finominfo.rnet.frontend.servant.counter.io;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import hu.finominfo.rnet.audio.CompletedEvent;
import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.frontend.servant.gameknock.GameKnockSimple;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.17..
 */
public abstract class HandlingIO {
    private final static Logger logger = Logger.getLogger(HandlingIO.class);

    private static final Pin STOP_PIN = RaspiPin.GPIO_09;
    private static final Pin STOP_AND_OPEN_PIN = RaspiPin.GPIO_08;
    private static final Pin OUT_DOOR_PIN = RaspiPin.GPIO_07;

    private final GpioPinDigitalInput stopButton = GpioFactory.getInstance().provisionDigitalInputPin(STOP_PIN, PinPullResistance.PULL_UP);
    private final GpioPinDigitalInput stopAndOpenButton = GpioFactory.getInstance().provisionDigitalInputPin(STOP_AND_OPEN_PIN, PinPullResistance.PULL_UP);
    private final GpioPinDigitalOutput outDoorPin = GpioFactory.getInstance().provisionDigitalOutputPin(OUT_DOOR_PIN, "FINISHED", PinState.LOW);


    public HandlingIO() {
        try {
            outDoorPin.setShutdownOptions(true, PinState.LOW);
            outDoorPin.low();
            //testOutdoor(outDoorPin);

            stopButton.setShutdownOptions(true);
            stopButton.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
                if (event.getPin().getPin().equals(STOP_PIN) && event.getState().equals(PinState.LOW)) {
                    if (checkAfterAWhile(stopButton) && checkAfterAWhile(stopButton)
                            && checkAfterAWhile(stopButton) && checkAfterAWhile(stopButton) && checkAfterAWhile(stopButton)) {
                        logger.info("Pressed stop button");
                        stopButtonPressed();
                    }
                }
            });
            stopAndOpenButton.setShutdownOptions(true);
            stopAndOpenButton.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
                if (event.getPin().getPin().equals(STOP_AND_OPEN_PIN) && event.getState().equals(PinState.LOW)) {
                    if (checkAfterAWhile(stopAndOpenButton) && checkAfterAWhile(stopAndOpenButton)
                            && checkAfterAWhile(stopAndOpenButton) && checkAfterAWhile(stopAndOpenButton) && checkAfterAWhile(stopAndOpenButton)) {
                        logger.info("Pressed stop and open button");
                        openDoor();
                        stopButtonPressed();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }

    private void testOutdoor(GpioPinDigitalOutput digitalOutput) {
        AtomicInteger i = new AtomicInteger(0);
        Globals.get().executor.schedule(new Runnable() {
            @Override
            public void run() {
                if ((i.incrementAndGet() & 1) == 0) {
                    digitalOutput.low();
                } else {
                    digitalOutput.high();
                }
                if (i.get() < 10) {
                    Globals.get().executor.schedule(this, 3, TimeUnit.SECONDS);
                } else {
                    digitalOutput.low();
                }
            }
        }, 3, TimeUnit.SECONDS);
    }

    private boolean checkAfterAWhile(GpioPinDigitalInput button) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() == start) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage());
            }
        }
        return button.getState().equals(PinState.LOW);
    }

    public abstract void stopButtonPressed();

    public void openDoor() {
        outDoorPin.high();
        Globals.get().executor.schedule(new Runnable() {

            @Override
            public void run() {
                outDoorPin.low();
            }
        }, 5, TimeUnit.SECONDS);
    }

}
