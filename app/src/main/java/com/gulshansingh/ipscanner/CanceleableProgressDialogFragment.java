package com.gulshansingh.ipscanner;

import android.content.Context;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

public class CanceleableProgressDialogFragment extends ProgressDialogFragment {

    public static ProgressDialogFragment newInstance(String title,
                                                     String message) {
        ProgressDialogFragment f = ProgressDialogFragment.newInstance(title, message);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog progress = super.onCreateDialog(savedInstanceState);
        Context c = getActivity();
        progress.addContentView(new Button(c), new LayoutParams(100, 100));
        return progress;
    }
}
