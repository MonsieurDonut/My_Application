package com.sami.oninecabsystem;

public class InfoPassenger {
    public String Username;
    public String FirstName;
    public String MiddleName;
    public String LastName;
    public String EmailAddress;
    public String DateOfBirth;
    public String UserType;

    public InfoPassenger(String Username, String FirstName, String MiddleName, String LastName, String EmailAddress, String DateOfBirth) {

        this.Username = Username;
        this.FirstName = FirstName;
        this.MiddleName = MiddleName;
        this.LastName = LastName;
        this.EmailAddress = EmailAddress;
        this.DateOfBirth = DateOfBirth;
        this.UserType = "Passenger";
    }
}
