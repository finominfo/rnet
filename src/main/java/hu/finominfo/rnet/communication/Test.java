package hu.finominfo.rnet.communication;

import hu.finominfo.rnet.communication.udp.out.ConnectionBroadcaster;
import hu.finominfo.rnet.communication.udp.in.ConnectionMonitor;

import java.net.UnknownHostException;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.17..
 */
public class Test {

    public static void main(String[] args) throws UnknownHostException {
        Interface.getInterfaces();
        ConnectionMonitor monitor = new ConnectionMonitor(10000);
        monitor.bind().addListener(future -> {
            if (future.isSuccess()) {
                ConnectionBroadcaster broadcaster = new ConnectionBroadcaster(10000);
                broadcaster.send(future1 -> {
                    if (future1.isSuccess()) {
                        broadcaster.send(future2 -> {
                            if (future2.isSuccess()) {
                                broadcaster.stop();
                                monitor.stop();
                            } else {
                                System.out.println("Future2 not success!!!");
                                broadcaster.stop();
                                monitor.stop();
                            }
                        });
                    } else {
                        System.out.println("Future1 not success!!!");
                        broadcaster.stop();
                        monitor.stop();
                    }
                });
            } else {
                System.out.println("Future not success!!!");
                monitor.stop();
            }
        });
    }
}
