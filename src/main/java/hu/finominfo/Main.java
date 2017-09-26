package hu.finominfo;

import hu.finominfo.properties.Props;
import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.frontend.FrontEndWorker;
import hu.finominfo.rnet.node.controller.Controller;
import hu.finominfo.rnet.taskqueue.ControllerRepeater;
import hu.finominfo.rnet.node.servant.Servant;
import hu.finominfo.rnet.common.Interface;
import hu.finominfo.rnet.taskqueue.TaskToDo;

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
            Globals.get().executor.schedule(new ControllerRepeater(), 5, TimeUnit.SECONDS);
            Globals.get().getFrontEnd();
            new Controller().run();
            new FrontEndWorker().run();
            Globals.get().addToFrontEndTasksIfNotExists(TaskToDo.LOAD_NAME_ADDRESS);


        } else {
            new Servant().run();
        }
    }

}
