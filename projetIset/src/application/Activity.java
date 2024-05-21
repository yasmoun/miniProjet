package application;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;



public class Activity {
	private int id;
    private StringProperty type;
    private StringProperty nom;
    private LocalDate date;
    private LocalTime heureDebut;
    public void setId(int id) {
		this.id = id;
	}

	public void setHeureDebut(LocalTime heureDebut) {
		this.heureDebut = heureDebut;
	}

	public void setHeureFin(LocalTime heureFin) {
		this.heureFin = heureFin;
	}

	private LocalTime heureFin;
    private ObjectProperty<LocalTime> hdProperty;
    private ObjectProperty<LocalTime> hfProperty;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private int agendaId;
    private boolean alerte; 
    private int ajoutePar;
    private StringProperty ajouteParNom;
    
    private StringProperty enseignantNomComplet = new SimpleStringProperty();

    public StringProperty enseignantNomCompletProperty() {
        return enseignantNomComplet;
    }

    public String getEnseignantNomComplet() {
        return enseignantNomComplet.get();
    }

    public void setEnseignantNomComplet(String nomComplet) {
        this.enseignantNomComplet.set(nomComplet);
    }
    public StringProperty getAjouteParNom() {
		return ajouteParNom;
	}
    public StringProperty setAjouteParNom(StringProperty nom) {
		return this.ajouteParNom=nom;
	}
    public void setAjouteParNom(String ajouteParNom) {
        this.ajouteParNom.set(ajouteParNom);
    }
    public void setAjouteParPrenom(String ajouteParPrenom) {
    }

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
   
    public Activity(String type, String nom, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        this.type = new SimpleStringProperty(type);
        this.nom = new SimpleStringProperty(nom);
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.hdProperty = new SimpleObjectProperty<>(heureDebut);
        this.hfProperty = new SimpleObjectProperty<>(heureFin);
        this.ajouteParNom = new SimpleStringProperty("");
    }

    public String getType() {
        return type.get();
    }
   
    public boolean isAlerte() {
        return true;
    }

    public void setAlerte(boolean alerte) {
        this.alerte = alerte;
    }

    public int getAjoutePar() {
        return ajoutePar;
    }

    public void setAjoutePar(int ajoutePar) {
        this.ajoutePar = ajoutePar;
    }
    public int getAgendaId() {
        return agendaId;
    }

    public void setAgendaId(int agendaId) {
        this.agendaId = agendaId;
    }
    public void setType(String type) {
        this.type.set(type);
    }

    public StringProperty typeProperty() {
        return type;
    }

    public String getNom() {
        return nom.get();
    }

    public void setNom(String nom) {
        this.nom.set(nom);
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public StringProperty hdProperty() {
        return new SimpleStringProperty(hdProperty.get().format(timeFormatter));
    }

    public StringProperty hfProperty() {
        return new SimpleStringProperty(hfProperty.get().format(timeFormatter));
    }


    
    public ObjectProperty<LocalDate> dateProperty() {
        return new SimpleObjectProperty<>(date);
    }

    public StringProperty ajouteParNomProperty() {
        return ajouteParNom;
    }


    public ObjectProperty<LocalDate> getDateProperty() {
        return new SimpleObjectProperty<>(date);
   
	}

	public int getId() {
		return id;
	}


}
