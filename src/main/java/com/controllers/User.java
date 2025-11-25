package com.controllers;

import java.util.*;

public class User {
    String username;
    String password;
    Set<String> ownedAlbums = new HashSet<>();
    Set<String> cart = new HashSet<>();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}