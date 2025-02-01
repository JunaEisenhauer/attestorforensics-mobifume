package com.attestorforensics.mobifumecore.controller.group.calculator;

import com.attestorforensics.mobifumecore.controller.CloseableController;
import com.attestorforensics.mobifumecore.controller.dialog.SaveDiscardCancelDialogController;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.controller.util.textformatter.UnsignedFloatTextFormatter;
import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.setting.Evaporant;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.setting.EvaporantSettings;
import com.attestorforensics.mobifumecore.model.setting.GroupSettings;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class GroupCalculatorController extends CloseableController {

  private static final double MAX_INPUT_VALUE = 999;

  @FXML
  Parent root;
  private Consumer<Double> callback;
  private Group group;
  @FXML
  private Label groupName;

  @FXML
  private ComboBox<String> evaporant;

  @FXML
  private TextField roomWidth;
  @FXML
  private TextField roomDepth;
  @FXML
  private TextField roomHeight;
  @FXML
  private TextField amountPerCm;
  @FXML
  private Text result;

  private TextField focusedField;
  private String selectedText;
  private boolean keyboardUsed;

  private Evaporant evaporantValue;
  private double roomWidthValue;
  private double roomDepthValue;
  private double roomHeightValue;
  private double amountPerCmValue;

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    Platform.runLater(() -> roomWidth.requestFocus());
  }

  void setCallback(Consumer<Double> callback) {
    this.callback = callback;
  }

  public void setGroup(Group group) {
    this.group = group;
    groupName.setText(group.getName() + " - " + group.getCycleNumber());

    GroupSettings groupSettings = group.getProcess().getSettings();
    EvaporantSettings evaporantSettings = groupSettings.evaporantSettings();

    roomWidth.setTextFormatter(new UnsignedFloatTextFormatter());
    roomWidth.setText(evaporantSettings.roomWidth() + "");
    roomWidth.textProperty().addListener((observableValue, oldText, newText) -> {
      updateInputEvaporantAmount();
      if (focusedField == roomWidth) {
        selectedText = roomWidth.getSelectedText();
      }
    });
    roomWidth.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(roomWidth, roomWidthValue, focused));

    roomDepth.setTextFormatter(new UnsignedFloatTextFormatter());
    roomDepth.setText(evaporantSettings.roomDepth() + "");
    roomDepth.textProperty().addListener((observableValue, oldText, newText) -> {
      updateInputEvaporantAmount();
      if (focusedField == roomDepth) {
        selectedText = roomDepth.getSelectedText();
      }
    });
    roomDepth.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(roomDepth, roomDepthValue, focused));

    roomHeight.setTextFormatter(new UnsignedFloatTextFormatter());
    roomHeight.setText(evaporantSettings.roomHeight() + "");
    roomHeight.textProperty().addListener((observableValue, oldText, newText) -> {
      updateInputEvaporantAmount();
      if (focusedField == roomHeight) {
        selectedText = roomHeight.getSelectedText();
      }
    });
    roomHeight.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(roomHeight, roomHeightValue, focused));

    amountPerCm.setTextFormatter(new UnsignedFloatTextFormatter());
    amountPerCm.setText(evaporantSettings.evaporantAmountPerCm() + "");
    amountPerCm.textProperty().addListener((observableValue, oldText, newText) -> {
      updateInputEvaporantAmount();
      if (focusedField == amountPerCm) {
        selectedText = amountPerCm.getSelectedText();
      }
    });
    amountPerCm.focusedProperty()
        .addListener((observableValue, oldState, focused) -> onFocus(amountPerCm, amountPerCmValue,
            focused));

    Evaporant evaporant = evaporantSettings.evaporant();

    ObservableList<String> evaporants = FXCollections.observableArrayList();
    Arrays.asList(Evaporant.values())
        .forEach(evapo -> evaporants.add(
            evapo.name().substring(0, 1).toUpperCase() + evapo.name().substring(1).toLowerCase()));
    this.evaporant.setItems(evaporants);
    this.evaporant.getSelectionModel()
        .select(evaporant.name().substring(0, 1).toUpperCase() + evaporant.name()
            .substring(1)
            .toLowerCase());
    this.evaporant.getSelectionModel()
        .selectedItemProperty()
        .addListener((observableValue, oldItem, newItem) -> {
          if (newItem.isEmpty()) {
            return;
          }

          evaporantValue = Evaporant.valueOf(newItem.toUpperCase());
          amountPerCmValue = evaporantValue.getAmountPerCm();
          amountPerCm.setText(amountPerCmValue + "");
          updateInputEvaporantAmount();
        });

    roomWidthValue = evaporantSettings.roomWidth();
    roomDepthValue = evaporantSettings.roomDepth();
    roomHeightValue = evaporantSettings.roomHeight();
    amountPerCmValue = evaporantSettings.evaporantAmountPerCm();
    evaporantValue = evaporantSettings.evaporant();
    updateInputEvaporantAmount();
  }

  private boolean haveSettingsChanged() {
    GroupSettings groupSettings = group.getProcess().getSettings();
    EvaporantSettings evaporantSettings = groupSettings.evaporantSettings();
    try {
      if (roomWidthValue <= MAX_INPUT_VALUE && roomWidthValue != evaporantSettings.roomWidth()) {
        return true;
      }
    } catch (NumberFormatException ignored) {
      // value invalid
    }
    try {
      if (roomDepthValue <= MAX_INPUT_VALUE && roomDepthValue != evaporantSettings.roomDepth()) {
        return true;
      }
    } catch (NumberFormatException ignored) {
      // value invalid
    }
    try {
      if (roomHeightValue <= MAX_INPUT_VALUE && roomHeightValue != evaporantSettings.roomHeight()) {
        return true;
      }
    } catch (NumberFormatException ignored) {
      // value invalid
    }
    try {
      if (amountPerCmValue <= MAX_INPUT_VALUE
          && amountPerCmValue != evaporantSettings.evaporantAmountPerCm()) {
        return true;
      }
    } catch (NumberFormatException ignored) {
      // value invalid
    }

    return evaporantValue != evaporantSettings.evaporant();
  }

  private void applySettings() {
    GroupSettings groupSettings = group.getProcess().getSettings();
    EvaporantSettings evaporantSettings = groupSettings.evaporantSettings();

    try {
      double roomWidthValue = Double.parseDouble(this.roomWidth.getText());
      if (roomWidthValue <= MAX_INPUT_VALUE) {
        evaporantSettings = evaporantSettings.roomWidth(roomWidthValue);
      }
    } catch (NumberFormatException ignored) {
      // value invalid
    }

    try {
      double roomDepthValue = Double.parseDouble(roomDepth.getText());
      if (roomDepthValue <= MAX_INPUT_VALUE) {
        evaporantSettings = evaporantSettings.roomDepth(roomDepthValue);
      }
    } catch (NumberFormatException ignored) {
      // value invalid
    }

    try {
      double roomHeightValue = Double.parseDouble(roomHeight.getText());
      if (roomHeightValue <= MAX_INPUT_VALUE) {
        evaporantSettings = evaporantSettings.roomHeight(roomHeightValue);
      }
    } catch (NumberFormatException ignored) {
      // value invalid
    }

    try {
      double amountPerCmValue = Double.parseDouble(amountPerCm.getText());
      if (amountPerCmValue <= MAX_INPUT_VALUE) {
        evaporantSettings = evaporantSettings.evaporantAmountPerCm(amountPerCmValue);
      }
    } catch (NumberFormatException ignored) {
      // value invalid
    }

    evaporantSettings = evaporantSettings.evaporant(evaporantValue);
    groupSettings = groupSettings.evaporantSettings(evaporantSettings);
    group.getProcess().setSettings(groupSettings);

    group.getLogger()
        .info("EVAPORANT;" + evaporantSettings.evaporant() + ";"
            + evaporantSettings.evaporantAmountPerCm() + ";" + evaporantSettings.roomWidth() + ";"
            + evaporantSettings.roomDepth() + ";" + evaporantSettings.roomHeight());
  }

  private void resetSettings() {
    GroupSettings groupSettings = group.getProcess().getSettings();
    EvaporantSettings evaporantSettings = groupSettings.evaporantSettings();
    roomWidthValue = evaporantSettings.roomWidth();
    roomWidth.setText(roomWidthValue + "");
    roomDepthValue = evaporantSettings.roomDepth();
    roomDepth.setText(roomDepthValue + "");
    roomHeightValue = evaporantSettings.roomHeight();
    roomHeight.setText(roomHeightValue + "");
    amountPerCmValue = evaporantSettings.evaporantAmountPerCm();
    amountPerCm.setText(amountPerCmValue + "");
    evaporantValue = evaporantSettings.evaporant();
    updateInputEvaporantAmount();
  }

  private void updateInputEvaporantAmount() {
    try {
      roomWidthValue = Double.parseDouble(this.roomWidth.getText());
      if (roomWidthValue > MAX_INPUT_VALUE) {
        result.setText(LocaleManager.getInstance().getString("group.amount.gramm", "-"));
        return;
      }
    } catch (NumberFormatException ignored) {
      // value invalid
      result.setText(LocaleManager.getInstance().getString("group.amount.gramm", "-"));
      return;
    }

    try {
      roomDepthValue = Double.parseDouble(roomDepth.getText());
      if (roomDepthValue > MAX_INPUT_VALUE) {
        result.setText(LocaleManager.getInstance().getString("group.amount.gramm", "-"));
        return;
      }
    } catch (NumberFormatException ignored) {
      // value invalid
      result.setText(LocaleManager.getInstance().getString("group.amount.gramm", "-"));
      return;
    }

    try {
      roomHeightValue = Double.parseDouble(roomHeight.getText());
      if (roomHeightValue > MAX_INPUT_VALUE) {
        result.setText(LocaleManager.getInstance().getString("group.amount.gramm", "-"));
        return;
      }
    } catch (NumberFormatException ignored) {
      // value invalid
      result.setText(LocaleManager.getInstance().getString("group.amount.gramm", "-"));
      return;
    }

    try {
      amountPerCmValue = Double.parseDouble(amountPerCm.getText());
      if (amountPerCmValue > MAX_INPUT_VALUE) {
        result.setText(LocaleManager.getInstance().getString("group.amount.gramm", "-"));
        return;
      }
    } catch (NumberFormatException ignored) {
      // value invalid
      result.setText(LocaleManager.getInstance().getString("group.amount.gramm", "-"));
      return;
    }

    double roomSize = roomWidthValue * roomDepthValue * roomHeightValue;
    double evaporantAmount = roomSize * amountPerCmValue;
    evaporantAmount = (double) Math.round(evaporantAmount * 100) / 100;
    result.setText(LocaleManager.getInstance().getString("group.amount.gramm", evaporantAmount));
  }

  private double calculateSettingsEvaporantAmount() {
    GroupSettings groupSettings = group.getProcess().getSettings();
    EvaporantSettings evaporantSettings = groupSettings.evaporantSettings();
    double roomSize = evaporantSettings.roomWidth() * evaporantSettings.roomDepth()
        * evaporantSettings.roomHeight();
    double evaporantAmount = roomSize * evaporantSettings.evaporantAmountPerCm();
    evaporantAmount = (double) Math.round(evaporantAmount * 100) / 100;
    return evaporantAmount;
  }

  private void onFocus(TextField field, double previousValue, boolean focused) {
    if (!focused) {
      Platform.runLater(() -> {
        if (!keyboardUsed) {
          focusedField = null;
          try {
            field.setText(Double.parseDouble(field.getText()) + "");
          } catch (NumberFormatException e) {
            field.setText(previousValue + "");
          }
        } else {
          field.requestFocus();
        }
      });

      return;
    }
    if (focusedField == field && keyboardUsed) {
      keyboardUsed = false;
      return;
    }

    Platform.runLater(() -> {
      focusedField = field;
      field.positionCaret(field.getLength());
      field.selectAll();
      selectedText = field.getSelectedText();
    });
  }

  private void closeCalculator() {
    close();
    if (callback != null) {
      callback.accept(calculateSettingsEvaporantAmount());
    }
  }

  @FXML
  public void onBack() {
    Sound.click();

    if (!haveSettingsChanged()) {
      closeCalculator();
      return;
    }

    this.<SaveDiscardCancelDialogController>loadAndOpenDialog("SaveDiscardCancelDialog.fxml")
        .thenAccept(controller -> {
          controller.setCallback(saveDiscardCancelResult -> {
            switch (saveDiscardCancelResult) {
              case SAVE:
                applySettings();
                closeCalculator();
                break;
              case DISCARD:
                resetSettings();
                closeCalculator();
                break;
              default:
                break;
            }
          });

          controller.setTitle(
              LocaleManager.getInstance().getString("dialog.calculator.save.title"));
          controller.setContent(
              LocaleManager.getInstance().getString("dialog.calculator.save.content"));
        });
  }

  @FXML
  public void onSelect(MouseEvent event) {
    Sound.click();

    TextField field = (TextField) event.getSource();
    Platform.runLater(field::selectAll);
  }

  @FXML
  public void onClear() {
    Sound.click();

    if (focusedField == null) {
      return;
    }
    focusedField.setText("");
    keyboardUsed = true;
  }

  @FXML
  public void onErase() {
    Sound.click();

    if (focusedField == null) {
      return;
    }

    if (selectedText.equals(focusedField.getText())) {
      focusedField.setText("");
    } else {
      focusedField.setText(focusedField.getText(0, focusedField.getLength() - 1));
    }
    Platform.runLater(() -> {
      focusedField.deselect();
      focusedField.positionCaret(focusedField.getLength());
    });
    keyboardUsed = true;
  }

  @FXML
  public void onMultiply() {
    Sound.click();

    if (focusedField == null) {
      return;
    }
    TextField field = focusedField;
    Platform.runLater(() -> {
      if (field == roomWidth) {
        roomDepth.requestFocus();
      } else if (field == roomDepth) {
        roomHeight.requestFocus();
      } else if (field == roomHeight) {
        amountPerCm.requestFocus();
      } else if (field == amountPerCm) {
        amountPerCm.getParent().requestFocus();
      }
    });
    keyboardUsed = true;
  }

  @FXML
  public void onCharacter(MouseEvent event) {
    Sound.click();

    if (focusedField == null) {
      return;
    }

    Button button = (Button) event.getSource();
    if (selectedText.equals(focusedField.getText())) {
      focusedField.setText(button.getText());
    } else {
      focusedField.appendText(button.getText());
    }

    Platform.runLater(() -> {
      focusedField.deselect();
      focusedField.positionCaret(focusedField.getLength());
    });

    keyboardUsed = true;
  }

  @FXML
  public void onSave() {
    Sound.click();

    applySettings();
    closeCalculator();
  }
}
