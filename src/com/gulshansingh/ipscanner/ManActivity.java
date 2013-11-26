package com.gulshansingh.ipscanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class ManActivity extends Activity {

	private static final String MAN_FILE = "nmap.html";
	private static String mManText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_man);
		new Thread(new ManPageLoader()).start();
	}

	private class ManPageLoader implements Runnable {
		@Override
		public void run() {
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
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					StringBuilder sb = new StringBuilder("<html><body><pre>");
					sb.append(mManText);
					sb.append("</pre></body></html>");
					WebView manWebView = (WebView) findViewById(R.id.man_text);
					manWebView.loadData(sb.toString(), "text/html", "UTF-8");
				}
			});
		}
	}
}
