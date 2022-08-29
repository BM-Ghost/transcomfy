package com.vinwil.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.vinwil.R;
import com.vinwil.dialog.ProgressDialog;
import com.vinwil.internet.Internet;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogIn;
    private Button btnSignUp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        if(auth.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            MainActivity.this.finish();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogIn = findViewById(R.id.btn_log_in);
        btnSignUp = findViewById(R.id.btn_sign_up);

        // Set action in the event log in button is clicked
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();
            }
        });

        // Set action in the event sign up button is clicked
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });
    }

    /**
     * Log in users based on username(email) and password
     */
    private void logIn() {
        // Custom progress dialog
        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.show();

        // Check if there is internet connection
        if(!Internet.isNetworkAvailable(MainActivity.this)) {
            Toast.makeText(MainActivity.this, R.string.msg_no_network, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        String username = etUsername.getText().toString().trim(); // Get username from edittext and trim whitespace
        String password = etPassword.getText().toString(); // Get password from edittext

        // Check if username is provided by user
        if(username.length() == 0) {
            Toast.makeText(MainActivity.this, R.string.msg_username_required, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Check if password is provided by user
        if(password.length() == 0) {
            Toast.makeText(MainActivity.this, R.string.msg_password_required, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Check if username is a valid email address by regular expression
        if(!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            Toast.makeText(MainActivity.this, R.string.msg_username_invalid, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Sign in user using provided username and password using Firebase Authentication service
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(username, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Successful sign in navigates you to home page
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                        MainActivity.this.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Unsuccessful sign in displays error that occurred
                        Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }

    private void signUp() {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

}
