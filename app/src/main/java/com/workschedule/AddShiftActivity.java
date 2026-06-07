package com.workschedule;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddShiftActivity extends Activity {

    private TextView tvDate;
    private TextView tvStartTime;
    private TextView tvEndTime;
    private Spinner spinnerType;
    private EditText etNote;
    private Button btnSave;
    private Button btnDelete;

    private ShiftStorage storage;
    private Shift editingShift;

    private int selectedYear, selectedMonth, selectedDay;
    private int startHour = 9, startMinute = 0;
    private int endHour = 18, endMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shift);

        storage = new ShiftStorage(this);

        tvDate = (TextView) findViewById(R.id.tvDate);
        tvStartTime = (TextView) findViewById(R.id.tvStartTime);
        tvEndTime = (TextView) findViewById(R.id.tvEndTime);
        spinnerType = (Spinner) findViewById(R.id.spinnerType);
        etNote = (EditText) findViewById(R.id.etNote);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnDelete = (Button) findViewById(R.id.btnDelete);

        // Setup type spinner
        String[] types = {
                getString(R.string.work_type_work),
                getString(R.string.work_type_dayoff),
                getString(R.string.work_type_vacation),
                getString(R.string.work_type_sick)
        };
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Check if editing existing shift
        editingShift = (Shift) getIntent().getSerializableExtra("shift");
        if (editingShift != null) {
            setTitle(R.string.edit_shift);
            selectedYear = editingShift.getYear();
            selectedMonth = editingShift.getMonth();
            selectedDay = editingShift.getDay();
            startHour = editingShift.getStartHour();
            startMinute = editingShift.getStartMinute();
            endHour = editingShift.getEndHour();
            endMinute = editingShift.getEndMinute();
            spinnerType.setSelection(editingShift.getType());
            if (editingShift.getNote() != null) {
                etNote.setText(editingShift.getNote());
            }
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            setTitle(R.string.add_shift);
            // Get date from intent or use today
            Calendar today = Calendar.getInstance();
            selectedYear = getIntent().getIntExtra("year", today.get(Calendar.YEAR));
            selectedMonth = getIntent().getIntExtra("month", today.get(Calendar.MONTH));
            selectedDay = getIntent().getIntExtra("day", today.get(Calendar.DAY_OF_MONTH));
        }

        updateDateDisplay();
        updateTimeDisplay();

        // Date picker
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Time pickers
        tvStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(true);
            }
        });

        tvEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(false);
            }
        });

        // Save button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveShift();
            }
        });

        // Delete button
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        // Back button
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedYear = year;
                        selectedMonth = month;
                        selectedDay = dayOfMonth;
                        updateDateDisplay();
                    }
                }, selectedYear, selectedMonth, selectedDay);
        dialog.show();
    }

    private void showTimePicker(final boolean isStart) {
        int hour = isStart ? startHour : endHour;
        int minute = isStart ? startMinute : endMinute;

        TimePickerDialog dialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (isStart) {
                            startHour = hourOfDay;
                            startMinute = minute;
                        } else {
                            endHour = hourOfDay;
                            endMinute = minute;
                        }
                        updateTimeDisplay();
                    }
                }, hour, minute, true);
        dialog.show();
    }

    private void updateDateDisplay() {
        Calendar cal = Calendar.getInstance();
        cal.set(selectedYear, selectedMonth, selectedDay);
        SimpleDateFormat fmt = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("ru"));
        String dateStr = fmt.format(cal.getTime());
        tvDate.setText(capitalize(dateStr));
    }

    private void updateTimeDisplay() {
        tvStartTime.setText(String.format("%02d:%02d", startHour, startMinute));
        tvEndTime.setText(String.format("%02d:%02d", endHour, endMinute));
    }

    private void saveShift() {
        int type = spinnerType.getSelectedItemPosition();
        String note = etNote.getText().toString().trim();

        Shift shift;
        if (editingShift != null) {
            shift = editingShift;
        } else {
            shift = new Shift();
        }

        shift.setYear(selectedYear);
        shift.setMonth(selectedMonth);
        shift.setDay(selectedDay);
        shift.setStartHour(startHour);
        shift.setStartMinute(startMinute);
        shift.setEndHour(endHour);
        shift.setEndMinute(endMinute);
        shift.setType(type);
        shift.setNote(note);

        storage.saveShift(shift);

        setResult(RESULT_OK);
        finish();
    }

    private void confirmDelete() {
        if (editingShift == null) return;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                storage.deleteShift(editingShift.getId());
                setResult(RESULT_OK);
                finish();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
