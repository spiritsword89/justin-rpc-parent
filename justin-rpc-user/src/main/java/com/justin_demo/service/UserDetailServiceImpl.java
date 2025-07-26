package com.justin_demo.service;

import com.justin.client.RpcClient;
import com.justin.cmmon.rpc.booking.BookingDetailService;
import com.justin.cmmon.rpc.user.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDetailServiceImpl implements UserDetailService {
    // Generate a proxy dynamically when Springboot starts up.
    // Inject the proxy here
    // Call the method locally and create a RPC call sent to the Booking service
    // The implementation of the BookingServiceDetail in the Booking service is responsible to produce the result
    // The result is returned to the User Service.

    @Autowired
    private RpcClient rpcClient;

    @Override
    public String getUserDetails(int userId) {
        // We call this RPC method as we call local methods.
        // Call the BookingDetailService remotely
        BookingDetailService bookingDetailService = rpcClient.generate(BookingDetailService.class, "demo-booking");

        return bookingDetailService.getBookingDetailsByUserId(userId);
    }
}
