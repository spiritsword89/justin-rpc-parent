package com.justin_demo.rpc;

import com.justin.cmmon.rpc.booking.BookingDetailService;
import com.justin.config.AutoRemoteInjection;

@AutoRemoteInjection(requestClientId = "demo-booking", fallbackClass = MyUserBookingDetailService.class)
public interface UserBookingDetailService extends BookingDetailService {
}
