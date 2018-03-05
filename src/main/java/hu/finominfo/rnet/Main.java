package hu.finominfo.rnet;

import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.http.HttpServer;
import hu.finominfo.rnet.database.H2KeyValue;
import hu.finominfo.rnet.frontend.servant.common.MessageDisplay;
import hu.finominfo.rnet.frontend.servant.counter.Counter;
import hu.finominfo.rnet.frontend.servant.gameknock.GameKnockSimple;
import hu.finominfo.rnet.frontend.servant.gameknock.io.HandlingIO;
import hu.finominfo.rnet.frontend.counteronly.GameFrontEnd;
import hu.finominfo.rnet.properties.Props;
import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.statistics.SendMail;
import hu.finominfo.rnet.statistics.Stat;
import hu.finominfo.rnet.taskqueue.FrontEndWorker;
import hu.finominfo.rnet.node.controller.Controller;
import hu.finominfo.rnet.taskqueue.ControllerRepeater;
import hu.finominfo.rnet.node.servant.Servant;
import hu.finominfo.rnet.common.Interface;
import hu.finominfo.rnet.taskqueue.FrontEndTaskToDo;
import hu.finominfo.rnet.taskqueue.ServantRepeater;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.time.*;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class Main {
    private static void setupLog4J() {
        try {
            System.setProperty("log4j.configuration", new File(".", File.separatorChar + "log4j.properties").toURI().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        setupLog4J();
        Logger logger = Logger.getLogger(Main.class);
        if (RunningChecker.check()) {
            try {
                Interface.getInterfaces();
                switch (Props.get().getNodeType()) {
                    case CONTROLLER:
                        logger.info("Version: " + Globals.getVersion());
                        handleController();
                        break;
                    case SERVANT:
                        String rnet = Utils.convertStreamToString(Utils.simpleProcessCommand(500, "ps aux | grep [j]ava"));
                        int found = 0;
                        int i = 0;
                        while (i > -1 && i < rnet.length()) {
                            i = rnet.indexOf("rnet.java", i);
                            if (i > -1) {
                                found++;
                                i++;
                            }
                        }
                        if (found > 1) {
                            logger.warn("Rnet " + Globals.getVersion() + " is already running on this machine: " + rnet + " - found: " + found);
                            System.exit(0);
                        } else {
                            logger.info("Version: " + Globals.getVersion());
                            handleServant(logger);
                        }
                        break;
                    case COUNTER:
                        logger.info("Version: " + Globals.getVersion());
                        handleCounter(logger);
                        break;
                }
            } catch (Throwable t) {
                //logger.error("Error in Main() ", t);
                System.exit(0);
            }
        } else {
            logger.warn("Rnet " + Globals.getVersion() + " is already running on this machine.");
            System.exit(0);
        }
    }

    private static void handleCounter(Logger logger) {
        SwingUtilities.invokeLater(() -> {
            GameFrontEnd.createAndShowGui();
        });
        Globals.get().executor.schedule(() -> startGameKnock(logger), 2, TimeUnit.SECONDS);
    }

    private static void handleController() {
        Globals.get().addToFrontEndTasksIfNotExists(FrontEndTaskToDo.LOAD_NAME_ADDRESS);
        Globals.get().executor.schedule(new ControllerRepeater(), 5, TimeUnit.SECONDS);
        Globals.get().getFrontEnd();
        Controller controller = new Controller();
        controller.run();
        Globals.get().controller = controller;
        new FrontEndWorker().run();
    }

    private static void handleServant(Logger logger) {
        Globals.get().executor.schedule(() -> {
            Counter.createAndShowGui();
            Globals.get().executor.schedule(() -> new HttpServer().start(), 3, TimeUnit.SECONDS);
            Globals.get().executor.schedule(() -> Stat.getInstance().init(), 8, TimeUnit.SECONDS);

            Globals.get().executor.schedule(() -> {
                long lastSending = Long.valueOf(H2KeyValue.getValue(H2KeyValue.LAST_SENDING));
                LocalDateTime last = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSending), TimeZone.getDefault().toZoneId());
                LocalDateTime current = LocalDateTime.now();
                boolean shouldSend = (System.currentTimeMillis() < 2514764800000L && last.getDayOfMonth() != current.getDayOfMonth())
                        || last.getMonthValue() != current.getMonthValue();
                if (shouldSend) {
                    SendMail.send();
                }
            }, 12, TimeUnit.SECONDS);

            Globals.get().executor.schedule(new ServantRepeater(), 15, TimeUnit.SECONDS);
            Servant servant = new Servant();
            Globals.get().servant = servant;
            Globals.get().executor.schedule(servant, 2, TimeUnit.SECONDS);

            Globals.get().executor.schedule(() ->
                            MessageDisplay.get().show("RNET servant version " + Globals.getVersion(), 3),
                    2, TimeUnit.SECONDS);

            LocalDateTime localNow = LocalDateTime.now();
            ZoneId currentZone = ZoneId.systemDefault();
            ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
            ZonedDateTime zonedNext5;
            zonedNext5 = zonedNow.withHour(4).withMinute(0).withSecond(0);
            if (zonedNow.compareTo(zonedNext5) > 0) {
                zonedNext5 = zonedNext5.plusDays(1);
            }
            Duration duration = Duration.between(zonedNow, zonedNext5);
            final long initialDelay = ((duration.getSeconds()) / 60) + Interface.fromAddress;
            Globals.get().executor.schedule(() -> Utils.restartApplication(), initialDelay, TimeUnit.MINUTES);
            startGameKnock(logger);
            Globals.get().executor.schedule(() -> checkFaultyRestart(), 25, TimeUnit.SECONDS);
        }, 2, TimeUnit.SECONDS);
    }

    private static void checkFaultyRestart() {
        if ((!H2KeyValue.getValue(H2KeyValue.COUNTER_CURRENT_STATE).equals(H2KeyValue.COUNTER_FINISHED)) &&
                (!H2KeyValue.getValue(H2KeyValue.COUNTER_CURRENT_STATE).equals(H2KeyValue.getValue(H2KeyValue.COUNTER)))) {
            Utils.startCounterMusic();
        }
    }

    private static void startGameKnock(Logger logger) {
        try {
            HandlingIO handlingIO = new HandlingIO(Globals.get().executor);
            Globals.get().executor.submit(new GameKnockSimple(handlingIO));
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }

}
