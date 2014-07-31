/*
 * Copyright 2014 CIRDLES.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.topsoil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import netscape.javascript.JSObject;
import org.cirdles.topsoil.builder.TopsoilBuilderFactory;
import org.cirdles.topsoil.chart.CorrelatedUncertaintyXYDataPoint;
import org.cirdles.topsoil.chart.concordia.ErrorEllipse;
import org.cirdles.topsoil.chart.concordia.RecordToErrorEllipseConverter;
import org.cirdles.topsoil.table.Field;
import org.cirdles.topsoil.table.NumberField;
import org.cirdles.topsoil.table.Record;
import org.cirdles.topsoil.table.RecordTableColumn;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.dialog.Dialog;

public class ColumnSelectorDialog extends Dialog {

    private static final String MASTHEAD_TEXT = "Select the column for each variable.";
    private static final String ACTION_NAME = "Create chart";

    private static final Map<Double, String> ERROR_SIZES = new HashMap<>();

    static {
        ERROR_SIZES.put(1., "1\u03c3");
        ERROR_SIZES.put(2., "2\u03c3");
    }

    private static final Map<ExpressionType, String> EXPRESSION_TYPES = new HashMap<ExpressionType, String>();

    static {
        EXPRESSION_TYPES.put(ExpressionType.ABSOLUTE, "Abs");
        EXPRESSION_TYPES.put(ExpressionType.PERCENTAGE, "%");
    }

    public ColumnSelectorDialog(TableView<Record> tableToReadArg) {
        super(null, null);

        setContent(new ColumnSelectorView(tableToReadArg));
        getActions().addAll(new ColumnSelectorAction(tableToReadArg), Dialog.Actions.CANCEL);

        setResizable(false);
        setMasthead(MASTHEAD_TEXT);
    }

    /**
     * This UI element is used by the user to choose which column in the main table determine which value of an ellipse.
     */
    private class ColumnSelectorView extends GridPane {

        @FXML
        private ChoiceBox<Field<Number>> choiceBoxX;

        @FXML
        private ChoiceBox<Field<Number>> choiceBoxSigmaX;
        @FXML
        private ChoiceBox<Double> choiceBoxErrorSizeSigmaX;
        @FXML
        private ChoiceBox<ExpressionType> choiceBoxExpressionTypeSigmaX;

        @FXML
        private ChoiceBox<Field<Number>> choiceBoxY;

        @FXML
        private ChoiceBox<Field<Number>> choiceBoxSigmaY;
        @FXML
        private ChoiceBox<Double> choiceBoxErrorSizeSigmaY;
        @FXML
        private ChoiceBox<ExpressionType> choiceBoxExpressionTypeSigmaY;

        @FXML
        private ChoiceBox<Field<Number>> choiceBoxRho;

        public ColumnSelectorView(TableView<Record> table) {
            setAlignment(Pos.CENTER);
            setHgap(12);

            // Make the labels right align.
            ColumnConstraints labelConstraints = new ColumnConstraints();
            labelConstraints.setHalignment(HPos.RIGHT);
            getColumnConstraints().add(labelConstraints);

            List<Field<Number>> fields = new ArrayList<>(table.getColumns().size());
            for (TableColumn<Record, ?> column : table.getColumns()) {
                // Only add Field<Number>s from RecordTableColumns to fields.
                if (column instanceof RecordTableColumn) {
                    RecordTableColumn recordColumn = (RecordTableColumn) column;

                    if (recordColumn.getField() instanceof NumberField) {
                        fields.add(recordColumn.getField());
                    }
                }
            }

            FXMLLoader loader = new FXMLLoader(ColumnSelectorView.class.getResource("columndialogselector.fxml"),
                                               ResourceBundle.getBundle("org.cirdles.topsoil.Resources"));

            loader.setRoot(this);
            loader.setController(this);
            loader.setBuilderFactory(new TopsoilBuilderFactory());

            try {
                loader.load();

                fillChoiceBox(choiceBoxX, fields, 0);

                fillChoiceBox(choiceBoxSigmaX, fields, 1);
                fillChoiceBox(choiceBoxErrorSizeSigmaX, ERROR_SIZES, 0);
                fillChoiceBox(choiceBoxExpressionTypeSigmaX, EXPRESSION_TYPES, 0);

                fillChoiceBox(choiceBoxY, fields, 2);

                fillChoiceBox(choiceBoxSigmaY, fields, 3);
                fillChoiceBox(choiceBoxErrorSizeSigmaY, ERROR_SIZES, 0);
                fillChoiceBox(choiceBoxExpressionTypeSigmaY, EXPRESSION_TYPES, 0);

                fillChoiceBox(choiceBoxRho, fields, 4);

                linkChoiceBoxesSequentially(choiceBoxX, choiceBoxSigmaX);
                linkChoiceBoxesSequentially(choiceBoxY, choiceBoxSigmaY);
            } catch (IOException ex) {
                Logger.getLogger(ColumnSelectorDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public Field<Number> getXSelection() {
            return getSelection(choiceBoxX);
        }

        public Field<Number> getXUncertaintyField() {
            return getSelection(choiceBoxSigmaX);
        }

        public double getXUncertaintyConfidence() {
            return getSelection(choiceBoxErrorSizeSigmaX);
        }

        public ExpressionType getXUncertaintyExpressionType() {
            return getSelection(choiceBoxExpressionTypeSigmaX);
        }

        public Field<Number> getYSelection() {
            return getSelection(choiceBoxY);
        }

        public Field<Number> getYUncertaintyField() {
            return getSelection(choiceBoxSigmaY);
        }

        public double getYUncertaintyConfidence() {
            return getSelection(choiceBoxErrorSizeSigmaY);
        }

        public ExpressionType getYUncertaintyExpressionType() {
            return getSelection(choiceBoxExpressionTypeSigmaY);
        }

        public Field<Number> getRhoSelection() {
            return getSelection(choiceBoxRho);
        }

        private <T> T getSelection(ChoiceBox<T> choiceBox) {
            return choiceBox.getSelectionModel().getSelectedItem();
        }
    }

    private class ColumnSelectorAction extends AbstractAction {

        private final TableView<Record> table;

        public ColumnSelectorAction(TableView<Record> table) {
            super(ACTION_NAME);
            this.table = table;
        }

        @Override
        public void handle(ActionEvent ae) {
            hide();

            final ColumnSelectorView columnSelector = (ColumnSelectorView) getContent();
            RecordToErrorEllipseConverter converter
                    = new RecordToErrorEllipseConverter(columnSelector.getXSelection(), columnSelector.getXUncertaintyField(),
                                                        columnSelector.getYSelection(), columnSelector.getYUncertaintyField(),
                                                        columnSelector.getRhoSelection());

            converter.setErrorSizeSigmaX(columnSelector.getXUncertaintyConfidence());
            converter.setExpressionTypeSigmaX(columnSelector.getXUncertaintyExpressionType());
            converter.setErrorSizeSigmaY(columnSelector.getYUncertaintyConfidence());
            converter.setExpressionTypeSigmaY(columnSelector.getYUncertaintyExpressionType());

            Series<Number, Number> series = new Series<>();
            CorrelatedUncertaintyXYDataPoint[] data = new CorrelatedUncertaintyXYDataPoint[table.getItems().size()];
            int i = 0;

            for (Record record : table.getItems()) {
                data[i++] = new CorrelatedUncertaintyXYDataPoint() {
                    private final Field<Number> xField = columnSelector.getXSelection();
                    private final Field<Number> sigmaXField = columnSelector.getXUncertaintyField();
                    private final Field<Number> yField = columnSelector.getYSelection();
                    private final Field<Number> sigmaYField = columnSelector.getYUncertaintyField();
                    private final Field<Number> rhoField = columnSelector.getRhoSelection();

                    @Override
                    public double getX() {
                        return record.getValue(xField).doubleValue();
                    }

                    @Override
                    public double getSigmaX() {
                        return record.getValue(sigmaXField).doubleValue() / columnSelector.getXUncertaintyConfidence()
                                * (columnSelector.getXUncertaintyExpressionType() == ExpressionType.ABSOLUTE ? 1 : record.getValue(xField).doubleValue() / 100);
                    }

                    @Override
                    public double getY() {
                        return record.getValue(yField).doubleValue();
                    }

                    @Override
                    public double getSigmaY() {
                        return record.getValue(sigmaYField).doubleValue() / columnSelector.getYUncertaintyConfidence()
                                * (columnSelector.getYUncertaintyExpressionType() == ExpressionType.ABSOLUTE ? 1 : record.getValue(yField).doubleValue() / 100);
                    }

                    @Override
                    public double getRho() {
                        return record.getValue(rhoField).doubleValue();
                    }
                }.wrap();
            }

            WebView chart = new WebView();
            WebEngine engine = chart.getEngine();

            engine.setOnError(event -> System.err.println(event.getMessage()));
            engine.setOnAlert(event -> System.out.println(event.getData()));

            engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                    if (newValue.equals(Worker.State.SUCCEEDED)) {
                        JSObject window = (JSObject) engine.executeScript("window");

                        window.setMember("data", data);
                        engine.executeScript("fitPlot();");
                    }
                }
            });

            Button fitPlot = new Button("Fit Plot");
            fitPlot.setOnAction(event -> engine.executeScript("fitPlot();"));

            engine.load(getClass().getResource("../../../web/concordia.html").toExternalForm());

            Scene scene = new Scene(new VBox(chart, fitPlot), 1200, 800);
            Stage chartStage = new Stage();
            chartStage.setScene(scene);
            chartStage.show();
        }
    }

    /*
     * Utility methods for ColumnSelectorView
     */
    /**
     * Create a <code>ChoiceBox</code> with the right parameters
     */
    private static void fillChoiceBox(ChoiceBox<Field<Number>> choiceBox, List<Field<Number>> fields, int initialSelection) {
        choiceBox.getItems().addAll(fields);
        choiceBox.getSelectionModel().select(initialSelection);
        choiceBox.setMinWidth(200);

        choiceBox.setConverter(new StringConverter<Field<Number>>() {

            /*
             * Converts a field to a <code>String</code> by returning its name with all newlines replaced by spaces.
             */
            @Override
            public String toString(Field field) {
                return field.getName().replaceAll("\n", " ");
            }

            /*
             * Instead of converting from a String to a Field as it should as a
             * StringConverter method, this method always returns null, since this method is
             * never used by the ChoiceBox and cannot be implemented deterministically (field names are not
             * unique).
             */
            @Override
            public Field fromString(String string) {
                return null;
            }
        });
    }

    private static <T> void fillChoiceBox(ChoiceBox<T> choiceBox, Map<T, String> contents, int initialSelection) {
        choiceBox.getItems().addAll(contents.keySet());
        choiceBox.setMinWidth(50);
        choiceBox.setMaxWidth(50);
        choiceBox.getSelectionModel().select(initialSelection);

        choiceBox.setConverter(new StringConverter<T>() {

            @Override
            public String toString(T object) {
                return contents.get(object);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        });

    }

    /**
     * Adds a listener to the first <code>ChoiceBox</code>'s <code>SelectionModel</code>'s index property that tries to
     * set the index of the second <code>ChoiceBox</code>'s <code>SelectionModel</code> to the next index whenever the
     * first's changes. This is because often times the error of a value immediately follows the value itself in the
     * table, so this should be a better guess than the default value once the first <code>ChoiceBox</code> is set by
     * the user.
     *
     * @param first
     * @param second
     */
    private static void linkChoiceBoxesSequentially(ChoiceBox first, ChoiceBox second) {
        first.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.doubleValue() + 1 < second.getItems().size()) {
                    second.getSelectionModel().select(newValue.intValue() + 1);
                }
            }
        });
    }

}
