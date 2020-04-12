package org.appledash.saneeconomy.utils;

import org.appledash.saneeconomy.SaneEconomy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by appledash on 7/11/16.
 * Blackjack is still best pony.
 */
public final class WebUtils {
    private WebUtils() {
    }

    public static String getContents(String url) {
        try {
            StringBuilder out = new StringBuilder();
            URL uri = new URL(url);
            BufferedReader br = new BufferedReader(new InputStreamReader(uri.openConnection().getInputStream()));
            String line;

            //noinspection NestedAssignment
            while ((line = br.readLine()) != null) {
                out.append(line).append("\n");
            }

            br.close();

            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get URL contents!", e);
        }
    }
}
