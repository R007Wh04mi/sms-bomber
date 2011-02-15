package text.androidad3;

import java.util.ArrayList;

import org.baole.core.ContactEntry;
import org.baole.service.Service;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;  
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SMSBomber extends Activity implements OnClickListener {
	private static final int DIALOG_CHANGLOGS = 1;
	private static final String PREFS_LAST_RUN = "lastrun";
	private static final String MESSAGE = "message";
	private static final String CONTACT = "contact";
	private static final String COUNTER = "%c%";

	private static final String NUMBER = "NUMBER";
	private static final String NAME = "NAME"; 

	public int recent_numberItems = 3;

	private Button btnSend;
	private Button btnCancel;
	private Button btnClear;
	private ProgressBar progressSend;
	private ArrayList<ContactEntry> contacts;
	private String message;
	private EditText txtMessage;
	private EditText txtMessageCount;
	private Spinner spdInterval;
	private TextView txtInfo;
	private TextView txtSelectedContact;
	private boolean isCancel = false;

	Service service;

	private boolean deliveryReport;
	private boolean isSaveSMS;
	private boolean isSplit;

	@Override
	protected void onCreate(Bundle inState) {
		super.onCreate(inState);

		setContentView(R.layout.compose);

		btnSend = (Button) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(this);

		btnClear = (Button) findViewById(R.id.btnClear);
		btnClear.setOnClickListener(this);

		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(this);
		btnCancel.setEnabled(false);

		progressSend = (ProgressBar) findViewById(R.id.progressSend);
		progressSend.setVisibility(View.GONE);

		txtMessage = (EditText) findViewById(R.id.txtMessage);
		txtMessageCount = (EditText) findViewById(R.id.txtMsgCount);
		spdInterval = (Spinner) findViewById(R.id.spnInterval);

		txtInfo = (TextView) findViewById(R.id.txtInfo);
		txtSelectedContact = (TextView) findViewById(R.id.txtSelectedContact);
		txtSelectedContact.setMovementMethod(new ScrollingMovementMethod());

		if (inState != null) {
			txtMessage.setText(inState.getString(MESSAGE));
			contacts = inState.getParcelableArrayList(CONTACT);
		}

		if (contacts == null) {
			contacts = new ArrayList<ContactEntry>();
		}

		String name = getIntent().getStringExtra(NAME);
		String number = getIntent().getStringExtra(NUMBER);
		if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(number)) {
			contacts.add(new ContactEntry(name, number));

			SharedPreferences settings = this
					.getSharedPreferences(SMSBOMBER, 0);
			Editor editor = settings.edit();
			int msgCount = getIntent().getIntExtra(MESSAGE_COUNT, 10);
			editor.putString(MESSAGE_COUNT, "" + msgCount);
			editor.commit();
		}

		adapter = new ContactListAdapter(this, ContactHelper.getInstance(this)
				.getContactCursor());

		txtMessage.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				setMessageCharCount();
			}
		});

		loadPreference();
		showContacts();

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		deliveryReport = preferences.getBoolean("delivery_report", true);
		isSaveSMS = preferences.getBoolean("save_sent_sms", true);
		isSplit = preferences.getBoolean("split_long_sms", true);

		StringBuffer buff = new StringBuffer();
		buff.append("Split long SMS (> 160 chars): " + (isSplit ? "ON" : "OFF")
				+ "\n");
		buff.append("Save sent SMS to Inbox: " + (isSaveSMS ? "ON" : "OFF")
				+ "\n");
		buff.append("Delivery report: " + (deliveryReport ? "ON" : "OFF"));

		addReport(buff.toString());

		final String v0 = preferences.getString(PREFS_LAST_RUN, "");
		final String v1 = this.getString(R.string.app_version);
		if (!v0.equals(v1)) {
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(PREFS_LAST_RUN, v1);
			editor.commit();
			this.showDialog(DIALOG_CHANGLOGS);
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(MESSAGE, txtMessage.getText().toString());
		outState.putParcelableArrayList(CONTACT, contacts);
		this.updatePreference();
	}

	@Override
	protected void onPause() {
		super.onPause();
		updatePreference();
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadPreference();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_CHANGLOGS:
			return DialogUtil.createChangLogDialog(this);
		default:
			return null;
		}
	}

	protected void setMessageCharCount() {
		btnSend.setText(getString(R.string.send_prog, txtMessage.getText()
				.length()));

	}

	ContactListAdapter adapter;
	ContactEntry ceContact = null;
	private int delay;
	private int messageCount;

	public void onClick(View v) {
		if (btnSend.equals(v)) {
			send();
		} else if (btnCancel.equals(v)) {
			isCancel = true;
		} else if (btnClear.equals(v)) {
			new AlertDialog.Builder(this).setIcon(
					android.R.drawable.ic_dialog_alert).setTitle(
					R.string.confirmation).setMessage(
					R.string.contact_remove_message).setPositiveButton(
					R.string.all, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							contacts.clear();
							showContacts();
						}
					}).setNeutralButton(R.string.last,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							int size = contacts.size();
							if (size > 0) {
								contacts.remove(size - 1);
								showContacts();
							}
						}
					}).setNegativeButton(R.string.cancel, null).show();
		}
	}

	private void send() {
		message = txtMessage.getText().toString();
		if (TextUtils.isEmpty(message)) {
			Toast.makeText(getApplicationContext(), R.string.enter_message,
					Toast.LENGTH_LONG).show();
			return;
		}

		if (contacts.size() <= 0) {
			Toast.makeText(getApplicationContext(), R.string.select_contact,
					Toast.LENGTH_LONG).show();
			return;
		}

		String msgCount = txtMessageCount.getText().toString();
		try {
			messageCount = Integer.parseInt(msgCount);
		} catch (Exception e) {
			messageCount = 0;
		}

		if (messageCount <= 0) {
			Toast.makeText(getApplicationContext(),
					R.string.incorrect_message_count, Toast.LENGTH_LONG).show();
			return;
		}

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txtMessage.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);

		new SendTask().execute();
	}

	@Override
	protected void onDestroy() {
		if (sentCount < messageCount && !isCancel) {
			SharedPreferences.Editor settingsEditor = getSharedPreferences(
					SMSBOMBER, 0).edit();
			settingsEditor.putInt(FORCE_CLOSE_MAX, sentCount);
			settingsEditor.commit();
		}
		super.onDestroy();
	}

	private static String SMSBOMBER = "sms_bomber";
	private static String INTERVAL = "interval";
	private static String MESSAGE_COUNT = "message_count";
	private static String FORCE_CLOSE_MAX = "force_close_max";

	private void loadPreference() {
		SharedPreferences settings = this.getSharedPreferences(SMSBOMBER, 0);
		spdInterval.setSelection(settings.getInt(INTERVAL, 0));

		int forceClose = settings.getInt(FORCE_CLOSE_MAX, 1000);
		int msgCount = Integer
				.parseInt(settings.getString(MESSAGE_COUNT, "10"));
		if (forceClose < msgCount) {
			addReport(getString(R.string.warning_force_close, forceClose));
			msgCount = forceClose;
		}
		txtMessageCount.setText("" + msgCount);

	}

	private void updatePreference() {
		int intervalIndex = spdInterval.getSelectedItemPosition();
		if (intervalIndex == Spinner.INVALID_POSITION) {
			intervalIndex = 0;
		}
		int[] intervalValues = getResources().getIntArray(
				R.array.interval_values);
		delay = intervalValues[intervalIndex];

		SharedPreferences.Editor settingsEditor = getSharedPreferences(
				SMSBOMBER, 0).edit();
		settingsEditor.putInt(INTERVAL, intervalIndex);
		settingsEditor.putString(MESSAGE_COUNT, "" + messageCount);
		settingsEditor.commit();

	}

	// stats
	int sentOKCount[];
	int sentFailedCount[];
	public int sentCount;

	private void showContacts() {
		int size = contacts.size();
		StringBuffer buff = new StringBuffer(getString(R.string.to, size));

		for (int i = 0; i < size; i++) {
			buff.append(contacts.get(i).name).append(
					"(" + contacts.get(i).number + "), ");
		}
		txtSelectedContact.setText(buff.toString());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_blacklist:
			onMarketLaunch("org.baole.app.blacklist");
			break;
		case R.id.menu_groupsms:
			onMarketLaunch("org.baole.app.groupsmsad2");
			break;
		case R.id.menu_sms_limitremoval:
			onMarketLaunch("org.baole.smslimitremoval");
			break;
		case R.id.menu_go_pro:
			openUrl("http://code.google.com/p/sms-bomber/");
			break;
		case R.id.menu_anti_sms_bomber:
			onMarketLaunch("org.baole.antibomber");
			break;
		case R.id.menu_counter:
			txtMessage.getEditableText().insert(txtMessage.getSelectionStart(),
					COUNTER);
			break;
		case R.id.menu_setting:
			startActivity(new Intent(this, Preferences.class));
			break;
		case R.id.menu_help:
			showDialog(DIALOG_CHANGLOGS);
			break;
		}

		return true;
	}

	private void openUrl(String url) {
		Intent donate = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(donate);
	}

	private void onMarketLaunch(String url) {
		Intent donate = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(
				"market://details?id=%s", url)));
		startActivity(donate);
	}

	private class SendTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			final ProgressBar progress = progressSend;

			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(SMSBomber.this);
			deliveryReport = settings.getBoolean("delivery_report", true);
			isSaveSMS = settings.getBoolean("save_sent_sms", true);
			isSplit = settings.getBoolean("split_long_sms", true);

			int size = contacts.size();
			sentFailedCount = new int[size];
			sentOKCount = new int[size];
			sentCount = 0;
			isCancel = false;
			updatePreference();

			// TODO
			service = getInstance(SMSBomber.this);
			service.setDeliveryReport(deliveryReport);
			service.setSplitLongSMS(isSplit);

			int status = service.init();
			if (status == Service.STATUS_LOGGIN_FAILED) {
				String info = service.getReport();
				txtInfo.setText(info);
				return;
			}

			progress.setVisibility(View.VISIBLE);
			progress.setMax(messageCount);
			progress.setProgress(0);
			btnSend.setEnabled(false);
			btnCancel.setEnabled(true);
			txtInfo.setText("");
		}

		@Override
		protected Void doInBackground(Void... params) {
			boolean isContainCounter = message.contains(COUNTER);

			int size = contacts.size();
			for (sentCount = 1; sentCount <= messageCount; sentCount++) {
				for (int i = 0; i < size; i++) {

					String msg = message;
					if (isContainCounter)
						msg = message.replaceAll(COUNTER, "" + sentCount);
					service.send(contacts.get(i).number, msg);
					try {
						Thread.sleep((delay == 1) ? 500 : delay * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			return null;
		}

	}

	ContentValues values = new ContentValues();

	private void addReport(String s) {
		txtInfo.setText(s + "\n" + txtInfo.getText());
	}

	private void saveMessage(ContentResolver cr, ContactEntry ce, long date,
			int type, String message) {
		values.put(ADDRESS, ce.number);
		values.put(DATE, date);
		values.put(READ, 1);
		values.put(STATUS, -1);
		values.put(SMS_TYPE, type);
		values.put(BODY, message);
		// Uri inserted =
		cr.insert(Uri.parse("content://sms"), values);
	}

	public static class ContactListAdapter extends CursorAdapter implements
			Filterable {
		Activity ctx;

		public ContactListAdapter(Activity context, Cursor c) {
			super(context, c);
			ctx = context;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final LayoutInflater inflater = LayoutInflater.from(context);
			final TextView view = (TextView) inflater.inflate(
					android.R.layout.simple_dropdown_item_1line, parent, false);
			view.setText(cursor.getString(5) + "(" + cursor.getString(3) + ")");
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			((TextView) view).setText(cursor.getString(5) + "("
					+ cursor.getString(3) + ")");
		}

		@Override
		public String convertToString(Cursor cursor) {
			return cursor.getString(5);
		}

		@Override
		public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
			if (getFilterQueryProvider() != null) {
				return getFilterQueryProvider().runQuery(constraint);
			}
			return ContactHelper.getInstance(ctx).queryFilter(constraint);
		}

		// private ContentResolver mContent;
	}

	public static final String ADDRESS = "address";
	public static final String PERSON = "person";
	public static final String DATE = "date";
	public static final String READ = "read";
	public static final String STATUS = "status";
	public static final String SMS_TYPE = "type";
	public static final String BODY = "body";
	public static final int TYPE_INBOX = 1;
	public static final int TYPE_SENT = 2;
	public static final int TYPE_DRAFT = 3;
	public static final int TYPE_OUTBOX = 4;

	private static Service sInstance;

	public static Service getInstance(Context ctx) {
		// if(true) return new MockService();
		if (sInstance == null) {
			String className;

			int sdkVersion = Integer.parseInt(Build.VERSION.SDK); // Cupcake
			// style
			if (sdkVersion < Build.VERSION_CODES.DONUT) {
				className = "org.baole.service.SMSService34";
			} else {
				className = "org.baole.service.SMSService";
			}

			/*
			 * Find the required class by name and instantiate it.
			 */
			try {
				Class<? extends Service> clazz = Class.forName(className)
						.asSubclass(Service.class);
				sInstance = clazz.newInstance();
				sInstance.setContext(ctx);
			} catch (Exception e) {
				Log.e("SMSWraper", "" + e.getMessage());
				e.printStackTrace();
			}
		}
		return sInstance;
	}

}
