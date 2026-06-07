package com.workschedule;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int REQUEST_ADD_SHIFT = 1;
    private static final int REQUEST_EDIT_SHIFT = 2;

    private ShiftStorage storage;
    private ShiftAdapter adapter;

    private TextView tvMonthYear;
    private TextView tvTotalHours;
    private ListView listShifts;
    private TextView tvEmptyList;

    private Calendar currentWeekStart;
    private Calendar selectedDate;

    // Day layout containers and text views
    private LinearLayout[] dayLayouts;
    private TextView[] dayNumViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = new ShiftStorage(this);

        // Initialize views
        tvMonthYear = (TextView) findViewById(R.id.tvMonthYear);
        tvTotalHours = (TextView) findViewById(R.id.tvTotalHours);
        listShifts = (ListView) findViewById(R.id.listShifts);
        tvEmptyList = (TextView) findViewById(R.id.tvEmptyList);

        // Week navigation
        findViewById(R.id.btnPrevWeek).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
                updateWeekView();
            }
        });

        findViewById(R.id.btnNextWeek).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
                updateWeekView();
            }
        });

        // Today button
        findViewById(R.id.btnToday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToToday();
            }
        });

        // FAB
        findViewById(R.id.fabAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddShift();
            }
        });

        // Init day layouts
        dayLayouts = new LinearLayout[7];
        dayNumViews = new TextView[7];

        dayLayouts[0] = (LinearLayout) findViewById(R.id.dayMon);
        dayLayouts[1] = (LinearLayout) findViewById(R.id.dayTue);
        dayLayouts[2] = (LinearLayout) findViewById(R.id.dayWed);
        dayLayouts[3] = (LinearLayout) findViewById(R.id.dayThu);
        dayLayouts[4] = (LinearLayout) findViewById(R.id.dayFri);
        dayLayouts[5] = (LinearLayout) findViewById(R.id.daySat);
        dayLayouts[6] = (LinearLayout) findViewById(R.id.daySun);

        dayNumViews[0] = (TextView) findViewById(R.id.tvMonNum);
        dayNumViews[1] = (TextView) findViewById(R.id.tvTueNum);
        dayNumViews[2] = (TextView) findViewById(R.id.tvWedNum);
        dayNumViews[3] = (TextView) findViewById(R.id.tvThuNum);
        dayNumViews[4] = (TextView) findViewById(R.id.tvFriNum);
        dayNumViews[5] = (TextView) findViewById(R.id.tvSatNum);
        dayNumViews[6] = (TextView) findViewById(R.id.tvSunNum);

        // Set click listeners for day views
        for (int i = 0; i < 7; i++) {
            final int dayIndex = i;
            dayLayouts[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar day = (Calendar) currentWeekStart.clone();
                    day.add(Calendar.DAY_OF_WEEK, dayIndex);
                    selectedDate = day;
                    updateDaySelection();
                    loadShiftsForSelectedDay();
                }
            });
        }

        // List item click
        listShifts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Shift shift = (Shift) adapter.getItem(position);
                openEditShift(shift);
            }
        });

        // Init calendar
        goToToday();
    }

    private void goToToday() {
        Calendar today = Calendar.getInstance();
        selectedDate = (Calendar) today.clone();

        // Set current week start to Monday
        currentWeekStart = (Calendar) today.clone();
        int dow = currentWeekStart.get(Calendar.DAY_OF_WEEK);
        int daysToMon = (dow == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dow;
        currentWeekStart.add(Calendar.DAY_OF_MONTH, daysToMon);

        updateWeekView();
    }

    private void updateWeekView() {
        // Update month/year title
        Calendar weekEnd = (Calendar) currentWeekStart.clone();
        weekEnd.add(Calendar.DAY_OF_MONTH, 6);

        SimpleDateFormat monthFmt = new SimpleDateFormat("LLLL yyyy", new Locale("ru"));
        String startMonth = monthFmt.format(currentWeekStart.getTime());
        String endMonth = monthFmt.format(weekEnd.getTime());

        if (startMonth.equals(endMonth)) {
            tvMonthYear.setText(capitalize(startMonth));
        } else {
            SimpleDateFormat shortMonthFmt = new SimpleDateFormat("LLL", new Locale("ru"));
            tvMonthYear.setText(capitalize(shortMonthFmt.format(currentWeekStart.getTime())) +
                    " – " + capitalize(shortMonthFmt.format(weekEnd.getTime())) +
                    " " + currentWeekStart.get(Calendar.YEAR));
        }

        // Update day numbers
        Calendar day = (Calendar) currentWeekStart.clone();
        Calendar today = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            int dayNum = day.get(Calendar.DAY_OF_MONTH);
            dayNumViews[i].setText(String.valueOf(dayNum));

            boolean isToday = sameDay(day, today);
            boolean isSelected = sameDay(day, selectedDate);
            boolean hasShift = storage.hasShiftOnDate(
                    day.get(Calendar.YEAR),
                    day.get(Calendar.MONTH),
                    day.get(Calendar.DAY_OF_MONTH));

            // Style the day circle
            if (isSelected) {
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(Color.WHITE);
                dayNumViews[i].setBackground(circle);
                dayNumViews[i].setTextColor(Color.parseColor("#1976D2"));
                dayNumViews[i].setTypeface(null, Typeface.BOLD);
            } else if (isToday) {
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(Color.parseColor("#33FFFFFF"));
                dayNumViews[i].setBackground(circle);
                dayNumViews[i].setTextColor(Color.WHITE);
                dayNumViews[i].setTypeface(null, Typeface.BOLD);
            } else {
                dayNumViews[i].setBackground(null);
                // Weekend colors
                if (i >= 5) {
                    dayNumViews[i].setTextColor(Color.parseColor("#FFCDD2"));
                } else {
                    dayNumViews[i].setTextColor(Color.WHITE);
                }
                dayNumViews[i].setTypeface(null, Typeface.NORMAL);
            }

            // Show dot indicator if has shift
            // (we indicate via bold/italic of the day name)

            day.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Update stats
        int year = currentWeekStart.get(Calendar.YEAR);
        int month = currentWeekStart.get(Calendar.MONTH);
        double totalHours = storage.getTotalHoursForMonth(year, month);
        int h = (int) totalHours;
        int m = (int) ((totalHours - h) * 60);
        String hoursStr = m > 0 ? h + " ч " + m + " м" : h + " ч";
        SimpleDateFormat mfmt = new SimpleDateFormat("LLLL", new Locale("ru"));
        tvTotalHours.setText(capitalize(mfmt.format(currentWeekStart.getTime())) + ": " + hoursStr);

        // Reload shifts for selected day
        loadShiftsForSelectedDay();
    }

    private void updateDaySelection() {
        Calendar day = (Calendar) currentWeekStart.clone();
        Calendar today = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            boolean isToday = sameDay(day, today);
            boolean isSelected = sameDay(day, selectedDate);

            if (isSelected) {
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(Color.WHITE);
                dayNumViews[i].setBackground(circle);
                dayNumViews[i].setTextColor(Color.parseColor("#1976D2"));
                dayNumViews[i].setTypeface(null, Typeface.BOLD);
            } else if (isToday) {
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(Color.parseColor("#33FFFFFF"));
                dayNumViews[i].setBackground(circle);
                dayNumViews[i].setTextColor(Color.WHITE);
                dayNumViews[i].setTypeface(null, Typeface.BOLD);
            } else {
                dayNumViews[i].setBackground(null);
                if (i >= 5) {
                    dayNumViews[i].setTextColor(Color.parseColor("#FFCDD2"));
                } else {
                    dayNumViews[i].setTextColor(Color.WHITE);
                }
                dayNumViews[i].setTypeface(null, Typeface.NORMAL);
            }

            day.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void loadShiftsForSelectedDay() {
        if (selectedDate == null) return;

        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        List<Shift> shifts = storage.getShiftsForDate(year, month, day);

        if (adapter == null) {
            adapter = new ShiftAdapter(this, shifts);
            listShifts.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(shifts);
            adapter.notifyDataSetChanged();
        }

        if (shifts.isEmpty()) {
            listShifts.setVisibility(View.GONE);
            tvEmptyList.setVisibility(View.VISIBLE);
        } else {
            listShifts.setVisibility(View.VISIBLE);
            tvEmptyList.setVisibility(View.GONE);
        }

        // Update action bar title with selected date
        SimpleDateFormat fmt = new SimpleDateFormat("d MMMM", new Locale("ru"));
        getActionBar().setTitle(capitalize(fmt.format(selectedDate.getTime())));
    }

    private void openAddShift() {
        Intent intent = new Intent(this, AddShiftActivity.class);
        if (selectedDate != null) {
            intent.putExtra("year", selectedDate.get(Calendar.YEAR));
            intent.putExtra("month", selectedDate.get(Calendar.MONTH));
            intent.putExtra("day", selectedDate.get(Calendar.DAY_OF_MONTH));
        }
        startActivityForResult(intent, REQUEST_ADD_SHIFT);
    }

    private void openEditShift(Shift shift) {
        Intent intent = new Intent(this, AddShiftActivity.class);
        intent.putExtra("shift", shift);
        startActivityForResult(intent, REQUEST_EDIT_SHIFT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            updateWeekView();
        }
    }

    private boolean sameDay(Calendar a, Calendar b) {
        if (a == null || b == null) return false;
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.MONTH) == b.get(Calendar.MONTH) &&
                a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
