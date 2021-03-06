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
package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.GuiDevelopmentException;
import com.haulmont.cuba.gui.components.OptionsGroup;
import org.dom4j.Element;

public class OptionsGroupLoader extends AbstractOptionsBaseLoader<OptionsGroup> {
    @Override
    public void loadComponent() {
        super.loadComponent();

        loadOrientation(resultComponent, element);
        loadCaptionProperty(resultComponent, element);

        loadOptionsEnum(resultComponent, element);
    }

    protected void loadOrientation(OptionsGroup component, Element element) {
        String orientation = element.attributeValue("orientation");

        if (orientation == null) {
            return;
        }

        if ("horizontal".equalsIgnoreCase(orientation)) {
            component.setOrientation(OptionsGroup.Orientation.HORIZONTAL);
        } else if ("vertical".equalsIgnoreCase(orientation)) {
            component.setOrientation(OptionsGroup.Orientation.VERTICAL);
        } else {
            throw new GuiDevelopmentException("Invalid orientation value for option group: " +
                    orientation, context.getFullFrameId(), "OptionsGroup ID", component.getId());
        }
    }

    @Override
    public void createComponent() {
        resultComponent = (OptionsGroup) factory.createComponent(OptionsGroup.NAME);
        loadId(resultComponent, element);
    }
}