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

package com.vaadin.server;

import java.util.Collection;
import java.util.Collections;

/**
 * Event fired when session access is ending.
 * 
 * @see AccessListener
 * 
 * @since 7.2
 * @author Vaadin Ltd
 */
public class AccessEndEvent extends AbstractAccessEvent {

    private final Collection<Integer> changesSentUis;

    /**
     * Creates a new access end event.
     * 
     * @param session
     *            the accessed vaadin session
     * @param changesSentUis
     *            a collection of ids of the UIs for which changes have been
     *            sent during the ending access
     */
    public AccessEndEvent(VaadinSession session,
            Collection<Integer> changesSentUis) {
        super(session);
        this.changesSentUis = Collections
                .unmodifiableCollection(changesSentUis);
    }

    /**
     * Gets the ids of the UIs for which changes have been sent during the
     * ending access.
     * 
     * @return the changesSentUis a collection of ids of the UIs for which
     *         changes have been sent during the ending access
     */
    public Collection<Integer> getChangesSentUis() {
        return changesSentUis;
    }

}
