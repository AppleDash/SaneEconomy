package org.appledash.saneeconomy.updates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.utils.WebUtils;

/**
 * Created by appledash on 7/11/16.
 * Blackjack is still best pony.
 */
public class GithubVersionChecker {
    public static final String DOWNLOAD_URL = "https://github.com/AppleDash/SaneEconomy/releases";
    private static final String RELEASES_URL = "https://api.github.com/repos/AppleDash/SaneEconomy/releases";
    private static boolean updateChecked = false;
    private static boolean updateAvailable = false;
    private static String newestVersion;

    public static void checkUpdateAvailable() {
        String jsonContent = WebUtils.getContents(RELEASES_URL);

        JsonArray array = (JsonArray)new JsonParser().parse(jsonContent);

        int currentVersion = releaseToInt(SaneEconomy.getInstance().getDescription().getVersion());
        int newestVersion = -1;
        // JsonObject newestObj = null;

        for (JsonElement elem : array) {
            if (elem instanceof JsonObject) {
                JsonObject releaseObj = (JsonObject)elem;
                String versionStr = releaseObj.get("tag_name").getAsString();
                int version = releaseToInt(versionStr);

                if (version > newestVersion) {
                    newestVersion = version;
                    GithubVersionChecker.newestVersion = versionStr;
                    // newestObj = releaseObj;
                }
            }
        }

        updateChecked = true;
        updateAvailable = newestVersion > currentVersion;
    }

    private static int releaseToInt(String release) {
        return Integer.valueOf(release.trim().replace(".", ""));
    }

    public static boolean isUpdateAvailable() {
        return updateChecked && updateAvailable;
    }

    public static String getNewestVersion() {
        return newestVersion;
    }
}
