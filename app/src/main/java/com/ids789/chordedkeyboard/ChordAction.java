/*
    Class to describe a specific chord
 */

package com.ids789.chordedkeyboard;

public class ChordAction {
    int trigger;    // binary representation of the chord buttons
    String name;    // Text to show for hints
    String action;  // key or switch or function
    String value;   // key to press or chord set to switch to

    public ChordAction() { }

    public ChordAction(int trigger, String name, String action, String value) {
        this.trigger = trigger;
        this.name = name;
        this.action = action;
        this.value = value;
    }

    public void setTrigger(int trigger) {
        this.trigger = trigger;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
