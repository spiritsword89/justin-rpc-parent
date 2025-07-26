package com.justin_demo.booking.service;

import com.justin.cmmon.rpc.booking.BookingDetailService;
import com.justin.config.MarkAsRpc;
import org.springframework.stereotype.Component;

//we do not accept interface or abstract
@Component
public class BookingDetailServiceImpl implements BookingDetailService {

    @MarkAsRpc
    @Override
    public String getBookingDetailsByUserId(int userId) {
        return "Booking Service is called, and the user id is : " + userId;
    }

    @Override
    public String getBookingInformationByUserId(int userId) {
        return "";
    }
}
