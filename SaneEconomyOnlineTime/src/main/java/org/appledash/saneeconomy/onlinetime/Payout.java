package org.appledash.saneeconomy.onlinetime;

import java.util.Map;

/**
 * Created by appledash on 7/13/17.
 * Blackjack is best pony.
 */
public class Payout {
    private final int secondsInterval;
    private final double amount;
    private final String message;
    private String permission;
    private long reportInterval;

    public Payout(int secondsInterval, double amount, String message, long reportInterval) {
        this.secondsInterval = secondsInterval;
        this.amount = amount;
        this.message = message;
        this.reportInterval = reportInterval;
    }

    public int getSecondsInterval() {
        return secondsInterval;
    }

    public double getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }

    public static Payout fromConfigMap(Map<?, ?> values) {
        return new Payout(Integer.valueOf(String.valueOf(values.get("seconds"))), Double.valueOf(String.valueOf(values.get("amount"))), String.valueOf(values.get("message")), Long.valueOf(String.valueOf(values.get("report_interval"))));
    }

    public String getPermission() {
        return permission;
    }

    public long getReportInterval() {
        return reportInterval;
    }
}
