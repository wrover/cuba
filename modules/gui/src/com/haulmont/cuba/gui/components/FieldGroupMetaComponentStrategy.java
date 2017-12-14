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

import org.springframework.core.Ordered;

import javax.annotation.Nullable;

@org.springframework.stereotype.Component(FieldGroupMetaComponentStrategy.NAME)
public class FieldGroupMetaComponentStrategy extends AbstractMetaComponentStrategy implements Ordered {
    public static final String NAME = "cuba_FieldGroupMetaComponentStrategy";

    @Nullable
    @Override
    public Component createComponent(MetaContext context) {
        if (context.getComponentClass() == null
                || !FieldGroup.class.isAssignableFrom(context.getComponentClass())) {
            return null;
        }

        return createComponentInternal(context);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 10;
    }
}
