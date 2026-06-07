package com.workschedule;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ShiftStorage {

    private static final String PREFS_NAME = "shifts_data";
    private static final String KEY_SHIFTS = "shifts";
    private static final String SEPARATOR = "|||";

    private final SharedPreferences prefs;

    public ShiftStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveShift(Shift shift) {
        List<Shift> shifts = getAllShifts();
        // Update existing or add new
        boolean found = false;
        for (int i = 0; i < shifts.size(); i++) {
            if (shifts.get(i).getId() == shift.getId()) {
                shifts.set(i, shift);
                found = true;
                break;
            }
        }
        if (!found) {
            shifts.add(shift);
        }
        persistShifts(shifts);
    }

    public void deleteShift(long shiftId) {
        List<Shift> shifts = getAllShifts();
        for (int i = 0; i < shifts.size(); i++) {
            if (shifts.get(i).getId() == shiftId) {
                shifts.remove(i);
                break;
            }
        }
        persistShifts(shifts);
    }

    public List<Shift> getAllShifts() {
        String data = prefs.getString(KEY_SHIFTS, "");
        List<Shift> shifts = new ArrayList<>();
        if (data == null || data.isEmpty()) return shifts;
        String[] parts = data.split("\\|\\|\\|");
        for (String part : parts) {
            if (!part.isEmpty()) {
                Shift shift = Shift.fromJson(part);
                if (shift != null) shifts.add(shift);
            }
        }
        return shifts;
    }

    public List<Shift> getShiftsForDate(int year, int month, int day) {
        List<Shift> all = getAllShifts();
        List<Shift> result = new ArrayList<>();
        for (Shift shift : all) {
            if (shift.getYear() == year && shift.getMonth() == month && shift.getDay() == day) {
                result.add(shift);
            }
        }
        return result;
    }

    public List<Shift> getShiftsForMonth(int year, int month) {
        List<Shift> all = getAllShifts();
        List<Shift> result = new ArrayList<>();
        for (Shift shift : all) {
            if (shift.getYear() == year && shift.getMonth() == month) {
                result.add(shift);
            }
        }
        return result;
    }

    public double getTotalHoursForMonth(int year, int month) {
        List<Shift> shifts = getShiftsForMonth(year, month);
        double total = 0;
        for (Shift shift : shifts) {
            if (shift.getType() == Shift.TYPE_WORK) {
                total += shift.getDurationHours();
            }
        }
        return total;
    }

    public boolean hasShiftOnDate(int year, int month, int day) {
        List<Shift> shifts = getShiftsForDate(year, month, day);
        return !shifts.isEmpty();
    }

    public int getShiftTypeForDate(int year, int month, int day) {
        List<Shift> shifts = getShiftsForDate(year, month, day);
        if (shifts.isEmpty()) return -1;
        return shifts.get(0).getType();
    }

    private void persistShifts(List<Shift> shifts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < shifts.size(); i++) {
            if (i > 0) sb.append(SEPARATOR);
            sb.append(shifts.get(i).toJson());
        }
        prefs.edit().putString(KEY_SHIFTS, sb.toString()).apply();
    }
}
