import dao.RequestDao;
import model.Request;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TestDb {

    RequestDao mongo;

    @Before
    public void setUp() throws Exception {
        Properties prop = new Properties();
        try {
            prop.load(TestDb.class.getClassLoader().getResourceAsStream("config/config.properies"));
        } catch (IOException e) {
            System.out.println("can't find config file");
        }
        mongo = new RequestDao(prop);
    }



    @Test
    public void testAddUser() throws Exception {
        mongo.save(new Request("4444", "444", "421"));
    }


}
