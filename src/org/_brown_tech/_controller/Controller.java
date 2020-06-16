package org._brown_tech._controller;

import animatefx.animation.*;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org._brown_tech.Main;
import org._brown_tech._custom.Brain;
import org._brown_tech._custom.PrintWork;
import org._brown_tech._object.Account;
import org._brown_tech._object.OnlineService;
import org._brown_tech._object._payment.*;
import org._brown_tech._outsourced.BCrypt;
import org._brown_tech._outsourced.PasswordDialog;
import org._brown_tech._table_object_model.Stock;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.controlsfx.control.Notifications;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * @author Mandela
 */
public class Controller extends Brain implements Initializable {

    public static HashMap<Integer, CartItem> cartItems;
    private final Notifications AMOUNT_PAID_IS_TOO_LOW_NOTIFICATION = error_message("Not enough", "You have to provide cash equal to or even more so as to provide change!");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private final JFXAutoCompletePopup<String> productSerialsPopup = new JFXAutoCompletePopup<>();
    private final JFXAutoCompletePopup<String> addProductSerialsPopup = new JFXAutoCompletePopup<>();
    private final JFXAutoCompletePopup<String> listOfProductDetailPopup = new JFXAutoCompletePopup<>();
    private final JFXAutoCompletePopup<String> mpesaPhoneAndTransactionCodePopup = new JFXAutoCompletePopup<>();
    private final UnaryOperator<TextFormatter.Change> integerFilter = change -> {
        String newText = change.getControlNewText();
        if (newText.matches("-?([1-9][0-9]*)?")) {
            return change;
        } else if ("-".equals(change.getText())) {
            if (change.getControlText().startsWith("-")) {
                change.setText("");
                change.setRange(0, 1);
                change.setCaretPosition(change.getCaretPosition() - 2);
                change.setAnchor(change.getAnchor() - 2);
            } else {
                change.setRange(0, 0);
            }
            return change;
        }
        return null;
    };
    private final StringConverter<Integer> converter = new IntegerStringConverter() {
        @Override
        public Integer fromString(@NotNull String string) {
            if (string.isEmpty()) {
                return 0;
            }
            return super.fromString(string);
        }
    };
    private Product product = null;
    private String USER_MWENYE_AMELOGIN = null;
    private Account myAccount = null;
    private String PATH_TO_IMAGE_OF_NEW_PRODUCT = null;
    private final List<String> knownProductSerials = new ArrayList<>();
    protected Task<Object> billAmountAnimation;
    protected static boolean anItemHasBeenRemovedFromCart = false;
    private final ObservableList<String> productSerials = FXCollections.observableArrayList();


    @FXML
    private StackPane phaseTwo;

    @FXML
    private StackPane displayPane;

    @FXML
    private StackPane paymentsPane;

    @FXML
    private VBox cartBox;

    @FXML
    protected Label totalBillLbl;

    @FXML
    private AnchorPane cashUnderline;

    @FXML
    private AnchorPane chequeUnderline;

    @FXML
    private AnchorPane mpesaUnderline;

    @FXML
    private AnchorPane mpesaPane;

    @FXML
    private JFXTextField mpesaDetailTF;

    @FXML
    private JFXTextField mpesaTF;

    @FXML
    private AnchorPane chequePane;

    @FXML
    private JFXTextField chequeTF;

    @FXML
    private JFXButton fromCheque;

    @FXML
    private JFXTextField chequeNumTF;

    @FXML
    private JFXTextField drawerNameTF;

    @FXML
    private JFXDatePicker maturityDateTF;

    @FXML
    private JFXComboBox<String> banksCbx;

    @FXML
    private AnchorPane cashPane;

    @FXML
    private JFXTextField cashTF;

    @FXML
    private JFXButton fromCash;

    @FXML
    private StackPane myaccountPane;

    @FXML
    private Label accInfoLbl;

    @FXML
    private JFXTextField accUsernameTF;

    @FXML
    private JFXTextField accFirstnameTF;

    @FXML
    private JFXTextField accSurnameTF;

    @FXML
    private JFXTextField accEmailTF;

    @FXML
    private JFXTextField plainSiriTF1;

    @FXML
    private JFXPasswordField siriPF1;

    @FXML
    private JFXButton hideShowSiriBtn1;

    @FXML
    private JFXTextField plainSiriTF2;

    @FXML
    private JFXPasswordField siriPF2;

    @FXML
    private JFXButton hideShowSiriBtn2;

    @FXML
    private StackPane addStockPane;

    @FXML
    private JFXTextField prodSerialTF;

    @FXML
    private JFXTextField prodQtyTF;

    @FXML
    private JFXTextField prodNameTF;

    @FXML
    private JFXTextField prodDescriptionTF;

    @FXML
    private JFXTextField prodMarkedPriceTF;

    @FXML
    private JFXTextField prodBuyingPriceTF;

    @FXML
    private JFXComboBox<String> prodRatingsCbx;

    @FXML
    private ImageView prodView;

    @FXML
    private StackPane viewStockPane;

    @FXML
    private JFXTextField productSearchDetailsTF;

    @FXML
    private JFXTreeTableView<Stock> stockTable;

    @FXML
    private TreeTableColumn<Stock, String> serialCol;

    @FXML
    private TreeTableColumn<Stock, String> nameCol;

    @FXML
    private TreeTableColumn<Stock, String> descriptionCol;

    @FXML
    private TreeTableColumn<Stock, ImageView> ratingsCol;

    @FXML
    private TreeTableColumn<Stock, String> quantityCol;

    @FXML
    private TreeTableColumn<Stock, String> markedPriceCol;

    @FXML
    private TreeTableColumn<Stock, String> statusCol;

    @FXML
    private StackPane servicesPane;

    @FXML
    private JFXComboBox<String> servicesCbx;

    @FXML
    private JFXTextField serviceCostTF;

    @FXML
    private StackPane productsPane;

    @FXML
    private JFXTextField productSerial;

    @FXML
    private ImageView productView;

    @FXML
    private Label pNameLbl;

    @FXML
    private Label pDescriptionLbl;

    @FXML
    private ImageView pQualityView;

    @FXML
    private Label pMarkedPriceLbl;

    @FXML
    private Label pQuantityLeftLbl;

    @FXML
    private JFXTextField quantityTF;

    @FXML
    private JFXTextField sellingPriceTF;

    @FXML
    private JFXButton fromProduct;

    @FXML
    private StackPane menuPane;

    @FXML
    private JFXButton productBtn;

    @FXML
    private StackPane phaseOne;

    @FXML
    private StackPane loginPane;

    @FXML
    private JFXTextField usernameTF;

    @FXML
    private JFXTextField plainSiriTF;

    @FXML
    private JFXPasswordField siriPF;

    @FXML
    private JFXButton hideShowSiriBtn;

    @FXML
    private StackPane splashPane;

    @FXML
    private JFXProgressBar loadingBar;

    @FXML
    private Label threadUpdate;

    @FXML
    private Label userFullnameTF;

    @FXML
    private void add_to_cart(@NotNull ActionEvent event) {
        if (event.getSource().equals(fromProduct)) {
            if (sellingPriceTF.getText().trim().isEmpty() || sellingPriceTF.getText() == null) {
                empty_and_null_pointer_message(sellingPriceTF).show();
                return;
            }
            if (product == null) {
                warning_message("Please hold up!", "Ensure you have searched for an item to precede!").show();
                return;
            }
            final int requestedQuantity = Integer.parseInt(quantityTF.getText().trim());
            if (requestedQuantity == 0) {
                warning_message("Incomplete!", "The customer has to be provided with one or more items not ZERO of them").show();
                return;
            }
            final double agreedCost = Double.parseDouble(sellingPriceTF.getText().trim());
            final double lowestCost = (product.getBuyingPrice() * requestedQuantity);
            if (agreedCost > lowestCost) {
                final CartItem cartItem = new CartItem();
                cartItem.setProductOrServiceSerial(product.getSerial_number());
                cartItem.setName(product.getName());
                cartItem.setQuantityRequested(requestedQuantity);
                cartItem.setPrice(agreedCost);
                cartItem.setBuyingPrice(product.getBuyingPrice());
                cartItem.setProduct(true);
                if (add_product_or_service_to_cart(cartItem)) {
                    final int rowId = get_row_id_of_cart_item(product.getSerial_number(), cartItem);
                    if (rowId == -1) {
                        error_message("Process incomplete!", "The product has been added to cart but i can't get its index position").show();
                        return;
                    }
                    quantityTF.setText("0");
                    sellingPriceTF.setText("0");
                    product = null;
                    cartItems = get_all_cart_items();
                    new Thread(display_cart_items()).start();
                    start_animate_bill_amount();
                    reset_product_search_info();
                    success_notification("Product has been successfully added to cart").show();
                } else {
                    error_message("Failed", "The product wa NOT successfully added to cart!").show();
                }
            } else {
                warning_message("Discount alert!", "The discount is too high for the product, otherwise please sell at a reasonable price").show();
            }
        } else {
            if (servicesCbx.getSelectionModel().getSelectedItem().trim().isEmpty() || servicesCbx.getSelectionModel().getSelectedItem() == null) {
                empty_and_null_pointer_message(servicesCbx).show();
                return;
            }
            final OnlineService onlineService = get_selected_service(servicesCbx.getSelectionModel().getSelectedItem());
            if (onlineService == null) {
                error_message("Not found", "Service has NOT been found").show();
                return;
            }
            if (Double.parseDouble(serviceCostTF.getText().trim()) > 0) {
                final CartItem cartItem = new CartItem();
                cartItem.setProductOrServiceSerial("S".concat(String.format("%d", onlineService.getSerial())));
                cartItem.setName(onlineService.getName());
                cartItem.setPrice(Double.parseDouble(serviceCostTF.getText().trim()));
                cartItem.setQuantityRequested(1);
                cartItem.setBuyingPrice(0.0);
                cartItem.setProduct(false);
                if (add_product_or_service_to_cart(cartItem)) {
                    final int rowId = get_row_id_of_cart_item(cartItem.getProductOrServiceSerial(), cartItem);
                    if (rowId == -1) {
                        error_message("Process incomplete!", "The service has been added to cart but i can't get its index position").show();
                        return;
                    }
                    serviceCostTF.setText("0");
                    cartItems = get_all_cart_items();
                    new Thread(display_cart_items()).start();
                    start_animate_bill_amount();
                    reset_product_search_info();
                    success_notification("The service has been successfully added to cart").show();
                } else {
                    error_message("Failed", "The service wa NOT successfully added to cart!").show();
                }
            } else {
                warning_message("Discount alert!", "The discount is too high for the service, otherwise please charge reasonably").show();
            }
        }
    }

    @FXML
    private void allow_user_to_login(ActionEvent event) {
        if (event != null) {
            if (usernameTF.getText().trim().isEmpty() || usernameTF.getText() == null) {
                empty_and_null_pointer_message(usernameTF.getParent()).show();
                return;
            }
            String password;
            if (hideShowSiriBtn.getText().startsWith("Show")) {
                if (siriPF.getText().trim().isEmpty() || siriPF.getText() == null) {
                    empty_and_null_pointer_message(siriPF.getParent().getParent()).show();
                    return;
                }
                password = siriPF.getText().trim();
            } else {
                if (plainSiriTF.getText().trim().isEmpty() || plainSiriTF.getText() == null) {
                    empty_and_null_pointer_message(plainSiriTF.getParent().getParent()).show();
                    return;
                }
                password = plainSiriTF.getText().trim();
            }
            final Account account = get_user_account(usernameTF.getText().trim(), password);
            if (account == null) {
                error_message("Failed!", "Your account could not be accessed").show();
            } else {
                this.myAccount = account;
                new FadeOut(loginPane).play();
                splashPane.toFront();
                new FadeIn(splashPane).setDelay(Duration.seconds(0.5)).play();

                breath_of_life();

                USER_MWENYE_AMELOGIN = this.myAccount.getUsername();

                usernameTF.setText("");
                siriPF.setText("");
                plainSiriTF.setText("");

                Main.stage.setTitle("Welcome ".concat(this.myAccount.getFname()));

                userFullnameTF.setText(this.myAccount.getFname().concat(" ").concat(this.myAccount.getSurname()));
            }
        }
    }

    @FXML
    private void check_if_the_serial_exists(ActionEvent event) {
        if (prodSerialTF.getText().trim().isEmpty() || prodSerialTF.getText() == null) {
            empty_and_null_pointer_message(prodSerialTF).show();
            return;
        }
        if (productSerials.contains(prodSerialTF.getText().trim())) {
            prodSerialTF.setText("");
            error_message("Item found", "The serial number already exists, please enter a new one").show();
        }
    }

    @FXML
    private void choose_image_from_local_drive(ActionEvent event) {
        try {
            PATH_TO_IMAGE_OF_NEW_PRODUCT = null;
            final FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG only", "*.png"));
            final File SOURCE_FILE = fileChooser.showOpenDialog(Main.stage);
            if (SOURCE_FILE == null) {
                error_message("No image selected", "Try again..").show();
                return;
            }
            final File destination_folder = new File(Main.RESOURCE_PATH.getAbsolutePath().concat("\\_gallery\\"));
            try {
                FileUtils.copyFileToDirectory(SOURCE_FILE, destination_folder);
                PATH_TO_IMAGE_OF_NEW_PRODUCT = destination_folder.getAbsolutePath().concat("\\".concat(SOURCE_FILE.getName()));
                prodView.setImage(new Image(new FileInputStream(destination_folder.getAbsolutePath().concat("\\".concat(SOURCE_FILE.getName())))));
            } catch (IOException e) {
                if (e.getLocalizedMessage().contains("are the same")) {
                    warning_message("Duplicate image id found", "The image you have selected already exists in the my gallery.").show();
                    prodView.setImage(new Image(new FileInputStream(destination_folder.getAbsolutePath().concat("\\".concat(SOURCE_FILE.getName())))));
                    return;
                }
                prodView.setImage(new Image(getClass().getResourceAsStream(INPUT_STREAM_TO_NO_IMAGE)));
                e.printStackTrace();
                programmer_error(e).show();
                new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
                new Thread(stack_trace_printing(e)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
        }
    }

    @FXML
    private void increase_quantity(ActionEvent event) {
        if (event != null) {
            int initialCount = Integer.parseInt(quantityTF.getText().trim());
            ++initialCount;
            if (product != null) {
                if (initialCount >= product.getStock()) {
                    quantityTF.setText(String.format("%d", product.getStock()));
                    error_message("MAX Reached", "Sorry but you cant request more than ".concat(quantityTF.getText()).concat(" pieces of it!")).show();
                    return;
                }
            }
            quantityTF.setText(String.format("%d", initialCount));
        }
    }

    @FXML
    private void log_out_from_my_account(ActionEvent event) {
        if (event != null) {
            //use fire() to trigger method events linked to the button
            productBtn.fire();
            new FadeOut(displayPane).play();
            new FadeOutLeft(menuPane).play();
            myAccount = null;
            product = null;
            USER_MWENYE_AMELOGIN = null;
            cartItems = null;
            loadingBar.setProgress(0);
            new FadeOut(phaseTwo).play();
            phaseOne.toFront();
            new FadeIn(phaseOne).setDelay(Duration.seconds(0.5)).play();
            new FadeOut(splashPane).play();
            loginPane.toFront();
            new FadeIn(loginPane).setDelay(Duration.seconds(0.5)).play();
        }
    }

    @FXML
    private void pick_details_of_the_transaction(ActionEvent event) {

    }

    /**
     * 0 for cash
     * 1 for cheque
     * 2 for mpesa
     */
    @FXML
    private void process_payment(ActionEvent event) {
        final double CART_BILL_AMOUNT = get_cost_of_items_in_cart();
        if (CART_BILL_AMOUNT > 0) {
            Sale sale = new Sale();
            sale.setDateTime(get_date_and_time());
            sale.setBillAmount(CART_BILL_AMOUNT);
            if (event.getSource().equals(fromCash)) {
                sale.setPaymentMethod(0);
                sale.setUsername(this.myAccount.getUsername());
                if (cashTF.getText().trim().equals("0") || cashTF.getText().trim().isEmpty() || cashTF.getText() == null) {
                    empty_and_null_pointer_message(cashTF).show();
                    return;
                }
                final double PAID_AMOUNT = Double.parseDouble(cashTF.getText().trim());
                if (PAID_AMOUNT >= CART_BILL_AMOUNT) {
                    sale.setReceipts(new ArrayList<>());
                    create_receipt_record_from_cart_items(sale);
                    final int NEW_RECEIPT_NUMBER = get_a_receipt_number_for_the_sale(sale);
                    final Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.initOwner(Main.stage);
                    alert.setTitle("BALANCE STATUS");
                    alert.setHeaderText("Change is Ksh ".concat(String.format("%,.2f", (PAID_AMOUNT - CART_BILL_AMOUNT))));
                    alert.setContentText("Customer Paid Ksh ".concat(String.format("%,.2f", PAID_AMOUNT)).concat("\nto clear his bill of Ksh ").concat(String.format("%,.2f", CART_BILL_AMOUNT)).concat("."));
                    final Cash cash = new Cash();
                    cash.setDateTime(get_date_and_time());
                    cash.setReceiptNumber(NEW_RECEIPT_NUMBER);
                    cash.setAmount(CART_BILL_AMOUNT);
                    if (upload_cash_record(cash)) {
                        if (update_products()) {
                            alert.show();
                            PrintWork.bill_items.addAll(get_receipt_items_from_cart(cartItems));
                            clear_cart(true);
                            success_notification("The sale has been recorded and the stock may have been updated").show();
                            receipt_formatting_for_printing(NEW_RECEIPT_NUMBER, PAID_AMOUNT, CART_BILL_AMOUNT, 0);
                            new Thread(display_cart_items()).start();
                            start_animate_bill_amount();
                            cashTF.setText("");
                        } else {
                            error_message("Incomplete!", "The stock was not updated").show();
                        }
                    } else {
                        error_message("Incomplete!", "The cash record was not uploaded!").show();
                    }
                } else {
                    AMOUNT_PAID_IS_TOO_LOW_NOTIFICATION.show();
                }
            } else if (event.getSource().equals(fromCheque)) {
                if (chequeNumTF.getText().trim().equals("0") || chequeNumTF.getText().trim().isEmpty() || chequeNumTF.getText() == null) {
                    empty_and_null_pointer_message(chequeNumTF).show();
                    return;
                }
                if (drawerNameTF.getText().trim().isEmpty() || drawerNameTF.getText() == null) {
                    empty_and_null_pointer_message(drawerNameTF).show();
                    return;
                }
                if (maturityDateTF.getValue() == null) {
                    empty_and_null_pointer_message(maturityDateTF).show();
                    return;
                }
                if (banksCbx.getSelectionModel().getSelectedItem().isEmpty() || banksCbx.getSelectionModel().getSelectedItem() == null) {
                    empty_and_null_pointer_message(banksCbx).show();
                    return;
                }
                if (chequeTF.getText().trim().equals("0") || chequeTF.getText().trim().isEmpty() || chequeTF.getText() == null) {
                    empty_and_null_pointer_message(chequeTF).show();
                    return;
                }
                final double PAID_AMOUNT = Double.parseDouble(chequeTF.getText().trim());
                if (PAID_AMOUNT >= CART_BILL_AMOUNT) {
                    sale.setReceipts(new ArrayList<>());
                    create_receipt_record_from_cart_items(sale);
                    final int NEW_RECEIPT_NUMBER = get_a_receipt_number_for_the_sale(sale);
                    Cheque cheque = new Cheque();
                    cheque.setDateTime(get_date_and_time());
                    cheque.setReceiptNumber(NEW_RECEIPT_NUMBER);
                    cheque.setChequeNumber(Integer.parseInt(chequeNumTF.getText().trim()));
                    cheque.setDrawerName(drawerNameTF.getText().trim());
                    cheque.setMaturityDate(maturityDateTF.getValue().format(dateTimeFormatter));
                    cheque.setBank(banksCbx.getSelectionModel().getSelectedItem());
                    cheque.setAmount(CART_BILL_AMOUNT);
                    if (upload_to_cheque_record(cheque)) {
                        if (update_products()) {
                            PrintWork.bill_items.addAll(get_receipt_items_from_cart(cartItems));
                            clear_cart(true);
                            success_notification("The sale has been recorded and the stock may have been updated").show();
                            receipt_formatting_for_printing(NEW_RECEIPT_NUMBER, PAID_AMOUNT, CART_BILL_AMOUNT, 1);
                            new Thread(display_cart_items()).start();
                            start_animate_bill_amount();
                            chequeNumTF.setText("");
                            drawerNameTF.setText("");
                            maturityDateTF.getEditor().setText("");
                            banksCbx.getSelectionModel().clearSelection();
                            chequeTF.setText("");
                        } else {
                            error_message("Incomplete!", "The stock was not updated").show();
                        }
                    } else {
                        error_message("Incomplete!", "The sale record has been created and its details was written to receipt record but the sale was NOT written to cheque record!").show();
                    }
                } else {
                    AMOUNT_PAID_IS_TOO_LOW_NOTIFICATION.show();
                }
            } else {
                warning_message("Incomplete function!", "This feature is still under development, stay tuned!").show();
            }
        } else {
            error_message("Empty Cart!", "You have to sell a product or charge a service to continue!").show();
        }
    }

    @FXML
    private void reduce_quantity(ActionEvent event) {
        if (event != null) {
            int initialCount = Integer.parseInt(quantityTF.getText().trim());
            --initialCount;
            if (initialCount < 0) {
                quantityTF.setText("0");
            } else {
                quantityTF.setText(String.format("%d", initialCount));
            }
        }
    }

    @FXML
    private void reload_stock(@NotNull MouseEvent mouseEvent) {
        final List<Product> productList = get_product_list();
        put_stock_items_into_table(generate_stock_model_from_product_model(productList));
        final Node node = (Node) mouseEvent.getSource();
        new RotateOut(node).setResetOnFinished(true).play();
    }

    @FXML
    private void search_for_product(ActionEvent event) {
        if (productSerial.getText().trim().isEmpty() || productSerial.getText() == null) {
            reset_product_search_info();
            empty_and_null_pointer_message(productSerial.getParent()).show();
            return;
        }
        product = get_details_of_a_product(productSerial.getText().trim());
        if (product == null) {
            reset_product_search_info();
            error_message("Not found!", "The serial number is NOT found").show();
        } else {
            Image image;
            if (product.getImage() == null) {
                image = new Image(getClass().getResourceAsStream(INPUT_STREAM_TO_NO_IMAGE));
            } else {
                try {
                    byte[] decodedImage = Base64.getDecoder().decode(product.getImage());
                    File productImageFile = new File(FileUtils.getTempDirectoryPath().concat("\\_brownTech\\_gallery\\").concat(RandomStringUtils.randomAlphabetic(10)).concat(".png"));
                    System.out.println("productImageFile >>" + productImageFile.getAbsolutePath());
                    FileUtils.writeByteArrayToFile(productImageFile, decodedImage);
                    image = new Image(new FileInputStream(productImageFile));
                } catch (Exception e) {
                    image = new Image(getClass().getResourceAsStream(INPUT_STREAM_TO_NO_IMAGE));
                }
            }
            view_image_with_dropShadow_effect(image, productView);
            pNameLbl.setText(product.getName());
            pDescriptionLbl.setText(product.getDescription());
            pQualityView.setImage(get_image_of_ratings(product.getRating()));
            pMarkedPriceLbl.setText("Ksh ".concat(String.format("%,.1f", product.getMarkedPrice())));
            pQuantityLeftLbl.setText(format_quantity(product.getStock()));
        }
    }

    @FXML
    private void show_cash(ActionEvent event) {
        if (event != null) {
            final JFXButton jfxButton = (JFXButton) event.getSource();
            highlight_selected_payment_option(jfxButton);
            view_payment_options(cashPane);
        }
    }

    @FXML
    private void show_cheque(ActionEvent event) {
        if (event != null) {
            final JFXButton jfxButton = (JFXButton) event.getSource();
            highlight_selected_payment_option(jfxButton);
            view_payment_options(chequePane);
        }
    }

    @FXML
    private void show_mpesa(ActionEvent event) {
        if (event != null) {
            final JFXButton jfxButton = (JFXButton) event.getSource();
            highlight_selected_payment_option(jfxButton);
            view_payment_options(mpesaPane);
        }
    }

    @FXML
    private void show_my_account(ActionEvent event) {
        if (event != null) {
            if (myAccount == null) {
                warning_message("Feature failed", "We could not fetch your account details to continue...").show();
                return;
            }
            switch_widows_in_displayPane(myaccountPane);
            final JFXButton jfxButton = (JFXButton) event.getSource();
            highlight_button_clicked_in_menuPane(jfxButton);
            display_or_hide_payments_pane(myaccountPane);
        }
    }

    @FXML
    private void show_or_hide_password(ActionEvent event) {
        if (event != null) {
            final JFXButton jfxButton = (JFXButton) event.getSource();
            if (jfxButton == hideShowSiriBtn) {
                show_or_hide_any_password(jfxButton, plainSiriTF, siriPF);
            } else if (jfxButton == hideShowSiriBtn1) {
                show_or_hide_any_password(jfxButton, plainSiriTF1, siriPF1);
            } else {
                show_or_hide_any_password(jfxButton, plainSiriTF2, siriPF2);
            }
        }
    }

    @FXML
    private void show_products(ActionEvent event) {
        if (event != null) {
            switch_widows_in_displayPane(productsPane);
            final JFXButton jfxButton = (JFXButton) event.getSource();
            highlight_button_clicked_in_menuPane(jfxButton);
            display_or_hide_payments_pane(productsPane);
        }
    }

    @FXML
    private void show_products_with_the_provided_details(ActionEvent event) {
        if (productSearchDetailsTF.getText().trim().isEmpty() || productSearchDetailsTF.getText() == null) {
            empty_and_null_pointer_message(productSearchDetailsTF.getParent()).show();
            return;
        }
        find_stock_item(productSearchDetailsTF.getText().trim());
    }

    @FXML
    private void show_services(ActionEvent event) {
        if (event != null) {
            switch_widows_in_displayPane(servicesPane);
            final JFXButton jfxButton = (JFXButton) event.getSource();
            highlight_button_clicked_in_menuPane(jfxButton);
            display_or_hide_payments_pane(servicesPane);
        }
    }

    @FXML
    private void reset_user_password(ActionEvent event) {
        if (event != null) {
            if (usernameTF.getText().trim().isEmpty() || usernameTF.getText() == null) {
                empty_and_null_pointer_message(usernameTF.getParent()).text("Ensure you type your valid username to continue!").show();
                return;
            }
            Account account = get_user_account(usernameTF.getText().trim(), null);
            if (account == null) {
                error_message("Sorry i don't know that username", "Try to remember your username to continue...").show();
            } else {
                final String GENERATED_PLAIN_PASSWORD = RandomStringUtils.randomAlphabetic(10);
                account.setPassword(GENERATED_PLAIN_PASSWORD);
                account = reset_password(account);
                if (account == null) {
                    error_message("Failed!", "Your password was not updated, please retry").show();
                } else {
                    success_notification("Your account password has been updated!").show();
                }
            }
        }
    }

    @FXML
    private void show_add_stock_page(ActionEvent event) {
        if (event != null) {
            switch_widows_in_displayPane(addStockPane);
            final JFXButton jfxButton = (JFXButton) event.getSource();
            highlight_button_clicked_in_menuPane(jfxButton);
            display_or_hide_payments_pane(addStockPane);
        }
    }

    @FXML
    private void show_stock_page(ActionEvent event) {
        if (event != null) {
            switch_widows_in_displayPane(viewStockPane);
            final JFXButton jfxButton = (JFXButton) event.getSource();
            highlight_button_clicked_in_menuPane(jfxButton);
            display_or_hide_payments_pane(viewStockPane);
        }
    }

    @FXML
    private void update_account_details(ActionEvent event) {
        if (event != null) {
            String newUsername = "";
            String firstName = "";
            String surname = "";
            String email = "";
            String newSiri;
            if (!accUsernameTF.getText().trim().equals(myAccount.getUsername())) {
                if (!accUsernameTF.getText().trim().isEmpty() || accUsernameTF.getText() != null) {
                    newUsername = accUsernameTF.getText().trim();
                }
            }
            if (!accFirstnameTF.getText().trim().equals(myAccount.getFname())) {
                if (!accFirstnameTF.getText().trim().isEmpty() || accFirstnameTF.getText() != null) {
                    firstName = accFirstnameTF.getText().trim();
                }
            }
            if (!accSurnameTF.getText().trim().equals(myAccount.getSurname())) {
                if (!accSurnameTF.getText().trim().isEmpty() || accSurnameTF.getText() != null) {
                    surname = accSurnameTF.getText().trim();
                }
            }
            if (!accEmailTF.getText().trim().equals(myAccount.getEmail())) {
                if (!accEmailTF.getText().trim().isEmpty() || accEmailTF.getText() != null) {
                    if (email_is_in_correct_format(accEmailTF.getText().trim())) {
                        email = accEmailTF.getText().trim();
                    } else {
                        error_message("Bad email!", "Kindly ensure that the email you have provided is in the correct formant").show();
                        return;
                    }
                }
            }
            String newPassword_typeA = "";
            if (hideShowSiriBtn1.getText().startsWith("Show")) {
                if (!siriPF1.getText().trim().isEmpty() || siriPF1.getText() != null) {
                    newPassword_typeA = siriPF1.getText().trim();
                }
            } else {
                if (!plainSiriTF1.getText().trim().isEmpty() || plainSiriTF1.getText() != null) {
                    newPassword_typeA = plainSiriTF1.getText().trim();
                }
            }
            String newPassword_typeB = "";
            if (hideShowSiriBtn2.getText().startsWith("Show")) {
                if (!siriPF2.getText().trim().isEmpty() || siriPF2.getText() != null) {
                    newPassword_typeB = siriPF2.getText().trim();
                }
            } else {
                if (!plainSiriTF2.getText().trim().isEmpty() || plainSiriTF2.getText() != null) {
                    newPassword_typeB = plainSiriTF2.getText().trim();
                }
            }
            if (newPassword_typeA.isEmpty() && !newPassword_typeB.isEmpty()) {
                empty_and_null_pointer_message(siriPF1.getParent().getParent()).show();
                return;
            } else {
                if (newPassword_typeB.isEmpty() && !newPassword_typeA.isEmpty()) {
                    empty_and_null_pointer_message(siriPF2.getParent().getParent()).show();
                    return;
                }
            }
            if (!newPassword_typeA.equals(newPassword_typeB)) {
                warning_message("Incomplete!", "The first and second password are NOT the same").show();
                return;
            }
            newSiri = newPassword_typeA;
            if (newUsername.isEmpty() && firstName.isEmpty() && surname.isEmpty() && email.isEmpty() && newSiri.isEmpty()) {
                warning_message("Wait!", "First edit any detail to continue...").show();
                return;
            }
            final Account updatedAccount = new Account();
            updatedAccount.setUsername(newUsername);
            updatedAccount.setFname(firstName);
            updatedAccount.setSurname(surname);
            updatedAccount.setEmail(email);
            updatedAccount.setPassword(newSiri);
            PasswordDialog passwordDialog = new PasswordDialog();
            Optional<String> password_copy = passwordDialog.showAndWait();
            password_copy.ifPresent(password -> {
                if (password_copy.get().isEmpty()) {
                    error_message("No password entered", "Please type a password to confirm the actions you request to be done.").show();
                } else {
                    if (BCrypt.checkpw(password_copy.get(), myAccount.getPassword())) {
                        boolean passwordOrUsernameHasBeenUpdated = false;
                        if (!updatedAccount.getUsername().isEmpty()) {
                            myAccount.setUsername(updatedAccount.getUsername());
                            myAccount = update_account(myAccount);
                            if (myAccount != null) {
                                accUsernameTF.setText("");
                                USER_MWENYE_AMELOGIN = myAccount.getUsername();
                                success_notification("Your new username has been saved").show();
                                passwordOrUsernameHasBeenUpdated = true;
                            } else {
                                myAccount = get_user_account(USER_MWENYE_AMELOGIN, null);
                                USER_MWENYE_AMELOGIN = myAccount.getUsername();
                                error_message("Failed!", "I was not able to save your new username").show();
                            }
                        }
                        if (!updatedAccount.getPassword().isEmpty()) {
                            final String newHashedSiri = BCrypt.hashpw(updatedAccount.getPassword(), BCrypt.gensalt(14));
                            myAccount.setPassword(newHashedSiri);
                            myAccount = update_account(myAccount);
                            if (myAccount != null) {
                                siriPF1.setText("");
                                plainSiriTF1.setText("");
                                siriPF2.setText("");
                                plainSiriTF2.setText("");
                                passwordOrUsernameHasBeenUpdated = true;
                                success_notification("Your new password has been saved").show();
                            } else {
                                myAccount = get_user_account(USER_MWENYE_AMELOGIN, null);
                                error_message("Failed!", "I was not able to save your new password").show();
                            }
                        }
                        if (!updatedAccount.getFname().isEmpty()) {
                            myAccount.setFname(updatedAccount.getFname());
                            myAccount = update_account(myAccount);
                            if (myAccount != null) {
                                accFirstnameTF.setText("");
                                success_notification("Your new firstname has been saved").show();
                            } else {
                                myAccount = get_user_account(USER_MWENYE_AMELOGIN, null);
                                error_message("Failed!", "I was not able to save your new firstname").show();
                            }
                        }
                        if (!updatedAccount.getSurname().isEmpty()) {
                            myAccount = update_account(myAccount);
                            if (myAccount != null) {
                                accSurnameTF.setText("");
                                success_notification("Your new surname has been saved").show();
                            } else {
                                myAccount = get_user_account(USER_MWENYE_AMELOGIN, null);
                                error_message("Failed!", "I was not able to save your new surname").show();
                            }
                        }
                        if (!updatedAccount.getEmail().isEmpty()) {
                            myAccount = update_account(myAccount);
                            if (myAccount != null) {
                                accEmailTF.setText("");
                                success_notification("Your new email has been saved").show();
                            } else {
                                myAccount = get_user_account(USER_MWENYE_AMELOGIN, null);
                                error_message("Failed!", "I was not able to save your new email").show();
                            }
                        }
                        display_account_details(myAccount);
                        String successMessage;
                        if (passwordOrUsernameHasBeenUpdated) {
                            log_out_from_my_account(new ActionEvent());
                            successMessage = "You are now required to Login again with your new details";
                        } else {
                            successMessage = "Your new account has been loaded";
                        }
                        success_notification(successMessage).title("Saved!").show();
                    } else {
                        error_message("Unknown password!", "Ensure you remember quite well your current password").show();
                    }
                }
            });
        }
    }

    @FXML
    private void upload_product(ActionEvent event) {
        if (prodSerialTF.getText().trim().isEmpty() || prodSerialTF.getText() == null) {
            empty_and_null_pointer_message(prodSerialTF).show();
            return;
        } else {
            if (productSerials.contains(prodSerialTF.getText().trim())) {
                prodSerialTF.setText("");
                error_message("Item found", "The serial number already exists, please enter a new one").show();
                return;
            }
        }
        if (prodRatingsCbx.getSelectionModel().getSelectedItem().trim().isEmpty() || prodRatingsCbx.getSelectionModel().getSelectedItem() == null) {
            empty_and_null_pointer_message(prodRatingsCbx).show();
            return;
        }
        if (prodQtyTF.getText().trim().isEmpty() || prodQtyTF.getText() == null) {
            empty_and_null_pointer_message(prodQtyTF).show();
            return;
        }
        if (prodNameTF.getText().trim().isEmpty() || prodNameTF.getText() == null) {
            empty_and_null_pointer_message(prodNameTF).show();
            return;
        }
        if (prodDescriptionTF.getText().trim().isEmpty() || prodDescriptionTF.getText() == null) {
            empty_and_null_pointer_message(prodDescriptionTF).show();
            return;
        }
        final Product product1 = new Product();
        product1.setSerial_number(prodSerialTF.getText().trim());
        product1.setStock(Integer.parseInt(prodQtyTF.getText().trim()));
        product1.setRating(Integer.parseInt(prodRatingsCbx.getSelectionModel().getSelectedItem()));
        String nameOfImage;
        if (PATH_TO_IMAGE_OF_NEW_PRODUCT == null) {
            nameOfImage = "";
            product1.setImage(null);
        } else {
            nameOfImage = new File(PATH_TO_IMAGE_OF_NEW_PRODUCT).getName();
            try {
                byte[] imageAsBytes = FileUtils.readFileToByteArray(new File(PATH_TO_IMAGE_OF_NEW_PRODUCT));
                String encodedImage = Base64.getEncoder().encodeToString(imageAsBytes);
                product1.setImage(encodedImage);
            } catch (IOException e) {
                product1.setImage(null);
                e.printStackTrace();
                programmer_error(e).show();
            }
        }
        if (nameOfImage.isEmpty()) {
            error_message("Image not found!", "The path to the image seems broken or is bad, but don't worry the default one has been set").show();
        }
        product1.setName(prodNameTF.getText().trim());
        product1.setDescription(prodDescriptionTF.getText().trim());
        product1.setMarkedPrice(Double.parseDouble(prodMarkedPriceTF.getText().trim()));
        product1.setBuyingPrice(Double.parseDouble(prodBuyingPriceTF.getText().trim()));
        if (insert_product_into_stock(product1)) {
            final List<Product> productList = get_product_list();
            update_all_product_and_mpesaDetails_suggestions_feature(productList);
            put_stock_items_into_table(generate_stock_model_from_product_model(productList));
            prodSerialTF.setText("");
            prodRatingsCbx.getSelectionModel().clearSelection();
            prodQtyTF.setText("");
            prodNameTF.setText("");
            prodDescriptionTF.setText("");
            prodView.setImage(new Image(getClass().getResourceAsStream(INPUT_STREAM_TO_NO_IMAGE)));
            success_notification("Product has been added to stock!").show();
        } else {
            error_message("Failed!", "Product was not added to stock!").show();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prepare_scene();
    }

    /**
     * UI driven updates
     *
     * <code> private void prepare_scene() {
     * start_loading_spinner();
     * view_image(new Image(getClass().getResourceAsStream(INPUT_STREAM_TO_NO_IMAGE)), productView);
     * display_or_hide_payments_pane(productsPane);
     * }
     * </code>
     */

    private void prepare_scene() {
        view_image_with_dropShadow_effect(new Image(getClass().getResourceAsStream(INPUT_STREAM_TO_NO_IMAGE)), productView);
        display_or_hide_payments_pane(productsPane);
    }

    /**
     * Functionality updates (code driven)
     * <code>updateAutoCompleteTextField(mpesaPhoneAndTransactionCodePopup, mpesaDetailTF);</code>
     */

    private void breath_of_life() {
        start_loading_spinner();
        Platform.runLater(() -> Main.stage.setOnCloseRequest(event -> {
            clear_cart(false);
            System.exit(0);
        }));
        productSerial.textProperty().addListener((observable, oldValue, newValue) -> {
            if (knownProductSerials.contains(newValue)) {
                product = get_details_of_a_product(newValue.trim());
                if (product != null) {
                    Image image;
                    if (product.getImage() != null) {
                        try {
                            image = new Image(new FileInputStream(product.getImage()));
                        } catch (FileNotFoundException e) {
                            image = new Image(getClass().getResourceAsStream(INPUT_STREAM_TO_NO_IMAGE));
                        }
                    } else {
                        image = new Image(getClass().getResourceAsStream(INPUT_STREAM_TO_NO_IMAGE));
                    }
                    view_image_with_dropShadow_effect(image, productView);
                    pNameLbl.setText(product.getName());
                    pDescriptionLbl.setText(product.getDescription());
                    pQualityView.setImage(get_image_of_ratings(product.getRating()));
                    pMarkedPriceLbl.setText("Ksh ".concat(String.format("%,.1f", product.getMarkedPrice())));
                    pQuantityLeftLbl.setText(format_quantity(product.getStock()));
                }
            }
        });
    }

    @Contract(value = " -> new", pure = true)
    private @NotNull Task<Object> update_cart_bill_amount_after_a_cart_item_is_removed() {
        return new Task<Object>() {
            @Override
            protected Object call() {
                while (true) {
                    try {
                        Thread.sleep(1);
                        if (anItemHasBeenRemovedFromCart) {
                            start_animate_bill_amount();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                return null;
            }
        };
    }

    @NotNull
    public Boolean email_is_in_correct_format(String param) {
        return Pattern.matches("^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$", param);
    }

    public Task<Object> printReceipt(String header, String footer) {
        return new Task<Object>() {
            @Override
            public Object call() {
                new PrintWork(header, footer);
                return false;
            }
        };
    }

    public void receipt_formatting_for_printing(int receipt_number, double amountPaid, double billAmount, int payment_method) {
        final Calendar calendar = new GregorianCalendar();
        final int time_of_day = calendar.get(Calendar.AM_PM);
        final String header = "\n;\n;"
                + "         BROWN TECH\n;"
                + "             Murang'a\n;".toUpperCase()
                + "       Mentor Sacco Building\n;".toUpperCase()
                + "            8th floor\n;".toUpperCase()
                + "    Call: 0724841648\n;\n;"
                + "           OFFICIAL BILL RECEIPT\n;"
                + " Receipt number: " + format_receiptNumber_to_something_understandable(String.format("%d", receipt_number)) + "\n;"
                + " Date: " + new SimpleDateFormat("dd-MMM-yyyy").format(Calendar.getInstance().getTime()) + "  Time: " + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + " " + is_it_am_pm(time_of_day) + "\n;\n;";
        final String footer = "\n;"
                + "   NOTE: Goods once sold can't be REFUNDED\n;"
                + " -----------------------------------------\n;"
                + " Total Cost: Ksh " + String.format("%,.1f", billAmount)
                + "\n;\n;"
                + " Paid Amount: Ksh " + String.format("%,.1f", amountPaid)
                + "\n;"
                + " Change: Ksh " + String.format("%,.1f", (amountPaid - billAmount))
                + "\n;"
                + " Payment method : " + decode_payment_method(payment_method).toUpperCase()
                + "\n;"
                + " ------------------------------------------\n;"
                + "   Thank You For Shopping From Us. \n;"
                + " ------------------------------------------\n;\n;";
        try {
            new Thread(printReceipt(header, footer)).start();
        } catch (Exception ex) {
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + ex, 1)).start();
            new Thread(stack_trace_printing(ex)).start();
            ex.printStackTrace();
        }
    }

    @NotNull
    @Contract(pure = true)
    private String is_it_am_pm(int x) {
        if (x == 0) {
            return "AM";
        }
        return "PM";
    }

    @NotNull
    @Contract(pure = true)
    private String decode_payment_method(int x) {
        if (x == 0) {
            return "cash";
        } else if (x == 1) {
            return "cheque";
        } else {
            return "mpesa";
        }
    }

    @NotNull
    private String format_receiptNumber_to_something_understandable(@NotNull String receipt_number) {
        if (receipt_number.length() == 1) {
            return "00" + receipt_number;
        } else if (receipt_number.length() == 2) {
            return "0" + receipt_number;
        } else {
            return receipt_number;
        }
    }

    protected void start_animate_bill_amount() {
        if (billAmountAnimation != null) {
            if (billAmountAnimation.isRunning()) {
                billAmountAnimation.cancel(false);
            }
        }
        billAmountAnimation = totalPrice_animation(get_cost_of_items_in_cart());
        new Thread(billAmountAnimation).start();
        billAmountAnimation = null;
        anItemHasBeenRemovedFromCart = false;
    }

    private void find_stock_item(String param) {
        final List<Product> productList = get_details_of_products_from_the_given_param(param);
        if (productList.isEmpty()) {
            error_message("Zero result found!", "I could not find any item with an attribute you have provided, try again with something different.").show();
            return;
        }
        put_stock_items_into_table(generate_stock_model_from_product_model(productList));
    }

    @NotNull
    @Contract(pure = true)
    private String decode_boolean_to_string(boolean status) {
        if (status) {
            return "We still sell";
        } else {
            return "We no longer sell";
        }
    }

    @NotNull
    private ObservableList<Stock> generate_stock_model_from_product_model(@NotNull List<Product> productList) {
        final ObservableList<Stock> stockObservableList = FXCollections.observableArrayList();
        if (!productList.isEmpty()) {
            for (Product product1 : productList) {
                stockObservableList.add(new Stock(product1.getSerial_number(), product1.getName(), product1.getDescription(), new ImageView(get_image_of_ratings_for_table(product1.getRating())), String.format("%d", product1.getStock()), "Ksh ".concat(String.format("%,.1f", product1.getMarkedPrice())), decode_boolean_to_string(product1.getAvailable())));
            }
        }
        return stockObservableList;
    }

    private void put_stock_items_into_table(ObservableList<Stock> stockObservableList) {
        JFXTreeTableView<Stock> jfxTreeTableView = stockTable;
        serialCol.setCellValueFactory(param -> param.getValue().getValue().serial);
        nameCol.setCellValueFactory(param -> param.getValue().getValue().name);
        descriptionCol.setCellValueFactory(param -> param.getValue().getValue().description);
        ratingsCol.setCellValueFactory(param -> param.getValue().getValue().imageview);
        quantityCol.setCellValueFactory(param -> param.getValue().getValue().quantity);
        markedPriceCol.setCellValueFactory(param -> param.getValue().getValue().markedPrice);
        statusCol.setCellValueFactory(param -> param.getValue().getValue().status);
        TreeItem<Stock> root = new RecursiveTreeItem<>(stockObservableList, RecursiveTreeObject::getChildren);
        jfxTreeTableView.setRoot(root);
        jfxTreeTableView.setShowRoot(false);
        if (!stockObservableList.isEmpty()) {
            jfxTreeTableView.refresh();
        }
    }

    private void update_list_of_product_serials() {
        final List<String> serials = get_list_of_product_serials();
        productSerials.addAll(serials);
    }

    private void update_all_product_and_mpesaDetails_suggestions_feature(List<Product> productList) {
        update_list_of_product_serials();
        if (!productSerials.isEmpty()) {
            update_autoComplete_suggestions(productSerialsPopup, productSerials);
            update_autoComplete_suggestions(addProductSerialsPopup, productSerials);
            if (!knownProductSerials.isEmpty()) {
                knownProductSerials.clear();
            }
            knownProductSerials.addAll(productSerials);
        }
        final ObservableList<String> stringObservableList = FXCollections.observableArrayList();
        for (Product product1 : productList) {
            stringObservableList.addAll(product1.getSerial_number(), product1.getName(), product1.getDescription());
        }
        update_autoComplete_suggestions(listOfProductDetailPopup, stringObservableList);
    }

    private void show_or_hide_any_password(@NotNull final JFXButton jfxButton, final JFXTextField jfxTextField, final JFXPasswordField jfxPasswordField) {
        if (jfxButton.getText().startsWith("Show")) {
            if (!jfxPasswordField.getText().trim().isEmpty() || jfxPasswordField.getText() != null) {
                jfxTextField.setText(jfxPasswordField.getText().trim());
            }
            new FadeOut(jfxPasswordField).play();
            jfxTextField.toFront();
            new FadeIn(jfxTextField).setDelay(Duration.seconds(0.5)).play();
            jfxButton.setText("Hide password");
        } else {
            if (!jfxTextField.getText().trim().isEmpty() || jfxTextField.getText() != null) {
                jfxPasswordField.setText(jfxTextField.getText().trim());
            }
            new FadeOut(jfxTextField).play();
            jfxPasswordField.toFront();
            new FadeIn(jfxPasswordField).setDelay(Duration.seconds(0.5)).play();
            jfxButton.setText("Show password");
        }
    }

    private void display_account_details(@NotNull Account userAccount) {
        accInfoLbl.setText("Hello " + userAccount.getFname() + ", you can edit your details below.");
        accUsernameTF.setText(userAccount.getUsername());
        accFirstnameTF.setText(userAccount.getFname());
        accSurnameTF.setText(userAccount.getSurname());
        accEmailTF.setText(userAccount.getEmail());
    }

    private void update_autoComplete_suggestions(@NotNull JFXAutoCompletePopup<String> jfxAutoCompletePopup, ObservableList<String> observableList) {
        if (jfxAutoCompletePopup.getSuggestions().size() > 0) {
            jfxAutoCompletePopup.getSuggestions().clear();
        }
        jfxAutoCompletePopup.getSuggestions().addAll(observableList);
    }

    public void assign_text_fields_suggestions_for_product_and_mpesaDetails_their_own_popups() {
        final JFXTextField[] jfxTextFields = new JFXTextField[]{productSerial, mpesaDetailTF, prodSerialTF, productSearchDetailsTF};
        for (JFXTextField jfxTextField : jfxTextFields) {
            if (jfxTextField.equals(productSerial)) {
                productSerialsPopup.setMinSize(400, 400);
                productSerialsPopup.setSelectionHandler(event -> {
                    jfxTextField.setText(event.getObject());
                    search_for_product(new ActionEvent());
                });
                jfxTextField.textProperty().addListener(observable -> {
                    productSerialsPopup.filter(string -> string.toLowerCase().contains(jfxTextField.getText().toLowerCase()));
                    if (productSerialsPopup.getFilteredSuggestions().isEmpty() || jfxTextField.getText().isEmpty()) {
                        Platform.runLater(productSerialsPopup::hide);
                    } else {
                        Platform.runLater(() -> productSerialsPopup.show(jfxTextField));
                    }
                });
            } else if (jfxTextField.equals(mpesaDetailTF)) {
                mpesaPhoneAndTransactionCodePopup.setMinSize(477, 500);
                mpesaPhoneAndTransactionCodePopup.setSelectionHandler(event -> jfxTextField.setText(event.getObject()));
                jfxTextField.textProperty().addListener(observable -> {
                    mpesaPhoneAndTransactionCodePopup.filter(string -> string.toLowerCase().contains(jfxTextField.getText().toLowerCase()));
                    if (mpesaPhoneAndTransactionCodePopup.getFilteredSuggestions().isEmpty() || jfxTextField.getText().isEmpty()) {
                        Platform.runLater(mpesaPhoneAndTransactionCodePopup::hide);
                    } else {
                        Platform.runLater(() -> mpesaPhoneAndTransactionCodePopup.show(jfxTextField));
                    }
                });
            } else if (jfxTextField.equals(prodSerialTF)) {
                addProductSerialsPopup.setMinSize(400, 400);
                addProductSerialsPopup.setSelectionHandler(event -> {
                    jfxTextField.setText(event.getObject());
                    check_if_the_serial_exists(new ActionEvent());
                });
                jfxTextField.textProperty().addListener(observable -> {
                    addProductSerialsPopup.filter(string -> string.toLowerCase().contains(jfxTextField.getText().toLowerCase()));
                    if (addProductSerialsPopup.getFilteredSuggestions().isEmpty() || jfxTextField.getText().isEmpty()) {
                        Platform.runLater(addProductSerialsPopup::hide);
                    } else {
                        Platform.runLater(() -> addProductSerialsPopup.show(jfxTextField));
                    }
                });
            } else {
                listOfProductDetailPopup.setMinSize(477, 500);
                listOfProductDetailPopup.setSelectionHandler(event -> {
                    jfxTextField.setText(event.getObject());
                    show_products_with_the_provided_details(new ActionEvent());
                });
                jfxTextField.textProperty().addListener(observable -> {
                    listOfProductDetailPopup.filter(string -> string.toLowerCase().contains(jfxTextField.getText().toLowerCase()));
                    if (listOfProductDetailPopup.getFilteredSuggestions().isEmpty() || jfxTextField.getText().isEmpty()) {
                        Platform.runLater(listOfProductDetailPopup::hide);
                    } else {
                        Platform.runLater(() -> listOfProductDetailPopup.show(jfxTextField));
                    }
                });
            }
        }
    }

    private void display_list_of_services() {
        final ObservableList<OnlineService> onlineServices = get_list_of_services();
        if (!onlineServices.isEmpty()) {
            final List<String> stringList = new ArrayList<>();
            for (OnlineService onlineService : onlineServices) {
                final String service = onlineService.getName();
                stringList.add(service);
            }
            servicesCbx.getItems().clear();
            servicesCbx.getItems().addAll(stringList);
        }
    }

    private void format_datePickers_to_show_my_preferred_date_style(@NotNull JFXDatePicker jfxDatePicker) {
        final StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            @NotNull
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateTimeFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateTimeFormatter);
                } else {
                    return null;
                }
            }
        };
        jfxDatePicker.setConverter(converter);
    }

    private void highlight_selected_payment_option(@NotNull JFXButton jfxButton) {
        final HBox hBox = (HBox) jfxButton.getParent();
        final ObservableList<Node> nodeObservableList = hBox.getChildren();
        for (Node node : nodeObservableList) {
            if (node.getClass().equals(JFXButton.class)) {
                if (node.equals(jfxButton)) {
                    node.setStyle("-fx-background-radius: 0px;" +
                            "-fx-text-fill: rgb(102, 47, 1);");
                    /*final AnchorPane [] underlines = new AnchorPane[] {cashUnderline, chequeUnderline, mpesaUnderline};
                    for (AnchorPane underline : underlines) {

                    }*/
                    if (jfxButton.getText().equalsIgnoreCase("cash")) {
                        cashUnderline.setStyle("-fx-background-color: linear-gradient(#FFB900, #F0D801);");
                        chequeUnderline.setStyle("-fx-background-color:  rgba(226, 123, 0, 0.0);");
                        mpesaUnderline.setStyle("-fx-background-color:  rgba(226, 123, 0, 0.0);");
                    } else if (jfxButton.getText().equalsIgnoreCase("cheque")) {
                        cashUnderline.setStyle("-fx-background-color: rgba(226, 123, 0, 0.0);");
                        chequeUnderline.setStyle("-fx-background-color: linear-gradient(#FFB900, #F0D801);");
                        mpesaUnderline.setStyle("-fx-background-color:  rgba(226, 123, 0, 0.0);");
                    } else {
                        cashUnderline.setStyle("-fx-background-color: rgba(226, 123, 0, 0.0);");
                        chequeUnderline.setStyle("-fx-background-color: rgba(226, 123, 0, 0.0);");
                        mpesaUnderline.setStyle("-fx-background-color: linear-gradient(#FFB900, #F0D801);");
                    }
                } else {
                    node.setStyle("-fx-background-radius: 0px;" +
                            "-fx-background-color: transparent;" +
                            "-fx-text-fill: rgba(102, 47, 1, 0.4);");
                }
            }
        }
    }

    private void view_payment_options(@NotNull AnchorPane hostAnchorPane) {
        if (hostAnchorPane.getOpacity() < 1) {
            final AnchorPane[] anchorPanes = new AnchorPane[]{cashPane, chequePane, mpesaPane};
            for (AnchorPane anchorPane : anchorPanes) {
                if (anchorPane.equals(hostAnchorPane)) {
                    anchorPane.toFront();
                    new FadeInUp(anchorPane).play();
                } else {
                    new FadeOutDown(anchorPane).play();
                }
            }
        }
    }

    private Double get_cost_of_items_in_cart() {
        final double[] totalCost = {0};
        if (cartItems == null) {
            return .0;
        } else {
            cartItems.forEach((id, object) -> totalCost[0] += object.getPrice());
        }
        return totalCost[0];
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    protected Task<Object> totalPrice_animation(double targetAmount) {
        final double[] pettyAmount = {1};
        return new Task<Object>() {
            @Override
            protected Object call() {
                if (totalBillLbl.getText().equals(String.format("%,.1f", targetAmount))) {
                    Platform.runLater(() -> totalBillLbl.setText(String.format("%,.1f", targetAmount)));
                    return null;
                }
                if (targetAmount != 0) {
                    try {
                        while (pettyAmount[0] > 0) {
                            Thread.sleep(1);
                            Platform.runLater(() -> totalBillLbl.setText(String.format("%,.1f", pettyAmount[0])));
                            pettyAmount[0] += (0.005125 * pettyAmount[0]);
                            if (pettyAmount[0] >= targetAmount) {
                                Platform.runLater(() -> totalBillLbl.setText(String.format("%,.1f", targetAmount)));
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Platform.runLater(() -> totalBillLbl.setText(String.format("%,.1f", targetAmount)));
                }
                new Flash(totalBillLbl).play();
                return null;
            }
        };
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    private Task<Object> display_cart_items() {
        return new Task<Object>() {
            @Override
            protected Object call() {
                ObservableList<Node> nodeObservableList = cartBox.getChildren();
                for (Node node : nodeObservableList) {
                    Platform.runLater(() -> {
                        VBox.clearConstraints(node);
                        cartBox.getChildren().remove(node);
                    });
                }
                final Set<Integer> integerSet = cartItems.keySet();
                for (Integer rowId : integerSet) {
                    try {
                        CartItemUI.row_id = rowId;
                        final Node node = FXMLLoader.load(getClass().getResource("/org/_brown_tech/_fxml/cartItemUI.fxml"));
                        Platform.runLater(() -> {
                            cartBox.getChildren().add(node);
                            new SlideInRight(node).play();
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        programmer_error(e).show();
                        new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
                        new Thread(stack_trace_printing(e)).start();
                        Platform.runLater(() -> error_message_alert("IOException: ", e.getLocalizedMessage()).show());
                    }
                }
                return null;
            }
        };
    }

    private void display_or_hide_payments_pane(@NotNull StackPane host) {
        if (host.equals(myaccountPane) || host.equals(viewStockPane) || host.equals(addStockPane)) {
            if (paymentsPane.getOpacity() > 0) {
                paymentsPane.toBack();
                new FadeOutRight(paymentsPane).play();
            }
        } else {
            paymentsPane.toFront();
            if (paymentsPane.getOpacity() < 1) {
                new FadeInRight(paymentsPane).play();
            }
        }
    }

    private void reset_product_search_info() {
        view_image_with_dropShadow_effect(new Image(getClass().getResourceAsStream(INPUT_STREAM_TO_NO_IMAGE)), productView);
        pNameLbl.setText("");
        pDescriptionLbl.setText("");
        pQualityView.setImage(null);
        pMarkedPriceLbl.setText("");
        pQuantityLeftLbl.setText("");
    }

    private void make_a_textField_numeric(@NotNull JFXTextField[] jfxTextFields) {
        for (JFXTextField jfxTextField : jfxTextFields) {
            TextFormatter<Integer> textFormatter = new TextFormatter<>(converter, 0, integerFilter);
            jfxTextField.setTextFormatter(textFormatter);
        }
    }

    @NotNull
    @Contract("_ -> new")
    private Image get_image_of_ratings_for_table(int count) {
        switch (count) {
            case 1:
                return new Image("/org/_brown_tech/_images/_icons/table_1_star.png");
            case 2:
                return new Image("/org/_brown_tech/_images/_icons/table_2_star.png");
            case 3:
                return new Image("/org/_brown_tech/_images/_icons/table_3_star.png");
            case 5:
                return new Image("/org/_brown_tech/_images/_icons/table_5_star.png");
            case 4:
            default:
                return new Image("/org/_brown_tech/_images/_icons/table_4_star.png");
        }
    }

    @NotNull
    @Contract("_ -> new")
    private Image get_image_of_ratings(int count) {
        switch (count) {
            case 1:
                return new Image("/org/_brown_tech/_images/_icons/1_star.png");
            case 2:
                return new Image("/org/_brown_tech/_images/_icons/2_star.png");
            case 3:
                return new Image("/org/_brown_tech/_images/_icons/3_star.png");
            case 5:
                return new Image("/org/_brown_tech/_images/_icons/5_star.png");
            case 4:
            default:
                return new Image("/org/_brown_tech/_images/_icons/4_star.png");
        }
    }

    private void view_image_with_dropShadow_effect(Image image, @NotNull ImageView imageView) {
        imageView.setImage(image);
        final Rectangle clip = new Rectangle(imageView.getFitWidth(), imageView.getFitHeight());
        clip.setArcWidth(5);
        clip.setArcHeight(5);
        imageView.setClip(clip);
        final SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        final WritableImage writableImage = imageView.snapshot(parameters, null);
        imageView.setClip(null);
        imageView.setEffect(new DropShadow(10.0, Color.rgb(0, 0, 0, 0.22)));
        imageView.setImage(writableImage);
    }

    @NotNull
    @Contract(pure = true)
    private String format_quantity(int quantity) {
        switch (quantity) {
            case 3:
            case 2:
                return quantity + " only in stock";
            case 1:
                return "One left in stock";
            default:
                return quantity + " in stock";
        }
    }

    private void highlight_button_clicked_in_menuPane(@NotNull JFXButton jfxButton) {
        final VBox vbox = (VBox) jfxButton.getParent();
        final ObservableList<Node> nodeObservableList = vbox.getChildren();
        for (Node node : nodeObservableList) {
            if (node.getClass().equals(JFXButton.class)) {
                if (node.equals(jfxButton)) {
                    node.setStyle("-fx-background-radius: 0px;" +
                            "-fx-background-color: linear-gradient(#FFB900, #F0D801);" +
                            "-fx-text-fill: #8e4b02;");
                } else {
                    node.setStyle("-fx-background-radius: 0px;" +
                            "-fx-background-color: transparent;" +
                            "-fx-text-fill: #868686;");
                }
            }
        }
    }

    private void switch_widows_in_displayPane(@NotNull StackPane host) {
        if (host.getOpacity() != 1) {
            final StackPane[] stackPanes = new StackPane[]{productsPane, servicesPane, viewStockPane, addStockPane, myaccountPane};
            for (StackPane stackPane : stackPanes) {
                if (stackPane.getOpacity() > 0) {
                    new FadeOut(stackPane).play();
                } else {
                    if (stackPane.equals(host)) {
                        if (stackPane.getOpacity() < 1) {
                            stackPane.toFront();
                            new FadeIn(stackPane).play();
                        }
                    }
                }
            }
        }
    }

    private void start_loading_spinner() {
        try {
            loadingBar.setProgress(0);
            loadingBar.progressProperty().unbind();
            final Task<Object> objectTask = loading_progress();
            loadingBar.progressProperty().bind(objectTask.progressProperty());
            objectTask.setOnSucceeded(event -> {
                loadingBar.progressProperty().unbind();
                new FadeOut(phaseOne).play();
                phaseTwo.toFront();
                new FadeIn(phaseTwo).play();
                new FadeIn(displayPane).play();
                new FadeInLeft(menuPane).play();
            });
            objectTask.setOnFailed(event -> {
                loadingBar.progressProperty().unbind();
                error_message("Awww!", "Something went wrong while loading application.").show();
            });
            objectTask.exceptionProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Exception e = (Exception) newValue;
                    e.printStackTrace();
                    programmer_error(e).show();
                    new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
                    new Thread(stack_trace_printing(e)).start();
                }
            }));
            new Thread(objectTask).start();
        } catch (Exception e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
        }
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    private Task<Object> loading_progress() {
        final String[] appInfo = new String[]{"Please wait as i freshen up :)",
                "Cart has been emptied, cart will update on removal of a cart item and strict measures have been put in place where only numbers are required.",
                "Services, bank and ratings list have been loaded.",
                "Dates have been set to my preference.",
                "Autosuggestion feature has been created, assigned and updated",
                "You are good to go!"};
        return new Task<Object>() {
            @Override
            protected Object call() {
                int steps = 0;
                while (steps <= 100) {
                    try {
                        updateProgress(steps, 100);
                        switch (steps) {
                            case 0:
                                Platform.runLater(() -> {
                                    display_account_details(myAccount);
                                    threadUpdate.setText(appInfo[0]);
                                });
                                break;
                            case 20:
                                //clear_cart(false);
                                //new Thread(update_cart_bill_amount_after_a_cart_item_is_removed()).start();
                                make_a_textField_numeric(new JFXTextField[]{sellingPriceTF, serviceCostTF, cashTF, chequeTF, mpesaTF, chequeNumTF, prodMarkedPriceTF, prodBuyingPriceTF, prodQtyTF});
                                Platform.runLater(() -> threadUpdate.setText(appInfo[1]));
                                break;
                            case 40:
                                display_list_of_services();
                                servicesCbx.setOnAction(event -> {
                                    final OnlineService onlineService = get_selected_service(servicesCbx.getSelectionModel().getSelectedItem());
                                    if (onlineService != null) {
                                        serviceCostTF.setText(String.format("%,.0f", onlineService.getDefaultCost()));
                                    }
                                });
                                banksCbx.getItems().clear();
                                banksCbx.getItems().addAll(get_data_from_properties_file_based_on_param("banks"));
                                prodRatingsCbx.getItems().clear();
                                prodRatingsCbx.getItems().addAll("1", "2", "3", "4", "5");
                                Platform.runLater(() -> threadUpdate.setText(appInfo[2]));
                                break;
                            case 60:
                                format_datePickers_to_show_my_preferred_date_style(maturityDateTF);
                                Platform.runLater(() -> threadUpdate.setText(appInfo[3]));
                                break;
                            case 80:
                                assign_text_fields_suggestions_for_product_and_mpesaDetails_their_own_popups();
                                final List<Product> productList = get_product_list();
                                Platform.runLater(() -> {
                                    update_all_product_and_mpesaDetails_suggestions_feature(productList);
                                    put_stock_items_into_table(generate_stock_model_from_product_model(productList));
                                    threadUpdate.setText(appInfo[4]);
                                });
                                break;
                            case 90:
                                Platform.runLater(() -> threadUpdate.setText(appInfo[5]));
                        }
                        Thread.sleep(100);
                        steps++;
                    } catch (InterruptedException e) {
                        new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
                        new Thread(stack_trace_printing(e)).start();
                        Platform.runLater(() -> error_message_alert("IOException", e.getLocalizedMessage()).show());
                        break;
                    }
                }
                return null;
            }
        };
    }

}

