package org.appledash.saneeconomy.utils;

import com.google.common.collect.ImmutableMap;
import org.appledash.saneeconomy.SaneEconomy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by AppleDash on 8/5/2016.
 * Blackjack is still best pony.
 */
public class I18n {
    private static final I18n INSTANCE = new I18n(SaneEconomy.getInstance());
    private final SaneEconomy plugin;
    private final Map<String, String> translations = new HashMap<>();

    private I18n(SaneEconomy plugin) {
        this.plugin = plugin;
    }

    public void loadTranslations() {
        File configFile = new File(plugin.getDataFolder(), "messages.yml");
        YamlConfiguration configJar = YamlConfiguration.loadConfiguration(new InputStreamReader(this.getClass().getResourceAsStream("/messages.yml")));

        if (configFile.exists()) { // Attempt to merge any new keys from the JAR's messages.yml into the copy in the plugin's data folder
            YamlConfiguration configDisk = YamlConfiguration.loadConfiguration(configFile);

            List<Map<?, ?>> finalKeys = configDisk.getMapList("messages");

            for (Map jarObject : configJar.getMapList("messages")) { // For every translation in the template config in the JAR
                String jarMessage = String.valueOf(jarObject.get("message")); // Key for this translation
                Map equivalentOnDisk = null; // Equivalent of this translation in the config file on disk

                for (Map diskMap : configDisk.getMapList("messages")) { // For every translation in the config on disk
                    if (String.valueOf(diskMap.get("message")).equals(jarMessage)) { // If the translation key on this object on disk is the same as the current one in the JAR
                        equivalentOnDisk = diskMap;
                        break;
                    }
                }

                if (equivalentOnDisk == null) { // This one isn't on disk yet - add it.
                    finalKeys.add(jarObject);
                } else {
                    String currentKey = String.valueOf(equivalentOnDisk.get("message"));
                    String convertedKey = convertOldTranslations(currentKey);

                    if (!currentKey.equals(convertedKey)) { // Key needs conversion
                        String convertedValue = convertOldTranslations(String.valueOf(equivalentOnDisk.get("translation")));

                        // Remove current key from map of things to go to the disk
                        Iterator<Map<?, ?>> iter = finalKeys.iterator();

                        while (iter.hasNext()) {
                            if (String.valueOf(iter.next().get("message")).equals(equivalentOnDisk.get("message"))) {
                                iter.remove();
                            }
                        }

                        // Add the converted one.
                        finalKeys.add(ImmutableMap.of("message", convertedKey, "translation", convertedValue));
                    }
                }

            }

            configDisk.set("messages", finalKeys);

            try {
                configDisk.save(configFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save translations file.", e);
            }
        } else {
            try {
                configJar.save(configFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save initial translations file.", e);
            }
        }

        YamlConfiguration configFileYaml = YamlConfiguration.loadConfiguration(configFile);
        configFileYaml.getMapList("messages").stream().filter(map -> map.containsKey("translation")).forEach(map -> {
            translations.put(map.get("message").toString(), map.get("translation").toString());
        });
    }

    private String convertOldTranslations(String input) {
        Matcher m = Pattern.compile("(%s)").matcher(input);
        StringBuffer converted = new StringBuffer();
        int index = 1;

        while (m.find()) {
            m.appendReplacement(converted, String.format("{%d}", index));
            index++;
        }

        m.appendTail(converted);

        return converted.toString();
    }

    private String translate(String input) {
        return translations.containsKey(input) ? ChatColor.translateAlternateColorCodes('&', translations.get(input)) : input;
    }

    public static String _(String s) {
        return INSTANCE.translate(s);
    }

    public static I18n getInstance() {
        return INSTANCE;
    }
}
