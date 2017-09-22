package org.appledash.saneeconomy.updates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.appledash.saneeconomy.utils.WebUtils;

/**
 * Created by appledash on 7/11/16.
 * Blackjack is still best pony.
 */
public class GithubVersionChecker {
    public static final String DOWNLOAD_URL = "https://github.com/AppleDash/SaneEconomy/releases";
    private static final String RELEASES_URL = "https://api.github.com/repos/AppleDash/SaneEconomy/releases";
    private static String newestFound;

    private boolean updateChecked;
    private boolean updateAvailable;
    private final String pluginName;
    private final String currentVersion;

    public GithubVersionChecker(String pluginName, String currentVersion) {
        this.pluginName = pluginName;
        this.currentVersion = currentVersion;
    }

    public void checkUpdateAvailable() {
        String jsonContent = WebUtils.getContents(RELEASES_URL);

        JsonArray array = (JsonArray)new JsonParser().parse(jsonContent);

        String currentVersion = this.currentVersion;
        String newestFound = null;

        for (JsonElement elem : array) {
            if (elem instanceof JsonObject) {
                JsonObject releaseObj = (JsonObject)elem;
                boolean isPrerelease = releaseObj.get("prerelease").getAsBoolean();

                if (isPrerelease) { // Don't tell them to update to prereleases, which I might release for individual users to test.
                    continue;
                }

                String releaseName = releaseObj.get("name").getAsString().split(" ")[0];

                if (!releaseName.equalsIgnoreCase(pluginName)) { // Not for this plugin.
                    continue;
                }

                String versionStr = releaseObj.get("tag_name").getAsString();

                if (VersionComparer.isSemVerGreaterThan(newestFound, versionStr)) {
                    newestFound = versionStr;
                    GithubVersionChecker.newestFound = versionStr;
                }
            }
        }

        updateChecked = true;
        updateAvailable = VersionComparer.isSemVerGreaterThan(currentVersion, newestFound);
    }

    public boolean isUpdateAvailable() {
        return updateChecked && updateAvailable;
    }

    public String getNewestVersion() {
        return newestFound;
    }
}
