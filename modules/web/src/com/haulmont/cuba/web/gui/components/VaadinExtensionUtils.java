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

import org.apache.commons.lang.reflect.FieldUtils;

// Vaadin Fork extension points
public final class VaadinExtensionUtils {

    private VaadinExtensionUtils() {
    }

    // vaadin8 replace with public access
    @SuppressWarnings("unchecked")
    public static <T> T getPrivateField(Object instance, String fieldName) {
        try {
            return (T) FieldUtils.readDeclaredField(instance, fieldName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to read private field", e);
        }
    }

    // vaadin8 replace with public access
    @SuppressWarnings("unchecked")
    public static void setPrivateField(Object instance, String fieldName, Object value) {
        try {
            FieldUtils.writeDeclaredField(instance, fieldName, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to read private field", e);
        }
    }
}