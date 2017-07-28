import dao.RequestDao;
import model.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class TestDb {

    RequestDao mongo;

    @Before
    public void setUp() throws Exception {
        Properties prop = new Properties();
        prop.setProperty("host", "localhost");
        prop.setProperty("port", "27017");
        prop.setProperty("dbname", "admin");
        prop.setProperty("login", "root");
        prop.setProperty("password", "root");
        prop.setProperty("table", "requests");
        mongo = new RequestDao(prop);
    }



    @Test
    public void testAddUser() throws Exception {
        mongo.save(new Request("test3", "test2", "test1"));
    }


}
