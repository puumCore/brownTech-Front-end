package org._brown_tech._custom;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org._brown_tech.Main;
import org._brown_tech._controller.Controller;
import org._brown_tech._object.Account;
import org._brown_tech._object.OnlineService;
import org._brown_tech._object._payment.*;
import org._brown_tech._outsourced.BCrypt;
import org._brown_tech._response_model.StandardResponse;
import org._brown_tech._response_model.StatusResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
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
public class Brain extends Watchdog {

    private final SQLiteDataSource ds = new SQLiteDataSource();
    public final String INPUT_STREAM_TO_NO_IMAGE = "/org/_brown_tech/_images/_icons/noimage.png";
    private final String PATH_TO_SERVICES_FILE = Main.RESOURCE_PATH.getAbsolutePath().concat("\\_config\\services.json");
    protected static String BASE_URL = "http://localhost:4567/brownTech/pos/api";

    public Connection connect_to_memory() {
        try {
            ds.setUrl("jdbc:sqlite:".concat(Main.RESOURCE_PATH.getAbsolutePath().concat("\\_dataSource\\bt_dataStore.db3")));
            return ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            programmer_error(e).show();
            return null;
        }
    }

    public Account reset_password(Account account) {
        try {
            final HttpPost httpPost = new HttpPost(BASE_URL.concat("/recovery"));
            httpPost.setEntity(new StringEntity(new Gson().toJson(account, Account.class)));
            final CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                final HttpEntity httpEntity = closeableHttpResponse.getEntity();
                final String objectAsString = EntityUtils.toString(httpEntity);
                final JsonParser jsonParser = new JsonParser();
                final JsonElement rootJsonElement = jsonParser.parse(objectAsString);
                final StandardResponse standardResponse = new Gson().fromJson(rootJsonElement, StandardResponse.class);
                if (standardResponse.getStatus().equals(StatusResponse.SUCCESS)) {
                    account = new Gson().fromJson(standardResponse.getData(), Account.class);
                } else {
                    error_message("Failed!", standardResponse.getMessage()).show();
                }
            } else {
                error_message("Error From Server : " + statusCode, "Reason: ".concat(closeableHttpResponse.getStatusLine().getReasonPhrase())).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
            account = null;
        }
        return account;
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
                    Platform.runLater(() -> information_message("Cart was successfully cleaned. We are ready for the next Customer").show());
                }
            }
            Controller.cartItems = integerCartItemHashMap;
        } catch (SQLException e) {
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            e.printStackTrace();
            programmer_error(e).show();
        }
    }

    public Boolean insert_product_into_stock(@NotNull final Product product) {
        try {
            final HttpPost httpPost = new HttpPost(BASE_URL.concat("/stock/products/new"));
            httpPost.setEntity(new StringEntity(new Gson().toJson(product, Product.class)));
            final CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                final HttpEntity httpEntity = closeableHttpResponse.getEntity();
                final String objectAsString = EntityUtils.toString(httpEntity);
                final JsonParser jsonParser = new JsonParser();
                final JsonElement rootJsonElement = jsonParser.parse(objectAsString);
                final StandardResponse standardResponse = new Gson().fromJson(rootJsonElement, StandardResponse.class);
                if (standardResponse.getStatus().equals(StatusResponse.SUCCESS)) {
                    return true;
                } else {
                    error_message("Failed!", standardResponse.getMessage()).show();
                }
            } else {
                error_message("Error From Server : " + statusCode, "Reason: ".concat(closeableHttpResponse.getStatusLine().getReasonPhrase())).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
        }
        return false;
    }

    @NotNull
    public final List<Product> get_details_of_products_from_the_given_param(final String param) {
        final List<Product> productList = new ArrayList<>();
        try {
            final HttpPost httpPost = new HttpPost(BASE_URL.concat("/stock/products/find"));
            httpPost.setEntity(new StringEntity(new Gson().toJson(param, String.class)));
            final CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                final HttpEntity httpEntity = closeableHttpResponse.getEntity();
                final String objectAsString = EntityUtils.toString(httpEntity);
                final JsonParser jsonParser = new JsonParser();
                final JsonElement rootJsonElement = jsonParser.parse(objectAsString);
                final StandardResponse standardResponse = new Gson().fromJson(rootJsonElement, StandardResponse.class);
                if (standardResponse.getStatus().equals(StatusResponse.SUCCESS)) {
                    JsonArray jsonArray = new Gson().fromJson(standardResponse.getData(), JsonArray.class);
                    jsonArray.forEach(jsonElement -> {
                        final Product product = new Gson().fromJson(jsonElement, Product.class);
                        productList.add(product);
                    });
                } else {
                    error_message("Failed!", standardResponse.getMessage()).show();
                }
            } else {
                error_message("Error From Server : " + statusCode, "Reason: ".concat(closeableHttpResponse.getStatusLine().getReasonPhrase())).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
        }
        return productList;
    }

    public final @NotNull List<String> get_list_of_product_serials() {
        final List<String> serials = new ArrayList<>();
        try {
            final HttpGet httpGet = new HttpGet(BASE_URL.concat("/stock/serials/false"));
            final CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                final HttpEntity httpEntity = closeableHttpResponse.getEntity();
                final String objectAsString = EntityUtils.toString(httpEntity);
                final JsonParser jsonParser = new JsonParser();
                final JsonElement rootJsonElement = jsonParser.parse(objectAsString);
                final StandardResponse standardResponse = new Gson().fromJson(rootJsonElement, StandardResponse.class);
                if (standardResponse.getStatus().equals(StatusResponse.SUCCESS)) {
                    JsonArray jsonArray = new Gson().fromJson(standardResponse.getData(), JsonArray.class);
                    for (JsonElement jsonElement : jsonArray) {
                        String serial = new Gson().fromJson(jsonElement, String.class);
                        serials.add(serial);
                    }
                } else {
                    error_message("Failed!", standardResponse.getMessage()).show();
                }
            } else {
                error_message("Error From Server : " + statusCode, "Reason: ".concat(closeableHttpResponse.getStatusLine().getReasonPhrase())).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
        }
        return serials;
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
                new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
                new Thread(stack_trace_printing(e)).start();
                programmer_error(e).show();
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
                new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
                new Thread(stack_trace_printing(e)).start();
                programmer_error(e).show();
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
                product.setStock((product.getStock() - cartItem.getQuantityRequested()));
                try {
                    final HttpPost httpPost = new HttpPost(BASE_URL.concat("/stock/products/quantity"));
                    httpPost.setEntity(new StringEntity(new Gson().toJson(product, Product.class)));
                    final CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
                    final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
                    final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
                    if (statusCode == HttpURLConnection.HTTP_OK) {
                        final HttpEntity httpEntity = closeableHttpResponse.getEntity();
                        final String objectAsString = EntityUtils.toString(httpEntity);
                        final JsonParser jsonParser = new JsonParser();
                        final JsonElement rootJsonElement = jsonParser.parse(objectAsString);
                        final StandardResponse standardResponse = new Gson().fromJson(rootJsonElement, StandardResponse.class);
                        if (!standardResponse.getStatus().equals(StatusResponse.SUCCESS)) {
                            error_message("Failed!", standardResponse.getMessage()).show();
                        }
                    } else {
                        error_message("Error From Server : " + statusCode, "Reason: ".concat(closeableHttpResponse.getStatusLine().getReasonPhrase())).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
                    new Thread(stack_trace_printing(e)).start();
                    programmer_error(e).show();
                }
            } else {
                allIsGood = true;
            }
        }
        return allIsGood;
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
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
        }
        return allIsGood;
    }

    public final @NotNull Boolean upload_to_cheque_record(Cheque cheque) {
        try {
            final HttpPost httpPost = new HttpPost(BASE_URL.concat("/sale/cheque"));
            httpPost.setEntity(new StringEntity(new Gson().toJson(cheque, Cheque.class)));
            final CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                final HttpEntity httpEntity = closeableHttpResponse.getEntity();
                final String objectAsString = EntityUtils.toString(httpEntity);
                final JsonParser jsonParser = new JsonParser();
                final JsonElement rootJsonElement = jsonParser.parse(objectAsString);
                final StandardResponse standardResponse = new Gson().fromJson(rootJsonElement, StandardResponse.class);
                if (standardResponse.getStatus().equals(StatusResponse.SUCCESS)) {
                    return true;
                } else {
                    error_message("Failed!", standardResponse.getMessage()).show();
                }
            } else {
                error_message("Error From Server : " + statusCode, "Reason: ".concat(closeableHttpResponse.getStatusLine().getReasonPhrase())).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
        }
        return false;
    }

    public final @NotNull Boolean upload_cash_record(final Cash cash) {
        try {
            final HttpPost httpPost = new HttpPost(BASE_URL.concat("/sale/cash"));
            httpPost.setEntity(new StringEntity(new Gson().toJson(cash, Cash.class)));
            final CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                final HttpEntity httpEntity = closeableHttpResponse.getEntity();
                final String objectAsString = EntityUtils.toString(httpEntity);
                final JsonParser jsonParser = new JsonParser();
                final JsonElement rootJsonElement = jsonParser.parse(objectAsString);
                final StandardResponse standardResponse = new Gson().fromJson(rootJsonElement, StandardResponse.class);
                if (standardResponse.getStatus().equals(StatusResponse.SUCCESS)) {
                    return true;
                } else {
                    error_message("Failed!", standardResponse.getMessage()).show();
                }
            } else {
                error_message("Error From Server : " + statusCode, "Reason: ".concat(closeableHttpResponse.getStatusLine().getReasonPhrase())).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
        }
        return false;
    }

    public final Integer get_a_receipt_number_for_the_sale(final Sale sale) {
        Integer receiptNumber = null;
        try {
            final HttpPost httpPost = new HttpPost(BASE_URL.concat("/sale"));
            httpPost.setEntity(new StringEntity(new Gson().toJson(sale, Sale.class)));
            final CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                final HttpEntity httpEntity = closeableHttpResponse.getEntity();
                final String objectAsString = EntityUtils.toString(httpEntity);
                final JsonParser jsonParser = new JsonParser();
                final JsonElement rootJsonElement = jsonParser.parse(objectAsString);
                final StandardResponse standardResponse = new Gson().fromJson(rootJsonElement, StandardResponse.class);
                if (standardResponse.getStatus().equals(StatusResponse.SUCCESS)) {
                    receiptNumber = new Gson().fromJson(standardResponse.getData(), Integer.class);
                } else {
                    error_message("Failed!", standardResponse.getMessage()).show();
                }
            } else {
                error_message("Error From Server : " + statusCode, "Reason: ".concat(closeableHttpResponse.getStatusLine().getReasonPhrase())).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
        }
        return receiptNumber;
    }

    public final void create_receipt_record_from_cart_items(final @NotNull Sale sale) {
        final HashMap<Integer, CartItem> cartItemHashMap = get_all_cart_items();
        final Set<Integer> integerSet = cartItemHashMap.keySet();
        final List<Receipt> receiptList = sale.getReceipts();
        for (Integer rowId : integerSet) {
            final CartItem cartItem = cartItemHashMap.get(rowId);
            final Receipt receipt = new Receipt();
            receipt.setDateAndTime(get_date_and_time());
            receipt.setSerial(cartItem.getProductOrServiceSerial());
            receipt.setQuantitySold(cartItem.getQuantityRequested());
            receipt.setSellingPrice(cartItem.getPrice());
            receipt.setBuyingPrice(cartItem.getBuyingPrice());
            receipt.setProduct(cartItem.getProduct());
            receiptList.add(receipt);
        }
    }

    @NotNull
    protected String get_date_and_time() {
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
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
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
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
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
                    "       secretPrice,\n" +
                    "       isProduct\n" +
                    "  FROM _cart");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.isBeforeFirst()) {
                while (resultSet.next()) {
                    final CartItem cartItem = new CartItem();
                    cartItem.setId(resultSet.getInt(1));
                    cartItem.setProductOrServiceSerial(resultSet.getString(2));
                    cartItem.setName(resultSet.getString(3));
                    cartItem.setQuantityRequested(resultSet.getInt(4));
                    cartItem.setPrice(resultSet.getDouble(5));
                    cartItem.setBuyingPrice(resultSet.getDouble(6));
                    cartItem.setProduct(resultSet.getBoolean(7));
                    integerCartItemHashMap.put(cartItem.getId(), cartItem);
                }
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
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
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
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
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
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
                    if (cartItem.getQuantityRequested() <= product.getStock()) {
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
                    } else {
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
                        "                      secretPrice,\n" +
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
                        "                      ?,\n" +
                        "                      ?\n" +
                        "                  )");
                preparedStatement.setBoolean(1, cartItem.getProduct());
                preparedStatement.setDouble(2, cartItem.getBuyingPrice());
                preparedStatement.setDouble(3, cartItem.getPrice());
                preparedStatement.setInt(4, cartItem.getQuantityRequested());
                preparedStatement.setString(5, cartItem.getName());
                preparedStatement.setString(6, cartItem.getProductOrServiceSerial());
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
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
        } finally {
            if (theQuantityIsBeyondStock) {
                error_message_alert("Failed!", "I could not increase the quantity of the requested item because the stock can only provide a maximum of " + product.getStock() + " only.").show();
            }
        }
        return it_is_done;
    }

    @NotNull
    public final List<Product> get_product_list() {
        final List<Product> productList = new ArrayList<>();
        try {
            final HttpGet httpGet = new HttpGet(BASE_URL.concat("/stock/products"));
            final CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                final HttpEntity httpEntity = closeableHttpResponse.getEntity();
                final String objectAsString = EntityUtils.toString(httpEntity);
                final JsonParser jsonParser = new JsonParser();
                final JsonElement rootJsonElement = jsonParser.parse(objectAsString);
                final StandardResponse standardResponse = new Gson().fromJson(rootJsonElement, StandardResponse.class);
                if (standardResponse.getStatus().equals(StatusResponse.SUCCESS)) {
                    JsonArray jsonArray = new Gson().fromJson(standardResponse.getData(), JsonArray.class);
                    jsonArray.forEach(jsonElement -> {
                        final Product product = new Gson().fromJson(jsonElement, Product.class);
                        productList.add(product);
                    });
                } else {
                    error_message("Failed!", standardResponse.getMessage()).show();
                }
            } else {
                error_message("Error From Server : " + statusCode, "Reason: ".concat(closeableHttpResponse.getStatusLine().getReasonPhrase())).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
        }
        return productList;
    }

    public final Product get_details_of_a_product(final String productSerial) {
        Product product = null;
        try {
            final HttpGet httpGet = new HttpGet(BASE_URL.concat("/stock/find/").concat(productSerial));
            final CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                final HttpEntity httpEntity = closeableHttpResponse.getEntity();
                final String objectAsString = EntityUtils.toString(httpEntity);
                final JsonParser jsonParser = new JsonParser();
                final JsonElement rootJsonElement = jsonParser.parse(objectAsString);
                final StandardResponse standardResponse = new Gson().fromJson(rootJsonElement, StandardResponse.class);
                if (standardResponse.getStatus().equals(StatusResponse.SUCCESS)) {
                    product = new Gson().fromJson(standardResponse.getData(), Product.class);
                } else {
                    error_message("Failed!", standardResponse.getMessage()).show();
                }
            } else {
                error_message("Error From Server : " + statusCode, "Reason: ".concat(closeableHttpResponse.getStatusLine().getReasonPhrase())).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
        }
        return product;
    }

    public Account update_account(@NotNull Account account) {
        try {
            account.setActive(true);
            final HttpPost httpPost = new HttpPost(BASE_URL.concat("/user/update"));
            httpPost.setEntity(new StringEntity(new Gson().toJson(account, Account.class)));
            final CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            account = null;
            if (statusCode == HttpURLConnection.HTTP_OK) {
                final HttpEntity httpEntity = closeableHttpResponse.getEntity();
                final String objectAsString = EntityUtils.toString(httpEntity);
                final JsonParser jsonParser = new JsonParser();
                final JsonElement rootJsonElement = jsonParser.parse(objectAsString);
                final StandardResponse standardResponse = new Gson().fromJson(rootJsonElement, StandardResponse.class);
                if (standardResponse.getStatus().equals(StatusResponse.SUCCESS)) {
                    account = new Gson().fromJson(standardResponse.getData(), Account.class);
                } else {
                    error_message("Failed!", standardResponse.getMessage()).show();
                }
            } else {
                error_message("Error From Server : " + statusCode, "Reason: ".concat(closeableHttpResponse.getStatusLine().getReasonPhrase())).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
            account = null;
        }
        return account;
    }

    public final Account get_user_account(final String username, final String password) {
        Account account = new Account();
        try {
            account.setUsername(username);
            final HttpPost httpPost = new HttpPost(BASE_URL.concat("/user/login"));
            httpPost.setEntity(new StringEntity(new Gson().toJson(account, Account.class)));
            final CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                final HttpEntity httpEntity = closeableHttpResponse.getEntity();
                final String objectAsString = EntityUtils.toString(httpEntity);
                final JsonParser jsonParser = new JsonParser();
                final JsonElement rootJsonElement = jsonParser.parse(objectAsString);
                final StandardResponse standardResponse = new Gson().fromJson(rootJsonElement, StandardResponse.class);
                if (standardResponse.getStatus().equals(StatusResponse.SUCCESS)) {
                    account = new Gson().fromJson(standardResponse.getData(), Account.class);
                    if (account != null) {
                        if (password != null) {
                            if (!BCrypt.checkpw(password, account.getPassword())) {
                                error_message("Invalid credentials!", "Check your username or password").show();
                                account = null;
                            }
                        }
                    }
                } else {
                    error_message("Failed!", standardResponse.getMessage()).show();
                }
            } else {
                account = null;
                error_message("Error From Server : " + statusCode, "Reason: ".concat(closeableHttpResponse.getStatusLine().getReasonPhrase())).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, 1)).start();
            new Thread(stack_trace_printing(e)).start();
            programmer_error(e).show();
            account = null;
        }
        return account;
    }

}
