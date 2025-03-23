package com.lordscave.hotel_lord.Entities;

import java.time.LocalDate;

public class OndeskBooking {
    private final String fullName, contactNumber, email, gender, address, paymentStatus = "Pending";
    private final int age, guests, roomId;
    private final LocalDate checkinDate, checkoutDate;

    public OndeskBooking(String fullName, String email, String contactNumber, int age, String gender, String address, int guests, int roomId, LocalDate checkinDate, LocalDate checkoutDate) {
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.age = age;
        this.gender = gender;
        this.address = address;
        this.guests = guests;
        this.roomId = roomId;
        this.checkinDate = checkinDate;
        this.checkoutDate = checkoutDate;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getContactNumber() { return contactNumber; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getAddress() { return address; }
    public int getGuests() { return guests; }
    public int getRoomId() { return roomId; }
    public LocalDate getCheckinDate() { return checkinDate; }
    public LocalDate getCheckoutDate() { return checkoutDate; }
    public String getPaymentStatus() { return paymentStatus; }
}
