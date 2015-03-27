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
package org.cirdles.topsoil.data;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;

/**
 *
 * @author CIRDLES
 */
public class Entry {

    private final Map<Field, Object> fieldValues;
    private final BooleanProperty selected = new BooleanPropertyBase(false) {
        
        @Override
        public Object getBean() {
            return Entry.this;
        }
        
        @Override
        public String getName() {
            return "selected";
        }
        
    };

    public Entry() {
        fieldValues = new TreeMap<>();
    }
    
    public Set<Field> getFields() {
        return fieldValues.keySet();
    }
    
    public boolean getSelected() {
        return selected.get();
    }

    public <T> T getValue(Field<T> field) {
        return (T) fieldValues.get(field);
    }

    public <T> Optional<T> getOptionalValue(Field<T> field) {
        return Optional.ofNullable((T) fieldValues.get(field));
    }
    
    public BooleanProperty selectedProperty() {
        return selected;
    }
    
    public void setSelected(boolean value) {
        selected.set(value);
    }

    public <T> void setValue(Field<T> field, T value) {
        fieldValues.put(field, value);
    }
    
}
