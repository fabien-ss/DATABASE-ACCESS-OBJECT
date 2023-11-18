package generic;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DbConnexion {
   
    String url;
    String username;
    String password;
    String driver;

    public static void main(String[] args) throws Exception {
        System.out.println("Test connexion");
        Connection c = new DbConnexion().enterToBdd();
        System.out.println(c);
    }
    
    public void init() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File("config.xml"));
        Node databaseNode = doc.getElementsByTagName("database").item(0);
        this.url = databaseNode.getChildNodes().item(1).getTextContent();
        this.username = databaseNode.getChildNodes().item(3).getTextContent();
        this.password = databaseNode.getChildNodes().item(5).getTextContent();  
        this.driver = databaseNode.getChildNodes().item(7).getTextContent();
    }

    public Connection enterToBdd() throws Exception{
        Class.forName(this.driver);
        Connection c = DriverManager.getConnection(this.url, this.username, this.password);
        c.setAutoCommit(false);
        return c;
    }
    
    public DbConnexion() throws Exception{
        this.init();
    }

    public DbConnexion(String url, String username, String password) {
        this.setUrl(url);
        this.setUsername(username);
        this.setPassword(password);
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
