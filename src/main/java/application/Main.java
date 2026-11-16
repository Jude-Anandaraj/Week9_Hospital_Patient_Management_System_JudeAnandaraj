/*
 * Jude Anandaraj
 * 2026/11/15
 * Hospital Patient System
 */
package application;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    // Database operations are kept in a separate class
    private final PatientDAO patientDAO = new PatientDAO();

    // Patient Details form
    private final TextField idField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField ageField = new TextField();
    private final ComboBox<String> genderBox = new ComboBox<>();
    private final TextField phoneField = new TextField();
    private final TextField diagnosisField = new TextField();
    private final DatePicker admissionDateField = new DatePicker();

    // Patient Records area
    private final TextField searchField = new TextField();
    private final TableView<Patient> patientTable = new TableView<>();

    private final Label statusLabel = new Label("Enter patient details.");

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Top: title and subtitle
        root.setTop(createHeader());

        // Center: Patient Details form on the left, Patient Records on the right
        VBox formPane = createFormPane();
        VBox recordsPane = createRecordsPane();
        HBox.setHgrow(recordsPane, Priority.ALWAYS);

        HBox center = new HBox(20, formPane, recordsPane);
        root.setCenter(center);

        // Bottom: status message
        root.setBottom(createStatusBar());

        Scene scene = new Scene(root, 1250, 650);
        URL css = getClass().getResource("application.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("Hospital Management System");
        stage.setScene(scene);
        stage.show();

        // Load the existing records when the application starts
        showAllPatients();
    }


    // Layout


    private VBox createHeader() {

        Label heading = new Label("Hospital Management System");
        heading.getStyleClass().add("title");

        Label subtitle = new Label("Patient Registration System");
        subtitle.getStyleClass().add("subtitle");

        VBox header = new VBox(5, heading, subtitle);
        header.setPadding(new Insets(0, 0, 20, 0));
        return header;
    }

    private VBox createFormPane() {

        Label title = new Label("Patient Details");
        title.getStyleClass().add("section-title");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);

        idField.setPromptText("Auto-generated");
        idField.setEditable(false);
        idField.setFocusTraversable(false);
        idField.getStyleClass().add("read-only-field");

        nameField.setPromptText("Full name");
        ageField.setPromptText("Age in years (0-125)");
        genderBox.setPromptText("Select gender");
        phoneField.setPromptText("Phone number");
        diagnosisField.setPromptText("Reason for visit");
        admissionDateField.setPromptText("Select admission date");

        genderBox.getItems().addAll(
                "Female",
                "Male",
                "Non-binary",
                "Prefer not to say"
        );

        // Show "Select gender" again after the form is cleared
        genderBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? genderBox.getPromptText() : item);
            }
        });

        // Future dates cannot be picked from the calendar
        admissionDateField.setDayCellFactory(picker ->
                new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        setDisable(empty || date.isAfter(LocalDate.now()));
                    }
                });

        addRow(form, 0, "Patient ID:", idField);
        addRow(form, 1, "Full name:", nameField);
        addRow(form, 2, "Age:", ageField);
        addRow(form, 3, "Gender:", genderBox);
        addRow(form, 4, "Phone:", phoneField);
        addRow(form, 5, "Diagnosis:", diagnosisField);
        addRow(form, 6, "Admission Date:", admissionDateField);

        ColumnConstraints labels = new ColumnConstraints(120);
        ColumnConstraints fields = new ColumnConstraints();
        fields.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labels, fields);

        // Form action area
        Button addButton = new Button("Add Patient");
        addButton.setOnAction(e -> addPatient());

        Button updateButton = new Button("Update");
        updateButton.setOnAction(e -> updatePatient());

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deletePatient());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearForm());

        HBox buttons = new HBox(10, addButton, updateButton, deleteButton, clearButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox formPane = new VBox(15, title, form, buttons);
        formPane.setPadding(new Insets(20));
        formPane.setPrefWidth(420);
        formPane.getStyleClass().add("panel");
        return formPane;
    }

    private void addRow(GridPane grid, int row, String text, Control control) {

        Label label = new Label(text);
        label.getStyleClass().add("form-label");

        control.setMaxWidth(Double.MAX_VALUE);

        grid.add(label, 0, row);
        grid.add(control, 1, row);
    }

    private VBox createRecordsPane() {

        Label title = new Label("Patient Records");
        title.getStyleClass().add("section-title");

        searchField.setPromptText("Search by name or phone");
        searchField.setOnAction(e -> searchPatients());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchPatients());

        Button showAllButton = new Button("Show All");
        showAllButton.setOnAction(e -> showAllPatients());

        HBox searchBar = new HBox(10, searchField, searchButton, showAllButton);

        createTable();
        VBox.setVgrow(patientTable, Priority.ALWAYS);

        VBox recordsPane = new VBox(15, title, searchBar, patientTable);
        recordsPane.setPadding(new Insets(20));
        recordsPane.getStyleClass().add("panel");
        return recordsPane;
    }

    private void createTable() {

        TableColumn<Patient, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        idColumn.setPrefWidth(40);

        TableColumn<Patient, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(125);

        TableColumn<Patient, Integer> ageColumn = new TableColumn<>("Age");
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageColumn.setPrefWidth(40);

        TableColumn<Patient, String> genderColumn = new TableColumn<>("Gender");
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderColumn.setPrefWidth(125);

        TableColumn<Patient, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneColumn.setPrefWidth(105);

        TableColumn<Patient, String> diagnosisColumn = new TableColumn<>("Diagnosis");
        diagnosisColumn.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        diagnosisColumn.setPrefWidth(155);

        TableColumn<Patient, LocalDate> dateColumn = new TableColumn<>("Admission Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("admissionDate"));
        dateColumn.setPrefWidth(120);

        patientTable.getColumns().addAll(List.of(idColumn, nameColumn, ageColumn,
                genderColumn, phoneColumn, diagnosisColumn, dateColumn));
        patientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        patientTable.setPlaceholder(new Label("No patient records to display."));

        // Selecting a row copies that patient into the form for Update / Delete
        patientTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldPatient, newPatient) -> {
                    if (newPatient != null) {
                        fillForm(newPatient);
                    }
                });
    }

    private VBox createStatusBar() {

        statusLabel.setWrapText(true);

        Label footerLabel = new Label("JavaFX • JDBC • MySQL");
        footerLabel.getStyleClass().add("footer");

        VBox footer = new VBox(8, statusLabel, footerLabel);
        footer.setPadding(new Insets(15, 0, 0, 0));
        return footer;
    }


    // CRUD actions (button handlers)


    // CREATE - Add Patient
    private void addPatient() {

        String error = validateForm();
        if (error != null) {
            setStatus(error, true);
            return;
        }

        Patient patient = getPatientFromForm(0);

        try {
            int count = patientDAO.addPatient(patient);

            if (count == 1) {
                refreshTable();
                clearFields();
                setStatus("Patient " + patient.getName() + " added successfully.", false);
            } else {
                setStatus("No record was inserted.", true);
            }

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // READ - Show All
    private void showAllPatients() {

        try {
            refreshTable();
            setStatus("Showing all " + patientTable.getItems().size()
                    + " patient record(s).", false);

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // UPDATE - Edit the selected patient
    private void updatePatient() {

        if (idField.getText().isEmpty()) {
            setStatus("Select a patient in the table before clicking Update.", true);
            return;
        }

        String error = validateForm();
        if (error != null) {
            setStatus(error, true);
            return;
        }

        int patientId = Integer.parseInt(idField.getText());
        Patient patient = getPatientFromForm(patientId);

        try {
            int count = patientDAO.updatePatient(patient);

            if (count == 1) {
                refreshTable();
                clearFields();
                setStatus("Patient #" + patientId + " updated successfully.", false);
            } else {
                setStatus("Patient #" + patientId + " was not found.", true);
            }

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // DELETE - Remove the selected patient after confirmation
    private void deletePatient() {

        if (idField.getText().isEmpty()) {
            setStatus("Select a patient in the table before clicking Delete.", true);
            return;
        }

        int patientId = Integer.parseInt(idField.getText());

        if (!confirmDelete(patientId, nameField.getText().trim())) {
            setStatus("Delete cancelled.", false);
            return;
        }

        try {
            int count = patientDAO.deletePatient(patientId);

            if (count == 1) {
                refreshTable();
                clearFields();
                setStatus("Patient #" + patientId + " deleted successfully.", false);
            } else {
                setStatus("Patient #" + patientId + " was not found.", true);
            }

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // SEARCH - Find patients by name or phone
    private void searchPatients() {

        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            setStatus("Enter a name or phone number to search.", true);
            return;
        }

        try {
            List<Patient> results = patientDAO.searchPatients(keyword);
            loadTable(results);
            setStatus(results.size() + " patient(s) found for \"" + keyword + "\".", false);

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // CLEAR - Reset the form and the table selection
    private void clearForm() {
        clearFields();
        setStatus("Form cleared.", false);
    }


    // Validation


    // Returns an error message, or null when every field is valid
    private String validateForm() {

        String name = nameField.getText().trim();
        String ageValue = ageField.getText().trim();
        String gender = genderBox.getValue();
        String phone = phoneField.getText().trim();
        String diagnosis = diagnosisField.getText().trim();
        LocalDate admissionDate = admissionDateField.getValue();

        if (name.isEmpty() || ageValue.isEmpty() || gender == null
                || phone.isEmpty() || diagnosis.isEmpty() || admissionDate == null) {
            return "Please complete all fields.";
        }

        int age;
        try {
            age = Integer.parseInt(ageValue);
        } catch (NumberFormatException ex) {
            return "Age must be a whole number.";
        }

        if (age < 0 || age > 125) {
            return "Age must be between 0 and 125.";
        }

        if (!phone.matches("[+0-9(). -]{7,20}")
                || phone.replaceAll("\\D", "").length() < 7) {
            return "Enter a valid phone number (at least 7 digits).";
        }

        if (admissionDate.isAfter(LocalDate.now())) {
            return "Admission date cannot be in the future.";
        }

        return null;
    }


    // Form and table helpers


    // Builds a Patient from the form (call validateForm() first)
    private Patient getPatientFromForm(int patientId) {

        return new Patient(
                patientId,
                nameField.getText().trim(),
                Integer.parseInt(ageField.getText().trim()),
                genderBox.getValue(),
                phoneField.getText().trim(),
                diagnosisField.getText().trim(),
                admissionDateField.getValue());
    }

    private void fillForm(Patient patient) {

        idField.setText(String.valueOf(patient.getPatientId()));
        nameField.setText(patient.getName());
        ageField.setText(String.valueOf(patient.getAge()));
        genderBox.setValue(patient.getGender());
        phoneField.setText(patient.getPhone());
        diagnosisField.setText(patient.getDiagnosis());
        admissionDateField.setValue(patient.getAdmissionDate());

        setStatus("Patient #" + patient.getPatientId()
                + " selected. Edit the details and click Update, or click Delete.", false);
    }

    private void clearFields() {

        patientTable.getSelectionModel().clearSelection();

        idField.clear();
        nameField.clear();
        ageField.clear();
        genderBox.setValue(null);
        phoneField.clear();
        diagnosisField.clear();
        admissionDateField.setValue(null);
    }

    // Reloads every patient from the database into the TableView
    private void refreshTable() throws SQLException {
        searchField.clear();
        loadTable(patientDAO.getAllPatients());
    }

    private void loadTable(List<Patient> patients) {
        patientTable.setItems(FXCollections.observableArrayList(patients));
    }

    
    // User feedback

    private boolean confirmDelete(int patientId, String name) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Patient");
        alert.setHeaderText("Delete patient #" + patientId + " (" + name + ")?");
        alert.setContentText("This record will be permanently removed from the database.");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void setStatus(String message, boolean error) {

        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.getStyleClass().add(error ? "status-error" : "status-success");
    }

    private void showDatabaseError(SQLException ex) {
        setStatus("Database error: " + ex.getMessage(), true);
        ex.printStackTrace();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
