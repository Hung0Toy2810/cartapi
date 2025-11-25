package com.controllers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStore {
    public static final Map<String, User> users = new ConcurrentHashMap<>();
    public static final Set<String> availableAlbums = Set.of("album1", "album2", "album3", "album4", "album5");
}