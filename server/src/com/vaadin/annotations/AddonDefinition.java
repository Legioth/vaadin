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

package com.vaadin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.server.VaadinService;
import com.vaadin.server.addon.Addon;

/**
 * Marks an annotation as a Vaadin add-on annotation. When initializing
 * {@link VaadinService}, the servlet or portlet class is checked for
 * annotations marked with this annotation. For all such annotations, the
 * {@link Addon} subclass defined in {@link #value()} is initialized.
 * 
 * @since 7.2
 * @author Vaadin Ltd
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AddonDefinition {
    /**
     * Gets the {@link Addon} class to use for this annotation.
     * 
     * @return the add-on class to use
     */
    public Class<? extends Addon<?>> value();
}
