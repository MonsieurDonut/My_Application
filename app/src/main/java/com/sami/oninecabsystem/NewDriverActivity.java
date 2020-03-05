package com.sami.oninecabsystem;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sami.oninecabsystem.DialogClasses.EmailVerificationDialog;
import com.sami.oninecabsystem.DialogClasses.OperationFailedDialog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewDriverActivity extends AppCompatActivity {
    private TextInputEditText regUsername, regPassword, regPasswordConfirm, regEmail, regFN, regMN, regLN, regRegistration, regLicenseID, regInsurance;
    private EditText regDOB;
    private FirebaseAuth fbAuth;
    private TextView AlreadyRegistered, pleaseWait;
    private Button Register, ChangeProfilePic;
    private String UniqueID;
    private ImageView ProfilePic;
    private Uri resultsUri,downloadUri;
    ProgressBar progressBar;
    DialogFragment EmailVerificationDialog = new EmailVerificationDialog();
    DialogFragment ErrorDialog = new OperationFailedDialog();
    final Calendar myCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_driver);
        createViews();
        fbAuth = FirebaseAuth.getInstance();
        ProfilePic.setImageResource(R.mipmap.ic_default_user);

        ChangeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        regDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(NewDriverActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        regDOB.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    new DatePickerDialog(NewDriverActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        });

        AlreadyRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewDriverActivity.this, MainActivity.class));
            }
        });


        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                pleaseWait.setVisibility(View.VISIBLE);
                CreateUser();
                progressBar.setVisibility(View.INVISIBLE);
                pleaseWait.setVisibility(View.INVISIBLE);
            }
        });
        regUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                regUsername.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                regUsername.setError(null);
            }
        });
        regPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                regPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                regPassword.setError(null);
            }
        });
        regPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                regPasswordConfirm.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                regPasswordConfirm.setError(null);
            }
        });
        regEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                regEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                regEmail.setError(null);
            }
        });
        regDOB.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                regDOB.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                regDOB.setError(null);
            }
        });
        regFN.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                regFN.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                regFN.setError(null);
            }
        });
        regLN.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                regLN.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                regLN.setError(null);
            }
        });
    }

    public void createViews() {
        regUsername = findViewById(R.id.etRegUsername);
        regPassword = findViewById(R.id.etRegPassword);
        regPasswordConfirm = findViewById(R.id.etRegConfirmPassword);
        regEmail = findViewById(R.id.etRegEmail);
        regFN = findViewById(R.id.etRegFirstName);
        regMN = findViewById(R.id.etRegMiddleName);
        regLN = findViewById(R.id.etRegLastName);
        regDOB = findViewById(R.id.etRegDOB);
        regLicenseID = findViewById(R.id.etRegLicenseID);
        regRegistration = findViewById(R.id.etRegRegistration);
        regInsurance = findViewById(R.id.etRegInsurance);
        Register = findViewById(R.id.btnRegNewAcc);
        AlreadyRegistered = findViewById(R.id.tvAlreadyhaveAcc);
        progressBar = findViewById(R.id.progressBarNewAcc);
        pleaseWait = findViewById(R.id.tvPleaseWait);
        ChangeProfilePic = findViewById(R.id.btnRegDriverChangeProfilePic);
        ProfilePic = findViewById(R.id.RegDriverProfileImage);
    }

    public boolean validate() {
        final boolean[] validated = {true};
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

        if (regUsername.getText().toString().length() <= 0) {
            regUsername.setError("Please enter a username");
            validated[0] = false;
        } else {
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.child(regUsername.getText().toString()).exists()) {
                            regUsername.setError("This username is already being used. Please pick another one");
                            validated[0] = false;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(NewDriverActivity.this, "Error in checking username", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (regPassword.getText().toString().length() <= 5) {
            regPassword.setError("Password needs to be at least 6 characters");
            validated[0] = false;
        }
        if (!(regPassword.getText().toString().equals(regPasswordConfirm.getText().toString()))) {
            regPasswordConfirm.setError("Passwords need to match");
            validated[0] = false;
        }

        if (regFN.getText().toString().length() <= 0) {
            regFN.setError("Please enter first name");
            validated[0] = false;
        }
        if (regLN.getText().toString().length() <= 0) {
            regLN.setError("Please enter last name");
            validated[0] = false;
        }
        if (!emailFormatVerification()) {
            regEmail.setError("Please enter a valid email address");
            validated[0] = false;
        } else {
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.child(regUsername.getText().toString()).exists()) {
                            regEmail.setError("This email is already being used. Please pick another one");
                            validated[0] = false;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(NewDriverActivity.this, "Error in checking emails", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (checkIf18())
            if (regInsurance.getText().toString().length() <= 0) {
                regUsername.setError("Please enter an insurance company");
                validated[0] = false;
            }
        if (regRegistration.getText().toString().length() <= 0) {
            regUsername.setError("Please enter a registration plate number");
            validated[0] = false;
        }
        if (regLicenseID.getText().toString().length() <= 0) {
            regUsername.setError("Please enter a license ID");
            validated[0] = false;
        }
        return validated[0];
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        regDOB.setText(sdf.format(myCalendar.getTime()));
    }

    public boolean checkIf18() {
        boolean is18check = false;
        try {
            //since we are coding with API 19 the function 'period' cannot be used
            //as a result we will use a very long basic code to do the job
            int years;
            int months;
            int days;
            String strMonths = regDOB.getText().toString().substring(0, 2);
            String strDays = regDOB.getText().toString().substring(3, 5);
            String strYears = (regDOB.getText().toString().substring(6, 10));
            String myFormat = "MMddyyyy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            Date birthDate = sdf.parse(strMonths + strDays + strYears);

            //create calendar object for birth day
            Calendar birthDay = Calendar.getInstance();
            birthDay.setTimeInMillis(birthDate.getTime());

            //create calendar object for current day
            long currentTime = System.currentTimeMillis();
            Calendar now = Calendar.getInstance();
            now.setTimeInMillis(currentTime);

            //Get difference between years
            years = now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
            int currMonth = now.get(Calendar.MONTH) + 1;
            int birthMonth = birthDay.get(Calendar.MONTH) + 1;

            //Get difference between months
            months = currMonth - birthMonth;

            //if month difference is in negative then reduce years by one
            //and calculate the number of months.
            if (months < 0) {
                years--;
                months = 12 - birthMonth + currMonth;
                if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE))
                    months--;
            } else if (months == 0 && now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
                years--;
                months = 11;
            }

            //Calculate the days
            if (now.get(Calendar.DATE) > birthDay.get(Calendar.DATE))
                days = now.get(Calendar.DATE) - birthDay.get(Calendar.DATE);
            else if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
                int today = now.get(Calendar.DAY_OF_MONTH);
                now.add(Calendar.MONTH, -1);
                days = now.getActualMaximum(Calendar.DAY_OF_MONTH) - birthDay.get(Calendar.DAY_OF_MONTH) + today;
            } else {
                days = 0;
                if (months == 12) {
                    years++;
                    months = 0;
                }
            }
            if (years < 18)
                is18check = true;
        } catch (StringIndexOutOfBoundsException e) {
            regDOB.setError("Please enter a date");
            is18check = false;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return is18check;
    }

    public void emailVerification() {
        FirebaseUser fbUser = fbAuth.getCurrentUser();
        if (fbUser != null) {
            fbUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        ErrorDialog.show(getSupportFragmentManager(), "AlertDialog");
                    } else sendUserInfo();
                }
            });
        }
    }

    public boolean emailFormatVerification() {
        String email = regEmail.getText().toString();
        String emailRegex = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void sendUserInfo() {
        FirebaseDatabase fbDb = FirebaseDatabase.getInstance();
        fbAuth.signInWithEmailAndPassword(regEmail.getText().toString(), regPassword.getText().toString());
        FirebaseUser fbUser = fbAuth.getCurrentUser();
        final DatabaseReference ref = fbDb.getReference();
        final String regusername, regfn, regmn, regln, regemail, regdob, regregistration, reginsurance, reglicenseid;

        regusername = regUsername.getText().toString().trim();
        regfn = regFN.getText().toString().trim();
        regln = regLN.getText().toString().trim();
        regemail = regEmail.getText().toString().trim();
        regdob = regDOB.getText().toString().trim();
        regregistration = regRegistration.getText().toString().trim();
        reginsurance = regInsurance.getText().toString().trim();
        reglicenseid = regLicenseID.getText().toString().trim();
        if (regMN.getText().toString().isEmpty())
            regmn = "-";
        else
            regmn = regMN.getText().toString().trim();
        if (resultsUri != null) {
            final StorageReference ProfilePicRef = FirebaseStorage.getInstance().getReference().child("Profile_Pics").child(UniqueID);
            Bitmap bitmap = null;
            try {

                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultsUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = ProfilePicRef.putBytes(data);
            final Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return ProfilePicRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    downloadUri= task.getResult();

                }
            });
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InfoDriverplusPicture infoDriver = new InfoDriverplusPicture(regusername, regfn, regmn, regln, regemail, regdob, reglicenseid, regregistration, reginsurance, downloadUri);
                    ref.child("Users").child(UniqueID).setValue(infoDriver).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                EmailVerificationDialog.show(getSupportFragmentManager(), "AlertDialog");
                                fbAuth.signOut();
                            } else {
                                ErrorDialog.show(getSupportFragmentManager(), "AlertDialog");
                                // fbAuth.signOut();
                            }
                        }
                    });
                }
            }, 5000);


        } else {
            InfoDriver infoDriver = new InfoDriver(regusername, regfn, regmn, regln, regemail, regdob, reglicenseid, regregistration, reginsurance);
            ref.child("Users").child(UniqueID).setValue(infoDriver).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        EmailVerificationDialog.show(getSupportFragmentManager(), "AlertDialog");
                        fbAuth.signOut();
                    } else {
                        ErrorDialog.show(getSupportFragmentManager(), "AlertDialog");
                        //                    //fbAuth.signOut();
                    }
                }
            });
        }

    }

    public void CreateUser() {
        if (validate()) {
            fbAuth.createUserWithEmailAndPassword(regEmail.getText().toString().trim(), regPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        UniqueID = fbAuth.getUid();
                        emailVerification();
                    } else{
                        ErrorDialog.show(getSupportFragmentManager(), "AlertDialog");
                    fbAuth.signOut();}
                }
            });


        } else
            Toast.makeText(NewDriverActivity.this, "Please make all necessary changes and try again", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri ImageUri = data.getData();
            resultsUri = ImageUri;
            ProfilePic.setImageURI(resultsUri);
        }
    }
}

