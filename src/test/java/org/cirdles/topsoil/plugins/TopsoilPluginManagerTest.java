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

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author johnzeringue
 */
public class TopsoilPluginManagerTest {

    private final Path pluginPath = Paths.get("target/test-classes/org/cirdles/topsoil/plugins");

    @Test
    public void testGetPlugins() {
        TopsoilPluginManager manager = new TopsoilPluginManager(pluginPath);

        // ensure that plugins are only available after loadPlugins()
        assertTrue(manager.getPlugins().isEmpty());
        manager.loadPlugins();
        assertEquals(1, manager.getPlugins().size());
    }

    @Test
    public void testLoadPlugins() {
        TopsoilPluginManager manager = new TopsoilPluginManager(pluginPath);

        // grab the only plugin there
        assertEquals(1, manager.getPlugins().size());
        TopsoilPlugin plugin = (TopsoilPlugin) manager.loadPlugins().toArray()[0];

        // plugin has two charts, Test #1 and Test #2
        assertEquals(2, plugin.getCharts().size());
        plugin.getCharts().stream().forEach(chart -> {
            chart.getName().ifPresent(name -> {
                if (name.equals("Test #1")) {
                    assertEquals(chart.getCategory().get(), "Test");
                } else if (name.equals("Test #2")) {
                    assertFalse(chart.getCategory().isPresent());
                } else {
                    fail("Unexpected chart name");
                }
            });
        });
    }

}
