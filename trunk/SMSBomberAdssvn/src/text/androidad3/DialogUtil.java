package text.androidad3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogUtil {

	public static final Dialog createChangLogDialog(Context context) {
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.updates_title);
		final String[] changes = context.getResources().getStringArray(
				R.array.updates);
		
		final StringBuilder buf = new StringBuilder(changes[0]);
		for (int i = 1; i < changes.length; i++) {
			buf.append("\n\n");
			buf.append(changes[i]);
		}
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setMessage(buf.toString());
		builder.setCancelable(true);
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						//write current version
						dialog.cancel();
					}
				});
		return builder.create();
	}
}
