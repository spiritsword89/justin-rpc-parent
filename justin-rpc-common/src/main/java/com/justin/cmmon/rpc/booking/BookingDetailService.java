package com.justin.cmmon.rpc.booking;

import com.justin.config.RemoteService;

public interface BookingDetailService extends RemoteService {

    //RPC method
    public String getBookingDetailsByUserId(int userId);

    //100 methods
    //Not RPC methode
    public String getBookingInformationByUserId(int userId);
}
