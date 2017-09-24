package hu.finominfo;

import hu.finominfo.properties.Props;
import hu.finominfo.rnet.node.controller.Controller;
import hu.finominfo.rnet.node.servant.Servant;
import hu.finominfo.rnet.common.Interface;

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
            //TODO: 1 percenként send broadcast küldése
        } else {
            new Servant().run();
            //TODO: 5 percenként find servers to connect a tasks queue-ba tenni
        }
    }

}
