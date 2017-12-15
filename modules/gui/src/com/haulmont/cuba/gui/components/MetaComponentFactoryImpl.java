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
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.chile.core.model.Range;
import com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesMetaProperty;
import com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesUtils;
import com.haulmont.cuba.core.app.dynamicattributes.PropertyType;
import com.haulmont.cuba.core.entity.CategoryAttribute;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.entity.annotation.CurrencyValue;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.dynamicattributes.DynamicAttributesGuiTools;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.sql.Time;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Component(MetaComponentFactory.NAME)
public class MetaComponentFactoryImpl implements MetaComponentFactory {

    @Inject
    protected Messages messages;
    @Inject
    protected ComponentsFactory componentsFactory;
    @Inject
    protected List<MetaComponentStrategy> metaComponentStrategies;

    /**
     * Creates a component according to the given {@link MetaContext}.
     * <p>
     * Creation sequence:
     * <ol>
     * <li>Trying to find custom strategies (). If at least one strategy exists, then:
     * <ol style="list-style-type: lower-alpha;">
     * <li>Iterate over strategies according to the {@link org.springframework.core.Ordered} interface.</li>
     * <li>The first not null component will be returned.</li>
     * </ol>
     * </li>
     * <li>If no component was created, create a component using the default realisation.</li>
     * </ol>
     *
     * @param context the {@link MetaContext} instance
     * @return a component according to the given {@link MetaContext}
     * @throws IllegalArgumentException if no component can be created for a given meta context
     */
    @Nullable
    @Override
    public Component createComponent(MetaContext context) {
        List<MetaComponentStrategy> strategies = getMetaComponentStrategies();

        for (MetaComponentStrategy strategy : strategies) {
            Component component = strategy.createComponent(context);
            if (component != null) {
                return component;
            }
        }

        throw new IllegalArgumentException(String.format("Can't create component for the '%s' with " +
                "given meta class '%s'", context.getProperty(), context.getMetaClass()));
    }

    public List<MetaComponentStrategy> getMetaComponentStrategies() {
        return metaComponentStrategies;
    }
}
