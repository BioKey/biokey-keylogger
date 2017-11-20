package com.uwo.keylogger;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;

/**
 * Hello world!
 *
 */
public class App {
	static boolean recording = true;
	static MenuItem stateItem;
	static MenuItem stateChangeItem;
	
	public static void main(String[] args) {
		
		final Recorder recorder = new Recorder();
		
		// Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        
        // Create System Tray
        String suffix = (!isMacMenuBarDarkMode() ? "-dark.png" : ".png");
        ImageIcon pauseIcon = new ImageIcon(App.class.getResource("/images/pause-icon" + suffix));
        Image pauseImage = pauseIcon.getImage();
        
        ImageIcon recordIcon = new ImageIcon(App.class.getResource("/images/record-icon" + suffix));
        Image recordImage = recordIcon.getImage();
        
        final TrayIcon trayIcon = new TrayIcon(pauseImage);
        final SystemTray tray = SystemTray.getSystemTray();
        
       
        // Create a pop-up menu components
        stateItem = new MenuItem("Paused");
        stateItem.setEnabled(false);
        
        stateChangeItem = new MenuItem("Start");
        stateChangeItem.addActionListener((e) -> {
        	recording = !recording;
        	
        	if (recording) recording = recorder.start();
        	else recording = !recorder.stop();
        	
        	stateChangeItem.setLabel((recording ? "Stop" : "Start"));
        	stateItem.setLabel((recording ? "Recording" : "Paused"));
        	trayIcon.setImage((recording ? recordImage : pauseImage));
        });
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener((e) -> {
        	System.exit(0);
        });
        
        //Add components to pop-up menu
        final PopupMenu popup = new PopupMenu();
        popup.add(stateItem);
        popup.add(stateChangeItem);
        popup.addSeparator();
        popup.add(exitItem);
       
        // Add pop-up menu to tray item
        trayIcon.setPopupMenu(popup);
       
        // Add tray item to tray
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
        
        // Start Recording and adjust the state
        if (recording) recording = recorder.start();
        stateChangeItem.setLabel((recording ? "Stop" : "Start"));
    	stateItem.setLabel((recording ? "Recording" : "Paused"));
    	trayIcon.setImage((recording ? recordImage : pauseImage));
	}
	
	private static boolean isMacMenuBarDarkMode() {
	    try {
	        // check for exit status only. Once there are more modes than "dark" and "default", we might need to analyze string contents..
	        final Process proc = Runtime.getRuntime().exec(new String[] {"defaults", "read", "-g", "AppleInterfaceStyle"});
	        proc.waitFor(100, TimeUnit.MILLISECONDS);
	        return proc.exitValue() == 0;
	    } catch (IOException | InterruptedException | IllegalThreadStateException ex) {
	        // IllegalThreadStateException thrown by proc.exitValue(), if process didn't terminate
	        System.out.println("Could not determine, whether 'dark mode' is being used. Falling back to default (light) mode.");
	        return false;
	    }
	}
}
