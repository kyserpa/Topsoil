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
package org.cirdles.topsoil.plugins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.cirdles.plugins.SimplePluginManager;

/**
 *
 * @author johnzeringue
 */
public class TopsoilPluginManager extends SimplePluginManager<TopsoilPlugin> {

    public TopsoilPluginManager(Path pluginDirectory) {
        super(pluginDirectory);
    }

    @Override
    protected Optional<TopsoilPlugin> loadPlugin(Path path) {
        try {
            final Collection<TopsoilChart> charts = new ArrayList<>();

            // convert JavaScript files into Topsoil charts
            Files.list(path).forEach(subpath -> {
                if (subpath.toString().endsWith(".js")) { // different than subpath.endsWith(".js")
                    try {
                        charts.add(new TopsoilJavaScriptChart(subpath));
                    } catch (IOException | ScriptException ex) {
                        Logger.getLogger(TopsoilPluginManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            return Optional.of(() -> Collections.unmodifiableCollection(charts));
        } catch (IOException ex) {
            Logger.getLogger(TopsoilPluginManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Optional.empty();
    }

}
