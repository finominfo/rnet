package hu.finominfo.rnet;

import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.http.HttpServer;
import hu.finominfo.rnet.communication.tcp.events.message.MessageEvent;
import hu.finominfo.rnet.frontend.servant.counter.Counter;
import hu.finominfo.rnet.frontend.servant.gameknock.GameKnockSimple;
import hu.finominfo.rnet.frontend.servant.gameknock.io.HandlingIO;
import hu.finominfo.rnet.properties.Props;
import hu.finominfo.rnet.common.Globals;
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
        Interface.getInterfaces();
        if (Props.get().isController()) {
            Globals.get().addToFrontEndTasksIfNotExists(FrontEndTaskToDo.LOAD_NAME_ADDRESS);
            Globals.get().executor.schedule(new ControllerRepeater(), 5, TimeUnit.SECONDS);
            Globals.get().getFrontEnd();
            Controller controller = new Controller();
            controller.run();
            Globals.get().controller = controller;
            new FrontEndWorker().run();
        } else {
            Globals.get().executor.schedule(() -> {
                Counter.createAndShowGui();
                Globals.get().executor.schedule(() -> new HttpServer().start(), 3, TimeUnit.SECONDS);
                Globals.get().executor.schedule(new ServantRepeater(), 15, TimeUnit.SECONDS);
                Servant servant = new Servant();
                Globals.get().servant = servant;
                Globals.get().executor.schedule(servant, 2, TimeUnit.SECONDS);
                MessageEvent messageEvent = new MessageEvent("RNET servant version " + Globals.getVersion(), 3);
                Globals.get().executor.schedule(() -> Utils.showMessage(messageEvent), 2, TimeUnit.SECONDS);
                try {
                    HandlingIO handlingIO = new HandlingIO(Globals.get().executor);
                    Globals.get().executor.submit(new GameKnockSimple(handlingIO));
                } catch (Exception e) {
                    logger.error(Utils.getStackTrace(e));
                }
            }, 2, TimeUnit.SECONDS);
        }
    }

}
