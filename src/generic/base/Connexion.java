package generic.base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;

import generic.dao.TableUtility;

public class Connexion {

    String url = "";
    String password = "";
    String username = "";
    String driver = "";

    public Connexion(String url, String password, String username, String driver) throws Exception {
        this.setUrl(url);
        this.setDriver(driver);
        this.setPassword(password);
        this.setUsername(username);
    }

    public Connexion() throws Exception{
        this.init();
    }

    public void init() throws Exception {
        String rootPath = System.getProperty("user.dir");
        String databaseconf = TableUtility.chargerModele(rootPath+"/database.conf");
        HashMap<String, String> config = TableUtility.fetchData(databaseconf);
        this.setUrl(config.get("url"));
        this.setUsername(config.get("user"));
        this.setPassword(config.get("password"));
        this.setDriver(config.get("driver"));
    }

    public Connection enterToBdd() throws Exception{
        Class.forName(this.driver);
        Connection c = DriverManager.getConnection(this.url, this.username, this.password);
        c.setAutoCommit(false);
        return c;
   }

    public void setUrl(String url) throws Exception{
        if(url == "" | url == null) throw new Exception("No url found in "+System.getProperty("user.dir"));
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) throws Exception{
        if(password == "" | password == null) throw new Exception("No password found");
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) throws Exception{
        if(username == "" | username == null) throw new Exception("No username found");
        this.username = username;
    }

    public void setDriver(String driver) throws Exception {
        if(driver == null | driver == "") throw new Exception("No driver found");
        this.driver = driver;
    }
    public String getDriver() {
        return driver;
    }
}
