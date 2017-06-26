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

package com.haulmont.cuba.web.gui.components;


import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.CustomField;

public class WebCustomField extends WebAbstractField<com.vaadin.ui.CustomField<Object>> implements CustomField {

    protected Component compositionRoot;
    protected ValueSource valueSource;

    public WebCustomField() {
        createComponent();
    }

    protected void createComponent() {
        component = new com.vaadin.ui.CustomField<Object>() {
            @Override
            protected com.vaadin.ui.Component initContent() {
                return compositionRoot.unwrap(com.vaadin.ui.Component.class);
            }

            @Override
            public Class<?> getType() {
                return Object.class;
            }
        };
    }

    @Override
    public void setCompositionRoot(Component component) {
        compositionRoot = component;
    }

    @Override
    public Component getCompositionRoot() {
        return compositionRoot;
    }

    @Override
    public void setValueSource(ValueSource validator) {
        this.valueSource = validator;
    }

    @Override
    public ValueSource getValueSource() {
        return valueSource;
    }

    @Override
    public void setValue(Object value) {
        if (valueSource != null) {
            super.setValue(valueSource.setValue(value));
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Object getValue() {
        if (valueSource != null) {
            return valueSource.getValue(super.getValue());
        }
        return super.getValue();
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);

        if (compositionRoot != null) {
            if (getWidth() < 0) {
                compositionRoot.setWidthAuto();
            } else {
                compositionRoot.setWidthFull();
            }
        }
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);

        if (compositionRoot != null) {
            if (getHeight() < 0) {
                compositionRoot.setHeightAuto();
            } else {
                compositionRoot.setHeightFull();
            }
        }
    }
}