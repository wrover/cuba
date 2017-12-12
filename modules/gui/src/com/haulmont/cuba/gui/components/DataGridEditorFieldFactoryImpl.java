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

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.chile.core.model.Range;
import com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesUtils;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import org.apache.commons.collections4.MapUtils;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

@org.springframework.stereotype.Component(DataGridEditorFieldFactory.NAME)
public class DataGridEditorFieldFactoryImpl implements DataGridEditorFieldFactory {

    @Inject
    protected Messages messages;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Override
    public Field createField(Datasource datasource, String property) {
        return createFieldComponent(datasource, property);
    }

    protected Field createFieldComponent(Datasource datasource, String property) {
        MetaClass metaClass = datasource.getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, property);


        MetaContext context = new MetaContext(metaClass, property, datasource);

        // TODO: gg, choose factory
        Map<String, MetaComponentFactory> factories = AppBeans.getAll(MetaComponentFactory.class);
        factories.remove(MetaComponentFactory.NAME);

        if (MapUtils.isNotEmpty(factories)) {
            // TODO: gg, implement
            throw new UnsupportedOperationException();
        } else {
            MetaComponentFactory factory = AppBeans.get(MetaComponentFactory.NAME);

            Component component = factory.createComponent(context);
            if (component != null && component instanceof Field) {
                Field field = ((Field) component);
                field.setDatasource(datasource, property);

                if (mpp != null) {
                    Range mppRange = mpp.getRange();
                    if (mppRange.isClass()) {
                        MetaProperty metaProperty = mpp.getMetaProperty();
                        Class<?> javaType = metaProperty.getJavaType();
                        if (!FileDescriptor.class.isAssignableFrom(javaType)
                                && !Collection.class.isAssignableFrom(javaType)) {
                            setEntityFieldAttributes(field);
                        }
                    }
                }

                return field;
            }
            // TODO: gg, more detailed message
            throw new UnsupportedOperationException();
        }

        /*String exceptionMessage;
        if (mpp != null) {
            String name = mpp.getRange().isDatatype()
                    ? mpp.getRange().asDatatype().toString()
                    : mpp.getRange().asClass().getName();
            exceptionMessage = String.format("Can't create field \"%s\" with data type: %s", property, name);
        } else {
            exceptionMessage = String.format("Can't create field \"%s\" with given data type", property);
        }
        throw new UnsupportedOperationException(exceptionMessage);*/
    }

    protected void setEntityFieldAttributes(Field field) {
        if (field instanceof PickerField) {
            PickerField.LookupAction lookupAction = ((PickerField) field).getLookupAction();
            if (lookupAction != null) {
                // Opening lookup screen in another mode will close editor
                lookupAction.setLookupScreenOpenType(WindowManager.OpenType.DIALOG);
                // In case of adding special logic for lookup screen opened from DataGrid editor
                lookupAction.setLookupScreenParams(ParamsMap.of("dataGridEditor", true));
            }
        }
    }

    protected MetaPropertyPath resolveMetaPropertyPath(MetaClass metaClass, String property) {
        MetaPropertyPath mpp = metaClass.getPropertyPath(property);

        if (mpp == null && DynamicAttributesUtils.isDynamicAttribute(property)) {
            mpp = DynamicAttributesUtils.getMetaPropertyPath(metaClass, property);
        }

        return mpp;
    }
}
