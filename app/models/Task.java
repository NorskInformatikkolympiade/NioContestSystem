package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class Task extends Model {
	public int number;
	public String title;
	public int weight;
	public String dataSetPath;
	
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
	public List<Submission> submissions;
	
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
	public List<DataSet> dataSets;
	
	public Task(int number, String title, int weight, String dataSetPath) {
		this.number = number;
		this.title = title;
		this.weight = weight;
		this.dataSetPath = dataSetPath;
	}
	
	public static List<Task> getAll() {
		return Task.find("order by number").fetch();
	}
}
