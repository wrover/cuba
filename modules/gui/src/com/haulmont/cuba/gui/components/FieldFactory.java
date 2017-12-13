/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.data.Datasource;
import org.dom4j.Element;

public interface FieldFactory {

    /**
     * Creates a component for {@link Table}.
     * <p>
     * Creation sequence:
     * <ol>
     * <li>Trying to find custom factories. If at least one factory exists, except the default, then:
     * <ol style="list-style-type: lower-alpha;">
     * <li>Iterate over factories according to the {@link org.springframework.core.Ordered} interface.</li>
     * <li>The first not null component will be returned.</li>
     * </ol>
     * </li>
     * <li>If either there are no custom factories or none&nbsp;of them returned a component, check if we need to create a specific component.</li>
     * <li>If no specific&nbsp;component was created, create a component using the default factory.</li>
     * <li>Throw an UnsupportedOperationException if no component was created</li>
     * </ol>
     *
     * @param datasource    a datasource
     * @param property      a property
     * @param xmlDescriptor an xml descriptor
     * @return created component
     * @throws UnsupportedOperationException if field cannot be created
     */
    Component createField(Datasource datasource, String property, Element xmlDescriptor);
}