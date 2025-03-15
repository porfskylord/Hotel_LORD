module com.lordscave.hotel_lord {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jBCrypt;
    requires jakarta.mail;


    opens com.lordscave.hotel_lord to javafx.fxml;
    exports com.lordscave.hotel_lord;
}