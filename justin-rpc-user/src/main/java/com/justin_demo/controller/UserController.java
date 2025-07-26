package com.justin_demo.controller;

import com.justin.cmmon.rpc.user.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserDetailService userDetailService;

    @GetMapping("/details/{id}")
    public String getUserDetails(@PathVariable(name = "id") int userId) {
        return userDetailService.getUserDetails(userId);
    }
}
