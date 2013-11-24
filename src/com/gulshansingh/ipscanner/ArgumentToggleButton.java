package com.gulshansingh.ipscanner;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

public class ArgumentToggleButton extends ToggleButton {

	private static final ArgumentGenerator mArgGenerator = new ArgumentGenerator();
	private Argument mArg;

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
		final String argumentName = arr
				.getString(R.styleable.ArgumentToggleButton_argument);
		final boolean takesArg = arr.getBoolean(
				R.styleable.ArgumentToggleButton_takes_arg, false);
		if (argumentName == null) {
			throw new RuntimeException(
					"ArgumentToggleButton requires argument attribute");
		}
		final String label;
		if (takesArg) {
			label = arr.getString(R.styleable.ArgumentToggleButton_label);
			if (label == null) {
				throw new RuntimeException(
						"ArgumentToggleButton requires label attribute when takes_arg is true");
			}
		} else {
			label = null;
		}
		arr.recycle();

		// Set button text
		String buttonText;
		if (text != null) {
			buttonText = text;
		} else {
			buttonText = argumentName;
		}
		setTextOn(buttonText);
		setTextOff(buttonText);
		setText(buttonText);

		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Error e = new Error();
				// If the user is enabling an argument and the argument takes an
				// argument, show an input dialog
				if (isChecked() && takesArg) {
					final EditText editText = new EditText(getContext());
					AlertDialog.Builder b = new AlertDialog.Builder(
							getContext());
					// View inputView =
					// LayoutInflater.from(getContext()).inflate(
					// R.layout.argument_input, null);
					b.setTitle("Enter " + label)
							.setMessage(label + ":")
							.setView(editText)
							.setPositiveButton("Set",
									new Dialog.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											String text = editText.getText()
													.toString();

											mArg = new Argument(argumentName,
													text);
											mArgGenerator.setArg(mArg, true);
										}
									}).create().show();
				} else {
					mArg = new Argument(argumentName);
					mArgGenerator.setArg(mArg, isChecked());
				}

				if (e.error) {
					setChecked(false);
				}
			}
		});
	}

	private static class Error {
		public boolean error;
	}

	public static List<String> getArguments() {
		return mArgGenerator.generateArgumentList();
	}

	public static void resetArgumentGenerator() {
		mArgGenerator.clear();
	}
}
