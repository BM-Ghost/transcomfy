package com.transcomfy.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.widget.TextView;

import com.transcomfy.R;

public class ProgressDialog extends AppCompatDialog {

    private TextView tvMessage;
    private String message;

    public ProgressDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress);

        tvMessage = findViewById(R.id.tv_message);

        if(message != null && message.length() == 0) {
            tvMessage.setText(message);
        }

        setCancelable(false);
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
