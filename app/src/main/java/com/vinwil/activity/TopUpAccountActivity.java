package com.vinwil.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.vinwil.R;
import com.vinwil.data.Keys;
import com.vinwil.dialog.ProgressDialog;
import com.vinwil.internet.Internet;

import java.util.Calendar;

public class TopUpAccountActivity extends AppCompatActivity {

    private Toolbar tbTopUpAccount;
    private EditText etAmount;
    private EditText etMpesaPin;
    private Button btnTopUp;

    private double balance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up_account);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        balance = getIntent().getDoubleExtra(Keys.EXTRA_BALANCE, 0);

        tbTopUpAccount = findViewById(R.id.tb_top_up_account);
        etAmount = findViewById(R.id.et_amount);
        etMpesaPin = findViewById(R.id.et_mpesa_pin);
        btnTopUp = findViewById(R.id.btn_top_up);

        setSupportActionBar(tbTopUpAccount);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set action in the event top up button is clicked
        btnTopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                topUp();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return false;
        }
    }

    private void topUp() {
        // Custom progress dialog
        final ProgressDialog dialog = new ProgressDialog(TopUpAccountActivity.this);
        dialog.show();

        // Check if there is internet connection
        if(!Internet.isNetworkAvailable(TopUpAccountActivity.this)) {
            Toast.makeText(TopUpAccountActivity.this, R.string.msg_no_network, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        final String amount = etAmount.getText().toString().trim(); // Get amount from edittext and trim whitespace
        final String mpesaPin = etMpesaPin.getText().toString().trim(); // Get mpesa pin from edittext and trim whitespace

        // Check if amount is provided by user
        if(amount.length() == 0) {
            Toast.makeText(TopUpAccountActivity.this, R.string.msg_amount_required, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Check if mpesa pin is provided by user
        if(mpesaPin.length() == 0) {
            Toast.makeText(TopUpAccountActivity.this, R.string.msg_mpesa_pin_required, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        long createdAt = calendar.getTimeInMillis();

        // Save payment info
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String id = database.getReference().push().getKey();
        String amt = "Top up Ksh ".concat(String.valueOf(amount)).concat(" via MPESA Online");
        database.getReference("users").child(auth.getUid()).child("billing").child("paymentHistory").child(id).child("amount").setValue(amt);
        database.getReference("users").child(auth.getUid()).child("billing").child("paymentHistory").child(id).child("createdAt").setValue(createdAt);
        double newBalance = balance + Double.parseDouble(amount);
        database.getReference("users").child(auth.getUid()).child("billing").child("balance").setValue(newBalance);
        TopUpAccountActivity.this.finish();
    }

}
