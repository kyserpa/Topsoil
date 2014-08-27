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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author johnzeringue
 */
public class JavaScriptTopsoilChart implements TopsoilChart {

    private static final ScriptEngineManager SCRIPT_ENGINE_MANAGER = new ScriptEngineManager();

    private final ScriptEngine javaScriptEngine;

    public JavaScriptTopsoilChart(Path javaScriptFile) throws IOException, ScriptException {
        javaScriptEngine = SCRIPT_ENGINE_MANAGER.getEngineByName("nashorn");
        javaScriptEngine.eval(Files.newBufferedReader(javaScriptFile));
    }

    @Override
    public Optional<String> getName() {
        return getStringProperty("chart.name");
    }

    @Override
    public Optional<String> getCategory() {
        return getStringProperty("chart.category");
    }
    
    private Optional<String> getStringProperty(String propertyName) {
        try {
            return Optional.ofNullable((String) javaScriptEngine.eval(propertyName));
        } catch (ScriptException ex) {
            Logger.getLogger(JavaScriptTopsoilChart.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return Optional.empty();
    }

}
