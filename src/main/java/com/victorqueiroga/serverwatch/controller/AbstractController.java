package com.victorqueiroga.serverwatch.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.victorqueiroga.serverwatch.security.KeycloakUserService;
import com.victorqueiroga.serverwatch.service.UserService;

public abstract class AbstractController {

    @Autowired
    protected UserService userService;
    @Autowired
    protected KeycloakUserService keycloakUserService;

    public AbstractController() {
        this.userService = null;
        this.keycloakUserService = null;
    }

    // @ModelAttribute("currentUser")
    // public UserInfoDto currentUser() {
    // User localUser = userService.getOrCreateUser();
    // KeycloakUser keycloakUser = keycloakUserService.getCurrentUser();
    // return new UserInfoDto(localUser.getFullName(), keycloakUser.getEmail());
    // }

}
