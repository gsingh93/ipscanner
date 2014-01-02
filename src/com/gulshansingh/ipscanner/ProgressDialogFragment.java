package com.gulshansingh.ipscanner;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {

    public static ProgressDialogFragment newInstance(String title,
                                                     String message) {
        Bundle b = new Bundle();
        b.putString("title", title);
        b.putString("message", message);

        ProgressDialogFragment f = new ProgressDialogFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle b = getArguments();
        ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(b.getString("title"));
        progress.setMessage(b.getString("message"));
        return progress;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }
}
