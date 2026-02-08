package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button btnResetPassword;
    private TextView tvBackToLogin;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        progressBar = findViewById(R.id.progressBar);

        // Reset password button click
        btnResetPassword.setOnClickListener(v -> sendPasswordResetEmail());

        // Back to login link
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void sendPasswordResetEmail() {
        String email = emailInput.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnResetPassword.setEnabled(false);

        // Send password reset email
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnResetPassword.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Password reset email sent! Check your inbox.",
                                Toast.LENGTH_LONG).show();

                        // Clear the email field
                        emailInput.setText("");

                        // Optional: Go back to login after 2 seconds
                        emailInput.postDelayed(() -> finish(), 2000);
                    } else {
                        String errorMessage = "Failed to send reset email";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Toast.makeText(ForgotPasswordActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
