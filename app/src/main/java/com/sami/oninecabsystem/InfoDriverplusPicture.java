package com.sami.oninecabsystem;

import android.net.Uri;

public class InfoDriverplusPicture {
    public String Username;
    public String FirstName;
    public String MiddleName;
    public String LastName;
    public String EmailAddress;
    public String DateOfBirth;
    public String UserType;
    public String LicenseID;
    public String Registration;
    public String Insurance;
    public String Rating;
    public String RatingCount;
    public String ProfilePictureURL;

    public InfoDriverplusPicture(String Username, String FirstName, String MiddleName, String LastName, String EmailAddress, String DateOfBirth, String LicenseID, String Registration, String Insurance, Uri uri) {

        this.Username = Username;
        this.FirstName = FirstName;
        this.MiddleName = MiddleName;
        this.LastName = LastName;
        this.EmailAddress = EmailAddress;
        this.DateOfBirth = DateOfBirth;
        this.UserType = "Taxi";
        this.LicenseID = LicenseID;
        this.Registration = Registration;
        this.Insurance = Insurance;
        String string = uri.toString();
        this.ProfilePictureURL=string;
        this.Rating="0";
        this.RatingCount="0";
    }
}