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
import com.haulmont.cuba.gui.components.CustomComponent;

public class WebCustomComponent extends WebAbstractComponent<com.vaadin.ui.CustomComponent> implements CustomComponent {

    protected Component compositionRoot;

    public WebCustomComponent() {
        component = new WebCustomComponentImpl();
    }

    @Override
    public void setCompositionRoot(Component component) {
        compositionRoot = component;
        ((WebCustomComponentImpl) this.component).setComposition(component);
    }

    @Override
    public Component getCompositionRoot() {
        return compositionRoot;
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

    protected class WebCustomComponentImpl extends com.vaadin.ui.CustomComponent {

        public WebCustomComponentImpl() {
        }

        public void setComposition(Component component) {
            setCompositionRoot(component.unwrap(com.vaadin.ui.Component.class));
        }
    }
}