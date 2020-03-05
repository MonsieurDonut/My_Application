package com.sami.oninecabsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.sami.oninecabsystem.DialogClasses.ForgotPasswordDialog;
import com.sami.oninecabsystem.DialogClasses.OperationFailedDialog;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputEditText PasswordEmail;
    private Button PasswordReset;
    private FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    DialogFragment PasswordDialog = new ForgotPasswordDialog();
    DialogFragment ErrorDialog = new OperationFailedDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        createViews();
        PasswordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = null;
                try {
                    email = PasswordEmail.getText().toString().trim();

                    fbAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                PasswordDialog.show(getSupportFragmentManager(), "AlertDialog");
                            } else
                                ErrorDialog.show(getSupportFragmentManager(), "AlertDialog");

                        }
                    });
                } catch (NullPointerException e) {
                    PasswordEmail.setError("Please enter an email address");
                    finish();
                }
            }
        });
    }

    public void createViews() {
        PasswordEmail = findViewById(R.id.etResetPassword);
        PasswordReset = findViewById(R.id.btnResetPassword);
    }

}


