package com.sample;

import javafx.application.Application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;

public class DroolsTest extends Application {

    private KieSession kSession;
    private VBox layout;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load up the knowledge base
            KieServices ks = KieServices.Factory.get();
            KieContainer kContainer = ks.getKieClasspathContainer();
            kSession = kContainer.newKieSession("ksession-rules");

            // Initialize JavaFX
            layout = new VBox(10);
            Scene scene = new Scene(layout, 500, 300);
            primaryStage.setTitle("Shakespear Novel for You");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Start the rule engine with the initial fact
            kSession.insert(new Path("start"));
            kSession.setGlobal("guiHandler", this);
            kSession.fireAllRules();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void updateQuestion(String question, List<String> options) {
        layout.getChildren().clear();

        Label questionLabel = new Label(question);
        layout.getChildren().add(questionLabel);

        ToggleGroup group = new ToggleGroup();
        List<RadioButton> buttons = new ArrayList<>();
        for (String option : options) {
            RadioButton button = new RadioButton(option);
            button.setToggleGroup(group);
            buttons.add(button);
            layout.getChildren().add(button);
        }

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(event -> {
            String selectedOption = buttons.stream()
                                           .filter(RadioButton::isSelected)
                                           .map(RadioButton::getText)
                                           .findFirst()
                                           .orElse(null);

            if (selectedOption != null) {
                // Pobierz istniej¹cy fakt Path i zaktualizuj jego stan
                Path path = kSession.getObjects(o -> o instanceof Path)
                                    .stream()
                                    .map(o -> (Path) o)
                                    .findFirst()
                                    .orElseThrow(() -> new IllegalStateException("Path object not found in session"));

                path.setState(selectedOption); // Aktualizacja stanu
                kSession.update(kSession.getFactHandle(path), path); // Aktualizacja faktu w Drools
                kSession.fireAllRules(); // Wywo³anie regu³
            }
            
            if (selectedOption.equals("Close")) {
                Platform.exit();
            }
        });

        layout.getChildren().add(submitButton);
    }

    @Override
    public void stop() {
        if (kSession != null) {
            kSession.dispose();
        }
    }

    public static class Path {
        private String state;
        private String Play;

        public Path(String state) {
            this.state = state;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
        
        public String getPlay() {
            return Play;
        }

        public void setPlay(String Play) {
            this.Play = Play;
        }
    }
}
