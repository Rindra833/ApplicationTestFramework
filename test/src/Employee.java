package test;

public class Employee {
    private String nom;
    private int age;
    private String email;

    // Champs prives : le framework utilise setAccessible(true).
    // Mais on met des getters pour que la JSP puisse lire via ${employee.nom}.

    public String getNom()   { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getAge()      { return age; }
    public void setAge(int age)    { this.age = age; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Employee{nom='" + nom + "', age=" + age + ", email='" + email + "'}";
    }
}