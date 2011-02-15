package org.baole.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


public abstract class Service {
	public static final int STATUS_SEND_OK = 0;
	public static final int STATUS_SEND_FAILED = 1;
	public static final int STATUS_LOGGIN_OK = 2;
	public static final int STATUS_LOGGIN_FAILED = 3;

	public static final String CMD_SMS_SENT = "SMS_SENT";
	public static final String CMD_SMS_DELIVERED = "SMS_DELIVERED";

	protected Context ctx;
	protected PendingIntent sentPI;
	protected PendingIntent deliveredPI;
	protected boolean splitLongSMS = false;

	public void setSplitLongSMS (boolean sp) {
		splitLongSMS = sp;
	}
	
	public void setDeliveryReport(boolean dr) {
		if (dr) {
			deliveredPI = PendingIntent.getBroadcast(ctx, 0, new Intent(
					CMD_SMS_DELIVERED), 0);
		} else {
			deliveredPI = null;
		}
	}
	public void setContext(Context ctx) {
		this.ctx = ctx;
		sentPI = PendingIntent.getBroadcast(ctx, 0, new Intent(CMD_SMS_SENT),
				0);
	}

	public abstract int init();
	public abstract int send(String number, String sms);
	public abstract String getReport();
}
