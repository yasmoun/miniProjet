package application;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LoginController implements Initializable {

    @FXML
    private Label loginMessageLabel;
    @FXML
    private TextField cin;
    @FXML
    private PasswordField password;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO Auto-generated method stub
    }

    @FXML
    public void loginButtonOnAction(ActionEvent event) {
        if (!cin.getText().isBlank() && !password.getText().isBlank()) {
            validateLogin();
        } else {
            loginMessageLabel.setText("Entrez vos données !");
        }
    }

    public void validateLogin() {
        Connection cnD = Connexion.getConnection();
        String verifyLogin = "SELECT COUNT(1) FROM enseignants WHERE cin = '" + cin.getText() + "' AND password = '" + password.getText() + "'";
        try {
            Statement statement = cnD.createStatement();
            ResultSet queryResult = statement.executeQuery(verifyLogin);
            while (queryResult.next()) {
                if (queryResult.getInt(1) == 1) {
                    String query = "SELECT nom, prenom FROM enseignants WHERE cin = '" + cin.getText() + "' AND password = '" + password.getText() + "'";
                    ResultSet resultSet = statement.executeQuery(query);
                    if (resultSet.next()) {
                        String nom = resultSet.getString("nom");
                        String prenom = resultSet.getString("prenom");

                        String checkChefQuery = "SELECT COUNT(1) FROM departements WHERE chef_cin = '" + cin.getText() + "'";
                        ResultSet chefResult = statement.executeQuery(checkChefQuery);
                        if (chefResult.next() && chefResult.getInt(1) == 1) {
                            loadChefPageWithEnseignantInfo(cin.getText(), nom, prenom);
                        } else {
                            loadCalendrierPageWithEnseignantInfo(cin.getText(), nom, prenom);
                        }
                    }
                } else {
                    loginMessageLabel.setText("Invalid !");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createountForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("register.fxml"));
            AnchorPane root = loader.load();
            Scene scene = new Scene(root, 600, 400);
            Stage registerStage = new Stage();
            registerStage.setTitle("Gestion Etudiants");
            registerStage.setScene(scene);
            registerStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }
    }

    @FXML
    private Label creer;

    @FXML
    private void redirectToRegisterPage() {
        try {
            Parent registerPageParent = FXMLLoader.load(getClass().getResource("register.fxml"));
            Scene registerPageScene = new Scene(registerPageParent);

            Stage stage = (Stage) creer.getScene().getWindow();

            stage.setScene(registerPageScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCalendrierPageWithEnseignantInfo(String enseignantCin, String nom, String prenom) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("calendrier.fxml"));
            Parent root = loader.load();

            CalendrierController controller = loader.getController();
            controller.setEnseignantInfo(enseignantCin, nom, prenom);

            Scene scene = new Scene(root, 746, 437);
            Stage primaryStage = (Stage) loginMessageLabel.getScene().getWindow();
            primaryStage.setScene(scene);
            primaryStage.setTitle("Calendrier Tâches");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChefPageWithEnseignantInfo(String enseignantCin, String nom, String prenom) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("chef.fxml"));
            Parent root = loader.load();

            chefController controller = loader.getController();
            controller.setEnseignantInfo(enseignantCin, nom, prenom);

            Scene scene = new Scene(root, 746, 437);
            Stage primaryStage = (Stage) loginMessageLabel.getScene().getWindow();
            primaryStage.setScene(scene);
            primaryStage.setTitle("Chef Département");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}