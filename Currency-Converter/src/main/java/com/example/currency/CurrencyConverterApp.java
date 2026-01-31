package com.example.currency;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CurrencyConverterApp extends Application {
    private final ExchangeRateService rateService = new ExchangeRateService();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Currency Converter");

        VBox root = new VBox(16);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(24));

        VBox card = new VBox(18);
        card.getStyleClass().add("card");
        card.setFillWidth(true);

        Label title = new Label("Currency Converter");
        title.getStyleClass().add("title");

        // Input amount
        Label amountLabel = new Label("Amount");
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount e.g. 100");

        VBox amountBox = new VBox(6, amountLabel, amountField);

        // Currency selectors
        Label fromLabel = new Label("From");
        ComboBox<CurrencyOption> fromCombo = new ComboBox<>();
        Label toLabel = new Label("To");
        ComboBox<CurrencyOption> toCombo = new ComboBox<>();

        List<CurrencyOption> options = commonCurrencies();
        fromCombo.getItems().addAll(options);
        toCombo.getItems().addAll(options);
        fromCombo.getSelectionModel().select(findByCode(options, "USD"));
        toCombo.getSelectionModel().select(findByCode(options, "INR"));

        fromCombo.setCellFactory(list -> new CurrencyOptionCell());
        fromCombo.setButtonCell(new CurrencyOptionCell());
        toCombo.setCellFactory(list -> new CurrencyOptionCell());
        toCombo.setButtonCell(new CurrencyOptionCell());

        GridPane selectGrid = new GridPane();
        selectGrid.setHgap(16);
        selectGrid.setVgap(8);
        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();
        c1.setPercentWidth(50);
        c2.setPercentWidth(50);
        selectGrid.getColumnConstraints().addAll(c1, c2);
        selectGrid.add(new VBox(6, fromLabel, fromCombo), 0, 0);
        selectGrid.add(new VBox(6, toLabel, toCombo), 1, 0);

        Button convertBtn = new Button("Convert");
        convertBtn.getStyleClass().add("primary");
        convertBtn.setMaxWidth(Double.MAX_VALUE);

        // Output section
        Label resultHeader = new Label("Result");
        Label resultLabel = new Label("Enter an amount and convert");
        resultLabel.getStyleClass().add("result");
        VBox resultBox = new VBox(8, resultHeader, resultLabel);
        resultBox.getStyleClass().add("output");

        // Validation label
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");

        convertBtn.setOnAction(e -> {
            errorLabel.setText("");
            resultLabel.setText("");
            String text = amountField.getText() == null ? "" : amountField.getText().trim();

            if (text.isEmpty()) {
                resultLabel.setText("Enter an amount to convert");
                return;
            }
            CurrencyOption from = fromCombo.getValue();
            CurrencyOption to = toCombo.getValue();
            if (from == null || to == null) {
                resultLabel.setText("Select currencies");
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(text);
            } catch (NumberFormatException ex) {
                resultLabel.setText("Amount must be a number");
                return;
            }
            if (amount.signum() < 0) {
                resultLabel.setText("Amount must be positive");
                return;
            }

            convertAsync(amount, from, to, resultLabel, errorLabel, convertBtn);
        });

        card.getChildren().addAll(title, amountBox, selectGrid, convertBtn, resultBox, errorLabel);

        StackPane container = new StackPane(card);
        StackPane.setAlignment(card, Pos.TOP_CENTER);
        Scene scene = new Scene(container, 520, 520);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void convertAsync(BigDecimal amount, CurrencyOption from, CurrencyOption to,
                              Label resultLabel, Label errorLabel, Button convertBtn) {
        convertBtn.setDisable(true);

        // 1) Immediate local conversion with fixed rates
        BigDecimal localRate = rateService.quickLocalRate(from.code(), to.code());
        if (localRate != null) {
            BigDecimal convertedLocal = amount.multiply(localRate).setScale(4, RoundingMode.HALF_UP);
            String fromText = formatAmount(amount, from);
            String toText = formatAmount(convertedLocal, to);
            resultLabel.setText(fromText + " → " + toText);
        } else {
            resultLabel.setText("Converting...");
        }

        // 2) Try live API; if succeeds, update the result silently
        CompletableFuture
                .supplyAsync(() -> rateService.fetchRate(from.code(), to.code()))
                .whenComplete((rate, throwable) -> Platform.runLater(() -> {
                    convertBtn.setDisable(false);
                    if (throwable != null || rate == null) {
                        if (localRate == null) {
                            errorLabel.setText("Rate unavailable for selected currencies.");
                            resultLabel.setText("");
                        }
                        return;
                    }

                    BigDecimal converted = amount.multiply(rate).setScale(4, RoundingMode.HALF_UP);
                    String fromText = formatAmount(amount, from);
                    String toText = formatAmount(converted, to);
                    resultLabel.setText(fromText + " → " + toText);
                }));
    }

    private String formatAmount(BigDecimal amount, CurrencyOption opt) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(4);
        nf.setMinimumFractionDigits(0);
        return nf.format(amount) + " " + opt.symbol() + " (" + opt.code() + ") " + opt.name();
    }

    private CurrencyOption findByCode(List<CurrencyOption> list, String code) {
        for (CurrencyOption o : list) {
            if (o.code().equals(code)) return o;
        }
        return list.isEmpty() ? null : list.get(0);
    }

    private List<CurrencyOption> commonCurrencies() {
        return List.of(
                new CurrencyOption("USD", "US Dollar", "$"),
                new CurrencyOption("EUR", "Euro", "€"),
                new CurrencyOption("GBP", "British Pound", "£"),
                new CurrencyOption("INR", "Indian Rupee", "₹"),
                new CurrencyOption("JPY", "Japanese Yen", "¥"),
                new CurrencyOption("AUD", "Australian Dollar", "$"),
                new CurrencyOption("CAD", "Canadian Dollar", "$"),
                new CurrencyOption("CHF", "Swiss Franc", "Fr"),
                new CurrencyOption("CNY", "Chinese Yuan", "¥"),
                new CurrencyOption("NZD", "New Zealand Dollar", "$")
        );
    }

    public static void main(String[] args) {
        launch(args);
    }

    private record CurrencyOption(String code, String name, String symbol) {}

    private static class CurrencyOptionCell extends ListCell<CurrencyOption> {
        @Override
        protected void updateItem(CurrencyOption item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.code() + " — " + item.name() + " (" + item.symbol() + ")");
            }
        }
    }
}
