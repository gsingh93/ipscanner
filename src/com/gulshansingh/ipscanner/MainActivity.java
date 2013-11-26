package com.gulshansingh.ipscanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
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
				message = "Could not get IP address. Are you sure you're connected to a WiFi network?";
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

	private String getIpAddress() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

		// Convert little-endian to big-endian if needed
		if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
			ipAddress = Integer.reverseBytes(ipAddress);
		}

		byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

		String ipAddressString;
		try {
			ipAddressString = InetAddress.getByAddress(ipByteArray)
					.getHostAddress();
		} catch (UnknownHostException ex) {
			Log.e("MainActivity", "Unable to get host address.");
			ipAddressString = null;
		}

		return ipAddressString;
	}
}
