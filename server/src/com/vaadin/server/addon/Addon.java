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
package com.vaadin.server.addon;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.annotations.AddonDefinition;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.ClientConnector.AttachListener;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

/**
 * Entry point for add-ons integrating with {@link VaadinService} or
 * {@link VaadinSession}.
 * <p>
 * An instance of a subclass is initialized during framework initialization if
 * the servlet or portlet class is annotated with an add-on annotation that in
 * turn contains an {@link AddonDefinition} annotation defining the
 * {@link Addon} subclass to use. The add-on annotation is also used for
 * configuring the add-on. Configuration values from the annotation can also be
 * overridden through web.xml.
 * <p>
 * This class provides helpers for managing the life-cycle of the add-on and for
 * adding listeners to various parts of the framework during initialization.
 * 
 * @since 7.2
 * @author Vaadin Ltd
 * @param <T>
 *            the type of the configuration annotation
 */
public abstract class Addon<T extends Annotation> {

    // Static valueOf(String) methods for all primitive types (except char)
    private static final Map<Class<?>, Method> valueOfMethods = Collections
            .unmodifiableMap(new HashMap<Class<?>, Method>() {
                {
                    try {
                        for (Class<?> wrapperType : new Class<?>[] {
                                Boolean.class, Byte.class, Short.class,
                                Integer.class, Long.class, Float.class,
                                Double.class }) {
                            Class<?> primitiveType = (Class<?>) wrapperType
                                    .getField("TYPE").get(null);
                            Method valueOfMethod = wrapperType.getMethod(
                                    "valueOf", String.class);
                            put(primitiveType, valueOfMethod);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Dude, your JRE is broken",
                                e);
                    }
                }
            });

    private static final String INITIALIZING_ASSERT_MESSAGE = "Listeners can only be added when the add-on is initializing";

    private VaadinService service;
    private T config;

    private boolean initializing = false;
    private boolean sessionInitListenerAdded = false;

    private List<AttachListener> uiAttachListeners = new ArrayList<ClientConnector.AttachListener>();
    private List<UIProvider> uiProviders = new ArrayList<UIProvider>();
    private List<BootstrapListener> bootstrapListeners = new ArrayList<BootstrapListener>();

    /**
     * Initializes this add-on by reading the configuration from an annotation
     * and then runs {@link #init()} where the subclass can integrate itself
     * into the framework.
     * 
     * @param service
     *            the Vaadin service for which the add-on is configured
     * @param annotationType
     *            the type of the configuration annotation
     * @param annotationInstance
     *            the configuration annotation instance, or <code>null</code> if
     *            configuration should be read from the
     *            {@link DeploymentConfiguration} of the service
     * @throws ServiceException
     *             if there's a problem configuring or initializing the add-on
     */
    public void configure(VaadinService service, Class<T> annotationType,
            T annotationInstance) throws ServiceException {
        this.service = service;

        this.config = readConfiguration(annotationType, annotationInstance,
                annotationType.getName(), service);

        initializing = true;
        init();
        initializing = false;
    }

    private static <T> T readConfiguration(Class<T> type, T referenceValue,
            String namespace, VaadinService service) throws ServiceException {
        if (Annotation.class.isAssignableFrom(type)) {
            final Map<String, Object> values = new HashMap<String, Object>();

            // Fetch values eagerly to verify everything is defined
            for (Method method : type.getDeclaredMethods()) {
                String methodName = method.getName();
                String propertyKey = namespace + "." + methodName;

                Object defaultValue;
                if (referenceValue != null) {
                    try {
                        defaultValue = method.invoke(referenceValue);
                    } catch (Exception e) {
                        throw new ServiceException(e);
                    }
                } else {
                    defaultValue = method.getDefaultValue();
                }

                Object value = readConfiguration(
                        (Class<Object>) method.getReturnType(), defaultValue,
                        propertyKey, service);
                if (value == null) {
                    throw new ServiceException("Property " + propertyKey
                            + " is not defined and @" + type.getName() + "."
                            + methodName + " has no default value.");
                }
                values.put(methodName, value);
            }

            final Class<?> annotationType = type;
            // Create proxy returning values from the map
            return type.cast(Proxy.newProxyInstance(service.getClassLoader(),
                    new Class[] { annotationType }, new InvocationHandler() {
                        private final Object identityObject = new Object();

                        @Override
                        public Object invoke(Object target, Method method,
                                Object[] arguments) throws Throwable {
                            String methodName = method.getName();
                            if (method.getDeclaringClass() == Annotation.class
                                    && methodName.equals("annotationType")) {
                                return annotationType;
                            } else if (method.getDeclaringClass() == Object.class) {
                                return method.invoke(identityObject, arguments);
                            } else if (method.getDeclaringClass() == annotationType) {
                                if (values.containsKey(methodName)) {
                                    return values.get(methodName);
                                } else {
                                    throw new ServiceException(
                                            "No value defined for "
                                                    + methodName + " in "
                                                    + annotationType.getName());
                                }
                            } else {
                                throw new ServiceException(
                                        "Don't know how to handle method "
                                                + method);
                            }
                        }
                    }));
        } else if (type.isArray()) {
            ArrayList<Object> values = new ArrayList<Object>();
            Class<?> componentType = type.getComponentType();

            // Read values defined using foo[n] syntax
            while (true) {
                String propertyName = namespace + "[" + values.size() + "]";
                String propertyValue = service.getDeploymentConfiguration()
                        .getApplicationOrSystemProperty(propertyName, null);
                if (propertyValue == null) {
                    // No more items defined
                    break;
                }
                Object itemValue = readConfiguration(componentType, null,
                        propertyName, service);
                assert itemValue != null;
                values.add(itemValue);
            }

            // Also support foo instead of foo[0] for single values
            String propertyValue = service.getDeploymentConfiguration()
                    .getApplicationOrSystemProperty(namespace, null);
            if (propertyValue != null) {
                if (!values.isEmpty()) {
                    throw new ServiceException("Can't define both " + namespace
                            + " and " + namespace + "[0]");
                }
                Object singleValue = readConfiguration(componentType, null,
                        namespace, service);
                assert singleValue != null;
                values.add(singleValue);
            }

            if (values.isEmpty()) {
                // Use reference value unless defined as empty using foo[]
                String emptyValue = service.getDeploymentConfiguration()
                        .getApplicationOrSystemProperty(namespace + "[]", null);
                if (emptyValue == null) {
                    return referenceValue;
                }
            }

            return type.cast(values.<Object> toArray((Object[]) Array
                    .newInstance(componentType, values.size())));
        }

        // Only leaf types remaining
        String stringValue = service.getDeploymentConfiguration()
                .getApplicationOrSystemProperty(namespace, null);
        if (stringValue == null) {
            // Use reference value if there's no property
            return referenceValue;
        } else if (type == String.class) {
            return type.cast(stringValue);
        } else if (type == char.class) {
            // char doesn't work the same way as the other primitives
            if (stringValue.length() == 1) {
                return (T) Character.valueOf(stringValue.charAt(0));
            } else {
                throw new ServiceException(
                        "Can't convert property "
                                + namespace
                                + " to char since the length of the value is not exactly 1 character: "
                                + stringValue);
            }
        } else if (type.isPrimitive()) {
            try {
                Method valueOf = valueOfMethods.get(type);
                return (T) valueOf.invoke(null, stringValue);
            } catch (Exception e) {
                throw new ServiceException("Error running " + type.getName()
                        + ".valueOf(" + stringValue + ") for " + namespace, e);
            }
        } else if (Enum.class.isAssignableFrom(type)) {
            return type.cast(Enum.valueOf((Class<? extends Enum>) type,
                    stringValue));
        } else if (type == Class.class) {
            try {
                return type.cast(Class.forName(stringValue, true,
                        service.getClassLoader()));
            } catch (ClassNotFoundException e) {
                throw new ServiceException(e);
            }
        } else {
            throw new IllegalArgumentException(type.getName()
                    + " is not supported in add-on annotations");
        }
    }

    /**
     * Initializes this add-on after the configuration has been read. This
     * method is implemented by subclasses to integrate that particular add-on
     * into the framework e.g. by adding listeners using one of the add*Listener
     * methods.
     * 
     * @throws ServiceException
     *             if there is a problem initializing this add-on
     */
    protected abstract void init() throws ServiceException;

    /**
     * Invoked when the {@link VaadinService} is destroyed.
     */
    public void destroy() {
        // Do nothing by default
    }

    /**
     * Called during initialization with the initial list of request handlers
     * for the service. This method can add new request handlers before the
     * service is initialized. Note that the returned list will be reversed so
     * the last handler will be called first.
     * 
     * @param handlers
     *            the list of request handlers that will be used
     * @throws ServiceException
     *             if there is a problem updating the list
     * 
     * @see RequestHandler
     * @see VaadinService#createRequestHandlers
     */
    public void updateRequestHandlers(List<RequestHandler> handlers)
            throws ServiceException {
        // Do nothing by default
    }

    /**
     * Gets the {@link VaadinService} for which this add-on has been configured.
     * 
     * @return the Vaadin service instance
     */
    protected VaadinService getService() {
        return service;
    }

    /**
     * Gets the add-on configuration derived from an annotation or from web.xml
     * 
     * @return the configuration of this add-on
     */
    protected T getConfig() {
        return config;
    }

    private void ensureSessionInitListener() {
        assert initializing : INITIALIZING_ASSERT_MESSAGE;

        if (!sessionInitListenerAdded) {
            // Not using this.addSessionInitListener as it might be overridden
            getService().addSessionInitListener(new SessionInitListener() {
                @Override
                public void sessionInit(SessionInitEvent event) {
                    VaadinSession session = event.getSession();
                    for (AttachListener l : uiAttachListeners) {
                        session.addUiAttachListener(l);
                    }

                    for (UIProvider p : uiProviders) {
                        session.addUIProvider(p);
                    }

                    for (BootstrapListener l : bootstrapListeners) {
                        session.addBootstrapListener(l);
                    }
                }
            });

            sessionInitListenerAdded = true;
        }
    }

    /**
     * Adds a {@link SessionInitListener} that will be invoked whenever a new
     * {@link VaadinSession} is initialized.
     * <p>
     * Please note that this method should only be called from {@link #init()}
     * 
     * @param sessionInitListener
     *            the session init listener to add
     */
    protected void addSessionInitListener(
            SessionInitListener sessionInitListener) {
        assert initializing : INITIALIZING_ASSERT_MESSAGE;
        getService().addSessionInitListener(sessionInitListener);
    }

    /**
     * Adds a {@link SessionDestroyListener} that will be invoked whenever a
     * {@link VaadinSession} is destroyed.
     * <p>
     * Please note that this method should only be called from {@link #init()}
     * 
     * @param sessionDestroyListener
     *            the session destroy listener to add
     */
    protected void addSessionDestoryListener(
            SessionDestroyListener sessionDestroyListener) {
        assert initializing : INITIALIZING_ASSERT_MESSAGE;
        getService().addSessionDestroyListener(sessionDestroyListener);
    }

    /**
     * Adds an {@link AttachListener} that will be invoked whenever a new
     * {@link UI} is attached to a session.
     * <p>
     * Please note that this method should only be called from {@link #init()}
     * 
     * @param attachListener
     *            the attach listener to add
     */
    protected void addUiAttachListener(AttachListener attachListener) {
        ensureSessionInitListener();
        uiAttachListeners.add(attachListener);
    }

    /**
     * Adds a {@link UIProvider} that will be used by all sessions.
     * <p>
     * Please note that this method should only be called from {@link #init()}
     * 
     * @param uiProvider
     *            the UI provider to add
     */
    protected void addUiProvider(UIProvider uiProvider) {
        ensureSessionInitListener();
        uiProviders.add(uiProvider);
    }

    /**
     * Adds a {@link BootstrapListener} that can modify the HTML of the
     * bootstrap page that loads a new Vaadin UI.
     * <p>
     * Please note that this method should only be called from {@link #init()}
     * 
     * @param bootstrapListener
     *            the bootstrap listener to add
     */
    protected void addBoostrapListener(BootstrapListener bootstrapListener) {
        ensureSessionInitListener();
        bootstrapListeners.add(bootstrapListener);
    }
}
