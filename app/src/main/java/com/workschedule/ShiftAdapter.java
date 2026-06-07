package com.workschedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ShiftAdapter extends ArrayAdapter<Shift> {

    private final Context context;
    private final List<Shift> shifts;

    public ShiftAdapter(Context context, List<Shift> shifts) {
        super(context, R.layout.item_shift, shifts);
        this.context = context;
        this.shifts = shifts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_shift, parent, false);
            holder = new ViewHolder();
            holder.colorBar = (View) convertView.findViewById(R.id.colorBar);
            holder.tvShiftType = (TextView) convertView.findViewById(R.id.tvShiftType);
            holder.tvShiftTime = (TextView) convertView.findViewById(R.id.tvShiftTime);
            holder.tvShiftNote = (TextView) convertView.findViewById(R.id.tvShiftNote);
            holder.tvShiftHours = (TextView) convertView.findViewById(R.id.tvShiftHours);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Shift shift = shifts.get(position);

        // Set color and type label
        int color = getTypeColor(shift.getType());
        holder.colorBar.setBackgroundColor(color);
        holder.tvShiftType.setText(getTypeLabel(shift.getType()));
        holder.tvShiftType.setTextColor(color);

        // Set time display
        if (shift.getType() == Shift.TYPE_WORK) {
            holder.tvShiftTime.setText(shift.getFormattedStartTime() + " – " + shift.getFormattedEndTime());
            holder.tvShiftHours.setText(shift.getFormattedDuration());
            holder.tvShiftHours.setTextColor(color);
        } else {
            holder.tvShiftTime.setText(getTypeLabel(shift.getType()));
            holder.tvShiftHours.setText("");
        }

        // Show note if exists
        String note = shift.getNote();
        if (note != null && !note.isEmpty()) {
            holder.tvShiftNote.setText(note);
            holder.tvShiftNote.setVisibility(View.VISIBLE);
        } else {
            holder.tvShiftNote.setVisibility(View.GONE);
        }

        return convertView;
    }

    private int getTypeColor(int type) {
        switch (type) {
            case Shift.TYPE_WORK:     return context.getResources().getColor(R.color.colorWork);
            case Shift.TYPE_DAYOFF:   return context.getResources().getColor(R.color.colorDayOff);
            case Shift.TYPE_VACATION: return context.getResources().getColor(R.color.colorVacation);
            case Shift.TYPE_SICK:     return context.getResources().getColor(R.color.colorSick);
            default:                  return context.getResources().getColor(R.color.colorWork);
        }
    }

    private String getTypeLabel(int type) {
        switch (type) {
            case Shift.TYPE_WORK:     return context.getString(R.string.work_type_work);
            case Shift.TYPE_DAYOFF:   return context.getString(R.string.work_type_dayoff);
            case Shift.TYPE_VACATION: return context.getString(R.string.work_type_vacation);
            case Shift.TYPE_SICK:     return context.getString(R.string.work_type_sick);
            default:                  return "";
        }
    }

    static class ViewHolder {
        View colorBar;
        TextView tvShiftType;
        TextView tvShiftTime;
        TextView tvShiftNote;
        TextView tvShiftHours;
    }
}
