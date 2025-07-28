package com.justin_demo.rpc;

import com.justin.cmmon.rpc.booking.BookingDetailService;

public class MyUserBookingDetailService implements BookingDetailService {
    @Override
    public String getBookingDetailsByUserId(int userId) {
        return "Booking Detail Service is now falling back.....";
    }

    @Override
    public String getBookingInformationByUserId(int userId) {
        return "Booking Detail Service is now falling back.....";
    }
}
