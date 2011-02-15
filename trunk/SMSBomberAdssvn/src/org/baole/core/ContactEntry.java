package org.baole.core;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactEntry implements Parcelable {

	public String name;
	public String number;
//	public Boolean isSelected;
	public long id;
//	long id;
	public int type;

	public int describeContents() { 
		return 0;
	}

	public void writeToParcel(Parcel par, int flags) {
//		par.writeLong(id);
		par.writeString(name);
		par.writeString(number);
//		par.writeValue(isSelected);
		par.writeLong(id);
	}

	public static final Parcelable.Creator<ContactEntry> CREATOR = new Parcelable.Creator<ContactEntry>() {
		public ContactEntry createFromParcel(Parcel in) {
			return new ContactEntry(in);
		}

		public ContactEntry[] newArray(int size) {
			return new ContactEntry[size];
		}
	};

	private ContactEntry(Parcel in) {
		name = in.readString();
		number = in.readString();
//		isSelected = (Boolean)in.readValue(Boolean.class.getClassLoader());
		id = in.readLong();
	}

	public ContactEntry(String name, String number) {
		super();
//		this.id = id;
		this.name = name;
		this.number = number;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContactEntry other = (ContactEntry) obj;
//		if (id != other.id)
//			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		return true;
	}

	public ContactEntry() {
	}
}
