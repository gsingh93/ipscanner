package com.gulshansingh.ipscanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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

		initNmap();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ArgumentToggleButton.resetArgumentGenerator();
	}

	private void initNmap() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		String version = sp.getString("NMAP_VERSION", "");
		if (!version.equals(NMAP_VERSION)) {
			ProgressDialog p = ProgressDialog
					.show(MainActivity.this, "Installing Nmap",
							"Please wait while Nmap is installed. This should only happen once.");
			new Thread(new InstallNmapTask(p)).start();
		}
	}

	private class InstallNmapTask implements Runnable {
		private ProgressDialog mProgress;

		public InstallNmapTask(ProgressDialog progress) {
			mProgress = progress;
		}

		@Override
		public void run() {
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(MainActivity.this);

			try {
				FileUtils.extractAssetDirectory(MainActivity.this, NMAP_DIR);
				FileUtils.chmod(getFilesDir().getCanonicalPath() + '/'
						+ NMAP_DIR, "744", true);
				boolean result = sp.edit()
						.putString("NMAP_VERSION", NMAP_VERSION).commit();
				if (!result) {
					Log.e("MainActivity", "Writing new version failed");
					throw new IOException();
				}
			} catch (IOException e) {
				e.printStackTrace();

				mProgress.dismiss();

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						new AlertDialog.Builder(MainActivity.this)
								.setMessage(
										"Nmap installation failed, please try again. If the problem continues, please contact the developer. Exiting.")
								.setPositiveButton("Exit",
										new OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												finish();
											}
										}).create().show();
					}
				});
			}
			mProgress.dismiss();
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
		List<String> args = ArgumentToggleButton.getArguments();
		runNmap(host, args);
	}

	public void runNmap(String host, List<String> args) {
		try {
			List<String> argList = new ArrayList<String>();
			String internalDirPath = getFilesDir().getCanonicalPath();
			argList.add(internalDirPath + "/nmap/bin/nmap");
			argList.add(host);
			argList.addAll(args);

			setCommandTextView(argList);

			ProcessBuilder pb = new ProcessBuilder(argList);
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

	private void setCommandTextView(List<String> args) {
		TextView commandView = (TextView) findViewById(R.id.command);
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String arg : args) {
			if (first) {
				sb.append("nmap");
				first = false;
			} else {
				sb.append(arg);
			}
			sb.append(" ");
		}
		commandView.setText(sb.toString());
	}
}
