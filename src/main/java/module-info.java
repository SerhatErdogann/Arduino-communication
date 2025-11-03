module com.example.arduinosinyalgonderme {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fazecast.jSerialComm;
    requires java.desktop;


    opens com.example.arduinosinyalgonderme to javafx.fxml;
    exports com.example.arduinosinyalgonderme;
}