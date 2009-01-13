/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 12.01.2009 17:25:38
 *
 * $Id$
 */
package com.haulmont.cuba.core;

import com.haulmont.cuba.core.global.ConfigProvider;
import com.haulmont.cuba.core.config.TestConfig;
import com.haulmont.cuba.core.config.TestPrefixedConfig;

import java.util.UUID;

public class ConfigProviderTest extends CubaTestCase
{
    public void test() {
        TestConfig config = ConfigProvider.getConfig(TestConfig.class);

        String stringProp = config.getStringProp();
        assertNull(stringProp);

        String stringPropDef = config.getStringPropDef();
        assertEquals("def_value", stringPropDef);

        config.setStringProp("test_value");
        stringProp = config.getStringProp();
        assertEquals("test_value", stringProp);

        Integer integerProp = config.getIntegerProp();
        assertNull(integerProp);

        Integer integerPropDef = config.getIntegerPropDef();
        assertEquals(Integer.valueOf(100), integerPropDef);

        Integer integerPropDefRuntime = config.getIntegerPropDefRuntime(200);
        assertEquals(Integer.valueOf(200), integerPropDefRuntime);

        config.setIntegerProp(10);
        integerProp = config.getIntegerProp();
        assertEquals(Integer.valueOf(10), integerProp);

        int intPropDef = config.getIntPropDef();
        assertEquals(0, intPropDef);

        config.setIntPropDef(1);
        intPropDef = config.getIntPropDef();
        assertEquals(1, intPropDef);

        int intPropDefRuntime = config.getIntPropDefRuntime(11);
        assertEquals(11, intPropDefRuntime);

        config.setIntPropDefRuntime(12);
        intPropDefRuntime = config.getIntPropDefRuntime(11);
        assertEquals(12, intPropDefRuntime);

        Boolean booleanProp = config.getBooleanProp();
        assertNull(booleanProp);

        config.setBooleanProp(true);
        booleanProp = config.getBooleanProp();
        assertTrue(booleanProp);

        Boolean booleanPropDef = config.getBooleanPropDef();
        assertTrue(booleanPropDef);

        boolean boolProp = config.getBoolProp();
        assertFalse(boolProp);

        config.setBoolProp(true);
        boolProp = config.getBoolProp();
        assertTrue(boolProp);

        UUID uuidProp = config.getUuidProp();
        assertNull(uuidProp);

        UUID uuid = UUID.randomUUID();
        config.setUuidProp(uuid);
        uuidProp = config.getUuidProp();
        assertEquals(uuid, uuidProp);
    }

    public void testPrefixedConfig() {
        TestPrefixedConfig config = ConfigProvider.getConfig(TestPrefixedConfig.class);

        String stringProp = config.getStringProp();
        assertNull(stringProp);
    }
}
