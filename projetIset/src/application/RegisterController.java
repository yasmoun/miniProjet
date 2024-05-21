package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController implements Initializable {

    @FXML
    private Button registerBtn;
    @FXML
    private Label registrationMessage;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private Label Cpassword;
    @FXML
    private ChoiceBox<String> departement;
    private String[] departements = {"T.I", "G.E", "G.C", "G.M", "S.E.G"};

    @FXML
    private TextField cin;
    @FXML
    private TextField nom;
    @FXML
    private TextField prenom;
    @FXML
    private TextField telephone;
    @FXML
    private TextField email;
    @FXML
    private CheckBox chef;
    
    @FXML
    private Label dep;
    @FXML 
    private Label InvalidEmail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        departement.getItems().addAll(departements);
    }

    public void RegisterButtonOnAction(ActionEvent event) {
        if (!password.getText().equals(confirmPassword.getText())) {
            Cpassword.setText("mot de passe non identique");
        } else if (!isEmailAdress(email.getText())) {
            InvalidEmail.setText("invalid email!");
        } else {
            registerEnseig();
            Cpassword.setText("");
        }
    }

    private int getDepartmentId(String departmentName) {
        switch (departmentName) {
            case "T.I":
                return 2;
            case "G.E":
                return 1;
            case "G.C":
                return 4;
            case "G.M":
                return 3;
            case "S.E.G":
                return 5;
            default:
                return -1;
        }
    }

    public void registerEnseig() {
        Connection cnD = Connexion.getConnection();

        String n = nom.getText();
        String p = prenom.getText();
        int c = Integer.parseInt(cin.getText());
        String t = telephone.getText();
        String e = email.getText();
        String pa = password.getText();
        String selectedDepartment = departement.getValue();
        int departmentId = getDepartmentId(selectedDepartment);

        if (departmentId == -1) {
            dep.setText("Département non valide.");
            return;
        }

        String insertEnseignant = "INSERT INTO enseignants (cin, nom, prenom, telephone, email, password, departement_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertAgenda = "INSERT INTO agendas (enseignant_cin, date_creation) VALUES (?, NOW())";

        try {
            cnD.setAutoCommit(false); 

            PreparedStatement psEnseignant = cnD.prepareStatement(insertEnseignant);
            psEnseignant.setInt(1, c);
            psEnseignant.setString(2, n);
            psEnseignant.setString(3, p);
            psEnseignant.setString(4, t);
            psEnseignant.setString(5, e);
            psEnseignant.setString(6, pa);
            psEnseignant.setInt(7, departmentId);
            psEnseignant.executeUpdate();

            PreparedStatement psAgenda = cnD.prepareStatement(insertAgenda);
            psAgenda.setInt(1, c);
            psAgenda.executeUpdate();

            if (chef.isSelected()) {
                String updateDepartment = "UPDATE departements SET chef_cin = ? WHERE id = ?";
                PreparedStatement psChef = cnD.prepareStatement(updateDepartment);
                psChef.setInt(1, c);
                psChef.setInt(2, departmentId);
                psChef.executeUpdate();
                String insertAgendaDep = "INSERT INTO agenda_departements (departement_id, date_creation) VALUES (?, NOW())";
                PreparedStatement psAgendaDep = cnD.prepareStatement(insertAgendaDep);
                psAgendaDep.setInt(1, departmentId);
                psAgendaDep.executeUpdate();
            }

            cnD.commit(); 

            
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Enseignant enregistré avec succès !");
            alert.showAndWait();

           
            Parent loginPageParent = FXMLLoader.load(getClass().getResource("login.fxml"));
            Scene loginPageScene = new Scene(loginPageParent);
            Stage stage = (Stage) registerBtn.getScene().getWindow();
            stage.setScene(loginPageScene);
            stage.show();

        } catch (SQLException | IOException ex) {
            try {
                cnD.rollback(); 
                registrationMessage.setText("Erreur lors de l'enregistrement. Transaction annulée.");
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            ex.printStackTrace();
        } finally {
            try {
                cnD.setAutoCommit(true); 
                cnD.close();
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }
    }

    public static boolean isEmailAdress(String email) {
        Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(email);
        return m.matches();
    }
    
    @FXML 
    private Label retour;
    @FXML
    public void RetourLogin() {
        try {
            Parent loginPageParent = FXMLLoader.load(getClass().getResource("login.fxml"));
            Scene loginPageScene = new Scene(loginPageParent);

            Stage stage = (Stage) retour.getScene().getWindow();
            stage.setScene(loginPageScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}