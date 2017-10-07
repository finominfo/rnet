package hu.finominfo;

import hu.finominfo.properties.Props;
import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.taskqueue.FrontEndWorker;
import hu.finominfo.rnet.node.controller.Controller;
import hu.finominfo.rnet.taskqueue.ControllerRepeater;
import hu.finominfo.rnet.node.servant.Servant;
import hu.finominfo.rnet.common.Interface;
import hu.finominfo.rnet.taskqueue.FrontEndTaskToDo;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class Main {
    private static void setupLog4J(){
        try {
            System.setProperty("log4j.configuration", new File(".", File.separatorChar+"log4j.properties").toURI().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String [] args) {
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
            Servant servant = new Servant();
            servant.run();
            Globals.get().servant = servant;
        }
    }

}
