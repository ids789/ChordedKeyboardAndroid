/*
    Class for managing each chord set and their actions
    - loads the chord set definitions from the chord_map.xml file
    - returns what should be done when a new chord is received from the keyboard
 */

package com.ids789.chordedkeyboard;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChordHandler {

    String currentSet = "GROUP A"; // The initial chord set

    private HashMap<String, ChordAction[]> chordTable = new HashMap<String, ChordAction[]>();

    public ChordHandler() { }

    void loadChordMap(XmlPullParser xpp) {
        List<ChordAction> set = new ArrayList<ChordAction>();
        ChordAction action = new ChordAction();
        String setName = "";

        Log.v("CHORD", "BEGIN XML LOAD");

        try {
            while (xpp.getEventType()!=XmlPullParser.END_DOCUMENT) {

                if (xpp.getEventType()==XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("ChordSet")) {
                        set = new ArrayList<ChordAction>();
                        setName = xpp.getAttributeValue(null, "name");
                    }
                    else if (xpp.getName().equals("ChordMapping")) {
                        action = new ChordAction();

                        String chord_str = xpp.getAttributeValue(null, "chord");
                        String action_str = xpp.getAttributeValue(null, "action");
                        String value_str = xpp.getAttributeValue(null, "value");

                        if (chord_str != null)
                            action.setTrigger(Integer.parseInt(chord_str));
                        if (action_str != null)
                            action.setAction(action_str);
                        if (value_str != null)
                            action.setValue(value_str);
                    }
                }
                else if (xpp.getEventType()==XmlPullParser.TEXT) {
                    action.setName(xpp.getText());
                }
                else if (xpp.getEventType()==XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("ChordSet")) {
                        chordTable.put(setName, set.toArray(new ChordAction[0]));
                    }
                    else if (xpp.getName().equals("ChordMapping")) {
                        set.add(action);
                    }
                }

                xpp.next();
            }
        }
        catch (Throwable t) {
            Log.v("CHORD", "XML Request Failed");
        }
    }

    ChordAction processChord(int chord) {
        if (chord == 0b11111) {
            return new ChordAction(0b11111, "BREAK", "key", "escape");
        }

        ChordAction match = null;
        for (int i = 0; i < chordTable.get(currentSet).length; i++) {
            if (chordTable.get(currentSet)[i].trigger == chord) {
                match = chordTable.get(currentSet)[i];
                break;
            }
        }

        if (match != null) {
            Log.v("CHORD", "MAPPED EVENT: " + match.action + " " + match.value);
            if (match.action.equals("switch")) {
                currentSet = match.value;
            }
            else if (match.action.equals("key")) {
                Log.v("CHORD", "Key EVENT: " + match.value);
                if (currentSet.contains("*"))
                    currentSet = currentSet.replace("*", "");
                return match;
            }
        }
        return null;
    }

    ChordAction[] availableChords() {
        return chordTable.get(currentSet);
    }
}
