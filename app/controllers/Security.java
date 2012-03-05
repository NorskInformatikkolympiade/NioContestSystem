package controllers;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import models.Contestant;

public class Security extends Secure.Security {
	static boolean authenticate(String username, String password) {
		String passwordHash = Contestant.hashPassword(password);
		return Contestant.find("byUsernameAndPasswordHash", username, passwordHash).first() != null;
	}
}
