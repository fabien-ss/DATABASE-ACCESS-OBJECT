package utils;

import java.util.Vector;

import java.lang.reflect.*;
import java.sql.Connection;

@Correspondance(nomTable = "voiture")
public class Voiture extends Crud{

    @Correspondance(primarykey = true)
    int a;
    @Correspondance
    String marque;
    @Correspondance
    String plaque;
    @Correspondance
    java.sql.Date dateN;
    String n;

    public void setA(int a){ this.a = a;}
    public int getA(){ return this.a;}
    public void setD(java.sql.Date d){ this.dateN=d;}
    public java.sql.Date getD(){ return this.dateN; }
    public void setN(String n){ this.n = n;}
    public String getN(){ return this.n; }
    public void setPlaque(String nom){ this.plaque = nom; }
    public String getPlaque(){ return this.plaque; }
    public void setMarque(String id){ this.marque = id; }
    public String getMarque(){ return this.marque; }
    
    public static void main(String[] args) throws Exception{
        Voiture v = new Voiture();
        Connection c = v.enterToBdd();
        System.out.println(c);
        // Voiture v = new Voiture();
        // v.setA(0);
        // v.setBase("taxibe");
        // v.setD(java.sql.Date.valueOf("2020-01-01"));
        // Voiture v2 = new Voiture();
        // v2.setMarque("V");
        // v2.setPlaque("sadas");
        // v2.setMarque("IUUI");
        // v2.setBase("y");
        // v2.setD(java.sql.Date.valueOf("2020-12-12"));
        // System.out.println(v.getTableName());
        // v2.delete(null);
        // Vector<Voiture> vs = v.select(null);
    }
}