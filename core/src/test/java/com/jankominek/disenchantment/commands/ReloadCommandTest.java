package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.commands.impl.Reload;
import com.jankominek.disenchantment.config.I18n;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReloadCommandTest extends DisenchantmentTestBase {

    @Test
    void givenValidConfig_whenReload_thenLocalizedConfirmationSent() {
        PlayerMock admin = server.addPlayer("Admin");

        Reload.execute(admin, new String[]{"reload"});

        String msg = admin.nextMessage();
        assertNotNull(msg);
        assertEquals(I18n.getPrefix() + " " + I18n.Messages.configReloaded(), msg);
    }

    @Test
    void givenCzechLocale_whenConfigReloadedMessage_thenTranslated() {
        plugin.getConfig().set("locale", "cs");
        com.jankominek.disenchantment.Disenchantment.localeConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                new java.io.InputStreamReader(plugin.getResource("locales/cs.yml"), java.nio.charset.StandardCharsets.UTF_8));

        assertEquals("§aKonfigurace byla znovu načtena.", I18n.Messages.configReloaded());
    }
}
