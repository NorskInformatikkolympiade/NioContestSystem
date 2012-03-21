package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class DataSet extends Model {
	@ManyToOne(optional = false)
	public Task task;
	
	public int number;
	public int weight;
	
	@OneToMany(mappedBy = "dataSet", cascade = CascadeType.ALL)
	public List<Run> runs = new ArrayList<Run>();
	
	public DataSet(Task task, int number, int weight) {
		this.task = task;
		this.number = number;
		this.weight = weight;
	}
	
	public String getInputFileName() {
		return task.dataSetPath + "/" + number + ".in";
	}
	
	public String getOutputFileName() {
		return task.dataSetPath + "/" + number + ".out";
	}
}
