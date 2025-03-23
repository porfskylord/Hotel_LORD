package com.lordscave.hotel_lord.Entities;

public class BookingRequest {
    private int id;
    private String bookingNo, bookingType, guestName, contactNumber, checkinDate, checkoutDate, bookingStatus, roomType;

    public BookingRequest(int id, String bookingNo, String bookingType, String guestName, String contactNumber, String checkinDate, String checkoutDate, String bookingStatus, String roomType) {
        this.id = id;
        this.bookingNo = bookingNo;
        this.bookingType = bookingType;
        this.guestName = guestName;
        this.contactNumber = contactNumber;
        this.checkinDate = checkinDate;
        this.checkoutDate = checkoutDate;
        this.bookingStatus = bookingStatus;
        this.roomType = roomType;
    }

    public int getId() { return id; }
    public String getBookingNo() { return bookingNo; }
    public String getBookingType() { return bookingType; }
    public String getGuestName() { return guestName; }
    public String getContactNumber() { return contactNumber; }
    public String getCheckinDate() { return checkinDate; }
    public String getCheckoutDate() { return checkoutDate; }
    public String getBookingStatus() { return bookingStatus; }
    public String getRoomType() { return roomType; }
}
