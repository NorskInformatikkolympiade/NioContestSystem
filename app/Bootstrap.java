import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {
	public void doJob() {
		//TODO: Delete database only when running locally 
		Fixtures.deleteDatabase();
		Fixtures.loadModels("initial-data.yml");
		Grader.getInstance().now();
	}
}
