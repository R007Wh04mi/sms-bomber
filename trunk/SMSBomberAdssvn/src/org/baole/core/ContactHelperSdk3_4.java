/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.baole.core;

import text.androidad3.ContactHelper; 
import android.database.Cursor;
import android.provider.Contacts;

/**
 * An implementation of {@link ContactAccessor} that uses legacy Contacts API.
 * These APIs are deprecated and should not be used unless we are running on a
 * pre-Eclair SDK.
 * <p>
 * There are several reasons why we wouldn't want to use this class on an Eclair
 * device:
 * <ul>
 * <li>It would see at most one account, namely the first Google account created
 * on the device.
 * <li>It would work through a compatibility layer, which would make it
 * inherently less efficient.
 * <li>Not relevant to this particular example, but it would not have access to
 * new kinds of data available through current APIs.
 * </ul>
 */
@SuppressWarnings("deprecation")
public class ContactHelperSdk3_4 extends ContactHelper {
	
	private static final String[] PEOPLE_PROJECTION = new String[] {
		Contacts.People._ID, Contacts.People.PRIMARY_PHONE_ID,
		Contacts.People.TYPE, Contacts.People.NUMBER,
		Contacts.People.LABEL, Contacts.People.NAME, };
	
	@Override
	public Cursor getContactCursor() {
		return ctx.getContentResolver().query(Contacts.People.CONTENT_URI,
				PEOPLE_PROJECTION, Contacts.People.NUMBER + " IS NOT NULL",
				null, Contacts.People.DEFAULT_SORT_ORDER);
	}

	@Override
	public String[] getFieldProjection() {
		return PEOPLE_PROJECTION;
	}

	public Cursor queryFilter(CharSequence constraint) {
		StringBuilder buffer = null;
		String[] args = null;
		
		if (constraint != null) {
			buffer = new StringBuilder();
			buffer.append("UPPER(");
			buffer.append(Contacts.ContactMethods.NAME);
			buffer.append(") GLOB ?");
			args = new String[] { constraint.toString().toUpperCase() + "*" };
		}
		
		return ctx.getContentResolver().query(Contacts.People.CONTENT_URI,
				PEOPLE_PROJECTION, buffer == null ? null : buffer
						.toString(), args,
				Contacts.People.DEFAULT_SORT_ORDER);
	}	
}
