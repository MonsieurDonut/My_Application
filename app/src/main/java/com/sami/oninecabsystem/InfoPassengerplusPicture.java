package com.sami.oninecabsystem;

import android.net.Uri;

public class InfoPassengerplusPicture {
    public String Username;
    public String FirstName;
    public String MiddleName;
    public String LastName;
    public String EmailAddress;
    public String DateOfBirth;
    public String UserType;
    public String ProfilePictureURL;

    public InfoPassengerplusPicture(String Username, String FirstName, String MiddleName, String LastName, String EmailAddress, String DateOfBirth, Uri uri) {

        this.Username = Username;
        this.FirstName = FirstName;
        this.MiddleName = MiddleName;
        this.LastName = LastName;
        this.EmailAddress = EmailAddress;
        this.DateOfBirth = DateOfBirth;
        this.UserType = "Passenger";
        String string = uri.toString();
        this.ProfilePictureURL=string;
    }
}
