/*
    Class to populate the chord display GridView
 */

package com.ids789.chordedkeyboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.Dimension;

public class ReferenceAdapter extends BaseAdapter {

    private final Context mContext;
    private final ChordAction[] chords;

    public ReferenceAdapter(Context context, ChordAction[] chords) {
        this.mContext = context;
        this.chords = chords;
    }

    @Override
    public int getCount() {
        return chords.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final ChordAction chord = chords[i];

        if (view == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            view = layoutInflater.inflate(R.layout.reference_item, null);
        }

        final TextView nameField = (TextView)view.findViewById(R.id.name);
        final ChordRefView chordRef = (ChordRefView)view.findViewById(R.id.chord);

        nameField.setText(chord.name);
        chordRef.setChord(chord.trigger);

        // if this chord switches to another chord set
        if (chord.action.equals("switch")) {
            view.setBackgroundColor(view.getResources().getColor(R.color.ref_switch));

            nameField.setTextSize(Dimension.DP, 25);
        }
        // if this chord is for switching to the current chord set, give it a distinctive colour
        else if (chord.action.equals("current-group")) {
            nameField.setTextSize(Dimension.DP, 25);

            if (chord.name.equals("GROUP A")) {
                view.setBackgroundColor(view.getResources().getColor(R.color.group_a));
            }
            if (chord.name.equals("GROUP B")) {
                view.setBackgroundColor(view.getResources().getColor(R.color.group_b));
            }
            else if (chord.name.equals("CONTROLS")) {
                view.setBackgroundColor(view.getResources().getColor(R.color.controls_group));
            }
            else if (chord.name.equals("SHIFT")) {
                view.setBackgroundColor(view.getResources().getColor(R.color.shift_group));
            }
        }
        else {
            view.setBackgroundColor(view.getResources().getColor(R.color.ref_key));
            nameField.setTextSize(Dimension.DP, 40);
        }

        return view;
    }
}
