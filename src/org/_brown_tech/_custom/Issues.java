package org._brown_tech._custom;

import animatefx.animation.Shake;
import org._brown_tech.Main;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

/**
 * @author Mandela
 */
public class Issues extends Brain {

    private final String pathToErrorFolder = Main.RESOURCE_PATH.getAbsolutePath() + "\\_watchDog\\_error";
    private final String pathToInfoFolder = Main.RESOURCE_PATH.getAbsolutePath() + "\\_watchDog\\_info";

    @NotNull
    public final Boolean i_am_sure_of_it(String nameOfAction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(Main.stage);
        alert.setTitle("The app requires your confirmation to continue...");
        alert.setHeaderText("Are you sure you want to ".concat(nameOfAction).concat(" ?"));
        alert.setContentText("This can not be undone!");
        ButtonType yesButtonType = new ButtonType("YES");
        ButtonType noButtonType = new ButtonType("NO", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().setAll(yesButtonType, noButtonType);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get().equals(yesButtonType);
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public final Task<Object> stack_trace_printing(StackTraceElement[] stackTraceElements) {
        return new Task<Object>() {
            @Override
            public Object call() {
                write_stack_trace(stackTraceElements);
                return false;
            }
        };
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public final Task<Object> write_log(String string, int q) {
        return new Task<Object>() {
            @Override
            public Object call() {
                if (q == 1) {
                    record_error(string);
                }
                if (q == 2) {
                    activityLog(string);
                }
                return false;
            }
        };
    }

    public final void activityLog(String message) {
        BufferedWriter bw = null;
        try {
            File log = new File(pathToInfoFolder.concat("\\Account for {" + new SimpleDateFormat("dd-MMM-yyyy").format(Calendar.getInstance().getTime()).replaceAll("-", " ") + "}.txt"));
            if (!log.exists()) {
                if (log.createNewFile()) {
                    if (log.canWrite() & log.canRead()) {
                        FileWriter fw = new FileWriter(log, true);
                        bw = new BufferedWriter(fw);
                        bw.write("\nThis is a newly created file [ " + timeStamp() + " ].");
                    }
                }
            }
            if (log.canWrite() & log.canRead()) {
                FileWriter fw = new FileWriter(log, true);
                bw = new BufferedWriter(fw);
                bw.write(message);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            programmer_error(ex).show();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (Exception ex) {
                new Thread(write_log("\n\n" + timeStamp() + ": The following Exception occurred,\n" + ex, 1)).start();
                new Thread(stack_trace_printing(ex.getStackTrace())).start();
                ex.printStackTrace();
                programmer_error(ex).show();
            }
        }
    }

    public final void record_error(String rec_text) {
        BufferedWriter bw = null;
        try {
            File log = new File(pathToErrorFolder.concat(new SimpleDateFormat("dd-MMM-yyyy").format(Calendar.getInstance().getTime()).replaceAll("-", " ") + " Summarised Error log.txt"));
            if (!log.exists()) {
                if (log.createNewFile()) {
                    if (log.canWrite() & log.canRead()) {
                        FileWriter fw = new FileWriter(log, true);
                        bw = new BufferedWriter(fw);
                        bw.write("\nThis is a newly created file [ " + timeStamp() + " ].");
                    }
                }
            }
            if (log.canWrite() & log.canRead()) {
                FileWriter fw = new FileWriter(log, true);
                bw = new BufferedWriter(fw);
                bw.write("\n" + rec_text);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            programmer_error(ex).show();

        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (Exception ex) {
                new Thread(write_log("\n\n" + timeStamp() + ": The following Exception occurred,\n" + ex, 1)).start();
                new Thread(stack_trace_printing(ex.getStackTrace())).start();
                ex.printStackTrace();
                programmer_error(ex).show();
            }
        }
    }

    public final void write_stack_trace(StackTraceElement[] message) {
        BufferedWriter bw = null;
        try {
            File log = new File(pathToErrorFolder.concat(new SimpleDateFormat("dd-MMM-yyyy").format(Calendar.getInstance().getTime()).replaceAll("-", " ") + " Detailed Stack Trace Log.txt"));
            if (!log.exists()) {
                if (log.createNewFile()) {
                    if (log.canWrite() & log.canRead()) {
                        FileWriter fw = new FileWriter(log, true);
                        bw = new BufferedWriter(fw);
                        bw.write("\nThis is a newly created file [ " + timeStamp() + " ].");
                    }
                }
            }
            if (log.canWrite() & log.canRead()) {
                FileWriter fw = new FileWriter(log, true);
                bw = new BufferedWriter(fw);
                for (StackTraceElement stackTraceElement : message) {
                    bw.write("\n " + timeStamp() + "\n " + stackTraceElement.toString() + "\n ");
                }
            }
        } catch (IOException ex) {
            new Thread(write_log("\n\n" + timeStamp() + ": The following Exception occurred,\n" + ex, 1)).start();
            new Thread(stack_trace_printing(ex.getStackTrace())).start();
            ex.printStackTrace();
            programmer_error(ex).show();

        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (Exception ex) {
                new Thread(write_log("\n\n" + timeStamp() + ": The following Exception occurred,\n" + ex, 1)).start();
                new Thread(stack_trace_printing(ex.getStackTrace())).start();
                ex.printStackTrace();
                programmer_error(ex).show();
            }
        }
    }

    @NotNull
    public final String timeStamp() {
        return new SimpleDateFormat("dd-MMM-yyyy").format(Calendar.getInstance().getTime()) + " at " + new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime());
    }

    public final Notifications information_message(String message) {
        MaterialDesignIconView icon = new MaterialDesignIconView(MaterialDesignIcon.INFORMATION_OUTLINE);
        icon.setGlyphSize(30);
        icon.setGlyphStyle("-fx-fill : rgb(0, 80, 143);");
        return Notifications.create()
                .title("Info")
                .text(message)
                .graphic(icon)
                .hideAfter(Duration.seconds(8))
                .position(Pos.BASELINE_RIGHT);
    }

    protected final Notifications warning_message(String title, String text) {
        Image image = new Image("/org/_brown_tech/_images/_icons/icons8_Error_48px.png");
        return Notifications.create()
                .title(title)
                .text(text)
                .graphic(new ImageView(image))
                .hideAfter(Duration.seconds(8))
                .position(Pos.TOP_LEFT);
    }

    protected final Notifications empty_and_null_pointer_message(Node node) {
        Image image = new Image("/org/_brown_tech/_images/_icons/icons8_Error_48px.png");
        return Notifications.create()
                .title("Something is Missing")
                .text("Click Here to trace this Error.")
                .graphic(new ImageView(image))
                .hideAfter(Duration.seconds(8))
                .position(Pos.TOP_LEFT)
                .onAction(event -> {
                    new Shake(node).play();
                    node.requestFocus();
                });
    }

    @NotNull
    public final Alert error_message_alert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Brown tech encountered an Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        return alert;
    }

    public final Notifications success_notification(String about) {
        return Notifications.create()
                .title("Success")
                .text(about)
                .position(Pos.BASELINE_LEFT)
                .hideAfter(Duration.seconds(5))
                .graphic(new ImageView(new Image("/org/_brown_tech/_images/_icons/icons8_Ok_48px.png")));
    }

    public final Notifications error_message(String title, String text) {
        Image image = new Image("/org/_brown_tech/_images/_icons/icons8_Close_Window_48px.png");
        return Notifications.create()
                .title(title)
                .text(text)
                .graphic(new ImageView(image))
                .hideAfter(Duration.seconds(8))
                .position(Pos.TOP_RIGHT);
    }

    @NotNull
    public final Alert programmer_error(@NotNull Object object) {
        Exception exception = (Exception) object;
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(Main.stage);
        alert.setTitle("WATCH DOG");
        alert.setHeaderText("ERROR TYPE : " + exception.getClass());
        alert.setContentText("This dialog is a detailed explanation of the error that has occurred");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        String exceptionText = stringWriter.toString();
        Label label = new Label("The exception stacktrace was: ");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane gridPane = new GridPane();
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(label, 0, 0);
        gridPane.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(gridPane);
        return alert;
    }

}
