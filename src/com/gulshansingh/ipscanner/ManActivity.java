package com.gulshansingh.ipscanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.TextView;

public class ManActivity extends Activity {

	private static final String MAN_FILE = "nmap.txt";
	private static String mManText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_man);
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Loading")
				.setMessage(
						"It may take a few seconds for the manpage to load. This will be fixed in future versions.")
				.setCancelable(false)
				.setPositiveButton("Ok", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new Thread(new ManPageLoader()).start();
					}
				}).setNegativeButton("Go back", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).create().show();
	}

	private class ManPageLoader implements Runnable {
		@Override
		public void run() {
			// if (mManText == null) {
			try {
				InputStream is = getAssets().open(MAN_FILE);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append('\n');
				}
				mManText = sb.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// }
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TextView manTextView = (TextView) findViewById(R.id.man_text);
					manTextView.setText(mManText);
				}
			});
		}
	}
}
