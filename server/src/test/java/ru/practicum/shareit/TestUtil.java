package ru.practicum.shareit;

import net.bytebuddy.utility.RandomString;

public class TestUtil {

    public static String getRandomEmail() {
        RandomString randomString = new RandomString();
        return randomString.nextString() + "@" + randomString.nextString() + ".ew";
    }

    public static String getRandomString() {
        RandomString randomString = new RandomString();
        return randomString.nextString();
    }
}
