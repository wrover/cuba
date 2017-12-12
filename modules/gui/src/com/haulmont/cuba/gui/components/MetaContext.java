/*
 * Copyright (c) 2008-2017 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.gui.components;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.gui.data.Datasource;

import java.util.Map;

public class MetaContext {
    protected MetaClass metaClass;
    protected String property;
    protected Datasource datasource;
    protected Map<String, Object> parameters;

    public MetaContext(MetaClass metaClass, String property) {
        this(metaClass, property, null, null);
    }

    public MetaContext(MetaClass metaClass, String property, Datasource datasource) {
        this(metaClass, property, datasource, null);
    }

    public MetaContext(MetaClass metaClass, String property,
                       Datasource datasource, Map<String, Object> parameters) {
        this.metaClass = metaClass;
        this.property = property;
        this.datasource = datasource;
        this.parameters = parameters;
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    public String getProperty() {
        return property;
    }

    public Datasource getDatasource() {
        return datasource;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
