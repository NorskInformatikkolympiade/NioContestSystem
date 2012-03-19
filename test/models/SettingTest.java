package models;
import org.junit.*;
import java.util.*;

import play.db.jpa.Transactional;
import play.test.*;
import utilities.CurrentDateTime;
import models.*;

public class SettingTest extends UnitTest {
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
	}
	
	@After
	public void tearDown() {
		CurrentDateTime.unmock();
	}
	
    @Test
    public void shouldBeAbleToSetAndGetContestStart() {
    	Date date = new Date(2009 - 1900, 4, 6, 12, 1, 35);
        Setting.setContestStart(date);
        Setting.setContestEnd(new Date(2010 - 1900, 1, 1));
        
        assertEquals(date, Setting.getContestStart());
    }
    
    @Test
    public void shouldBeAbleToSetAndGetContestEnd() {
    	Date date = new Date(2008 - 1900, 3, 9, 23, 45, 51);
        Setting.setContestStart(new Date(2010 - 1900, 1, 1));
        Setting.setContestEnd(date);
        
        assertEquals(date, Setting.getContestEnd());
    }
    
    @Test
    public void isContestRunningShouldReturnTrueWhenContestStartAndContestEndAreNotSet() {
    	assertTrue(Setting.isContestRunning());
    }
    
    @Test
    public void isContestRunningShouldReturnTrueWhenCurrentTimeIsGreaterThanOrEqualToContestStartAndLessThanContestEnd() {
    	Setting.setContestStart(new Date(2008 - 1900, 4, 7, 20, 10, 49));
    	Setting.setContestEnd(new Date(2008 - 1900, 4, 7, 23, 45, 52));
    	
    	CurrentDateTime.mock(new Date(2008 - 1900, 4, 7, 20, 10, 49));
    	assertTrue(Setting.isContestRunning());
    	CurrentDateTime.mock(new Date(2008 - 1900, 4, 7, 21, 0, 0));
    	assertTrue(Setting.isContestRunning());
    	CurrentDateTime.mock(new Date(2008 - 1900, 4, 7, 23, 45, 51));
    	assertTrue(Setting.isContestRunning());
    }
    
    @Test
    public void isContestRunningShouldReturnFalseWhenCurrentTimeIsLessThanContestStartOrGreaterThanOrEqualToContestEnd() {
    	Setting.setContestStart(new Date(2008 - 1900, 4, 7, 20, 10, 49));
    	Setting.setContestEnd(new Date(2008 - 1900, 4, 7, 23, 45, 52));
    	
    	CurrentDateTime.mock(new Date(2008 - 1900, 4, 6, 23, 59, 59));
    	assertFalse(Setting.isContestRunning());
    	CurrentDateTime.mock(new Date(2008 - 1900, 4, 7, 20, 10, 48));
    	assertFalse(Setting.isContestRunning());
    	CurrentDateTime.mock(new Date(2008 - 1900, 4, 7, 23, 45, 52));
    	assertFalse(Setting.isContestRunning());
    	CurrentDateTime.mock(new Date(2008 - 1900, 4, 8, 0, 0, 0));
    	assertFalse(Setting.isContestRunning());
    }
}
