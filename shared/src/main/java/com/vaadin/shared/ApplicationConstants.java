/*
 * Copyright 2000-2016 Vaadin Ltd.
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
package com.vaadin.shared;

import java.io.Serializable;

public class ApplicationConstants implements Serializable {

    // This indicates the whole page is generated by us (not embedded)
    public static final String GENERATED_BODY_CLASSNAME = "v-generated-body";

    public static final String APP_PATH = "APP";

    public static final String UIDL_PATH = "UIDL";

    public static final String HEARTBEAT_PATH = "HEARTBEAT";

    public static final String PUSH_PATH = "PUSH";

    public static final String PUBLISHED_FILE_PATH = APP_PATH + '/'
            + "PUBLISHED";

    public static final String APP_PROTOCOL_PREFIX = "app://";
    public static final String VAADIN_PROTOCOL_PREFIX = "vaadin://";
    public static final String FONTICON_PROTOCOL_PREFIX = "fonticon://";
    public static final String PUBLISHED_PROTOCOL_NAME = "published";
    public static final String PUBLISHED_PROTOCOL_PREFIX = PUBLISHED_PROTOCOL_NAME
            + "://";

    public static final String REFRESH_PATH = APP_PATH + '/' + "REFRESH/";

    /**
     * Prefix used for theme resource URLs
     *
     * @see com.vaadin.server.ThemeResource
     * @since 7.5.0
     */
    public static final String THEME_PROTOCOL_PREFIX = "theme://";

    public static final String UIDL_SECURITY_TOKEN_ID = "Vaadin-Security-Key";

    @Deprecated
    public static final String UPDATE_VARIABLE_INTERFACE = "v";
    @Deprecated
    public static final String UPDATE_VARIABLE_METHOD = "v";

    public static final String SERVICE_URL = "serviceUrl";

    public static final String SERVICE_URL_PARAMETER_NAME = "pathParameterName";

    // Javadocs in ApplicationConfiguration should be updated if this is changed
    public static final String V_RESOURCE_PATH = "v-resourcePath";

    @Deprecated
    public static final String DRAG_AND_DROP_CONNECTOR_ID = "DD";

    /**
     * URL parameter used in UIDL requests to indicate that the full server-side
     * state should be returned to the client, i.e. without any incremental
     * changes.
     */
    public static final String URL_PARAMETER_REPAINT_ALL = "repaintAll";

    /**
     * Configuration parameter giving the (in some cases relative) URL to the
     * VAADIN folder from where themes and widgetsets are loaded.
     * <p>
     * <b>Refactor warning:</b> This value is also hardcoded in
     * vaadinBootstrap.js.
     * </p>
     */
    public static final String VAADIN_DIR_URL = "vaadinDir";

    /**
     * The name of the javascript containing the bootstrap code. The file is
     * located in the VAADIN directory.
     *
     * @since 7.3
     */
    public static final String VAADIN_BOOTSTRAP_JS = "vaadinBootstrap.js";

    /**
     * The name of the javascript containing push support. The file is located
     * in the VAADIN directory.
     */
    public static final String VAADIN_PUSH_JS = "vaadinPush.js";

    /**
     * The name of the debug version of the javascript containing push support.
     * The file is located in the VAADIN directory.
     *
     * @since 7.1.6
     */
    public static final String VAADIN_PUSH_DEBUG_JS = "vaadinPush.debug.js";

    /**
     * Name of the parameter used to transmit the CSRF token.
     */
    public static final String CSRF_TOKEN_PARAMETER = "v-csrfToken";

    /**
     * The name of the parameter used to transmit RPC invocations
     *
     * @since 7.2
     */
    public static final String RPC_INVOCATIONS = "rpc";

    /**
     * The name of the parameter used to transmit the CSRF token
     *
     * @since 7.2
     */
    public static final String CSRF_TOKEN = "csrfToken";

    /**
     * The name of the parameter used to transmit the sync id. The value can be
     * set to -1 e.g. when testing with pre-recorded requests to make the
     * framework ignore the sync id.
     *
     * @see com.vaadin.ui.ConnectorTracker#getCurrentSyncId()
     * @since 7.2
     */
    public static final String SERVER_SYNC_ID = "syncId";

    /**
     * The name of the parameter used to transmit the id of the client to server
     * messages.
     *
     * @since 7.6
     */
    public static final String CLIENT_TO_SERVER_ID = "clientId";

    /**
     * Default value to use in case the security protection is disabled.
     */
    public static final String CSRF_TOKEN_DEFAULT_VALUE = "init";

    /**
     * The name of the parameter used for re-synchronizing.
     */
    public static final String RESYNCHRONIZE_ID = "resynchronize";

    /**
     * The name of the parameter used for sending the widget set version to the
     * server
     *
     * @since 7.6
     */
    public static final String WIDGETSET_VERSION_ID = "wsver";

}
