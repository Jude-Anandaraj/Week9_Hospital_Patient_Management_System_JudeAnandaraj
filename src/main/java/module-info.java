module Week9_Lab5_JudeAnandaraj {
    requires javafx.controls;
    requires java.sql;

    // javafx.graphics launches Main, javafx.base reads Patient getters for the TableView
    opens application to javafx.graphics, javafx.base;
}
