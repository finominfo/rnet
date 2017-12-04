package hu.finominfo.rnet.statistics;

import hu.finominfo.rnet.common.Interface;
import hu.finominfo.rnet.database.H2KeyValue;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

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
        stat.append("\nMAC: ").append(Long.toHexString(Interface.addresses.get(0)));
        stat.append("\nDefault video: ").append(H2KeyValue.getValue(H2KeyValue.DEF_VIDEO));
        stat.append("\nDefault audio: ").append(H2KeyValue.getValue(H2KeyValue.DEF_AUDIO)).append("\n");
        if (!Clicker.get().getToday().isEmpty()) {
            stat.append("\nToday: ").append(Clicker.get().getToday()).append("\n");
        }
        stat.append(getInstance().initStat);
        return stat.toString();
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
