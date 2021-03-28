# Android Chorded Keyboard
 
An Android virtual keyboard to connect to an experimental Bluetooth Chorded Keyboard. [link]

The app registers itself onto a phone as a virtual keyboard service, when a text field is selected it will search for the keyboard over bluetooth. (by looking for a preset Bluetooth UUID)

As the Chorded Keyboard has 5 buttons it can output a maximum of 31 unique chords.  In order to provide all the keys from a standard keyboard the keys are divided into 5 sets: Letters, Symbols, Shifted Letters, Shifted Symbols and Control Characters (such as Left Arrow and Home Button).  The key pressed for each chord depends on what set it currently selected, the selected set can be changed using a 1 finger chord.  

The app implements a virtual keyboard allowing the use of the keyboard from any application on the phone.  The virtual keyboard displays a grid on screen showing each of the current available chords.  This makes learning the chords easier and helps when using rarely used chords.  Each key is shown using 5 dots indicating which fingers to use for that specific key.  


#### Usage
- Switch Bluetooth ON and ensure that the keyboard is paired.  
- Set the current input mode to be the Chorded Keyboard.  
- Type onto the keyboard using the chords displayed on screen as a reference.  
- Switch between sets using the chords on the top row, each has a distinctive colour to make it clear which set is selected.  
- Note: after installation it won't appear in the keyboard menu until it has been enabled from the input settings page
