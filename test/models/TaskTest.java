package models;
import org.junit.*;
import java.util.*;

import play.db.jpa.Transactional;
import play.test.*;
import models.*;

public class TaskTest extends UnitTest {
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
	}
	
    @Test
    public void getAllShouldReturnAllTasksSortedByNumber() {
        new Task(3, "Banana Republic", 100, "").save();
        new Task(1, "Heisaturen", 50, "").save();
        new Task(2, "Hei på deg", 200, "").save();
        
        List<Task> tasks = Task.getAll();
        assertEquals(3, tasks.size());
        assertEquals("Heisaturen", tasks.get(0).title);
        assertEquals("Hei på deg", tasks.get(1).title);
        assertEquals("Banana Republic", tasks.get(2).title);
    }
}
