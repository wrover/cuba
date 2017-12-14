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
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.dynamicattributes.DynamicAttributesGuiTools;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
        // Step 1. Trying to find custom factories
        // TODO: gg,
        Map<String, MetaComponentStrategy> strategyMap = AppBeans.getAll(MetaComponentStrategy.class);
        if (MapUtils.isNotEmpty(strategyMap)) {
            List<MetaComponentStrategy> strategies = new ArrayList<>(strategyMap.values());

            AnnotationAwareOrderComparator.sort(strategies);

            for (MetaComponentStrategy strategy : strategies) {
                Component component = strategy.createComponent(context);
                if (component != null) {
                    return component;
                }
            }
        }

        // Step 2. Create a default field
        String property = context.getProperty();
        MetaPropertyPath mpp = resolveMetaPropertyPath(context.getMetaClass(), property);

        Component component = createComponentInternal(context, mpp);
        if (component != null) {
            return component;
        }

        // Step 3. No component created, throw the exception
        String exceptionMessage;
        if (mpp != null) {
            String name = mpp.getRange().isDatatype()
                    ? mpp.getRange().asDatatype().toString()
                    : mpp.getRange().asClass().getName();
            exceptionMessage = String.format("Can't create field \"%s\" with data type: %s", property, name);
        } else {
            exceptionMessage = String.format("Can't create field \"%s\" with given data type", property);
        }
        throw new UnsupportedOperationException(exceptionMessage);
    }

    @Nullable
    protected Component createComponentInternal(MetaContext context, MetaPropertyPath mpp) {
        if (mpp != null) {
            Range mppRange = mpp.getRange();
            if (mppRange.isDatatype()) {
                Class type = mppRange.asDatatype().getJavaClass();

                MetaProperty metaProperty = mpp.getMetaProperty();
                if (DynamicAttributesUtils.isDynamicAttribute(metaProperty)) {
                    CategoryAttribute categoryAttribute = DynamicAttributesUtils.getCategoryAttribute(metaProperty);
                    if (categoryAttribute != null && categoryAttribute.getDataType() == PropertyType.ENUMERATION) {
                        return createEnumField(context);
                    }
                }

                if (type.equals(String.class)) {
                    return createStringField(context);
                } else if (type.equals(UUID.class)) {
                    return createUuidField(context);
                } else if (type.equals(Boolean.class)) {
                    return createBooleanField(context);
                } else if (type.equals(java.sql.Date.class) || type.equals(Date.class)) {
                    return createDateField(context);
                } else if (type.equals(Time.class)) {
                    return createTimeField(context);
                } else if (Number.class.isAssignableFrom(type)) {
                    Field currencyField = createCurrencyField(context, mpp);
                    if (currencyField != null) {
                        return currencyField;
                    }

                    return createNumberField(context);
                }
            } else if (mppRange.isClass()) {
                MetaProperty metaProperty = mpp.getMetaProperty();
                Class<?> javaType = metaProperty.getJavaType();
                if (FileDescriptor.class.isAssignableFrom(javaType)) {
                    return createFileUploadField(context);
                }
                if (!Collection.class.isAssignableFrom(javaType)) {
                    return createEntityField(context, mpp);
                }
            } else if (mppRange.isEnum()) {
                return createEnumField(context);
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

    protected void setDatasource(Field field, MetaContext context) {
        if (context.getDatasource() != null && StringUtils.isNotEmpty(context.getProperty())) {
            field.setDatasource(context.getDatasource(), context.getProperty());
        }
    }

    protected Field createEnumField(MetaContext context) {
        LookupField component = componentsFactory.createComponent(LookupField.class);
        setDatasource(component, context);
        return component;
    }

    protected Field createStringField(MetaContext context) {
        TextField component = componentsFactory.createComponent(TextField.class);
        setDatasource(component, context);
        return component;
    }

    protected Field createUuidField(MetaContext context) {
        MaskedField maskedField = componentsFactory.createComponent(MaskedField.class);
        setDatasource(maskedField, context);
        maskedField.setMask("hhhhhhhh-hhhh-hhhh-hhhh-hhhhhhhhhhhh");
        maskedField.setSendNullRepresentation(false);
        return maskedField;
    }

    protected Field createBooleanField(MetaContext context) {
        CheckBox component = componentsFactory.createComponent(CheckBox.class);
        setDatasource(component, context);
        return component;
    }

    protected Field createDateField(MetaContext context) {
        DateField component = componentsFactory.createComponent(DateField.class);
        setDatasource(component, context);
        return component;
    }

    protected Field createTimeField(MetaContext context) {
        TimeField component = componentsFactory.createComponent(TimeField.class);
        setDatasource(component, context);
        return component;
    }

    protected Field createNumberField(MetaContext context) {
        TextField component = componentsFactory.createComponent(TextField.class);
        setDatasource(component, context);
        return component;
    }

    @Nullable
    protected Field createCurrencyField(MetaContext context, MetaPropertyPath mpp) {
        if (DynamicAttributesUtils.isDynamicAttribute(mpp.getMetaProperty()))
            return null;

        Object currencyAnnotation = mpp.getMetaProperty().getAnnotations().get(CurrencyValue.class.getName());
        if (currencyAnnotation == null) {
            return null;
        }

        CurrencyField component = componentsFactory.createComponent(CurrencyField.class);
        setDatasource(component, context);
        return component;
    }

    protected Field createFileUploadField(MetaContext context) {
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

        setDatasource(fileUploadField, context);

        return fileUploadField;
    }

    protected Field createEntityField(MetaContext context, MetaPropertyPath mpp) {
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
            setDatasource(pickerField, context);
            pickerField.addLookupAction();
            if (DynamicAttributesUtils.isDynamicAttribute(mpp.getMetaProperty())) {
                DynamicAttributesGuiTools dynamicAttributesGuiTools = AppBeans.get(DynamicAttributesGuiTools.class);
                DynamicAttributesMetaProperty dynamicAttributesMetaProperty = (DynamicAttributesMetaProperty) mpp.getMetaProperty();
                dynamicAttributesGuiTools.initEntityPickerField(pickerField, dynamicAttributesMetaProperty.getAttribute());
            }

            boolean actionsByMetaAnnotations = ComponentsHelper.createActionsByMetaAnnotations(pickerField);
            if (!actionsByMetaAnnotations) {
                pickerField.addClearAction();
            }
        } else {
            LookupPickerField lookupPickerField = componentsFactory.createComponent(LookupPickerField.class);
            setDatasource(lookupPickerField, context);
            lookupPickerField.setOptionsDatasource(optionsDatasource);

            pickerField = lookupPickerField;

            ComponentsHelper.createActionsByMetaAnnotations(pickerField);
        }

        return pickerField;
    }
}
