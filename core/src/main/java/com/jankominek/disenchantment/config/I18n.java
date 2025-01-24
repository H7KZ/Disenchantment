package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.types.ConfigKeys;

import static com.jankominek.disenchantment.Disenchantment.config;

public class I18n {
    public class Messages {
        public static String getPermission() {
            return config.getString(ConfigKeys.I18N_PERMISSION.getKey());
        }
    }
}
