package hu.finominfo.rnet.communication.udp;

import hu.finominfo.common.Globals;
import hu.finominfo.node.CompletedEvent;
import hu.finominfo.rnet.communication.udp.out.ConnectionBroadcaster;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21.
 */
public class Broadcaster implements ChannelFutureListener, Runnable{

    private final static Logger logger = Logger.getLogger(Broadcaster.class);
    private final int port;
    private final AtomicInteger times;
    private final AtomicInteger numOfSuccessfulSending;
    private final AtomicInteger faultTimes;
    private final AtomicInteger waitingInSeconds;
    private final ConnectionBroadcaster broadcaster;
    private volatile CompletionHandler<CompletedEvent, Integer> completionHandler;

    public Broadcaster(int port) {
        this.port = port;
        this.times = new AtomicInteger();
        this.faultTimes = new AtomicInteger();
        this.waitingInSeconds = new AtomicInteger();
        this.numOfSuccessfulSending = new AtomicInteger();
        broadcaster = new ConnectionBroadcaster(port);
    }

    public void start(int times, int waitingInSeconds, final CompletionHandler<CompletedEvent, Integer> completionHandler) {
        this.times.set(times);
        this.faultTimes.set(times);
        this.waitingInSeconds.set(waitingInSeconds);
        this.numOfSuccessfulSending.set(0);
        this.completionHandler = completionHandler;
        run();
    }

    @Override
    public void run() {
        try {
            if (times.decrementAndGet() >= 0) {
                logger.info("Sending broadcast...");
                broadcaster.send(this);
            } else if (null != completionHandler) {
                logger.info("Broadcasting finished, number of successful sending: " + numOfSuccessfulSending.get());
                completionHandler.completed(CompletedEvent.BROADCAST_FINISHED, numOfSuccessfulSending.get());
            }
        } catch (Exception e) {
            logger.error(e);
            if (faultTimes.decrementAndGet() >= 0) {
                times.incrementAndGet();
            }
            Globals.get().executor.schedule(this, waitingInSeconds.get(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            logger.error("Broadcast sending was unsuccessful");
            if (faultTimes.decrementAndGet() >= 0) {
                times.incrementAndGet();
            }
        } else {
            numOfSuccessfulSending.incrementAndGet();
        }
        Globals.get().executor.schedule(this, waitingInSeconds.get(), TimeUnit.SECONDS);
    }

    public void stop() {
        broadcaster.stop();
    }

}
