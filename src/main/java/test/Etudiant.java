package test;

import generic.annotation.C;

@C(t = "Voiture")
public class Etudiant extends Personne {

    @C(c = "plaque", pk = true)
    String matricule;

    String prenom;
    public Etudiant(){
    }
    public Etudiant(String name, String matricule){
        this.setMatricule(matricule);
        this.setNom(name);
    }
    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }
    public String getMatricule() {
        return matricule;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public String getPrenom() {
        return prenom;
    }
}
