package hu.finominfo.rnet.frontend.servant.gameknock.io;

import com.pi4j.io.gpio.GpioController;
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
 *
 * @author User
 */
public class HandlingIO {

    private final static Logger logger = Logger.getLogger(HandlingIO.class);

    private static final Pin DOOR_PIN = RaspiPin.GPIO_00;
    private static final Pin KNOCK_PIN = RaspiPin.GPIO_02;
    private static final Pin OUT_DOOR_PIN = RaspiPin.GPIO_03;
    private final GpioPinDigitalInput knockButton = GpioFactory.getInstance().provisionDigitalInputPin(KNOCK_PIN, PinPullResistance.PULL_UP);
    private final GpioPinDigitalOutput outDoorPin = GpioFactory.getInstance().provisionDigitalOutputPin(OUT_DOOR_PIN, "FINISHED", PinState.LOW);
    private final GpioPinDigitalOutput outDoorPin2 = GpioFactory.getInstance().provisionDigitalOutputPin(DOOR_PIN, "FINISHED2", PinState.LOW);

    private volatile IOActionType lastAction = null;
    private final ScheduledExecutorService ses;

    public IOActionType getLastAction() {
        IOActionType ioat = lastAction;
        lastAction = null;
        return ioat;
    }

    public HandlingIO(ScheduledExecutorService ses) {
        outDoorPin.setShutdownOptions(true, PinState.LOW);
        outDoorPin2.setShutdownOptions(true, PinState.LOW);
        outDoorPin.low();
        outDoorPin2.low();
        this.ses = ses;
        knockButton.setShutdownOptions(true);
        knockButton.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
            if (event.getPin().getPin().equals(KNOCK_PIN) && event.getState().equals(PinState.LOW) && lastAction == null) {
                if (checkAfterAWhile() && checkAfterAWhile() && checkAfterAWhile()) {
                    logger.info("Pressed knock button");
                    lastAction = IOActionType.KnockPressed;
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
