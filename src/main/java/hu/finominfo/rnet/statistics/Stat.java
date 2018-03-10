package hu.finominfo.rnet.statistics;

import hu.finominfo.rnet.common.Interface;
import hu.finominfo.rnet.database.H2KeyValue;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Created by kalman.kovacs@gmail.com on 2017.12.02.
 */
public class Stat {
    private static Stat ourInstance = new Stat();

    public static Stat getInstance() {
        return ourInstance;
    }

    public StringBuilder initStat = new StringBuilder();

    private final DecimalFormat df = new DecimalFormat("00");

    public void init() {
        int currentMonthSum = 0;
        int previousMonthSum = 0;
        LocalDateTime dateTime = LocalDateTime.now();
        int currentMonth = dateTime.getMonthValue();
        int previousMonth = LocalDateTime.now().minusMonths(1).getMonthValue();
        while (dateTime.getMonthValue() == currentMonth || dateTime.getMonthValue() == previousMonth) {
            int before = dateTime.getMonthValue();
            String beforeYearMonth = getYearMonth(dateTime);
            dateTime = dateTime.minusDays(1);
            int after = dateTime.getMonthValue();
            if (before != after) {
                initStat.append("\nSUM: ").append(beforeYearMonth).append(" ")
                        .append(before == currentMonth ? currentMonthSum : previousMonthSum)
                        .append("\n");
            }
            if (after == currentMonth || after == previousMonth) {
                String date = getName(dateTime);
                String value = H2KeyValue.getValue(date);
                if (null == value) {
                    value = "0";
                }
                if (dateTime.getMonthValue() == currentMonth) {
                    currentMonthSum += Integer.valueOf(value);
                }
                if (dateTime.getMonthValue() == previousMonth) {
                    previousMonthSum += Integer.valueOf(value);
                }
                initStat.append("\n").append(date).append(" ").append(value);
            }
        }
    }

    public static String get() {
        StringBuilder stat = new StringBuilder();
        stat.append("\nIP addresseso: ").append(list()).append("\n");
        stat.append("\nMAC: ").append(Long.toHexString(Interface.addresses.get(0)));
        stat.append("\nDefault video: ").append(H2KeyValue.getValue(H2KeyValue.DEF_VIDEO));
        stat.append("\nDefault audio: ").append(H2KeyValue.getValue(H2KeyValue.DEF_AUDIO)).append("\n");
        if (!Clicker.get().getToday().isEmpty()) {
            stat.append("\nToday: ").append(Clicker.get().getToday()).append("\n");
        }
        stat.append(getInstance().initStat);
        return stat.toString();
    }

    public static StringBuilder list() {
        StringBuilder result = new StringBuilder();
        Enumeration<NetworkInterface> nets = null;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                displayInterfaceInformation(netint, result);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return result;
    }

    static void displayInterfaceInformation(NetworkInterface netint, StringBuilder result) throws SocketException {
        result.append("Display name:").append(netint.getDisplayName()).append(System.lineSeparator());
        result.append("Name:").append(netint.getName()).append(System.lineSeparator());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            result.append("InetAddress:").append(inetAddress).append(System.lineSeparator());
        }
        result.append(System.lineSeparator());
    }

    public String getName(LocalDateTime dateTime) {
        return String.valueOf(dateTime.getYear()).substring(2) + df.format(dateTime.getMonthValue()) + df.format(dateTime.getDayOfMonth());
    }

    public String getYearMonth(LocalDateTime dateTime) {
        String year = String.valueOf(dateTime.getYear()).substring(2);
        String month = df.format(dateTime.getMonthValue());
        return year + month;
    }

}
