package com.ravishandev.epicreads.util;

import com.google.gson.Gson;

import java.security.SecureRandom;

public class AppUtil {

    public static final Gson GSON = new Gson();
    public static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generateCode(){
        int randomNumber  = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", randomNumber);
    }
}
