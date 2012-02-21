package models;

import java.util.List;

import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Task extends Model {
	public int number;
	public String title;
	public int weight;
	
	public Task(int number, String title, int weight) {
		this.number = number;
		this.title = title;
		this.weight = weight;
	}
	
	public static List<Task> getAll() {
		return Task.find("order by number").fetch();
	}
}
