package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connexion {
	private static final String URL = "jdbc:mysql://localhost:3306/agenda";
    private static final String USER = "root";
    private static final String PASSWORD = "lnnaya";

    public static Connection getConnection() {
        Connection connexion = null;
        try {
            connexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion établie avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la connexion à la base de données : " + e.getMessage());
        }
        return connexion;
}
}
