package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

public class CalendrierController {
    @FXML
    private Label cin;
    @FXML
    private Label nometprenom;
    @FXML
    private SplitMenuButton mois;
    @FXML
    private SplitMenuButton annee;
    @FXML
    private Button addBtn;
    @FXML
    private GridPane calendrierJ;
    @FXML
    private TableView<Activity> activites;
    @FXML
    private TableColumn<Activity, String> typeCol;
    @FXML
    private TableColumn<Activity, String> nomCol;
    @FXML
    private TableColumn<Activity, String> dateCol;
    @FXML
    private TableColumn<Activity, String> hdCol;
    @FXML
    private TableColumn<Activity, String> hfCol;
    @FXML
    private TableColumn<Activity, String> mjCol;
    @FXML
    private Label date;

    private YearMonth currentYearMonth;
    private List<Activity> activityList;
    private Connection cnD;
    @FXML
    private Button supprimerBtn;
    
    @FXML
    private Label deconnecter;

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        activityList = new ArrayList<>();
        cnD = Connexion.getConnection();
        initializeMonthAndYear();
        initializeCalendar();
        initializeTable();
        
        addBtn.setOnAction(event -> handleAddActivity());
        
        if (cnD != null) {
            System.out.println("Connexion à la base de données établie avec succès.");
        } else {
            System.out.println("Erreur lors de l'établissement de la connexion à la base de données.");
        }
    }

    private void initializeMonthAndYear() {
        mois.setText(currentYearMonth.getMonth().name());
        annee.setText(String.valueOf(currentYearMonth.getYear()));

        mois.getItems().clear();
        for (int i = 1; i <= 12; i++) {
            final int month = i; 
            MenuItem monthItem = new MenuItem(YearMonth.of(2000, month).getMonth().name());
            monthItem.setOnAction(event -> handleMonthChange(month));
            mois.getItems().add(monthItem);
        }

        annee.getItems().clear();
        int currentYear = YearMonth.now().getYear();
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            final int year = i; 
            MenuItem yearItem = new MenuItem(String.valueOf(year));
            yearItem.setOnAction(event -> handleYearChange(year));
            annee.getItems().add(yearItem);
        }
    }

    private void handleMonthChange(int month) {
        currentYearMonth = YearMonth.of(currentYearMonth.getYear(), month);
        mois.setText(currentYearMonth.getMonth().name());
        updateCalendar();
    }

    private void handleYearChange(int year) {
        currentYearMonth = YearMonth.of(year, currentYearMonth.getMonthValue());
        annee.setText(String.valueOf(year));
        updateCalendar();
    }

    private void initializeCalendar() {
        updateCalendar();
    }

    private void updateCalendar() {
        calendrierJ.getChildren().clear();
        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekIndex = firstDayOfMonth.getDayOfWeek().getValue() % 7;

        for (int day = 1; day <= currentYearMonth.lengthOfMonth(); day++) {
            LocalDate date = currentYearMonth.atDay(day);
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setOnMouseClicked(event -> handleDateClick(date));
            calendrierJ.add(dayLabel, (dayOfWeekIndex + day - 1) % 7, (dayOfWeekIndex + day - 1) / 7 + 1);
        }
    }

    private void handleDateClick(LocalDate date) {
        this.date.setText(date.toString());
        loadActivitiesForDate(date);
    }

    private void initializeTable() {
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        nomCol.setCellValueFactory(cellData -> cellData.getValue().nomProperty());
        hdCol.setCellValueFactory(cellData -> cellData.getValue().hdProperty());
        hfCol.setCellValueFactory(cellData -> cellData.getValue().hfProperty());
        dateCol.setCellValueFactory(cellData -> cellData.getValue().getDateProperty().asString());
        mjCol.setCellValueFactory(cellData -> cellData.getValue().enseignantNomCompletProperty());  
        activites.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                if (newSelection.getEnseignantNomComplet().equals(nometprenom.getText())) {
                	typeCol.setCellFactory(col -> new EditingCell());
                	typeCol.setCellFactory(col -> new EditingCell());
                    typeCol.setOnEditCommit(event -> {
                        Activity activity = event.getRowValue();
                        activity.setType(event.getNewValue());
                        updateActivityInDatabase(activity);
                    });

                    nomCol.setCellFactory(col -> new EditingCell());
                    nomCol.setOnEditCommit(event -> {
                        Activity activity = event.getRowValue();
                        activity.setNom(event.getNewValue());
                        updateActivityInDatabase(activity);
                    });

                    hdCol.setCellFactory(col -> new EditingCell());
                    hdCol.setOnEditCommit(event -> {
                        Activity activity = event.getRowValue();
                        try {
                            LocalTime newStartTime = LocalTime.parse(event.getNewValue(), DateTimeFormatter.ofPattern("HH:mm"));
                            activity.setHeureDebut(newStartTime);
                            updateActivityInDatabase(activity);
                        } catch (DateTimeParseException e) {
                            showError("Format de l'heure invalide. Utilisez HH:mm.");
                        }
                    });

                    hfCol.setCellFactory(col -> new EditingCell());
                    hfCol.setOnEditCommit(event -> {
                        Activity activity = event.getRowValue();
                        try {
                            LocalTime newEndTime = LocalTime.parse(event.getNewValue(), DateTimeFormatter.ofPattern("HH:mm"));
                            activity.setHeureFin(newEndTime);
                            updateActivityInDatabase(activity);
                        } catch (DateTimeParseException e) {
                            showError("Format de l'heure invalide. Utilisez HH:mm.");
                        }
                    });

                    activites.setEditable(true);
                }
                } else {
                    activites.setEditable(false);
                }
            
        });
    
    }

    private void handleAddActivity() {
    	
        Dialog<Pair<Activity, Boolean>> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Activité");
        dialog.setHeaderText("Entrez les détails de la nouvelle activité");

        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField typeField = new TextField();
        typeField.setPromptText("Type");
        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date");
        TextField hdField = new TextField();
        hdField.setPromptText("Heure Début (HH:MM)");
        TextField hfField = new TextField();
        hfField.setPromptText("Heure Fin (HH:MM)");

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeField, 1, 0);
        grid.add(new Label("Nom:"), 0, 1);
        grid.add(nomField, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Heure Début:"), 0, 3);
        grid.add(hdField, 1, 3);
        grid.add(new Label("Heure Fin:"), 0, 4);
        grid.add(hfField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    LocalTime start = LocalTime.parse(hdField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                    LocalTime end = LocalTime.parse(hfField.getText(), DateTimeFormatter.ofPattern("HH:mm"));

                    Activity newActivity = new Activity(
                        typeField.getText(),
                        nomField.getText(),
                        datePicker.getValue(),
                        start,
                        end
                    );

                    int enseignantCin = Integer.parseInt(cin.getText());
                    int agendaId;
                    try {
                        agendaId = getAgendaIdForCin(enseignantCin);
                    } catch (SQLException e) {
                    	showError("Erreur lors de la récupération de l'ID de l'agenda: " + e.getMessage());
                    	return null;
                    	}
                    if (agendaId != 0) {
                        newActivity.setAgendaId(agendaId);
                        return new Pair<>(newActivity, true);
                    } else {
                        showError("Aucun agenda trouvé pour le cin " + enseignantCin);
                        return null;
                    }
                } catch (DateTimeParseException e) {
                    showError("Format de l'heure invalide. Utilisez HH:mm.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pair -> {
            if (pair != null && pair.getValue()) {
                Activity newActivity = pair.getKey();
                boolean success = addActivityToDatabase(newActivity);
                if (success) {
                    activityList.add(newActivity);
                    showSuccess("Activité ajoutée avec succès.");
                    loadActivitiesForTeacher(Integer.parseInt(cin.getText()));
                } else {
                    showError("Erreur lors de l'ajout de l'activité à la base de données.");
                }
            }
        });
    }

    private int getAgendaIdForCin(int enseignantCin) throws SQLException {
        String query = "SELECT id FROM agendas WHERE enseignant_cin = ?";
        try (PreparedStatement statement = cnD.prepareStatement(query)) {
            statement.setInt(1, enseignantCin);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }
        return 0;
    }

    private boolean addActivityToDatabase(Activity activity) {
        String query = "INSERT INTO activites (agenda_id, type, nom, date, heure_debut, heure_fin ,alerte ,ajoute_par ,date_ajout) VALUES (?, ?, ?, ?, ?, ? ,?,?,?)";
        try (PreparedStatement statement = cnD.prepareStatement(query)) {
            statement.setInt(1, activity.getAgendaId());
            statement.setString(2, activity.getType());
            statement.setString(3, activity.getNom());
            statement.setDate(4, java.sql.Date.valueOf(activity.getDate()));
            statement.setTime(5, java.sql.Time.valueOf(activity.getHeureDebut()));
            statement.setTime(6, java.sql.Time.valueOf(activity.getHeureFin()));
            statement.setBoolean(7, activity.isAlerte());
            statement.setInt(8,Integer.parseInt(cin.getText()));
            statement.setDate(9, java.sql.Date.valueOf(java.time.LocalDate.now()));
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            showError("Erreur lors de l'insertion de l'activité dans la base de données: " + e.getMessage());
            return false;
        }
    }

    private void loadActivitiesForDate(LocalDate date) {
       
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setEnseignantInfo(String cin, String nom, String prenom) {
        nometprenom.setText(nom + " " + prenom);
        this.cin.setText(cin);
        loadActivitiesForTeacher(Integer.parseInt(cin));
    }
    
    
    private void loadActivitiesForTeacher(int enseignantCIN) {
        activites.getItems().clear(); // Nettoyer la table view
        String query = "SELECT activites.*, CONCAT(enseignants.nom, ' ', enseignants.prenom) AS nom_complet FROM activites " +
                       "INNER JOIN enseignants ON activites.ajoute_par = enseignants.cin " +
                       "WHERE ajoute_par = ?";
        try (PreparedStatement statement = cnD.prepareStatement(query)) {
            statement.setInt(1, enseignantCIN);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id"); 
                int agendaId = resultSet.getInt("agenda_id");
                String type = resultSet.getString("type");
                String nom = resultSet.getString("nom");
                LocalDate date = resultSet.getDate("date").toLocalDate();
                LocalTime heureDebut = resultSet.getTime("heure_debut").toLocalTime();
                LocalTime heureFin = resultSet.getTime("heure_fin").toLocalTime();
                boolean alerte = resultSet.getBoolean("alerte");
                String nomComplet = resultSet.getString("nom_complet");
                LocalDate dateAjout = resultSet.getDate("date_ajout").toLocalDate();

                Activity activity = new Activity(type, nom, date, heureDebut, heureFin);
                activity.setId(id); 
                activity.setAgendaId(agendaId);
                activity.setAlerte(alerte);
                activity.setEnseignantNomComplet(nomComplet);
                activity.setDate(dateAjout);

                activites.getItems().add(activity);
            }
        } catch (SQLException e) {
            showError("Erreur lors de la récupération des activités : " + e.getMessage());
        }
    }
    

    

    @FXML
    private void Supprimer(ActionEvent event) {
        Activity selectedActivity = activites.getSelectionModel().getSelectedItem();
        if (selectedActivity != null && selectedActivity.getEnseignantNomComplet().equals(nometprenom.getText())) {
            boolean success = deleteActivityFromDatabase(selectedActivity);
            if (success) {
                activites.getItems().remove(selectedActivity);
                showSuccess("Activité supprimée avec succès.");
            } else {
                showError("Erreur lors de la suppression de l'activité.");
            }
        } else {
            showError("Vous ne pouvez pas supprimer cette activité.");
        }
    }
    public boolean deleteActivityFromDatabase(Activity activity) {
        String query = "DELETE FROM activites WHERE id = ?";
        try (PreparedStatement statement = cnD.prepareStatement(query)) {
            statement.setInt(1, activity.getId());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0; 
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'activité : " + e.getMessage());
            return false;
        }
    }
    
    private void updateActivityInDatabase(Activity activity) {
        String query = "UPDATE activites SET type = ?, nom = ?, heure_debut = ?, heure_fin = ? WHERE id = ?";

        try (PreparedStatement statement = cnD.prepareStatement(query)) {
            statement.setString(1, activity.getType());
            statement.setString(2, activity.getNom());
            statement.setTime(3, java.sql.Time.valueOf(activity.getHeureDebut()));
            statement.setTime(4, java.sql.Time.valueOf(activity.getHeureFin()));
            statement.setInt(5, activity.getId()); 

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Activité mise à jour dans la base de données avec succès.");
            } else {
                System.out.println("Aucune activité mise à jour dans la base de données.");
            }
        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour de l'activité dans la base de données : " + e.getMessage());
        }
    }
    
    @FXML
    void handleDeconnecterClick(MouseEvent event) {
        Stage stage = (Stage) deconnecter.getScene().getWindow();
        stage.close();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loader.load()));
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

}