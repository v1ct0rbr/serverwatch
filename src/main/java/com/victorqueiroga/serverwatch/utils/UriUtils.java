package com.victorqueiroga.serverwatch.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UriUtils {
    public static String encode(String uri) {
       try {
            String encodedValue = URLEncoder.encode(uri, StandardCharsets.UTF_8.toString());
            System.out.println("Encoded query parameter: " + encodedValue);
            return encodedValue;
        } catch (UnsupportedEncodingException e) {            
            e.printStackTrace();
            return null;
        }
      
    }


}
