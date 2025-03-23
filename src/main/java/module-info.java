module com.lordscave.hotel_lord {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires java.sql;
    requires jBCrypt;
    requires jakarta.mail;
    requires org.controlsfx.controls;

    opens com.lordscave.hotel_lord to javafx.fxml;
    exports com.lordscave.hotel_lord;
    opens com.lordscave.hotel_lord.utilities to javafx.fxml;
    exports com.lordscave.hotel_lord.utilities;
    opens com.lordscave.hotel_lord.Controllers to javafx.fxml;
    exports com.lordscave.hotel_lord.Controllers;
    opens com.lordscave.hotel_lord.Entities to javafx.fxml;
    exports com.lordscave.hotel_lord.Entities;
}
