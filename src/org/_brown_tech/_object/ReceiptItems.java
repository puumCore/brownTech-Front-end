package org._brown_tech._object;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Muriithi_Mandela
 */

@SuppressWarnings("WeakerAccess")
public class ReceiptItems {

    public static int spaceBtwnLines, someOtherSpacing;
    public static Graphics graphics;

    private String item_name;
    private String item_code;
    private int item_quantity;
    private double item_sellingPrice;

    private ReceiptItems(String item_code, String item_name, int item_quantity, double item_sellingPrice) {
        this.setItem_code(item_code);
        this.setItem_name(item_name);
        this.setItem_quantity(item_quantity);
        this.setItem_sellingPrice(item_sellingPrice);
    }

    public static void printHeader() {
        graphics.drawString(String.format("%s %28s %4s %7s %4s", " Item", "|", "Selling Price", "x", "Qty"), someOtherSpacing, spaceBtwnLines);
        spaceBtwnLines += 5;
        graphics.drawString(String.format("%s", " ---------------------------------------------------"), someOtherSpacing, spaceBtwnLines);
        spaceBtwnLines += 8;
    }

    @NotNull
    public static List<ReceiptItems> builtStoreItems(@NotNull List<Object> xArrayList) {
        final List<ReceiptItems> itemList = new ArrayList<>();
        for (int a = 0; a < xArrayList.size(); ++a) {
            itemList.add(new ReceiptItems((String) xArrayList.get(a), (String) xArrayList.get(++a), Integer.parseInt((String) xArrayList.get(++a)), Double.parseDouble((String) xArrayList.get(++a))));
        }
        return itemList;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getItem_code() {
        return item_code;
    }

    public void setItem_code(String item_code) {
        this.item_code = item_code;
    }

    public int getItem_quantity() {
        return item_quantity;
    }

    public void setItem_quantity(int item_quantity) {
        this.item_quantity = item_quantity;
    }

    public double getItem_sellingPrice() {
        return item_sellingPrice;
    }

    public void setItem_sellingPrice(double item_sellingPrice) {
        this.item_sellingPrice = item_sellingPrice;
    }

    public void printInvoice() {
        int a = 28 - getItem_code().toCharArray().length;
        graphics.drawString(String.format("%s", " " + getItem_name().trim()), someOtherSpacing, spaceBtwnLines);
        spaceBtwnLines += 8;
        graphics.drawString(String.format("%s %" + a + "s %4s %4s %5s", " " + getItem_code().trim(), "|", String.format("%,.1f", (getItem_sellingPrice() / getItem_quantity())), "x", String.format("%d", getItem_quantity())), someOtherSpacing, spaceBtwnLines);
        spaceBtwnLines += 10;
    }

}
