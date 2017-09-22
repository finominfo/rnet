package hu.finominfo;

import hu.finominfo.common.Props;
import hu.finominfo.node.controller.Controller;
import hu.finominfo.node.servant.Servant;
import hu.finominfo.rnet.communication.Interface;

import java.io.File;

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
            new Controller().run();
        } else {
            new Servant().run();
        }
    }
}
