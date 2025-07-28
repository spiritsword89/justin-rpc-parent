package com.justin_demo.service;

import com.justin.cmmon.rpc.booking.BookingDetailService;
import com.justin.config.AutoRemoteInjection;
import com.justin_demo.rpc.UserBookingDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class AdminDetailServiceImpl {

    @Autowired
    private UserBookingDetailService bookingDetailService;
}
