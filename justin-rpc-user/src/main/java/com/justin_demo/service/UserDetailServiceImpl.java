package com.justin_demo.service;

import com.justin.client.RpcClient;
import com.justin.cmmon.rpc.booking.BookingDetailService;
import com.justin.cmmon.rpc.user.UserDetailService;
import com.justin.config.AutoRemoteInjection;
import com.justin_demo.rpc.MyUserBookingDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDetailServiceImpl implements UserDetailService {
    // Generate a proxy dynamically when Springboot starts up.
    // Inject the proxy here
    // Call the method locally and create a RPC call sent to the Booking service
    // The implementation of the BookingServiceDetail in the Booking service is responsible to produce the result
    // The result is returned to the User Service.

    // It shall be done at the phase when spring application context is ready.
    @Autowired
//    @AutoRemoteInjection(requestClientId = "demo-booking", fallbackClass = MyUserBookingDetailService.class)
    private BookingDetailService bookingDetailService;

    @Override
    public String getUserDetails(int userId) {
        // We call this RPC method as we call local methods.
        // Call the BookingDetailService remotely
        String bookingDetailsByUserId = bookingDetailService.getBookingDetailsByUserId(userId);

        return bookingDetailsByUserId;
    }
}
