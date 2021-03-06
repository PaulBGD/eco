package com.willfp.eco.util.config;

import com.willfp.eco.internal.config.AbstractUpdatableConfig;
import com.willfp.eco.util.plugin.AbstractEcoPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class ExtendableConfig extends AbstractUpdatableConfig {
    /**
     * Config implementation for configs present in the plugin's base directory (eg config.yml, lang.yml).
     * <p>
     * Automatically updates.
     *
     * @param configName       The name of the config
     * @param removeUnused     Whether keys not present in the default config should be removed on update.
     * @param plugin           The plugin.
     * @param updateBlacklist  Substring of keys to not add/remove keys for.
     * @param subDirectoryPath The subdirectory path.
     * @param source           The class that owns the resource.
     */
    protected ExtendableConfig(@NotNull final String configName,
                               final boolean removeUnused,
                               @NotNull final AbstractEcoPlugin plugin,
                               @NotNull final Class<?> source,
                               @NotNull final String subDirectoryPath,
                               @NotNull final String... updateBlacklist) {
        super(configName, plugin, subDirectoryPath, source, removeUnused, updateBlacklist);
    }
}
