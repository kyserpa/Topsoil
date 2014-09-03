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
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author johnzeringue
 */
public class JavaScriptTopsoilChart implements TopsoilChart {

    private static final String JAVASCRIPT_ENGINE_NAME = "nashorn";
    private static final ScriptEngineManager SCRIPT_ENGINE_MANAGER = new ScriptEngineManager();
    
    private final String name;
    private final Optional<String> category;

    public JavaScriptTopsoilChart(Path javascriptFile) throws IOException, ScriptException {
        ScriptEngine javascriptEngine = SCRIPT_ENGINE_MANAGER.getEngineByName(JAVASCRIPT_ENGINE_NAME);
        javascriptEngine.eval(Files.newBufferedReader(javascriptFile));
        
        name = (String) javascriptEngine.eval("chart.name");
        category = Optional.ofNullable((String) javascriptEngine.eval("chart.category"));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<String> getCategory() {
        return category;
    }

}
