package com.gulshansingh.ipscanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class ArgumentToggleButton extends ToggleButton {

	private static final ArgumentGenerator mArgGenerator = new ArgumentGenerator();
	private String mArgument;

	public ArgumentToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public ArgumentToggleButton(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		String text = null;

		// Parse attributes
		TypedArray arr = getContext().obtainStyledAttributes(attrs,
				R.styleable.ArgumentToggleButton);
		text = arr.getString(R.styleable.ArgumentToggleButton_android_text);
		mArgument = arr.getString(R.styleable.ArgumentToggleButton_argument);
		if (mArgument == null) {
			throw new RuntimeException("Argument is null");
		}
		arr.recycle();

		// Set button text
		String buttonText;
		if (text != null) {
			buttonText = text;
		} else {
			buttonText = mArgument;
		}
		setTextOn(buttonText);
		setTextOff(buttonText);
		setText(buttonText);

		setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mArgGenerator.setArg(mArgument, isChecked);
			}
		});
	}
}
