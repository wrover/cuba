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

    protected Component content;
    protected ValueValidator valueValidator;

    public WebCustomField() {
        createComponent();
    }

    protected void createComponent() {
        component = new com.vaadin.ui.CustomField<Object>() {
            @Override
            protected com.vaadin.ui.Component initContent() {
                return content.unwrap(com.vaadin.ui.Component.class);
            }

            @Override
            public Class<?> getType() {
                return Object.class;
            }
        };
    }

    @Override
    public void setContent(Component component) {
        content = component;
    }

    @Override
    public Component getContent() {
        return content;
    }

    @Override
    public void setValueValidator(ValueValidator validator) {
        this.valueValidator = validator;
    }

    @Override
    public ValueValidator getValueValidator() {
        return valueValidator;
    }

    @Override
    public void setValue(Object value) {
        if (valueValidator != null) {
            super.setValue(valueValidator.validateSetValue(value));
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Object getValue() {
        if (valueValidator != null) {
            return valueValidator.validateGetValue(super.getValue());
        }
        return super.getValue();
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);

        if (content != null) {
            if (getWidth() < 0) {
                content.setWidthAuto();
            } else {
                content.setWidthFull();
            }
        }
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);

        if (content != null) {
            if (getHeight() < 0) {
                content.setHeightAuto();
            } else {
                content.setHeightFull();
            }
        }
    }
}