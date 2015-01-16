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

        setOnClickListener(new ClickListener(argumentName, takesArg, label));
    }

    public static List<String> getArguments() {
        return mArgGenerator.generateArgumentList();
    }

    public static void resetArgumentGenerator() {
        mArgGenerator.clear();
    }

    private class ClickListener implements OnClickListener {
        private String mArgumentName;
        private boolean mTakesArg;
        private String mLabel;

        public ClickListener(String argumentName, boolean takesArg, String label) {
            mArgumentName = argumentName;
            mTakesArg = takesArg;
            mLabel = label;
        }

        @Override
        public void onClick(View v) {
            // If the user is enabling an argument and the argument takes an
            // argument, show an input dialog
            if (isChecked()) {
                if (mTakesArg) {
                    final EditText editText = new EditText(getContext());
                    if (mArg != null) {
                        String argVal = mArg.getArg();
                        if (argVal != null) {
                            editText.setText(argVal);
                            editText.setSelection(argVal.length());
                        }
                    }
                    AlertDialog.Builder b = new AlertDialog.Builder(
                                                                    getContext());
                    b.setTitle("Enter " + mLabel)
                        .setCancelable(false)
                        .setView(editText)
                        .setPositiveButton("Set",
                                           new Dialog.OnClickListener() {
                                               @Override
                                               public void onClick(
                                                                   DialogInterface dialog,
                                                                   int which) {
                                                   String text = editText.getText()
                                                       .toString();

                                                   mArg = new Argument(mArgumentName,
                                                                       text);
                                                   boolean result = mArgGenerator
                                                       .setArg(mArg, true);
                                                   if (!result) {
                                                       setChecked(false);
                                                   }
                                               }
                                           })
                        .setNegativeButton("Cancel",
                                           new DialogInterface.OnClickListener() {
                                               public void onClick(
                                                                   DialogInterface dialog, int id) {
                                                   setChecked(false);
                                                   dialog.cancel();
                                               }
                                           }).create().show();
                } else { // Doesn't take an argument
                    mArg = new Argument(mArgumentName);
                    mArgGenerator.setArg(mArg, true);
                }
            } else { // Unchecking
                mArgGenerator.setArg(mArg, false);
            }
        }
    }
}
