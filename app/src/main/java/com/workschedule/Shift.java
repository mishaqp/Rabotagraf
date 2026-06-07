package com.workschedule;

import java.io.Serializable;

public class Shift implements Serializable {

    public static final int TYPE_WORK = 0;
    public static final int TYPE_DAYOFF = 1;
    public static final int TYPE_VACATION = 2;
    public static final int TYPE_SICK = 3;

    private long id;
    private int year;
    private int month;
    private int day;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private int type;
    private String note;

    public Shift() {
        this.id = System.currentTimeMillis();
        this.type = TYPE_WORK;
        this.note = "";
    }

    public Shift(int year, int month, int day, int startHour, int startMinute,
                 int endHour, int endMinute, int type, String note) {
        this.id = System.currentTimeMillis();
        this.year = year;
        this.month = month;
        this.day = day;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.type = type;
        this.note = note;
    }

    public double getDurationHours() {
        int startMins = startHour * 60 + startMinute;
        int endMins = endHour * 60 + endMinute;
        if (endMins < startMins) endMins += 24 * 60; // overnight shift
        return (endMins - startMins) / 60.0;
    }

    public String getFormattedStartTime() {
        return String.format("%02d:%02d", startHour, startMinute);
    }

    public String getFormattedEndTime() {
        return String.format("%02d:%02d", endHour, endMinute);
    }

    public String getFormattedDuration() {
        double hours = getDurationHours();
        int h = (int) hours;
        int m = (int) ((hours - h) * 60);
        if (m == 0) return h + " ч";
        return h + " ч " + m + " м";
    }

    public String toJson() {
        return "{\"id\":" + id +
                ",\"year\":" + year +
                ",\"month\":" + month +
                ",\"day\":" + day +
                ",\"startHour\":" + startHour +
                ",\"startMinute\":" + startMinute +
                ",\"endHour\":" + endHour +
                ",\"endMinute\":" + endMinute +
                ",\"type\":" + type +
                ",\"note\":\"" + escapeJson(note) + "\"}";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    public static Shift fromJson(String json) {
        try {
            Shift shift = new Shift();
            shift.id = parseLong(json, "id");
            shift.year = parseInt(json, "year");
            shift.month = parseInt(json, "month");
            shift.day = parseInt(json, "day");
            shift.startHour = parseInt(json, "startHour");
            shift.startMinute = parseInt(json, "startMinute");
            shift.endHour = parseInt(json, "endHour");
            shift.endMinute = parseInt(json, "endMinute");
            shift.type = parseInt(json, "type");
            shift.note = parseString(json, "note");
            return shift;
        } catch (Exception e) {
            return null;
        }
    }

    private static int parseInt(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return 0;
        idx += search.length();
        int end = idx;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try {
            return Integer.parseInt(json.substring(idx, end));
        } catch (Exception e) {
            return 0;
        }
    }

    private static long parseLong(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return 0;
        idx += search.length();
        int end = idx;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try {
            return Long.parseLong(json.substring(idx, end));
        } catch (Exception e) {
            return 0;
        }
    }

    private static String parseString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        idx += search.length();
        StringBuilder sb = new StringBuilder();
        while (idx < json.length()) {
            char c = json.charAt(idx);
            if (c == '\\' && idx + 1 < json.length()) {
                char next = json.charAt(idx + 1);
                if (next == '"') { sb.append('"'); idx += 2; continue; }
                if (next == '\\') { sb.append('\\'); idx += 2; continue; }
                if (next == 'n') { sb.append('\n'); idx += 2; continue; }
            }
            if (c == '"') break;
            sb.append(c);
            idx++;
        }
        return sb.toString();
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }
    public int getStartHour() { return startHour; }
    public void setStartHour(int startHour) { this.startHour = startHour; }
    public int getStartMinute() { return startMinute; }
    public void setStartMinute(int startMinute) { this.startMinute = startMinute; }
    public int getEndHour() { return endHour; }
    public void setEndHour(int endHour) { this.endHour = endHour; }
    public int getEndMinute() { return endMinute; }
    public void setEndMinute(int endMinute) { this.endMinute = endMinute; }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
