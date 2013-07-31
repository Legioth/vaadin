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

import java.io.Serializable;

/**
 * Listener that is notified when access to a {@link VaadinSession} is about to
 * start or when it has been completed. See the individual methods for exact
 * definitions of when they are invoked.
 * <p>
 * Each access consists of one or several operations on session data while the
 * session is locked. At the end of the access, state changes or RPC invocations
 * may be sent to the client. An access can thus be seen as a transaction for a
 * session.
 * <p>
 * In some situations, session data might be used without starting an access.
 * Doing so should however be limited to internal framework operations that do
 * not invoke application code.
 * 
 * @see VaadinSession#addAccessListener(AccessListener)
 * @see VaadinSession#removeAccessListener(AccessListener)
 * 
 * @since 7.2
 * @author Vaadin Ltd
 */
public interface AccessListener extends Serializable {

    /**
     * Notifies this listener that session access is about to begin. This
     * happens when a session lock has been acquired but before the task that
     * initiated the locking is executed. Reentrantly acquiring the session lock
     * while already holding it will not cause a new start event to be fired.
     * <p>
     * Since listeners are run in the order they have been added and later
     * listeners might assume that that the contents of the accessed session has
     * not yet been touched, any access to session data should preferably be
     * performed asynchronously using {@link VaadinSession#access(Runnable)}.
     * 
     * @param event
     *            event with details about the starting access
     */
    public void accessStart(AccessStartEvent event);

    /**
     * Notifies this listener that the session access is about to end. This
     * happens when a session lock is about to be released after any pending
     * changes have been written to the client.
     * <p>
     * End events are fired in the opposite order of start events. Later
     * listeners might assume that that the contents of the accessed session
     * will not be touched any more. Any access to session data should therefore
     * be performed asynchronously using {@link VaadinSession#access(Runnable)},
     * causing a new access start event to be fired to all listeners before
     * running the scheduled task (and any other tasks scheduled in between),
     * pushing changes to the client and finally firing a new access end event.
     * 
     * @param event
     *            event with details about the ending access
     */
    public void accessEnd(AccessEndEvent event);

}
