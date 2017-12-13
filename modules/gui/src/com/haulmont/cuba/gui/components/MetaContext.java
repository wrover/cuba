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

import javax.annotation.Nullable;
import java.util.Map;

/**
 * A class that stores information that can be used to create
 * a component in a {@link MetaComponentFactory} implementation.
 */
public class MetaContext {
    public static final String PARAM_XML_DESCRIPTOR = "xmlDescriptor";
    public static final String PARAM_CALLING_CODE = "callingCode";

    protected MetaClass metaClass;
    protected String property;
    protected Datasource datasource;
    protected Map<String, Object> parameters;

    /**
     * Creates an instance of MetaContext.
     *
     * @param metaClass an instance of {@link MetaClass} for which a component should be created
     * @param property  a property of meta class for which a component should be created
     */
    public MetaContext(MetaClass metaClass, String property) {
        this(metaClass, property, null, null);
    }

    /**
     * Creates an instance of MetaContext.
     *
     * @param metaClass  an instance of {@link MetaClass} for which a component should be created
     * @param property   a property of meta class for which a component should be created
     * @param datasource a datasource that can be used to create the component
     */
    public MetaContext(MetaClass metaClass, String property, @Nullable Datasource datasource) {
        this(metaClass, property, datasource, null);
    }

    /**
     * Creates an instance of MetaContext.
     *
     * @param metaClass  an instance of {@link MetaClass} for which a component should be created
     * @param property   a property of meta class for which a component should be created
     * @param parameters additional parameters that can be used to create the component
     */
    public MetaContext(MetaClass metaClass, String property, @Nullable Map<String, Object> parameters) {
        this(metaClass, property, null, parameters);
    }

    /**
     * Creates an instance of MetaContext.
     *
     * @param metaClass  an instance of {@link MetaClass} for which a component should be created
     * @param property   a property of meta class for which a component should be created
     * @param datasource a datasource that can be used to create the component
     * @param parameters additional parameters that can be used to create the component
     */
    public MetaContext(MetaClass metaClass, String property,
                       @Nullable Datasource datasource, @Nullable Map<String, Object> parameters) {
        this.metaClass = metaClass;
        this.property = property;
        this.datasource = datasource;
        this.parameters = parameters;
    }

    /**
     * @return an instance of {@link MetaClass} for which a component should be created
     */
    public MetaClass getMetaClass() {
        return metaClass;
    }

    /**
     * @return a property of meta class for which a component should be created
     */
    public String getProperty() {
        return property;
    }

    /**
     * @return a datasource that can be used to create the component
     */
    @Nullable
    public Datasource getDatasource() {
        return datasource;
    }

    /**
     * @return additional parameters that can be used to create the component
     */
    @Nullable
    public Map<String, Object> getParameters() {
        return parameters;
    }
}
