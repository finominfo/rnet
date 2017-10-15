package hu.finominfo.rnet;

import hu.finominfo.rnet.frontend.servant.counter.Counter;
import hu.finominfo.rnet.frontend.servant.gameknock.GameKnockSimple;
import hu.finominfo.rnet.properties.Props;
import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.taskqueue.FrontEndWorker;
import hu.finominfo.rnet.node.controller.Controller;
import hu.finominfo.rnet.taskqueue.ControllerRepeater;
import hu.finominfo.rnet.node.servant.Servant;
import hu.finominfo.rnet.common.Interface;
import hu.finominfo.rnet.taskqueue.FrontEndTaskToDo;
import hu.finominfo.rnet.taskqueue.ServantRepeater;

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
        Interface.getInterfaces();
        if (Props.get().isController()) {
            Globals.get().addToFrontEndTasksIfNotExists(FrontEndTaskToDo.LOAD_NAME_ADDRESS);
            Globals.get().executor.schedule(new ControllerRepeater(), 5, TimeUnit.SECONDS);
            Globals.get().getFrontEnd();
            Controller controller = new Controller();
            controller.run();
            Globals.get().controller = controller;
            new FrontEndWorker().run();
            /*Globals.get().executor.schedule(()
                    -> Globals.get().addToTasksIfNotExists(TaskToDo.SEND_FILE, "Red.avi", FileType.VIDEO,"192.168.0.111"),
                    15, TimeUnit.SECONDS);*/
        } else {
            Globals.get().executor.schedule(() ->
            SwingUtilities.invokeLater(() -> {
                Counter.createAndShowGui();
                Globals.get().executor.schedule(new ServantRepeater(), 15, TimeUnit.SECONDS);
                Servant servant = new Servant();
                servant.run();
                Globals.get().servant = servant;
                Globals.get().executor.submit(new GameKnockSimple());
            }), 3, TimeUnit.SECONDS);
        }
    }

}
