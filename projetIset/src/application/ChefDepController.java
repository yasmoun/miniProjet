package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

public class ChefDepController {

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
    @FXML
    private Button supprimerBtn;
    @FXML
    private Label deconnecter;
    @FXML
    private Label departement;

    private YearMonth currentYearMonth;
    private List<Activity> activityList;
    private Connection cnD;

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
                } else {
                    typeCol.setCellFactory(col -> null);
                    nomCol.setCellFactory(col -> null);
                    hdCol.setCellFactory(col -> null);
                    hfCol.setCellFactory(col -> null);
                }
            }
        });
    }

    private void loadActivitiesForDate(LocalDate date) {
        activites.getItems().clear();
        String query = "SELECT activites_departements.*, CONCAT(enseignants.nom, ' ', enseignants.prenom) AS nom_complet " +
                       "FROM activites_departements " +
                       "INNER JOIN enseignants ON activites_departements.ajoute_par = enseignants.cin " +
                       "WHERE date = ?";
        try (PreparedStatement statement = cnD.prepareStatement(query)) {
            statement.setDate(1, java.sql.Date.valueOf(date));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int agendaDepartementId = resultSet.getInt("agenda_departement_id");
                String type = resultSet.getString("type");
                String nom = resultSet.getString("nom");
                LocalTime heureDebut = resultSet.getTime("heure_debut").toLocalTime();
                LocalTime heureFin = resultSet.getTime("heure_fin").toLocalTime();
                boolean alerte = resultSet.getBoolean("alerte");
                String nomComplet = resultSet.getString("nom_complet");
                LocalDate dateAjout = resultSet.getDate("date_ajout").toLocalDate();

                Activity activity = new Activity(type, nom, date, heureDebut, heureFin);
                activity.setId(id);
                activity.setAgendaId(agendaDepartementId);
                activity.setAlerte(alerte);
                activity.setEnseignantNomComplet(nomComplet);
                activity.setDate(dateAjout);

                activites.getItems().add(activity);
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des activités : " + e.getMessage());
        }
    }

    private void handleAddActivity() {
        Dialog<Pair<Activity, Boolean>> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Activité");
        dialog.setHeaderText("Entrez les détails de la nouvelle activité");

        // Set the button types
        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the labels and fields
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
                        agendaId = getAgendaDepartementId(enseignantCin);
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
                boolean success = addActivityToAgendaDepartements(newActivity, newActivity.getAgendaId());
                if (success) {
                    activityList.add(newActivity);
                    showSuccess("Activité ajoutée avec succès.");
                    loadActivitiesForDate(newActivity.getDate());
                } else {
                    showError("Erreur lors de l'ajout de l'activité à la base de données.");
                }
            }
        });
    }

    private int getAgendaDepartementId(int enseignantCin) throws SQLException {
        String query = "SELECT id FROM agendas WHERE enseignant_cin = ?";
        try (PreparedStatement statement = cnD.prepareStatement(query)) {
            statement.setInt(1, enseignantCin);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                throw new SQLException("Aucun agenda trouvé pour le CIN donné.");
            }
        }
    }

    private boolean addActivityToAgendaDepartements(Activity activity, int agendaDepartementId) {
        String query = "INSERT INTO activites_departements (agenda_departement_id, type, nom, date, heure_debut, heure_fin, alerte, ajoute_par, date_ajout) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = cnD.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, agendaDepartementId);
            statement.setString(2, activity.getType());
            statement.setString(3, activity.getNom());
            statement.setDate(4, java.sql.Date.valueOf(activity.getDate()));
            statement.setTime(5, java.sql.Time.valueOf(activity.getHeureDebut()));
            statement.setTime(6, java.sql.Time.valueOf(activity.getHeureFin()));
            statement.setBoolean(7, activity.isAlerte());
            statement.setInt(8, Integer.parseInt(cin.getText()));
            statement.setDate(9, java.sql.Date.valueOf(java.time.LocalDate.now()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Échec de l'ajout de l'activité à l'agenda du département, aucune ligne affectée.");
            }

            return true;
        } catch (SQLException e) {
            showError("Erreur lors de l'ajout de l'activité à l'agenda du département : " + e.getMessage());
            return false;
        }
    }

    private void updateActivityInDatabase(Activity activity) {
        String query = "UPDATE activites_departements SET type = ?, nom = ?, heure_debut = ?, heure_fin = ? WHERE id = ?";
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
            Stage primaryStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            showError("Erreur lors de la déconnexion : " + e.getMessage());
        }
    }

    @FXML
    void handleSupprimerClick(ActionEvent event) {
        Activity selectedActivity = activites.getSelectionModel().getSelectedItem();
        if (selectedActivity != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette activité ?");

            ButtonType buttonTypeOui = new ButtonType("Oui");
            ButtonType buttonTypeNon = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

            alert.showAndWait().ifPresent(type -> {
                if (type == buttonTypeOui) {
                    deleteActivityFromDatabase(selectedActivity);
                    loadActivitiesForDate(selectedActivity.getDate());
                }
            });
        } else {
            showError("Veuillez sélectionner une activité à supprimer.");
        }
    }

    private void deleteActivityFromDatabase(Activity activity) {
        String query = "DELETE FROM activites_departements WHERE id = ?";
        try (PreparedStatement statement = cnD.prepareStatement(query)) {
            statement.setInt(1, activity.getId());
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Activité supprimée de la base de données avec succès.");
                activites.getItems().remove(activity); // Remove the activity from the list
            } else {
                System.out.println("Aucune activité supprimée de la base de données.");
            }
        } catch (SQLException e) {
            showError("Erreur lors de la suppression de l'activité dans la base de données : " + e.getMessage());
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}