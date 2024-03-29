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
	}
	
	static boolean authenticate(String username, String password) {
		Contestant contestant = Contestant.find("byUsername", username).first();
		if (contestant == null)
			return false;
		String passwordHash = Contestant.hashPassword(password, contestant.passwordSalt);
		return passwordHash.equals(contestant.passwordHash);
	}
	
	static Contestant getCurrentContestant() {
		Contestant emptyContestant = new Contestant("", "", false);
		String username = Security.connected();
		if (username == null)
			return emptyContestant;
		Contestant contestant = Contestant.find("byUsername", username).first();
		return contestant == null ? emptyContestant : contestant;
	}
}
