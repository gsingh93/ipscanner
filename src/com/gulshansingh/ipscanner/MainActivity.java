package com.gulshansingh.ipscanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.http.conn.util.InetAddressUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String NMAP_DIR = "nmap";
	private static final String NMAP_VERSION = "5.61TEST4";

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TextView command = (TextView) findViewById(R.id.command);
		TextView results = (TextView) findViewById(R.id.results);

		if (Build.VERSION.SDK_INT >= 11) {
			command.setTextIsSelectable(true);
			results.setTextIsSelectable(true);
		}

		initNmap();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_man_nmap:
			Intent intent = new Intent(this, ManActivity.class);
			startActivity(intent);
			break;
		case R.id.action_show_ip:
			String ipAddress = getIpAddress();
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			String message;
			if (ipAddress != null) {
				message = "Your IP address is " + ipAddress;
			} else {
				message = "Could not get IP address. Please check if you have a WiFi or data connection.";
			}
			b.setTitle("IP Address").setMessage(message).create().show();
			break;
		}
		return true;
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

	public void runClicked(View v) {
		EditText editText = (EditText) findViewById(R.id.hostname);
		String host = editText.getText().toString();
		List<String> args = ArgumentToggleButton.getArguments();
		ProgressDialog progress = new ProgressDialog(this);
		progress.setCancelable(false);
		progress.setTitle("Running command");
		progress.setMessage("Running command, please wait.");
		progress.show();
		new Thread(new RunNmap(host, args, progress)).start();
	}

	public class RunNmap implements Runnable {
		private String mHost;
		private List<String> mArgs;
		private ProgressDialog mProgress;

		public RunNmap(String host, List<String> args, ProgressDialog progress) {
			mHost = host;
			mArgs = args;
			mProgress = progress;
		}

		@Override
		public void run() {
			try {
				final List<String> argList = new ArrayList<String>();
				String internalDirPath = getFilesDir().getCanonicalPath();
				argList.add(internalDirPath + "/nmap/bin/nmap");
				argList.add(mHost);
				argList.addAll(mArgs);

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setCommandTextView(argList);
					}
				});

				ProcessBuilder pb = new ProcessBuilder(argList);
				pb.redirectErrorStream(true);
				Process process = pb.start();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						process.getInputStream()));
				String line;
				try {
					final StringBuilder sb = new StringBuilder();
					while ((line = br.readLine()) != null) {
						sb.append(line).append('\n');
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							TextView results = (TextView) findViewById(R.id.results);
							results.setTypeface(Typeface.MONOSPACE);
							results.setText(sb.toString());
						}
					});

				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				mProgress.dismiss();
			}
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

	private String getIpAddress() {
		// Fallback address
		String ipv6Addr = null;

		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf
						.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase(
								Locale.US);
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (isIPv4) {
							return sAddr;
						} else {
							ipv6Addr = sAddr;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ipv6Addr;
	}
}
