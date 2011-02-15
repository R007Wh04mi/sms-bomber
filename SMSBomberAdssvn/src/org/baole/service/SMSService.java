package org.baole.service;

import android.telephony.SmsManager;

public class SMSService extends Service {
	int status;
	String message;
	String username;
	String password;
	

	// @Override
	public int send(String number, String sms) {
		try {
			smsManager.sendTextMessage(number, null, sms, sentPI, deliveredPI);
		} catch (Throwable e) {
			return STATUS_SEND_FAILED;
		}
		return STATUS_SEND_OK;
	}

	// @Override
	public String getReport() {
		return message;
	}

	SmsManager smsManager;

	// @Override
	public int init() {
		smsManager = SmsManager.getDefault();
		return STATUS_LOGGIN_OK;
	}
}
