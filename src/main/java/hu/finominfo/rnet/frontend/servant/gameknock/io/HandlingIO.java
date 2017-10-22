package hu.finominfo.rnet.frontend.servant.gameknock.io;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import hu.finominfo.rnet.audio.CompletedEvent;
import hu.finominfo.rnet.frontend.servant.gameknock.GameKnockSimple;
import org.apache.log4j.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author User
 */
public class HandlingIO {

    private final static Logger logger = Logger.getLogger(HandlingIO.class);

    private volatile Pin DOOR_PIN;
    private volatile Pin KNOCK_PIN;
    private volatile Pin OUT_DOOR_PIN;
    private volatile GpioPinDigitalInput knockButton;
    private volatile GpioPinDigitalOutput outDoorPin;
    private volatile GpioPinDigitalOutput outDoorPin2;

    private volatile IOActionType lastAction = null;
    private final ScheduledExecutorService ses;

    public IOActionType getLastAction() {
        IOActionType ioat = lastAction;
        lastAction = null;
        return ioat;
    }

    public HandlingIO(ScheduledExecutorService ses) {
        logger.info("eddig 0");
        this.ses = ses;
        try {
            DOOR_PIN = RaspiPin.GPIO_00;
            KNOCK_PIN = RaspiPin.GPIO_02;
            OUT_DOOR_PIN = RaspiPin.GPIO_03;

            knockButton = GpioFactory.getInstance().provisionDigitalInputPin(KNOCK_PIN, PinPullResistance.PULL_UP);
            outDoorPin = GpioFactory.getInstance().provisionDigitalOutputPin(OUT_DOOR_PIN, "FINISHED", PinState.LOW);
            outDoorPin2 = GpioFactory.getInstance().provisionDigitalOutputPin(DOOR_PIN, "FINISHED2", PinState.LOW);

            outDoorPin.setShutdownOptions(true, PinState.LOW);
            outDoorPin2.setShutdownOptions(true, PinState.LOW);
            outDoorPin.low();
            outDoorPin2.low();
            logger.info("eddig 1");
            knockButton.setShutdownOptions(true);
            knockButton.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
                if (event.getPin().getPin().equals(KNOCK_PIN) && event.getState().equals(PinState.LOW) && lastAction == null) {
                    if (checkAfterAWhile() && checkAfterAWhile() && checkAfterAWhile()) {
                        logger.info("Pressed knock button");
                        lastAction = IOActionType.KnockPressed;
                    }
                }
            });
        } catch (Exception ex) {
            logger.error(ex);
        }
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
        return knockButton.getState().equals(PinState.LOW);
    }

    public void openDoor(final GameKnockSimple comp) {
        outDoorPin.high();
        outDoorPin2.high();
        ses.schedule(new Runnable() {

            @Override
            public void run() {
                outDoorPin.low();
                outDoorPin2.low();
                comp.completed(CompletedEvent.DoorOpened, null);
            }
        }, 5, TimeUnit.SECONDS);
    }

}
