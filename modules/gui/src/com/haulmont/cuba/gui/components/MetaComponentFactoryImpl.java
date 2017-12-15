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

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

@org.springframework.stereotype.Component(MetaComponentFactory.NAME)
public class MetaComponentFactoryImpl implements MetaComponentFactory {

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
