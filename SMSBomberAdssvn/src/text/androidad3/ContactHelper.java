package text.androidad3;

import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

public abstract class ContactHelper {
	abstract public String[] getFieldProjection();
	abstract public Cursor getContactCursor();
	abstract public Cursor queryFilter(CharSequence constraint);
	
	protected Activity ctx;

	public void setActivity(Activity ctx) {
		this.ctx = ctx;
	}

	private static ContactHelper sInstance;

	public static ContactHelper getInstance(Activity ctx) {
		if (sInstance == null) {
			String className;

			@SuppressWarnings("deprecation")
			int sdkVersion = Integer.parseInt(Build.VERSION.SDK); // Cupcake
																	// style
			if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
				className = "org.baole.core.ContactHelperSdk3_4";
			} else {
				className = "org.baole.core.ContactHelperSdk5";
			}

			/*
			 * Find the required class by name and instantiate it.
			 */
			try {
				Class<? extends ContactHelper> clazz = Class.forName(
						className).asSubclass(ContactHelper.class);
				sInstance = clazz.newInstance();
				sInstance.setActivity(ctx);
			} catch (Exception e) {
				Log.e("ContactAccessor", e.getMessage());
				e.printStackTrace();
			}
		}

		return sInstance;
	}
}
