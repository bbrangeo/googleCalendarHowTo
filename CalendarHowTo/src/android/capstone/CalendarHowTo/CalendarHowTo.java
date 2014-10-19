/*
 * Author: Swapnil Devikar
 * Description: This application is an example of how to use the Calendar Providers in android
 * 				This application starts by making a new local calendar in the device, adds a recurring event and 
 * 				retrieves the events.
 */

package android.capstone.CalendarHowTo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import android.app.Activity;
import android.capstone.CalendarHowTo.R;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CalendarHowTo extends Activity {

	// Projection array. Creating indices for this array instead of doing
	// dynamic lookups improves performance.
	public static final String[] EVENT_PROJECTION = new String[] {
			Calendars._ID, // 0
			Calendars.ACCOUNT_NAME, // 1
			Calendars.CALENDAR_DISPLAY_NAME, // 2
			Calendars.OWNER_ACCOUNT // 3
	};

	// The indices for the projection array above.
	private static final int PROJECTION_ID_INDEX = 0;
	private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
	private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
	private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

	private static final String PATIENT_ACCOUNT_NAME = "Patient Status Calendar";
	private static final String PATIENT_NAME = "Sample Cancer Patient";

	// email id for local calendar
	private static final String PATIENT_EMAIL = "patient@cancerhospital.com";
	
	// TODO - Change this email id to your email id
	//email id for gmail calendar
	private static final String EMAIL_ID = "person@gmail.com";

	public void onCreate(Bundle savedInstanceState) {

		// Required call through to Activity.onCreate()
		// Restore any saved instance state
		super.onCreate(savedInstanceState);

		// Set up the application's user interface (content view)
		setContentView(R.layout.main);

		// Make a new calendar
		try {
			makeLocalCalendar();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// determine the existing calendar in use
		// long calID = getExistingCalendarId();
		
		// create a local calendar
		long calID = getLocalCalendarId();

		Calendar beginTime = Calendar.getInstance();
		beginTime.set(2014, 10, 18, 0, 0); // start on 18th Oct 2014
		long startMillis = beginTime.getTimeInMillis();
		Calendar endTime = Calendar.getInstance();
		endTime.set(2015, 10, 18, 0, 0); // end in a year
		long endMillis = endTime.getTimeInMillis();

		long eventID = insertRecurringEvent(calID, startMillis, endMillis);

		ArrayList<String> recurringEvents = getRecurringEvents(eventID,
				startMillis, endMillis);

		int length = recurringEvents.size();

		// now you can get recurring instances of this event from the event
		// table
		// ...
		
		setTemporaryList();

	}

	/*
	 * Create a local calendar in android device
	 */
	private void makeLocalCalendar() {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();
		values.put(Calendars.ACCOUNT_NAME, PATIENT_ACCOUNT_NAME);
		values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
		values.put(Calendars.NAME, PATIENT_NAME);
		values.put(Calendars.CALENDAR_DISPLAY_NAME, PATIENT_NAME);
		values.put(Calendars.CALENDAR_COLOR, Color.BLACK);
		values.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
		values.put(Calendars.OWNER_ACCOUNT, PATIENT_EMAIL);
		values.put(Calendars.CALENDAR_TIME_ZONE, TimeZone.getAvailableIDs()
				.toString());
		values.put(Calendars.SYNC_EVENTS, 1);

		Uri.Builder builder = CalendarContract.Calendars.CONTENT_URI
				.buildUpon();
		builder.appendQueryParameter(Calendars.ACCOUNT_NAME,
				"com.grokkingandroid");
		builder.appendQueryParameter(Calendars.ACCOUNT_TYPE,
				CalendarContract.ACCOUNT_TYPE_LOCAL);
		builder.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,
				"true");

		Uri uri = getContentResolver().insert(builder.build(), values);

	}

	/*
	 * Returns a list of instances of an event with eventID
	 */
	private ArrayList<String> getRecurringEvents(long eventID,
			long startMillis, long endMillis) {

		ArrayList<String> eventList = new ArrayList<String>();

		final String[] INSTANCE_PROJECTION = new String[] { Instances.EVENT_ID, // 0
				Instances.BEGIN, // 1
				Instances.TITLE // 2
		};

		// The indices for the projection array above.
		final int PROJECTION_ID_INDEX = 0;
		final int PROJECTION_BEGIN_INDEX = 1;
		final int PROJECTION_TITLE_INDEX = 2;

		Cursor cur = null;
		ContentResolver cr = getContentResolver();

		// The ID of the recurring event whose instances you are searching
		// for in the Instances table
		String selection = Instances.EVENT_ID + " = ?";
		String[] selectionArgs = new String[] { "" + eventID };

		// Construct the query with the desired date range.
		Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, startMillis);
		ContentUris.appendId(builder, endMillis);

		// Submit the query
		cur = cr.query(builder.build(), INSTANCE_PROJECTION, selection,
				selectionArgs, null);

		while (cur.moveToNext()) {
			String title = null;

			// Get the field values
			title = cur.getString(PROJECTION_TITLE_INDEX);

			eventList.add(title);
		}

		return eventList;
	}

	/*
	 * Function to inserts a recurring calendar event 
	 */
	private long insertRecurringEvent(long calID, long startMillis,
			long endMillis) {
		long eventID = 0;

		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		values.put(Events.DTSTART, startMillis);
		values.put(Events.DTEND, endMillis);
		values.put(Events.RRULE, "FREQ=DAILY;COUNT=10");
		values.put(Events.TITLE, "Patient Status Check");
		values.put(Events.DESCRIPTION,
				"Check weather patient has taken medicines or not");
		values.put(Events.CALENDAR_ID, calID);
		values.put(Events.EVENT_TIMEZONE, "America/Los_Angeles");
		Uri uri = cr.insert(Events.CONTENT_URI, values);

		eventID = Long.parseLong(uri.getLastPathSegment());
		return eventID;
	}

	/*
	 * This function reads the android device for existing caledars in android
	 * and returns its id
	 */
	private long getExistingCalendarId() {
		long calID = 0;

		Cursor cur = null;
		ContentResolver cr = getContentResolver();
		Uri uri = Calendars.CONTENT_URI;
		String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
				+ Calendars.ACCOUNT_TYPE + " = ?) AND ("
				+ Calendars.OWNER_ACCOUNT + " = ?))";
		String[] selectionArgs = new String[] { EMAIL_ID,
				"com.google", EMAIL_ID };
		// Submit the query and get a Cursor object back.
		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

		// Use the cursor to step through the returned records
		while (cur.moveToNext()) {
			calID = 0;

			String displayName = null;
			String accountName = null;
			String ownerName = null;

			// Get the field values
			calID = cur.getLong(PROJECTION_ID_INDEX);
			displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
			accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
			ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

		}
		return calID;
	}

	/*
	 * This function returns a local calendar ID that was created earlier by
	 * this app
	 */
	private long getLocalCalendarId() {

		String[] projection = new String[] { Calendars._ID };

		String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
				+ Calendars.ACCOUNT_TYPE + " = ?))";

		// use the same values as above:

		String[] selArgs = new String[] { PATIENT_ACCOUNT_NAME,
				CalendarContract.ACCOUNT_TYPE_LOCAL };

		Cursor cursor = getContentResolver().query(Calendars.CONTENT_URI,
				projection, selection, selArgs, null);

		if (cursor.moveToFirst()) {

			return cursor.getLong(0);

		} else {
			System.out.println("Cursor did not return any values");
		}

		return -1;

	}

	private void setTemporaryList() {
		final ListView listview = (ListView) findViewById(R.id.listView);

		/*
		 * Dummy data for now, it'll be replaced by the event titles returned by
		 * the calendar
		 */
		String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
				"Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
				"Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
				"OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
				"Android", "iPhone", "WindowsMobile" };

		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < values.length; ++i) {
			list.add(values[i]);
		}
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		listview.setAdapter(adapter);
	}

}