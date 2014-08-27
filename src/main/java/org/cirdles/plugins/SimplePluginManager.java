/*
 * Copyright 2014 CIRDLES.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.plugins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author johnzeringue
 * @param <T>
 */
public abstract class SimplePluginManager<T extends Plugin> implements PluginManager<T> {

    private final Collection<T> plugins = new ArrayList<>();

    private final Path pluginDirectory;

    public SimplePluginManager(Path pluginDirectory) {
        if (!Files.isDirectory(pluginDirectory)) {
            throw new IllegalArgumentException("Specified path must be a directory.");
        }

        this.pluginDirectory = pluginDirectory;
    }

    @Override
    public Collection<T> getPlugins() {
        return Collections.unmodifiableCollection(plugins);
    }

    protected abstract Optional<T> loadPlugin(Path path);

    @Override
    public Collection<T> loadPlugins() {
        plugins.clear();

        try {
            Files.list(pluginDirectory)
                    .filter(path -> Files.isDirectory(path)) // ignore files
                    .forEach(path -> {
                        loadPlugin(path).ifPresent(plugin -> plugins.add(plugin));
                    });
        } catch (IOException ex) {
            Logger.getLogger(SimplePluginManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Collections.unmodifiableCollection(plugins);
    }

}
