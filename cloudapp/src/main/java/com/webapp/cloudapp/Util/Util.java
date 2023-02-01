package com.webapp.cloudapp.Util;

import org.mindrot.jbcrypt.BCrypt;


public class Util {
    
    public static boolean isNullOrEmpty(String param) { 
        return param != null && param.length() != 0; 
    }

    public static String hashPassword(String plainTextPassword){
		return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
	}
}
