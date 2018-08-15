package com.bitshares.bitshareswallet.util;

public class Safe {

    public static String encryptDecrypt(String input, char[] key) {
        StringBuilder output = new StringBuilder();

        for(int i = 0; i < input.length(); i++) {
            output.append((char) (input.charAt(i) ^ key[i % key.length]));
        }

        return output.toString();
    }

}
