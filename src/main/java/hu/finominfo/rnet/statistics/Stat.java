package hu.finominfo.rnet.statistics;

import hu.finominfo.rnet.database.H2KeyValue;

import java.time.LocalDateTime;

/**
 * Created by kalman.kovacs@gmail.com on 2017.12.02.
 */
public class Stat {
    private static Stat ourInstance = new Stat();

    public static Stat getInstance() {
        return ourInstance;
    }

    public StringBuilder lastStat = new StringBuilder();

    private volatile int currentMonthSum = 0;
    private volatile int previousMonthSum = 0;

    public void init() {
        LocalDateTime dateTime = LocalDateTime.now();
        int currentMonth = dateTime.getMonthValue();
        int previousMonth = LocalDateTime.now().minusMonths(1).getMonthValue();
        boolean dataExists = true;
        while ((dateTime.getMonthValue() == currentMonth || dateTime.getMonthValue() == previousMonth) && dataExists) {
            int before = dateTime.getMonthValue();
            dateTime.minusDays(1);
            int after = dateTime.getMonthValue();
            if (before != after) {
                dateTime.plusDays(1);
                lastStat.append("\nSUM: ").append(Clicker.getYearMonth(dateTime)).append(" ")
                        .append(before == currentMonth ? currentMonthSum : previousMonthSum)
                        .append("\n");
                dateTime.minusDays(1);
            }
            String date = Clicker.getName(dateTime);
            String value = H2KeyValue.getValue(date);
            if (null == value) {
                dataExists = false;
                lastStat.append("\nSUM: ").append(Clicker.getYearMonth(dateTime)).append(" ")
                        .append(before == currentMonth ? currentMonthSum : previousMonthSum)
                        .append("\n");
            } else {
                if (dateTime.getMonthValue() == currentMonth) {
                    currentMonthSum += Integer.valueOf(value);
                }
                if (dateTime.getMonthValue() == previousMonth) {
                    previousMonthSum += Integer.valueOf(value);
                }
                lastStat.append("\n").append(date).append(" ").append(value);
            }
        }
    }


}
