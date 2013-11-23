package com.gulshansingh.ipscanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String NMAP_DIR = "nmap";
	private static final String NMAP_VERSION = "5.61TEST4";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try {
			initNmap();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void runNmap(String host) {
		try {
			String internalDirPath = getFilesDir().getCanonicalPath();
			ProcessBuilder pb = new ProcessBuilder(internalDirPath
					+ "/nmap/bin/nmap", host);
			pb.redirectErrorStream(true);
			Process process = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line;
			try {
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line).append('\n');
				}
				TextView results = (TextView) findViewById(R.id.results);
				results.setTypeface(Typeface.MONOSPACE);
				results.setText(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initNmap() throws IOException {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		String version = sp.getString("NMAP_VERSION", "");
		if (!version.equals(NMAP_VERSION)) {
			FileUtils.extractAssetDirectory(this, NMAP_DIR);
			FileUtils.chmod(getFilesDir().getCanonicalPath() + '/' + NMAP_DIR,
					"744", true);
			boolean result = sp.edit().putString("NMAP_VERSION", NMAP_VERSION)
					.commit();
			if (!result) {
				Log.e("MainActivity", "Writing new version failed");
				throw new IOException();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void runClicked(View v) {
		EditText editText = (EditText) findViewById(R.id.hostname);
		String host = editText.getText().toString();
		runNmap(host);
	}
}
