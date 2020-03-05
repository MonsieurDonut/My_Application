package com.sami.oninecabsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sami.oninecabsystem.DialogClasses.ChooseRegUserDialog;
import com.sami.oninecabsystem.DialogClasses.ChooseSignInUserDialog;

public class MainActivity extends AppCompatActivity {


    private TextView forgotPass;
    private TextView newUser;
    private TextView pleasewait;
    private Button login;
    private TextInputEditText username;
    private TextInputEditText password;
    private ProgressBar progressBar;
    private FirebaseAuth fbAuth;
    DialogFragment ChooseRegUser = new ChooseRegUserDialog();
    DialogFragment ChooseSignInUser = new ChooseSignInUserDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupViews();
        progressBar.setVisibility(View.INVISIBLE);
        fbAuth = FirebaseAuth.getInstance();
  //      if(fbAuth!=null)
 //           fbAuth.signOut();
        FirebaseUser user = fbAuth.getCurrentUser();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
        ||ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_DENIED
        ||ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            startActivity(new Intent(MainActivity.this, PermissionsActivity.class));
        if (user != null) {
            final Intent intent = new Intent(MainActivity.this, PlaceHolderActivity.class);
            startActivity(intent);
        }

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class));
            }
        });

        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseRegUser.show(getSupportFragmentManager(), "AlertDialog");
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login();
            }

        });

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                username.setError(null);
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                password.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                password.setError(null);
            }
        });
    }

    public void setupViews() {
        setContentView(R.layout.activity_main);
        username = findViewById(R.id.etUsername);
        password = findViewById((R.id.etPassword));
        login = findViewById(R.id.btnLogin);
        forgotPass = findViewById(R.id.tvForgotPassword);
        newUser = findViewById(R.id.tvNewUser);
        progressBar = findViewById(R.id.progressBarLogin);
        pleasewait = findViewById(R.id.tvPleaseWait);
    }

    public void Login() {

        progressBar.setVisibility(View.VISIBLE);
        pleasewait.setVisibility(View.VISIBLE);

        int exceptionType = 0;

        if (username.getText().toString().length() <= 0)
            exceptionType = 1;


        if (password.getText().toString().length() <= 0)
            exceptionType = 2;

        if (username.getText().toString().length() <= 0 && password.getText().toString().length() <= 0)
            exceptionType = 3;
        try {
            fbAuth.signInWithEmailAndPassword(username.getText().toString().trim(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                        emailVerification();
                    else
                        Toast.makeText(MainActivity.this, "Incorrect Password/Username", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    pleasewait.setVisibility(View.INVISIBLE);

                }
            });
        } catch (IllegalArgumentException e) {
            switch (exceptionType) {
                case 1:
                    username.setError(getString(R.string.noUsername));
                    username.requestFocus();
                    break;
                case 2:
                    password.setError(getString(R.string.noPassword));
                    password.requestFocus();
                    break;
                case 3:
                    username.setError(getString(R.string.noUsername));
                    password.setError(getString(R.string.noPassword));
                    username.requestFocus();
                    break;
            }
            progressBar.setVisibility(View.INVISIBLE);
            pleasewait.setVisibility(View.INVISIBLE);
        }

    }

    public void emailVerification() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean emailFlag = fbUser.isEmailVerified();
        if (emailFlag) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(fbAuth.getCurrentUser().getUid()).child("UserType");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String userType = dataSnapshot.getValue(String.class);
                    if (userType.equals("Passenger")) {
                        startActivity(new Intent(MainActivity.this, MainPassengerActivity.class));
                        finish();
                    } else
                        ChooseSignInUser.show(getSupportFragmentManager(), "AlertDialog");

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Please verify your email to continue", Toast.LENGTH_SHORT).show();
            fbAuth.signOut();
        }

    }


}
