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

public interface MetaComponentFactory {
    String NAME = "cuba_MetaComponentFactory";

    /**
     * Creates a component according to the given {@link MetaContext}.
     *
     * @param context the {@link MetaContext} instance
     * @return a component according to the given {@link MetaContext}
     * @throws IllegalArgumentException if no component can be created for a given meta context
     */
    Component createComponent(MetaContext context);
}
