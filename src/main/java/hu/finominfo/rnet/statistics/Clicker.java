package hu.finominfo.rnet.statistics;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.database.H2KeyValue;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.12.01.
 */
public class Clicker {

    private static Clicker ourInstance = new Clicker();
    private volatile String today = "";

    public static Clicker get() {
        return ourInstance;
    }

    public static void click() {
        ourInstance.incrementCounter();
    }

    public static int getTodayStat() {
        AtomicInteger atomicInteger = ourInstance.clicks.get(ourInstance.getCurrentName());
        return atomicInteger == null ? 0 : atomicInteger.get();
    }

    public Clicker() {
        String name = getCurrentName();
        String value = H2KeyValue.getValue(name);
        clicks.put(name, new AtomicInteger(Integer.valueOf(value == null ? "0" : value)));
    }

    private final DecimalFormat df = new DecimalFormat("00");

    private final ConcurrentMap<String, AtomicInteger> clicks = new ConcurrentHashMap<>();

    private final AtomicBoolean started = new AtomicBoolean(false);

    public String getToday() {
        return today;
    }

    private void incrementCounter() {
        if (started.compareAndSet(false, true)) {
            Globals.get().executor.schedule(() -> started.set(false), 10, TimeUnit.MINUTES);
            String name = getCurrentName();
            AtomicInteger counter = clicks.get(name);
            if (counter == null) {
                final AtomicInteger value = new AtomicInteger(0);
                counter = clicks.putIfAbsent(name, value);
                if (counter == null) {
                    counter = value;
                }
            }
            final int currentCounter = counter.incrementAndGet();
            today = name + " " + currentCounter;
            H2KeyValue.set(name, String.valueOf(currentCounter));
        }
    }

    public String getCurrentName() {
        return getName(LocalDateTime.now());
    }

    public String getName(LocalDateTime dateTime) {
        return String.valueOf(dateTime.getYear()).substring(2) + df.format(dateTime.getMonthValue()) + df.format(dateTime.getDayOfMonth());
    }

    public static void main(String[] args) {
        Clicker.click();
        System.out.println("OK");
    }
}
