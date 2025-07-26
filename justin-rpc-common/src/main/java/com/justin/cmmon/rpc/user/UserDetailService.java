package com.justin.cmmon.rpc.user;

import com.justin.config.RemoteService;

public interface UserDetailService extends RemoteService {
    public String getUserDetails(int userId);
}
