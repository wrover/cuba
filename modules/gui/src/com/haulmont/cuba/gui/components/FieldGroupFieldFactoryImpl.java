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

import com.google.common.base.Strings;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.chile.core.model.Range;
import com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesMetaProperty;
import com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesUtils;
import com.haulmont.cuba.core.app.dynamicattributes.PropertyType;
import com.haulmont.cuba.core.entity.CategoryAttribute;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.RuntimePropsDatasource;
import com.haulmont.cuba.gui.dynamicattributes.DynamicAttributesGuiTools;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import javax.inject.Inject;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component(FieldGroupFieldFactory.NAME)
public class FieldGroupFieldFactoryImpl implements FieldGroupFieldFactory {
    @Inject
    protected Messages messages;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Override
    public GeneratedField createField(FieldGroup.FieldConfig fc) {
        return createFieldComponent(fc);
    }

    protected GeneratedField createFieldComponent(FieldGroup.FieldConfig fc) {
        // Step 0. Making preparation
        Datasource targetDs = fc.getTargetDatasource();
        String property = fc.getProperty();
        MetaClass metaClass = targetDs.getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, fc.getProperty());

        MetaContext context = new MetaContext(metaClass, property, targetDs,
                ParamsMap.of("xmlDescriptor", fc.getXmlDescriptor()));

        // Step 1. Trying to find a custom factory
        Map<String, MetaComponentFactory> factoryMap = AppBeans.getAll(MetaComponentFactory.class);
        factoryMap.remove(MetaComponentFactoryImpl.NAME);

        if (MapUtils.isNotEmpty(factoryMap)) {
            List<MetaComponentFactory> availableFactories = new ArrayList<>(factoryMap.values());

            AnnotationAwareOrderComparator.sort(availableFactories);

            for (MetaComponentFactory factory : availableFactories) {
                Component component = factory.createComponent(context);
                if (component != null) {
                    return new GeneratedField(component);
                }
            }
        }

        // Step 2. Check if we need to create a specific field
        if (mpp != null) {
            Range mppRange = mpp.getRange();
            if (mppRange.isDatatype()) {
                Datatype datatype = mppRange.asDatatype();

                if (fc.getXmlDescriptor() != null
                        && "true".equalsIgnoreCase(fc.getXmlDescriptor().attributeValue("link"))) {
                    return createDatatypeLinkField(fc);
                } else if (datatype.getJavaClass().equals(String.class)) {
                    if (fc.getXmlDescriptor() != null
                            && fc.getXmlDescriptor().attribute("mask") != null) {
                        return createMaskedField(fc);
                    } else {
                        return createStringField(fc);
                    }
                } else if ((datatype.getJavaClass().equals(java.sql.Date.class))
                        || (datatype.getJavaClass().equals(Date.class))) {
                    return createDateField(fc);
                } else if (datatype.getJavaClass().equals(Time.class)) {
                    return createTimeField(fc);
                } else if (Number.class.isAssignableFrom(datatype.getJavaClass())
                        && fc.getXmlDescriptor() != null
                        && fc.getXmlDescriptor().attribute("mask") != null) {
                    GeneratedField generatedField = createMaskedField(fc);
                    MaskedField maskedField = (MaskedField) generatedField.getComponent();
                    maskedField.setValueMode(MaskedField.ValueMode.MASKED);
                    maskedField.setSendNullRepresentation(false);
                    return new GeneratedField(maskedField);
                }
            } else if (mppRange.isClass()) {
                MetaProperty metaProperty = mpp.getMetaProperty();
                Class<?> javaType = metaProperty.getJavaType();
                if (!FileDescriptor.class.isAssignableFrom(javaType)
                        && !Collection.class.isAssignableFrom(javaType)) {
                    return createEntityField(fc);
                }
            }
        }

        // Step 3. Create a default field
        MetaComponentFactory factory = AppBeans.get(MetaComponentFactoryImpl.NAME);
        Component component = factory.createComponent(context);
        if (component != null) {
            return new GeneratedField(component);
        }

        // Step 4. No component created, throw the exception
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

    protected GeneratedField createMaskedField(FieldGroup.FieldConfig fc) {
        MaskedField maskedField = componentsFactory.createComponent(MaskedField.class);
        maskedField.setDatasource(fc.getTargetDatasource(), fc.getProperty());
        if (fc.getXmlDescriptor() != null) {
            maskedField.setMask(fc.getXmlDescriptor().attributeValue("mask"));

            String valueModeStr = fc.getXmlDescriptor().attributeValue("valueMode");
            if (StringUtils.isNotEmpty(valueModeStr)) {
                maskedField.setValueMode(MaskedField.ValueMode.valueOf(valueModeStr.toUpperCase()));
            }
        }
        return new GeneratedField(maskedField);
    }

    protected GeneratedField createStringField(FieldGroup.FieldConfig fc) {
        TextInputField textField = null;

        if (fc.getXmlDescriptor() != null) {
            final String rows = fc.getXmlDescriptor().attributeValue("rows");
            if (!StringUtils.isEmpty(rows)) {
                TextArea textArea = componentsFactory.createComponent(TextArea.class);
                textArea.setRows(Integer.parseInt(rows));
                textField = textArea;
            }
        }
        if (DynamicAttributesUtils.isDynamicAttribute(fc.getProperty())) {
            MetaClass metaClass = fc.getTargetDatasource() instanceof RuntimePropsDatasource ?
                    ((RuntimePropsDatasource) fc.getTargetDatasource()).resolveCategorizedEntityClass() : fc.getTargetDatasource().getMetaClass();
            MetaPropertyPath mpp = DynamicAttributesUtils.getMetaPropertyPath(metaClass, fc.getProperty());
            if (mpp != null) {
                CategoryAttribute categoryAttribute = DynamicAttributesUtils.getCategoryAttribute(mpp.getMetaProperty());
                if (categoryAttribute != null && categoryAttribute.getDataType() == PropertyType.STRING
                        && categoryAttribute.getRowsCount() != null && categoryAttribute.getRowsCount() > 1) {
                    TextArea textArea = componentsFactory.createComponent(TextArea.class);
                    textArea.setRows(categoryAttribute.getRowsCount());
                    textField = textArea;
                }
            }
        }

        if (textField == null) {
            textField = componentsFactory.createComponent(TextField.class);
        }

        textField.setDatasource(fc.getTargetDatasource(), fc.getProperty());

        String maxLength = fc.getXmlDescriptor() != null ? fc.getXmlDescriptor().attributeValue("maxLength") : null;
        if (!Strings.isNullOrEmpty(maxLength)) {
            ((TextInputField.MaxLengthLimited) textField).setMaxLength(Integer.parseInt(maxLength));
        }

        return new GeneratedField(textField);
    }

    protected GeneratedField createDatatypeLinkField(FieldGroup.FieldConfig fc) {
        EntityLinkField linkField = componentsFactory.createComponent(EntityLinkField.class);

        linkField.setDatasource(fc.getTargetDatasource(), fc.getProperty());

        setLinkFieldAttributes(fc, linkField);

        return new GeneratedField(linkField);
    }

    protected GeneratedField createDateField(FieldGroup.FieldConfig fc) {
        DateField dateField = componentsFactory.createComponent(DateField.class);
        dateField.setDatasource(fc.getTargetDatasource(), fc.getProperty());

        Element xmlDescriptor = fc.getXmlDescriptor();

        String resolution = xmlDescriptor == null ? null : xmlDescriptor.attributeValue("resolution");
        String dateFormat = xmlDescriptor == null ? null : xmlDescriptor.attributeValue("dateFormat");

        DateField.Resolution dateResolution = DateField.Resolution.MIN;

        if (StringUtils.isNotEmpty(resolution)) {
            dateResolution = DateField.Resolution.valueOf(resolution);
            dateField.setResolution(dateResolution);
        }

        if (dateFormat == null) {
            if (dateResolution == DateField.Resolution.DAY) {
                dateFormat = "msg://dateFormat";
            } else if (dateResolution == DateField.Resolution.MIN) {
                dateFormat = "msg://dateTimeFormat";
            }
        }

        if (StringUtils.isNotEmpty(dateFormat)) {
            if (dateFormat.startsWith("msg://")) {
                dateFormat = messages.getMainMessage(dateFormat.substring(6, dateFormat.length()));
            }
            dateField.setDateFormat(dateFormat);
        }

        return new GeneratedField(dateField);
    }

    protected GeneratedField createTimeField(FieldGroup.FieldConfig fc) {
        TimeField timeField = componentsFactory.createComponent(TimeField.class);
        timeField.setDatasource(fc.getTargetDatasource(), fc.getProperty());

        if (fc.getXmlDescriptor() != null) {
            String showSeconds = fc.getXmlDescriptor().attributeValue("showSeconds");
            if (Boolean.parseBoolean(showSeconds)) {
                timeField.setShowSeconds(true);
            }
        }
        return new GeneratedField(timeField);
    }

    protected GeneratedField createEntityField(FieldGroup.FieldConfig fc) {
        MetaClass metaClass = fc.getTargetDatasource().getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, fc.getProperty());

        String linkAttribute = null;
        if (fc.getXmlDescriptor() != null) {
            linkAttribute = fc.getXmlDescriptor().attributeValue("link");
        }

        if (!Boolean.parseBoolean(linkAttribute)) {
            CollectionDatasource optionsDatasource = fc.getOptionsDatasource();

            if (DynamicAttributesUtils.isDynamicAttribute(mpp.getMetaProperty())) {
                DynamicAttributesMetaProperty metaProperty = (DynamicAttributesMetaProperty) mpp.getMetaProperty();
                CategoryAttribute attribute = metaProperty.getAttribute();
                if (Boolean.TRUE.equals(attribute.getLookup())) {
                    DynamicAttributesGuiTools dynamicAttributesGuiTools = AppBeans.get(DynamicAttributesGuiTools.class);
                    optionsDatasource = dynamicAttributesGuiTools.createOptionsDatasourceForLookup(metaProperty.getRange().asClass(),
                            attribute.getJoinClause(), attribute.getWhereClause());
                }
            }

            PickerField pickerField;
            if (optionsDatasource == null) {
                pickerField = componentsFactory.createComponent(PickerField.class);

                pickerField.setDatasource(fc.getTargetDatasource(), fc.getProperty());
                if (mpp.getMetaProperty().getType() == MetaProperty.Type.ASSOCIATION) {
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
                    pickerField.addOpenAction();
                    pickerField.addClearAction();
                }
            } else {
                LookupPickerField lookupPickerField = componentsFactory.createComponent(LookupPickerField.class);

                lookupPickerField.setDatasource(fc.getTargetDatasource(), fc.getProperty());
                lookupPickerField.setOptionsDatasource(optionsDatasource);

                pickerField = lookupPickerField;

                ComponentsHelper.createActionsByMetaAnnotations(pickerField);
            }

            if (fc.getXmlDescriptor() != null) {
                String captionProperty = fc.getXmlDescriptor().attributeValue("captionProperty");
                if (StringUtils.isNotEmpty(captionProperty)) {
                    pickerField.setCaptionMode(CaptionMode.PROPERTY);
                    pickerField.setCaptionProperty(captionProperty);
                }
            }

            return new GeneratedField(pickerField);
        } else {
            EntityLinkField linkField = componentsFactory.createComponent(EntityLinkField.class);

            linkField.setDatasource(fc.getTargetDatasource(), fc.getProperty());

            setLinkFieldAttributes(fc, linkField);

            return new GeneratedField(linkField);
        }
    }

    protected void setLinkFieldAttributes(FieldGroup.FieldConfig fc, EntityLinkField linkField) {
        if (fc.getXmlDescriptor() != null) {
            String linkScreen = fc.getXmlDescriptor().attributeValue("linkScreen");
            if (StringUtils.isNotEmpty(linkScreen)) {
                linkField.setScreen(linkScreen);
            }

            final String invokeMethodName = fc.getXmlDescriptor().attributeValue("linkInvoke");
            if (StringUtils.isNotEmpty(invokeMethodName)) {
                linkField.setCustomClickHandler(new AbstractFieldFactory.InvokeEntityLinkClickHandler(invokeMethodName));
            }

            String openTypeAttribute = fc.getXmlDescriptor().attributeValue("linkScreenOpenType");
            if (StringUtils.isNotEmpty(openTypeAttribute)) {
                WindowManager.OpenType openType = WindowManager.OpenType.valueOf(openTypeAttribute);
                linkField.setScreenOpenType(openType);
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