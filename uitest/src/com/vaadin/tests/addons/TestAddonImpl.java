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

package com.vaadin.tests.addons;

import java.io.IOException;
import java.util.List;

import org.jsoup.nodes.Element;

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.ClientConnector.AttachEvent;
import com.vaadin.server.ClientConnector.AttachListener;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.addon.Addon;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

public class TestAddonImpl extends Addon<TestAddon> {
    @Override
    protected void init() {
        final String message = getConfig().value();

        addBoostrapListener(new BootstrapListener() {
            @Override
            public void modifyBootstrapPage(BootstrapPageResponse response) {
                Element span = response.getDocument().body()
                        .appendElement("span");
                span.text("Bootstrap message: " + message);
                span.attr("style", "position: absolute;top:0;right:0");
            }

            @Override
            public void modifyBootstrapFragment(
                    BootstrapFragmentResponse response) {
                // Nothing here
            }
        });

        addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent event)
                    throws ServiceException {
                event.getSession().setAttribute(
                        TestAddonImpl.class.getName() + ".sessionInit",
                        "Session init message: " + message);
            }
        });

        addUiAttachListener(new AttachListener() {
            @Override
            public void attach(AttachEvent event) {
                UI ui = (UI) event.getConnector();

                String notification = String
                        .valueOf(ui.getSession().getAttribute(
                                TestAddonImpl.class.getName() + ".sessionInit"));
                notification += "\n"
                        + String.valueOf(ui.getSession().getAttribute(
                                TestAddonImpl.class.getName() + ".getUIClass"));
                notification += "\n"
                        + VaadinService.getCurrentRequest().getAttribute(
                                TestAddonImpl.class.getName());

                new Notification(notification).show(ui.getPage());
            }
        });

        addUiProvider(new UIProvider() {
            @Override
            public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
                VaadinSession session = VaadinSession.getCurrent();
                session.setAttribute(TestAddonImpl.class.getName()
                        + ".getUIClass", "UI provider message: " + message);
                return null;
            }
        });
    }

    @Override
    public void updateRequestHandlers(List<RequestHandler> handlers)
            throws ServiceException {
        handlers.add(new RequestHandler() {
            @Override
            public boolean handleRequest(VaadinSession session,
                    VaadinRequest request, VaadinResponse response)
                    throws IOException {
                request.setAttribute(TestAddonImpl.class.getName(),
                        "Request handler message: " + getConfig().value());
                return false;
            }
        });
    }

}
