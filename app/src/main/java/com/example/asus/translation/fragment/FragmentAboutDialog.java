package com.example.asus.translation.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class FragmentAboutDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("关于")
                .setMessage(
                        "com.windplume.pri.translation" + "\n" + "\n" +
                        "https://github.com/wplume/Translation" + "\n" + "\n" +
                        "windplume.mail@gmail.com" + "\n" +
                        "337540029@qq.com" + "\n" + "\n" +
                        "Licensed under the Apache License, Version 2.0(the /'the License/'')" + "\n" +
                        "http://www.apache.org/licenses/LICENSE-2.0.html")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return builder.create();
    }
}

