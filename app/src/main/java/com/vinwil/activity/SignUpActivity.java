package com.vinwil.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.vinwil.R;
import com.vinwil.dialog.ProgressDialog;
import com.vinwil.internet.Internet;

public class SignUpActivity extends AppCompatActivity {

    private Toolbar tbSignUp;
    private EditText etName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnSignUp;
    private Button btnLogIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        tbSignUp = findViewById(R.id.tb_sign_up);
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignUp = findViewById(R.id.btn_sign_up);
        btnLogIn = findViewById(R.id.btn_log_in);

        setSupportActionBar(tbSignUp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set action in the event sign up button is clicked
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        // Set action in the event login button is clicked
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignUpActivity.super.onBackPressed();
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

    private void signUp() {
        // Custom progress dialog
        final ProgressDialog dialog = new ProgressDialog(SignUpActivity.this);
        dialog.show();

        // Check if there is internet connection
        if(!Internet.isNetworkAvailable(SignUpActivity.this)) {
            Toast.makeText(SignUpActivity.this, R.string.msg_no_network, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        final String name = etName.getText().toString().trim(); // Get name from edittext and trim whitespace
        final String phone = etPhone.getText().toString().trim(); // Get phone from edittext and trim whitespace
        final String email = etEmail.getText().toString().trim(); // Get email from edittext and trim whitespace
        String password = etPassword.getText().toString(); // Get password from edittext
        String confirmPassword = etConfirmPassword.getText().toString(); // Get password confirmation from edittext

        // Check if name is provided by user
        if(name.length() == 0) {
            Toast.makeText(SignUpActivity.this, R.string.msg_name_required, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Check if phone is provided by user
        if(phone.length() == 0) {
            Toast.makeText(SignUpActivity.this, R.string.msg_phone_required, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Check if email is provided by user
        if(email.length() == 0) {
            Toast.makeText(SignUpActivity.this, R.string.msg_email_required, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Check if email is a valid email address by regular expression
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(SignUpActivity.this, R.string.msg_email_invalid, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Check if password is provided by user
        if(password.length() == 0) {
            Toast.makeText(SignUpActivity.this, R.string.msg_password_required, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Check if confirm password is provided by user
        if(confirmPassword.length() == 0) {
            Toast.makeText(SignUpActivity.this, R.string.msg_confirm_password_required, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        //Check if password and confirmation are alike
        if(!password.equals(confirmPassword)) {
            Toast.makeText(SignUpActivity.this, R.string.msg_password_confirmation_invalid, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Sign up user using provided email and password using Firebase Authentication service
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Save user info
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        database.getReference("users").child(authResult.getUser().getUid()).child("name").setValue(name);
                        database.getReference("users").child(authResult.getUser().getUid()).child("email").setValue(email);
                        database.getReference("users").child(authResult.getUser().getUid()).child("phone").setValue(Long.valueOf(phone));
                        database.getReference("users").child(authResult.getUser().getUid()).child("billing").child("balance").setValue(0);
                        // Successful sign up navigates you to main page
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        dialog.dismiss();
                        SignUpActivity.this.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Unsuccessful sign up displays error that occurred
                        Toast.makeText(SignUpActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }

}
