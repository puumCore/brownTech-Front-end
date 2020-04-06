package org._brown_tech._custom;

import org._brown_tech.Main;
import org._brown_tech._controller.MainUI;
import org._brown_tech._object.Account;
import org._brown_tech._object.CartItem;
import org._brown_tech._object.OnlineService;
import org._brown_tech._object.Product;
import org._brown_tech._outsourced.BCrypt;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteDataSource;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Mandela
 */
public class Brain {

    private final SQLiteDataSource ds = new SQLiteDataSource();
    public final String INPUT_STREAM_TO_NO_IMAGE = "/org/_brown_tech/_images/_icons/noimage.png";
    private final String PATH_TO_SERVICES_FILE = Main.RESOURCE_PATH.getAbsolutePath().concat("\\_config\\services.json");

    public Connection connect_to_memory() {
        try {
            ds.setUrl("jdbc:sqlite:".concat(Main.RESOURCE_PATH.getAbsolutePath().concat("\\_dataSource\\bt_dataStore.db3")));
            return ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            new Issues().programmer_error(e).show();
            return null;
        }
    }

    public Boolean update_password(String username, String hashedPassword) {
        boolean allIsGood = false;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE _user\n" +
                    "   SET password = ?\n" +
                    " WHERE username = '" + username + "'");
            preparedStatement.setString(1, hashedPassword);
            final int executionStatus = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            if (executionStatus > 0) {
                allIsGood = true;
            }
        } catch (SQLException e) {
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            e.printStackTrace();
            new Issues().programmer_error(e).show();
        }
        return allIsGood;
    }

    public List<String> get_receipt_items_from_cart(@NotNull final HashMap<Integer, CartItem> integerCartItemHashMap) {
        final List<String> stringList = new ArrayList<>();
        if (!integerCartItemHashMap.isEmpty()) {
            final Set<Integer> integerSet = integerCartItemHashMap.keySet();
            for (Integer key : integerSet) {
               final CartItem cartItem = integerCartItemHashMap.get(key);
               stringList.add(cartItem.getProductOrServiceSerial());
               stringList.add(cartItem.getName());
               stringList.add(String.format("%d", cartItem.getQuantityRequested()));
               stringList.add(String.format("%.1f", cartItem.getPrice()));
            }
        }
        return stringList;
    }

    public void clear_cart(boolean its_time_for_final_cleanup) {
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM _cart");
            preparedStatement.executeUpdate();
            preparedStatement = connection.prepareStatement("DELETE FROM sqlite_sequence\n" +
                    "      WHERE name = '_cart'");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            final HashMap<Integer, CartItem> integerCartItemHashMap = get_all_cart_items();
            if (!integerCartItemHashMap.isEmpty()) {
                if (its_time_for_final_cleanup) {
                    Platform.runLater(() -> new Issues().information_message("Cart was successfully cleaned. We are ready for the next Customer").show());
                }
            }
            MainUI.cartItems = integerCartItemHashMap;
        } catch (SQLException e) {
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            e.printStackTrace();
            new Issues().programmer_error(e).show();
        }
    }

    public Boolean insert_product_into_stock(@NotNull final Product product, final String nameOfImage) {
        boolean allIsGood = false;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO _product (\n" +
                    "                         imageName,\n" +
                    "                         buyPrice,\n" +
                    "                         markedPrice,\n" +
                    "                         quantity,\n" +
                    "                         ratings,\n" +
                    "                         description,\n" +
                    "                         name,\n" +
                    "                         serial\n" +
                    "                     )\n" +
                    "                     VALUES (\n" +
                    "                         ?,\n" +
                    "                         ?,\n" +
                    "                         ?,\n" +
                    "                         ?,\n" +
                    "                         ?,\n" +
                    "                         ?,\n" +
                    "                         ?,\n" +
                    "                         ?\n" +
                    "                     )");
            preparedStatement.setString(1, nameOfImage);
            preparedStatement.setDouble(2, product.getBuyingPrice());
            preparedStatement.setDouble(3, product.getMarkedPrice());
            preparedStatement.setInt(4, product.getStockQuantity());
            preparedStatement.setInt(5, product.getStarCount());
            preparedStatement.setString(6, product.getDescription());
            preparedStatement.setString(7, product.getName());
            preparedStatement.setString(8, product.getSerial());
            final int executionStatus = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            if (executionStatus > 0) {
                allIsGood = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return allIsGood;
    }

    @NotNull
    public final List<Product> get_details_of_products_from_the_given_param(final String param) {
        final List<Product> productList = new ArrayList<>();
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT serial,\n" +
                    "       name,\n" +
                    "       description,\n" +
                    "       ratings,\n" +
                    "       quantity,\n" +
                    "       markedPrice,\n" +
                    "       isAvailable\n" +
                    "  FROM _product\n" +
                    " WHERE description = '" + param + "' OR \n" +
                    "       serial = '" + param + "' OR \n" +
                    "       name = '" + param + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.isBeforeFirst()) {
                while (resultSet.next()) {
                    final Product product = new Product();
                    product.setSerial(resultSet.getString(1));
                    product.setName(resultSet.getString(2));
                    product.setDescription(resultSet.getString(3));
                    product.setStarCount(resultSet.getInt(4));
                    product.setStockQuantity(resultSet.getInt(5));
                    product.setMarkedPrice(resultSet.getDouble(6));
                    product.setAvailable(resultSet.getBoolean(7));
                    productList.add(product);
                }
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return productList;
    }

    public final Boolean update_account_siri(final String username, final String siri) {
        boolean allIsGood = false;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE _user\n" +
                    "   SET password = ?\n" +
                    " WHERE isActive = true AND \n" +
                    "       username = '" + username + "'");
            preparedStatement.setString(1, siri);
            final int executionStatus = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            if (executionStatus > 0) {
                allIsGood = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return allIsGood;
    }

    public final Boolean update_account_email(final String username, final String email) {
        boolean allIsGood = false;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE _user\n" +
                    "   SET email = ?\n" +
                    " WHERE isActive = true AND \n" +
                    "       username = '" + username + "'");
            preparedStatement.setString(1, email);
            final int executionStatus = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            if (executionStatus > 0) {
                allIsGood = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return allIsGood;
    }

    public final Boolean update_account_surname(final String username, final String surname) {
        boolean allIsGood = false;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE _user\n" +
                    "   SET surname = ?\n" +
                    " WHERE isActive = true AND \n" +
                    "       username = '" + username + "'");
            preparedStatement.setString(1, surname);
            final int executionStatus = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            if (executionStatus > 0) {
                allIsGood = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return allIsGood;
    }

    public final Boolean update_account_firstname(final String username, final String firstname) {
        boolean allIsGood = false;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE _user\n" +
                    "   SET firstname = ?\n" +
                    " WHERE isActive = true AND \n" +
                    "       username = '" + username + "'");
            preparedStatement.setString(1, firstname);
            final int executionStatus = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            if (executionStatus > 0) {
                allIsGood = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return allIsGood;
    }

    public final Boolean update_account_username(final String oldOne, final String newOne) {
        boolean allIsGood = false;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE _user\n" +
                    "   SET username = ?\n" +
                    " WHERE isActive = true AND \n" +
                    "       username = '" + oldOne + "'");
            preparedStatement.setString(1, newOne);
            final int executionStatus = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            if (executionStatus > 0) {
                allIsGood = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return allIsGood;
    }

    public final Account get_details_of_a_user(final String userName) {
        Account account = null;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT username,\n" +
                    "       firstname,\n" +
                    "       surname,\n" +
                    "       password,\n" +
                    "       email,\n" +
                    "       isAdmin\n" +
                    "  FROM _user\n" +
                    " WHERE username = '" + userName + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                account = new Account(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5), resultSet.getBoolean(6));
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return account;
    }

    @NotNull
    public final ObservableList<String> get_list_of_product_serials() {
        final ObservableList<String> observableList = FXCollections.observableArrayList();
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT serial\n" +
                    "  FROM _product\n" +
                    " WHERE isAvailable = true");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.isBeforeFirst()) {
                while (resultSet.next()) {
                    observableList.add(resultSet.getString(1));
                }
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return observableList;
    }

    @NotNull
    public final ObservableList<String> get_data_from_properties_file_based_on_param(String param) {
        final ObservableList<String> results = FXCollections.observableArrayList();
        if ("banks".equals(param)) {
            try {
                FileReader fileReader = new FileReader(new File(Main.RESOURCE_PATH.getAbsolutePath().concat("\\_config\\banks.properties")));
                Properties properties = new Properties();
                properties.load(fileReader);
                int noOfBanks = Integer.parseInt(properties.getProperty("bankCount"));
                for (int index = 0; index < noOfBanks; ++index) {
                    results.add(properties.getProperty("bank" + index));
                }
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
                new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
                new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
                new Issues().programmer_error(e).show();
            }
        } else {
            try {
                FileReader fileReader = new FileReader(new File(Main.RESOURCE_PATH.getAbsolutePath().concat("\\_config\\months.properties")));
                Properties properties = new Properties();
                properties.load(fileReader);
                int count = Integer.parseInt(properties.getProperty("monthCount"));
                for (int index = 0; index < count; ++index) {
                    results.add(properties.getProperty("month" + index));
                }
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
                new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
                new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
                new Issues().programmer_error(e).show();
            }
        }
        return results;
    }

    public final Boolean update_products() {
        boolean allIsGood = false;
        final HashMap<Integer, CartItem> cartItemHashMap = get_all_cart_items();
        final Set<Integer> integerSet = cartItemHashMap.keySet();
        for (Integer key : integerSet) {
            final CartItem cartItem = cartItemHashMap.get(key);
            if (cartItem.getProduct()) {
                final Product product = get_details_of_a_product(cartItem.getProductOrServiceSerial());
                product.setStockQuantity((product.getStockQuantity() - cartItem.getQuantityRequested()));
                try {
                    Connection connection = connect_to_memory();
                    PreparedStatement preparedStatement = connection.prepareStatement("UPDATE _product\n" +
                            "   SET quantity = ?\n" +
                            " WHERE serial = '" + product.getSerial() + "'");
                    preparedStatement.setInt(1, product.getStockQuantity());
                    final int executionStatus = preparedStatement.executeUpdate();
                    preparedStatement.close();
                    connection.close();
                    if (executionStatus > 0) {
                        allIsGood = true;
                    } else {
                        allIsGood = false;
                        break;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
                    new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
                    new Issues().programmer_error(e).show();
                    allIsGood = false;
                    break;
                }
            } else {
                allIsGood = true;
            }
        }
        return allIsGood;
    }

    public final Integer get_latest_receipt_number() {
        int count = 0;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(receiptNum) \n" +
                    "  FROM _sale");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return count;
    }

    public final Boolean write_mpesa_to_transaction_record(final int LATEST_RECEIPT_NUMBER, final String transactionCode, final String customerName, final String phone, final double bill) {
        boolean allIsGood = false;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO _mpesa (\n" +
                    "                       amount,\n" +
                    "                       phone,\n" +
                    "                       customerName,\n" +
                    "                       transactionId,\n" +
                    "                       receiptNum,\n" +
                    "                       dateTime\n" +
                    "                   )\n" +
                    "                   VALUES (\n" +
                    "                       ?,\n" +
                    "                       ?,\n" +
                    "                       ?,\n" +
                    "                       ?,\n" +
                    "                       ?,\n" +
                    "                       ?\n" +
                    "                   )");
            preparedStatement.setDouble(1, bill);
            preparedStatement.setString(2, phone);
            preparedStatement.setString(3, customerName);
            preparedStatement.setString(4, transactionCode);
            preparedStatement.setInt(5, LATEST_RECEIPT_NUMBER);
            preparedStatement.setString(6, get_date_and_time());
            final int executionStatus = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            if (executionStatus > 0) {
                allIsGood = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return allIsGood;
    }

    public final Boolean write_to_cheque_record(final int LATEST_RECEIPT_NUMBER, final int chequeNum, String drawerName, String maturityDate, final double bill) {
        boolean allIsGood = false;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO _cheque (\n" +
                    "                        amount,\n" +
                    "                        maturityDate,\n" +
                    "                        drawerName,\n" +
                    "                        chequeNo,\n" +
                    "                        receiptNum,\n" +
                    "                        dateTime\n" +
                    "                    )\n" +
                    "                    VALUES (\n" +
                    "                        ?,\n" +
                    "                        ?,\n" +
                    "                        ?,\n" +
                    "                        ?,\n" +
                    "                        ?,\n" +
                    "                        ?\n" +
                    "                    )");
            preparedStatement.setDouble(1, bill);
            preparedStatement.setString(2, maturityDate);
            preparedStatement.setString(3, drawerName);
            preparedStatement.setInt(4, chequeNum);
            preparedStatement.setInt(5, LATEST_RECEIPT_NUMBER);
            preparedStatement.setString(6, get_date_and_time());
            final int executionStatus = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            if (executionStatus > 0) {
                allIsGood = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return allIsGood;
    }

    public final Boolean write_to_cash_record(final int LATEST_RECEIPT_NUMBER, final double bill) {
        boolean allIsGood = false;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO _cash (\n" +
                    "                      amount,\n" +
                    "                      receiptNum,\n" +
                    "                      dateTime\n" +
                    "                  )\n" +
                    "                  VALUES (\n" +
                    "                      ?,\n" +
                    "                      ?,\n" +
                    "                      ?\n" +
                    "                  )");
            preparedStatement.setDouble(1, bill);
            preparedStatement.setInt(2, LATEST_RECEIPT_NUMBER);
            preparedStatement.setString(3, get_date_and_time());
            final int executionStatus = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            if (executionStatus > 0) {
                allIsGood = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return allIsGood;
    }

    public final Boolean write_sale_record(final double bill, final int payMethod, final String loggedInUsername) {
        boolean allIsGood = false;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO _sale (\n" +
                    "                      username,\n" +
                    "                      paymentMethod,\n" +
                    "                      billAmount,\n" +
                    "                      dateTime\n" +
                    "                  )\n" +
                    "                  VALUES (\n" +
                    "                      ?,\n" +
                    "                      ?,\n" +
                    "                      ?,\n" +
                    "                      ?\n" +
                    "                  )");
            preparedStatement.setString(1, loggedInUsername);
            preparedStatement.setInt(2, payMethod);
            preparedStatement.setDouble(3, bill);
            preparedStatement.setString(4, get_date_and_time());
            final int executionStatus = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            if (executionStatus > 0) {
                allIsGood = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return allIsGood;
    }

    public final Boolean put_cart_items_to_receipt_records(final int LATEST_RECEIPT_NUMBER) {
        boolean allIsGood = false;
        final HashMap<Integer, CartItem> cartItemHashMap = get_all_cart_items();
        final Set<Integer> integerSet = cartItemHashMap.keySet();
        for (Integer rowId : integerSet) {
            final CartItem cartItem = cartItemHashMap.get(rowId);
            double buyingPrice = 0;
            if (cartItem.getProduct()) {
                final Product product = get_details_of_a_product(cartItem.getProductOrServiceSerial());
                buyingPrice = product.getBuyingPrice();
            }
            try {
                Connection connection = connect_to_memory();
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO _receipt (\n" +
                        "                         isProduct,\n" +
                        "                         buyPrice,\n" +
                        "                         sellingPrice,\n" +
                        "                         quantitySold,\n" +
                        "                         serial,\n" +
                        "                         receiptNum,\n" +
                        "                         dateTime\n" +
                        "                     )\n" +
                        "                     VALUES (\n" +
                        "                         ?,\n" +
                        "                         ?,\n" +
                        "                         ?,\n" +
                        "                         ?,\n" +
                        "                         ?,\n" +
                        "                         ?,\n" +
                        "                         ?\n" +
                        "                     )");
                preparedStatement.setBoolean(1, cartItem.getProduct());
                preparedStatement.setDouble(2, buyingPrice);
                preparedStatement.setDouble(3, cartItem.getPrice());
                preparedStatement.setInt(4, cartItem.getQuantityRequested());
                preparedStatement.setString(5, cartItem.getProductOrServiceSerial());
                preparedStatement.setInt(6, LATEST_RECEIPT_NUMBER);
                preparedStatement.setString(7, get_date_and_time());
                final int executionStatus = preparedStatement.executeUpdate();
                preparedStatement.close();
                connection.close();
                if (executionStatus == 0) {
                    allIsGood = false;
                    break;
                } else {
                    allIsGood = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
                new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
                new Issues().programmer_error(e).show();
                allIsGood = false;
                break;
            }
        }
        return allIsGood;
    }

    @NotNull
    private String get_date_and_time() {
        return get_time().concat(" on ").concat(get_date());
    }

    @NotNull
    private String get_time() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss:SSS"));
    }

    @NotNull
    private String get_date() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
    }

    public final OnlineService get_selected_service(final String serviceName) {
        OnlineService onlineService = null;
        try {
            JsonParser jsonParser = new JsonParser();
            FileReader fileReader = new FileReader(PATH_TO_SERVICES_FILE);
            JsonArray jsonArray = (JsonArray) jsonParser.parse(fileReader);
            fileReader.close();
            for (JsonElement jsonElement : jsonArray) {
                OnlineService onlineService1 = new Gson().fromJson(jsonElement, OnlineService.class);
                if (onlineService1.getName().equals(serviceName)) {
                    onlineService = onlineService1;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return onlineService;
    }

    @NotNull
    public final ObservableList<OnlineService> get_list_of_services() {
        ObservableList<OnlineService> onlineServices = FXCollections.observableArrayList();
        try {
            JsonParser jsonParser = new JsonParser();
            FileReader fileReader = new FileReader(PATH_TO_SERVICES_FILE);
            JsonArray jsonArray = (JsonArray) jsonParser.parse(fileReader);
            fileReader.close();
            jsonArray.forEach(object -> {
                OnlineService onlineService = new Gson().fromJson(object, OnlineService.class);
                onlineServices.add(onlineService);

            });
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return onlineServices;
    }

    @NotNull
    public final HashMap<Integer, CartItem> get_all_cart_items() {
        final HashMap<Integer, CartItem> integerCartItemHashMap = new HashMap<>();
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id,\n" +
                    "       p_s_serial,\n" +
                    "       name,\n" +
                    "       quantity,\n" +
                    "       price,\n" +
                    "       isProduct\n" +
                    "  FROM _cart");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.isBeforeFirst()) {
                while (resultSet.next()) {
                    final CartItem cartItem = new CartItem();
                    cartItem.setProductOrServiceSerial(resultSet.getString(2));
                    cartItem.setName(resultSet.getString(3));
                    cartItem.setQuantityRequested(resultSet.getInt(4));
                    cartItem.setPrice(resultSet.getDouble(5));
                    cartItem.setProduct(resultSet.getBoolean(6));
                    integerCartItemHashMap.put(resultSet.getInt(1), cartItem);
                }
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return integerCartItemHashMap;
    }

    public final Boolean delete_cart_item(final int rowId) {
        boolean allIsGood = false;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM _cart\n" +
                    "      WHERE id = " + rowId);
            final int executionStatus = preparedStatement.executeUpdate();
            if (executionStatus > 0) {
                allIsGood = true;
            }
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return allIsGood;
    }

    public final Integer get_row_id_of_cart_item(String productOrServiceSerial, @NotNull CartItem cartItem) {
        int serial = -1;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id\n" +
                    "  FROM _cart\n" +
                    " WHERE isProduct = " + cartItem.getProduct() + " AND \n" +
                    "       p_s_serial = '" + productOrServiceSerial + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                serial = resultSet.getInt(1);
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return serial;
    }

    public final Boolean add_product_or_service_to_cart(@NotNull final CartItem cartItem) {
        boolean it_is_done = false;
        Product product = null;
        OnlineService onlineService = null;
        boolean theQuantityIsBeyondStock = false;
        if (cartItem.getProduct()) {
            product = get_details_of_a_product(cartItem.getProductOrServiceSerial());
        } else {
            onlineService = get_selected_service(cartItem.getName());
        }
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement;
            preparedStatement = connection.prepareStatement("SELECT quantity,\n" +
                    "       price\n" +
                    "  FROM _cart\n" +
                    " WHERE p_s_serial = '" + cartItem.getProductOrServiceSerial() + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                if (product != null) {
                    cartItem.setQuantityRequested((cartItem.getQuantityRequested() + resultSet.getInt(1)));
                    cartItem.setPrice((cartItem.getPrice() + resultSet.getDouble(2)));
                    if (cartItem.getQuantityRequested() <= product.getStockQuantity()) {
                        preparedStatement = connection.prepareStatement("UPDATE _cart\n" +
                                "   SET quantity = ?,\n" +
                                "       price = ?\n" +
                                " WHERE p_s_serial = '" + cartItem.getProductOrServiceSerial() + "'");
                        preparedStatement.setInt(1, cartItem.getQuantityRequested());
                        preparedStatement.setDouble(2, cartItem.getPrice());
                        final int executionStatus = preparedStatement.executeUpdate();
                        if (executionStatus > 0) {
                            it_is_done = true;
                        }
                    } else  {
                        theQuantityIsBeyondStock = true;
                        it_is_done = true;
                    }
                } else {
                    if (onlineService != null) {
                        cartItem.setQuantityRequested((cartItem.getQuantityRequested() + resultSet.getInt(1)));
                        cartItem.setPrice((cartItem.getPrice() + resultSet.getDouble(2)));
                        preparedStatement = connection.prepareStatement("UPDATE _cart\n" +
                                "   SET quantity = ?,\n" +
                                "       price = ?\n" +
                                " WHERE p_s_serial = '" + cartItem.getProductOrServiceSerial() + "'");
                        preparedStatement.setInt(1, cartItem.getQuantityRequested());
                        preparedStatement.setDouble(2, cartItem.getPrice());
                        final int executionStatus = preparedStatement.executeUpdate();
                        if (executionStatus > 0) {
                            it_is_done = true;
                        }
                    }
                }
            } else {
                preparedStatement = connection.prepareStatement("INSERT INTO _cart (\n" +
                        "                      isProduct,\n" +
                        "                      price,\n" +
                        "                      quantity,\n" +
                        "                      name,\n" +
                        "                      p_s_serial\n" +
                        "                  )\n" +
                        "                  VALUES (\n" +
                        "                      ?,\n" +
                        "                      ?,\n" +
                        "                      ?,\n" +
                        "                      ?,\n" +
                        "                      ?\n" +
                        "                  )");
                preparedStatement.setBoolean(1, cartItem.getProduct());
                preparedStatement.setDouble(2, cartItem.getPrice());
                preparedStatement.setInt(3, cartItem.getQuantityRequested());
                preparedStatement.setString(4, cartItem.getName());
                preparedStatement.setString(5, cartItem.getProductOrServiceSerial());
                final int executionStatus = preparedStatement.executeUpdate();
                if (executionStatus > 0) {
                    it_is_done = true;
                }
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        } finally {
            if (theQuantityIsBeyondStock) {
                new Issues().error_message_alert("Failed!", "I could not increase the quantity of the requested item because the stock can only provide a maximum of " + product.getStockQuantity() + " only.").show();
            }
        }
        return it_is_done;
    }

    @NotNull
    public final List<Product> get_product_list() {
        final List<Product> productList = new ArrayList<>();
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT serial,\n" +
                    "       name,\n" +
                    "       description,\n" +
                    "       ratings,\n" +
                    "       quantity,\n" +
                    "       markedPrice,\n" +
                    "       imageName,\n" +
                    "       isAvailable\n" +
                    "  FROM _product");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.isBeforeFirst()) {
                while (resultSet.next()) {
                    Image image;
                    try {
                        image = new Image(new FileInputStream(Main.RESOURCE_PATH.getAbsolutePath().concat("\\_gallery\\").concat(resultSet.getString(7))));
                    } catch (FileNotFoundException e) {
                        image = new Image(getClass().getResourceAsStream(INPUT_STREAM_TO_NO_IMAGE));
                    }
                    final Product product = new Product();
                    product.setSerial(resultSet.getString(1));
                    product.setName(resultSet.getString(2));
                    product.setDescription(resultSet.getString(3));
                    product.setStarCount(resultSet.getInt(4));
                    product.setStockQuantity(resultSet.getInt(5));
                    product.setMarkedPrice(resultSet.getDouble(6));
                    product.setItemImage(image);
                    product.setAvailable(resultSet.getBoolean(8));
                    productList.add(product);
                }
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return productList;
    }

    public final Product get_details_of_a_product(final String productSerial) {
        Product product = null;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT serial,\n" +
                    "       name,\n" +
                    "       description,\n" +
                    "       ratings,\n" +
                    "       quantity,\n" +
                    "       markedPrice,\n" +
                    "       buyPrice,\n" +
                    "       imageName\n" +
                    "  FROM _product\n" +
                    " WHERE serial = '" + productSerial + "'  AND \n" +
                    "       isAvailable = true");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Image image;
                try {
                    image = new Image(new FileInputStream(Main.RESOURCE_PATH.getAbsolutePath().concat("\\_gallery\\").concat(resultSet.getString(8))));
                } catch (FileNotFoundException e) {
                    image = new Image(getClass().getResourceAsStream(INPUT_STREAM_TO_NO_IMAGE));
                }
                product = new Product(resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getInt(4),
                        resultSet.getInt(5),
                        resultSet.getDouble(6),
                        resultSet.getDouble(7),
                        image,
                        true);
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return product;
    }

    public final String verify_user(final String username, final String password) {
        String firstName = null;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT firstname,\n" +
                    "       password\n" +
                    "  FROM _user\n" +
                    " WHERE isActive = 1 AND \n" +
                    "       username = '" + username + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                final String hashedText = resultSet.getString(2);
                if (BCrypt.checkpw(password, hashedText)) {
                    firstName = resultSet.getString(1);
                }
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return firstName;
    }

    public final String check_if_the_username_is_legit(final String username) {
        String fullname = null;
        try {
            Connection connection = connect_to_memory();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT firstname,\n" +
                    "       surname\n" +
                    "  FROM _user\n" +
                    " WHERE isActive = 1 AND \n" +
                    "       username = '" + username + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                fullname = resultSet.getString(1).concat(" ").concat(resultSet.getString(2));
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(new Issues().write_log("\n\n" + new Issues().time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(new Issues().stack_trace_printing(e.getStackTrace())).start();
            new Issues().programmer_error(e).show();
        }
        return fullname;
    }

}
