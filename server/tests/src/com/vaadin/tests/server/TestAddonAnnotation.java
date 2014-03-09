/*
 * Copyright 2000-2013 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.tests.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import com.vaadin.annotations.AddonDefinition;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.addon.Addon;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.tests.util.MockDeploymentConfiguration;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;

public class TestAddonAnnotation extends TestCase {
    // Used to find the add-on instance initialized for a given service
    private static final Map<VaadinService, TestAddon> addonInstances = Collections
            .synchronizedMap(new WeakHashMap<VaadinService, TestAddon>());

    public static class TestAddon extends Addon<TestAnnotation> {
        @Override
        protected void init() throws ServiceException {
            addonInstances.put(getService(), this);
        }

        @Override
        public TestAnnotation getConfig() {
            // Defined as public for easier testing
            return super.getConfig();
        }
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @AddonDefinition(TestAddon.class)
    public @interface TestAnnotation {
        public String stringValue() default "foo";

        public boolean booleanValue() default true;

        public byte byteValue() default 1;

        public char charValue() default '2';

        public short shortValue() default 3;

        public int intValue() default 4;

        public long longValue() default 5;

        public float floatValue() default 6;

        public double doubleValue() default 7;

        public Class<? extends Component> classValue() default Label.class;

        public ContentMode enumValue() default ContentMode.PREFORMATTED;

        public VaadinServletConfiguration annotationValue() default @VaadinServletConfiguration(productionMode = false, ui = UI.class);

        public String[] simpleArrayValue() default { "1", "2" };

        public VaadinServletConfiguration[] complexArrayValue() default {
                @VaadinServletConfiguration(productionMode = true, ui = UI.class),
                @VaadinServletConfiguration(productionMode = false, ui = UI.class) };

        public int valueWithoutDefault();

    }

    @TestAnnotation(stringValue = "bar", booleanValue = false, byteValue = 2, charValue = '3', shortValue = 4, intValue = 5, longValue = 6, floatValue = 7, doubleValue = 8, valueWithoutDefault = 9, classValue = Table.class, enumValue = ContentMode.TEXT, annotationValue = @VaadinServletConfiguration(productionMode = true, heartbeatInterval = 42, ui = UI.class), simpleArrayValue = {
            "first", "second" }, complexArrayValue = {
            @VaadinServletConfiguration(productionMode = false, ui = UI.class),
            @VaadinServletConfiguration(productionMode = true, ui = UI.class) })
    public static class HasAllAnnotationValues extends VaadinServlet {

    }

    @TestAnnotation(valueWithoutDefault = 10)
    public static class HasDefaultValues extends VaadinServlet {

    }

    // Test reading of all config types from actual annotation
    public void testReadAnnotationValues() throws ServiceException {
        TestAddon testAddon = new TestAddon();
        VaadinServlet vaadinServlet = new VaadinServlet();
        VaadinServletService service = new VaadinServletService(vaadinServlet,
                new MockDeploymentConfiguration());

        testAddon.configure(service, TestAnnotation.class,
                HasAllAnnotationValues.class
                        .getAnnotation(TestAnnotation.class));
        TestAnnotation config = testAddon.getConfig();

        assertEquals("bar", config.stringValue());
        assertEquals(false, config.booleanValue());
        assertEquals(2, config.byteValue());
        assertEquals('3', config.charValue());
        assertEquals(4, config.shortValue());
        assertEquals(5, config.intValue());
        assertEquals(6, config.longValue());
        assertEquals(7, config.floatValue(), 0);
        assertEquals(8, config.doubleValue(), 0);
        assertEquals(9, config.valueWithoutDefault());
        assertEquals(Table.class, config.classValue());
        assertEquals(ContentMode.TEXT, config.enumValue());
        assertEquals(true, config.annotationValue().productionMode());
        assertEquals(42, config.annotationValue().heartbeatInterval());

        assertTrue(Arrays.deepEquals(new String[] { "first", "second" },
                config.simpleArrayValue()));

        VaadinServletConfiguration[] complexArrayValue = config
                .complexArrayValue();
        assertEquals(2, complexArrayValue.length);
        assertEquals(false, complexArrayValue[0].productionMode());
        assertEquals(true, complexArrayValue[1].productionMode());
    }

    // Test reading of all config types from annotation default values
    public void testReadDefaultValues() throws ServiceException {
        TestAddon testAddon = new TestAddon();
        VaadinServlet vaadinServlet = new VaadinServlet();
        VaadinServletService service = new VaadinServletService(vaadinServlet,
                new MockDeploymentConfiguration());

        testAddon.configure(service, TestAnnotation.class,
                HasDefaultValues.class.getAnnotation(TestAnnotation.class));
        TestAnnotation config = testAddon.getConfig();

        assertEquals("foo", config.stringValue());
        assertEquals(true, config.booleanValue());
        assertEquals(1, config.byteValue());
        assertEquals('2', config.charValue());
        assertEquals(3, config.shortValue());
        assertEquals(4, config.intValue());
        assertEquals(5, config.longValue());
        assertEquals(6, config.floatValue(), 0);
        assertEquals(7, config.doubleValue(), 0);
        assertEquals(10, config.valueWithoutDefault());
        assertEquals(Label.class, config.classValue());
        assertEquals(ContentMode.PREFORMATTED, config.enumValue());
        assertEquals(false, config.annotationValue().productionMode());
        assertEquals(300, config.annotationValue().heartbeatInterval());

        assertTrue(Arrays.deepEquals(new String[] { "1", "2" },
                config.simpleArrayValue()));

        VaadinServletConfiguration[] complexArrayValue = config
                .complexArrayValue();
        assertEquals(2, complexArrayValue.length);
        assertEquals(true, complexArrayValue[0].productionMode());
        assertEquals(false, complexArrayValue[1].productionMode());
    }

    // Test reading of all config types from init parameters
    public void testReadInitParamValues() throws ServiceException {
        TestAddon testAddon = new TestAddon();
        VaadinServlet vaadinServlet = new VaadinServlet();
        MockDeploymentConfiguration configuration = new MockDeploymentConfiguration();

        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".stringValue", "baz");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".booleanValue", "false");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".byteValue", "3");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".charValue", "4");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".shortValue", "5");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".intValue", "6");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".longValue", "7");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".floatValue", "8");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".doubleValue", "9");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".valueWithoutDefault", "12");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".classValue",
                Button.class.getName());
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".enumValue", "HTML");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName()
                        + ".annotationValue.heartbeatInterval", "43");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".simpleArrayValue[0]", "A");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".simpleArrayValue[1]", "B");

        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".complexArrayValue[0]", "");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName()
                        + ".complexArrayValue[0].productionMode", "true");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".complexArrayValue[0].ui",
                UI.class.getName());
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".complexArrayValue[1]", "");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName()
                        + ".complexArrayValue[1].productionMode", "false");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".complexArrayValue[1].ui",
                UI.class.getName());

        VaadinServletService service = new VaadinServletService(vaadinServlet,
                configuration);

        testAddon.configure(service, TestAnnotation.class, null);
        TestAnnotation config = testAddon.getConfig();

        assertEquals("baz", config.stringValue());
        assertEquals(false, config.booleanValue());
        assertEquals(3, config.byteValue());
        assertEquals('4', config.charValue());
        assertEquals(5, config.shortValue());
        assertEquals(6, config.intValue());
        assertEquals(7, config.longValue());
        assertEquals(8, config.floatValue(), 0);
        assertEquals(9, config.doubleValue(), 0);
        assertEquals(12, config.valueWithoutDefault());
        assertEquals(Button.class, config.classValue());
        assertEquals(ContentMode.HTML, config.enumValue());
        assertEquals(false, config.annotationValue().productionMode());
        assertEquals(43, config.annotationValue().heartbeatInterval());

        assertTrue(Arrays.deepEquals(new String[] { "A", "B" },
                config.simpleArrayValue()));

        VaadinServletConfiguration[] complexArrayValue = config
                .complexArrayValue();
        assertEquals(2, complexArrayValue.length);
        assertEquals(true, complexArrayValue[0].productionMode());
        assertEquals(false, complexArrayValue[1].productionMode());
    }

    // Test overriding single annotation attribute with init parameter
    public void testReadInitParamValuesOverDefaults() throws ServiceException {
        TestAddon testAddon = new TestAddon();
        VaadinServlet vaadinServlet = new VaadinServlet();
        MockDeploymentConfiguration configuration = new MockDeploymentConfiguration();

        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".stringValue", "baz2");

        VaadinServletService service = new VaadinServletService(vaadinServlet,
                configuration);

        testAddon.configure(service, TestAnnotation.class,
                HasDefaultValues.class.getAnnotation(TestAnnotation.class));
        TestAnnotation config = testAddon.getConfig();

        assertEquals("baz2", config.stringValue());
        // Default value which was not redefined
        assertEquals(true, config.booleanValue());
    }

    // Test that exception is thrown if there's no annotation and non-default
    // value is not defined
    public void testNoAnnotationNoParam() throws ServiceException {
        VaadinServlet vaadinServlet = new VaadinServlet();
        MockDeploymentConfiguration configuration = new MockDeploymentConfiguration();
        VaadinServletService service = new VaadinServletService(vaadinServlet,
                configuration);

        try {
            TestAddon testAddon = new TestAddon();
            testAddon.configure(service, TestAnnotation.class, null);
            throw new AssertionFailedError("Exception should have been thrown");
        } catch (ServiceException e) {
            // Expected to get here
        }

        // Should work after defining missing attribute
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".valueWithoutDefault", "20");
        TestAddon testAddon = new TestAddon();
        testAddon.configure(service, TestAnnotation.class, null);
        assertEquals(20, testAddon.getConfig().valueWithoutDefault());
    }

    // Test that exception is thrown eagerly for invalid init param format
    public void testInvalidParamType() throws ServiceException {
        VaadinServlet vaadinServlet = new VaadinServlet();
        MockDeploymentConfiguration configuration = new MockDeploymentConfiguration();
        VaadinServletService service = new VaadinServletService(vaadinServlet,
                configuration);

        configuration
                .setApplicationOrSystemProperty(TestAnnotation.class.getName()
                        + ".valueWithoutDefault", "asdf");

        try {
            TestAddon testAddon = new TestAddon();
            testAddon.configure(service, TestAnnotation.class, null);
            throw new AssertionFailedError("Exception should have been thrown");
        } catch (ServiceException e) {
            // Expected to get here
        }

        // Should work with a properly formatted value
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".valueWithoutDefault", "20");
        TestAddon testAddon = new TestAddon();
        testAddon.configure(service, TestAnnotation.class, null);
        assertEquals(20, testAddon.getConfig().valueWithoutDefault());
    }

    // Test that array is emptied using foo[]
    public void testExplicitEmptyArray() throws ServiceException {
        VaadinServlet vaadinServlet = new VaadinServlet();
        MockDeploymentConfiguration configuration = new MockDeploymentConfiguration();
        VaadinServletService service = new VaadinServletService(vaadinServlet,
                configuration);

        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".simpleArrayValue[]", "");

        TestAddon testAddon = new TestAddon();
        testAddon.configure(service, TestAnnotation.class,
                HasAllAnnotationValues.class
                        .getAnnotation(TestAnnotation.class));

        TestAnnotation config = testAddon.getConfig();
        assertEquals(0, config.simpleArrayValue().length);
    }

    // Test assigning single array value directly to foo
    public void testArraySingleValue() throws ServiceException {
        VaadinServlet vaadinServlet = new VaadinServlet();
        MockDeploymentConfiguration configuration = new MockDeploymentConfiguration();
        VaadinServletService service = new VaadinServletService(vaadinServlet,
                configuration);

        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".simpleArrayValue", "asdf");

        TestAddon testAddon = new TestAddon();
        testAddon.configure(service, TestAnnotation.class,
                HasAllAnnotationValues.class
                        .getAnnotation(TestAnnotation.class));

        TestAnnotation config = testAddon.getConfig();
        assertEquals(1, config.simpleArrayValue().length);
        assertEquals("asdf", config.simpleArrayValue()[0]);
    }

    // Test that it's illegal to define both foo[0] and foo
    public void testArraySingleAndNormalValue() throws ServiceException {
        VaadinServlet vaadinServlet = new VaadinServlet();
        MockDeploymentConfiguration configuration = new MockDeploymentConfiguration();
        VaadinServletService service = new VaadinServletService(vaadinServlet,
                configuration);

        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".simpleArrayValue", "asdf");
        configuration
                .setApplicationOrSystemProperty(TestAnnotation.class.getName()
                        + ".simpleArrayValue[0]", "asdf");

        try {
            TestAddon testAddon = new TestAddon();
            testAddon.configure(service, TestAnnotation.class,
                    HasAllAnnotationValues.class
                            .getAnnotation(TestAnnotation.class));
            throw new AssertionFailedError("Exception should have been thrown");

        } catch (ServiceException e) {
            // All is fine
        }

    }

    // Test that add-on is actually found from servlet class
    public void testBasicInitialization() throws ServiceException {
        VaadinServlet vaadinServlet = new HasAllAnnotationValues();
        MockDeploymentConfiguration configuration = new MockDeploymentConfiguration();
        VaadinServletService service = new VaadinServletService(vaadinServlet,
                configuration);

        service.init();

        TestAddon addon = addonInstances.get(service);
        TestAnnotation config = addon.getConfig();

        assertEquals(2, config.byteValue());
    }

    // Test that add-on can be explicitly disabled with init parameter
    public void testInitializationDisabled() throws ServiceException {
        VaadinServlet vaadinServlet = new HasAllAnnotationValues();
        MockDeploymentConfiguration configuration = new MockDeploymentConfiguration();
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName(), "disabled");

        VaadinServletService service = new VaadinServletService(vaadinServlet,
                configuration);

        service.init();

        assertFalse(addonInstances.containsKey(service));
    }

    // Test that annotation is ignored if init param is "enabled"
    public void testInitializationIgnoreAnnotation() throws ServiceException {
        VaadinServlet vaadinServlet = new HasAllAnnotationValues();
        MockDeploymentConfiguration configuration = new MockDeploymentConfiguration();
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName(), "enabled");

        try {
            VaadinServletService service = new VaadinServletService(
                    vaadinServlet, configuration);
            service.init();
            throw new AssertionFailedError("Exception should have been thrown");
        } catch (ServiceException e) {
            // All is fine
        }

        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".valueWithoutDefault", "20");

        VaadinServletService service = new VaadinServletService(vaadinServlet,
                configuration);
        service.init();

        TestAddon addon = addonInstances.get(service);
        TestAnnotation config = addon.getConfig();

        assertEquals(1, config.byteValue());
    }

    // Test that nothing happens if there is no annotation and no init param
    public void testInitializationWithoutAnnotation() throws ServiceException {
        VaadinServlet vaadinServlet = new VaadinServlet();
        MockDeploymentConfiguration configuration = new MockDeploymentConfiguration();

        VaadinServletService service = new VaadinServletService(vaadinServlet,
                configuration);

        service.init();

        assertFalse(addonInstances.containsKey(service));
    }

    // Test that add-on is found using init param when there's no annotation
    public void testInitializationWithoutAnnotationExplicitlyEnabled()
            throws ServiceException {
        VaadinServlet vaadinServlet = new VaadinServlet();
        MockDeploymentConfiguration configuration = new MockDeploymentConfiguration();
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName(), "enabled");
        configuration.setApplicationOrSystemProperty(
                TestAnnotation.class.getName() + ".valueWithoutDefault", "20");

        VaadinServletService service = new VaadinServletService(vaadinServlet,
                configuration);

        service.init();

        TestAddon addon = addonInstances.get(service);
        TestAnnotation config = addon.getConfig();

        assertEquals(1, config.byteValue());
    }

}
