package models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import play.db.jpa.Model;
import utilities.CurrentDateTime;

@Entity
public class Setting extends Model {
	@Column(unique = true, nullable = false)
	String name;
	
	@Column(nullable = false)
	String value;
	
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String CONTEST_START = "contestStart";
	private static final String CONTEST_END = "contestEnd";
	
	private Setting(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public static String get(String name) {
		Setting setting = find("byName", name).first();
		if (setting == null)
			return null;
		return setting.value;
	}
	
	public static void set(String name, String value) {
		Setting setting = find("byName", name).first();
		if (setting == null)
			setting = new Setting(name, value);
		else
			setting.value = value;
		setting.save();
	}
	
	public static Date getDate(String name) {
		String value = get(name);
		if (value == null)
			return null;
		try {
			return dateFormat.parse(value);
		}
		catch (ParseException e) {
			return null;
		}
	}
	
	public static void setDate(String name, Date value) {
		if (value == null)
			set(name, null);
		else
			set(name, dateFormat.format(value));
	}
	
	public static Date getContestStart() {
		return getDate(CONTEST_START);
	}
	
	public static void setContestStart(Date contestStart) {
		setDate(CONTEST_START, contestStart);
	}
	
	public static Date getContestEnd() {
		return getDate(CONTEST_END);
	}
	
	public static void setContestEnd(Date contestEnd) {
		setDate(CONTEST_END, contestEnd);
	}
	
	public static boolean isContestRunning() {
		Date now = CurrentDateTime.get();
		Date start = getContestStart();
		Date end = getContestEnd();
		if (start != null && now.before(start))
			return false;
		if (end != null && !now.before(end))
			return false;
		return true;
	}
}
