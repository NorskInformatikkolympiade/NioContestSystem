package controllers;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import play.mvc.Before;

import models.Contestant;

public class Security extends Secure.Security {
	@Before
	static void checkUserType() {
		Contestant current = getCurrentContestant();
		renderArgs.put("currentContestant", current);
		renderArgs.put("isAdmin", current != null && current.isAdmin);
		System.out.println(current != null ? current.getFullName() : "null");
	}
	
	static boolean authenticate(String username, String password) {
		String passwordHash = Contestant.hashPassword(password);
		return Contestant.find("byUsernameAndPasswordHash", username, passwordHash).first() != null;
	}
	
	static Contestant getCurrentContestant() {
		return Contestant.find("byUsername", Security.connected()).first();
	}
}
