package base;

import java.sql.*;

public class ConnexionPOSTGRES{
    public static Connection enterToBdd(){
        try{
            Connection c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ticketing","postgres","postgres");
            c.setAutoCommit(false);
            return c;
        }
        catch(Exception e){
            System.out.println(e);
        }
        return null;
    }
}