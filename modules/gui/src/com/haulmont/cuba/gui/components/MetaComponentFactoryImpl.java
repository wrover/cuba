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
import com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesMetaProperty;
import com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesUtils;
import com.haulmont.cuba.core.app.dynamicattributes.PropertyType;
import com.haulmont.cuba.core.entity.CategoryAttribute;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.entity.annotation.CurrencyValue;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.dynamicattributes.DynamicAttributesGuiTools;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.sql.Time;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@org.springframework.stereotype.Component(MetaComponentFactory.NAME)
public class MetaComponentFactoryImpl implements MetaComponentFactory {

    @Inject
    protected Messages messages;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Nullable
    @Override
    public Component createComponent(MetaContext context) {
        String property = context.getProperty();
        MetaPropertyPath mpp = resolveMetaPropertyPath(context.getMetaClass(), property);

        if (mpp != null) {
            Range mppRange = mpp.getRange();
            if (mppRange.isDatatype()) {
                Class type = mppRange.asDatatype().getJavaClass();

                MetaProperty metaProperty = mpp.getMetaProperty();
                if (DynamicAttributesUtils.isDynamicAttribute(metaProperty)) {
                    CategoryAttribute categoryAttribute = DynamicAttributesUtils.getCategoryAttribute(metaProperty);
                    if (categoryAttribute != null && categoryAttribute.getDataType() == PropertyType.ENUMERATION) {
                        return createEnumField();
                    }
                }

                if (type.equals(String.class)) {
                    return createStringField();
                } else if (type.equals(UUID.class)) {
                    return createUuidField();
                } else if (type.equals(Boolean.class)) {
                    return createBooleanField();
                } else if (type.equals(java.sql.Date.class) || type.equals(Date.class)) {
                    return createDateField();
                } else if (type.equals(Time.class)) {
                    return createTimeField();
                } else if (Number.class.isAssignableFrom(type)) {
                    return createNumberField(mpp);
                }
            } else if (mppRange.isClass()) {
                MetaProperty metaProperty = mpp.getMetaProperty();
                Class<?> javaType = metaProperty.getJavaType();
                if (FileDescriptor.class.isAssignableFrom(javaType)) {
                    return createFileUploadField();
                }
                if (!Collection.class.isAssignableFrom(javaType)) {
                    return createEntityField(mpp);
                }
            } else if (mppRange.isEnum()) {
                return createEnumField();
            }
        }

        return null;
    }

    protected MetaPropertyPath resolveMetaPropertyPath(MetaClass metaClass, String property) {
        MetaPropertyPath mpp = metaClass.getPropertyPath(property);

        if (mpp == null && DynamicAttributesUtils.isDynamicAttribute(property)) {
            mpp = DynamicAttributesUtils.getMetaPropertyPath(metaClass, property);
        }

        return mpp;
    }

    protected Field createEnumField() {
        return componentsFactory.createComponent(LookupField.class);
    }

    protected Field createStringField() {
        return componentsFactory.createComponent(TextField.class);
    }

    protected Field createUuidField() {
        MaskedField maskedField = componentsFactory.createComponent(MaskedField.class);
        maskedField.setMask("hhhhhhhh-hhhh-hhhh-hhhh-hhhhhhhhhhhh");
        maskedField.setSendNullRepresentation(false);
        return maskedField;
    }

    protected Field createBooleanField() {
        return componentsFactory.createComponent(CheckBox.class);
    }

    protected Field createDateField() {
        return componentsFactory.createComponent(DateField.class);
    }

    protected Field createTimeField() {
        return componentsFactory.createComponent(TimeField.class);
    }

    protected Field createNumberField(MetaPropertyPath mpp) {
        Field currencyField = createCurrencyField(mpp);
        if (currencyField != null) {
            return currencyField;
        }

        return componentsFactory.createComponent(TextField.class);
    }

    @Nullable
    protected Field createCurrencyField(MetaPropertyPath mpp) {
        if (DynamicAttributesUtils.isDynamicAttribute(mpp.getMetaProperty()))
            return null;

        // TODO: gg, test with double
        Object currencyAnnotation = mpp.getMetaProperty().getAnnotations().get(CurrencyValue.class.getName());
        if (currencyAnnotation == null) {
            return null;
        }

        return componentsFactory.createComponent(CurrencyField.class);
    }

    protected Field createFileUploadField() {
        FileUploadField fileUploadField = (FileUploadField) componentsFactory.createComponent(FileUploadField.NAME);
        fileUploadField.setMode(FileUploadField.FileStoragePutMode.IMMEDIATE);

        fileUploadField.setUploadButtonCaption(null);
        fileUploadField.setUploadButtonDescription(messages.getMainMessage("upload.submit"));
        fileUploadField.setUploadButtonIcon("icons/upload.png");

        fileUploadField.setClearButtonCaption(null);
        fileUploadField.setClearButtonDescription(messages.getMainMessage("upload.clear"));
        fileUploadField.setClearButtonIcon("icons/remove.png");

        fileUploadField.setShowFileName(true);
        fileUploadField.setShowClearButton(true);

        return fileUploadField;
    }

    protected Field createEntityField(MetaPropertyPath mpp) {
        CollectionDatasource optionsDatasource = null;

        if (DynamicAttributesUtils.isDynamicAttribute(mpp.getMetaProperty())) {
            DynamicAttributesMetaProperty metaProperty = (DynamicAttributesMetaProperty) mpp.getMetaProperty();
            CategoryAttribute attribute = metaProperty.getAttribute();
            if (Boolean.TRUE.equals(attribute.getLookup())) {
                DynamicAttributesGuiTools dynamicAttributesGuiTools = AppBeans.get(DynamicAttributesGuiTools.class);
                optionsDatasource = dynamicAttributesGuiTools.createOptionsDatasourceForLookup(metaProperty.getRange()
                        .asClass(), attribute.getJoinClause(), attribute.getWhereClause());
            }
        }

        PickerField pickerField;
        if (optionsDatasource == null) {
            pickerField = componentsFactory.createComponent(PickerField.class);
//            pickerField.setDatasource(datasource, property);
            pickerField.addLookupAction();
            if (DynamicAttributesUtils.isDynamicAttribute(mpp.getMetaProperty())) {
                DynamicAttributesGuiTools dynamicAttributesGuiTools = AppBeans.get(DynamicAttributesGuiTools.class);
                DynamicAttributesMetaProperty dynamicAttributesMetaProperty = (DynamicAttributesMetaProperty) mpp.getMetaProperty();
                dynamicAttributesGuiTools.initEntityPickerField(pickerField, dynamicAttributesMetaProperty.getAttribute());
            }
            PickerField.LookupAction lookupAction =
                    (PickerField.LookupAction) pickerField.getActionNN(PickerField.LookupAction.NAME);
            // Opening lookup screen in another mode will close editor
            lookupAction.setLookupScreenOpenType(WindowManager.OpenType.DIALOG);
            // In case of adding special logic for lookup screen opened from DataGrid editor
            lookupAction.setLookupScreenParams(ParamsMap.of("dataGridEditor", true));
            boolean actionsByMetaAnnotations = ComponentsHelper.createActionsByMetaAnnotations(pickerField);
            if (!actionsByMetaAnnotations) {
                pickerField.addClearAction();
            }
        } else {
            LookupPickerField lookupPickerField = componentsFactory.createComponent(LookupPickerField.class);
//            lookupPickerField.setDatasource(datasource, property);
            lookupPickerField.setOptionsDatasource(optionsDatasource);

            pickerField = lookupPickerField;

            ComponentsHelper.createActionsByMetaAnnotations(pickerField);
        }

        return pickerField;
    }
}
