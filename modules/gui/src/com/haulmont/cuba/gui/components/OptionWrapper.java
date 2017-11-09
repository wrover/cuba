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

import com.haulmont.bali.util.Preconditions;

public class OptionWrapper {
    protected String caption;
    protected Object value;

    public OptionWrapper(String caption, Object value) {
        Preconditions.checkNotNullArgument(caption);
        Preconditions.checkNotNullArgument(value);

        this.caption = caption;
        this.value = value;
    }

    public String getCaption() {
        return caption;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OptionWrapper that = (OptionWrapper) o;

        return caption.equals(that.caption) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = caption.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return caption;
    }
}
