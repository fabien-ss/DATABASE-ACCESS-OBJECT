package test;

import generic.annotation.C;
import generic.dao.Model;

@C(t = "Voiture")
public class Personne extends Model{

    @C(c = "marque")
    String nom;

    public Personne(){}
    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getNom() {
        return nom;
    }
}
