package SpicyShops.util;

import com.megacrit.cardcrawl.random.Random;

import java.util.ArrayList;

public class HelperClass {
    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String capitalizeAfterColon(String input) {
        int colonIndex = input.indexOf(":");
        if (colonIndex != -1) {
            // Capitalize the first letter after the colon
            String firstChar = input.substring(colonIndex + 1, colonIndex + 2).toUpperCase();
            return input.substring(0, colonIndex + 1) + firstChar + input.substring(colonIndex + 2);
        } else {
            return input;
        }
    }

    public static String capitalize(String str, String match) {
        return str.replace(match, capitalize(match));
    }

    public static <T> T getRandomItem(ArrayList<T> list, Random rng) {
        return list.isEmpty() ? null : list.get(rng.random(list.size() - 1));
    }
}
