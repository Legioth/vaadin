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

package com.vaadin.tests.application;

import java.util.ArrayList;
import java.util.Collection;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.AccessEndEvent;
import com.vaadin.server.AccessListener;
import com.vaadin.server.AccessStartEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.communication.UIInitHandler;
import com.vaadin.tests.components.AbstractTestUIWithLog;
import com.vaadin.tests.integration.FlagSeResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Image;
import com.vaadin.ui.UI;

public class AccessListenerTest extends AbstractTestUIWithLog {

    protected AccessListener myAccessListener = new AccessListener() {
        @Override
        public void accessStart(AccessStartEvent event) {
            VaadinRequest currentRequest = VaadinService.getCurrentRequest();
            UI currentUi = UI.getCurrent();

            String message = "Access starting.";
            if (currentRequest != null) {
                message += " Request to " + currentRequest.getPathInfo();
                if (currentRequest
                        .getParameter(UIInitHandler.BROWSER_DETAILS_PARAMETER) != null) {
                    message += "?" + UIInitHandler.BROWSER_DETAILS_PARAMETER;
                }
                message += ".";
            }

            if (currentUi != null) {
                message += " Current UI id: " + currentUi.getUIId() + ".";
            }

            // Should not directly modify UI from event, but who cares?
            log(message);
        }

        @Override
        public void accessEnd(AccessEndEvent event) {
            Collection<Integer> changesSentUis = event.getChangesSentUis();

            Collection<Integer> dirtyUis = new ArrayList<Integer>();

            Collection<UI> uIs = event.getSource().getUIs();
            for (UI ui : uIs) {
                if (ui.getConnectorTracker().hasDirtyConnectors()) {
                    dirtyUis.add(Integer.valueOf(ui.getUIId()));
                }
            }

            // Should not directly modify UI from event, but who cares?
            final String message = "Access ending. Changes sent to UIs: "
                    + changesSentUis
                    + ". Still dirty UIs (before this message was added): "
                    + dirtyUis;
            log(message);
        }
    };

    @Override
    protected void setup(VaadinRequest request) {
        final CheckBox checkBox = new CheckBox("Use access listener");
        checkBox.setImmediate(true);
        checkBox.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                if (checkBox.getValue().booleanValue()) {
                    getSession().addAccessListener(myAccessListener);
                }
            }
        });

        addComponent(checkBox);

        addComponent(new Button("Sync messages", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                // Do nothing
            }
        }));

        addComponent(new Button("Show resource", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                addComponent(new Image("An image", new FlagSeResource()));
            }
        }));

        addComponent(new Button("Load Vaadin UI (in an iframe)",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        String url = getPage().getLocation().toString();
                        url = url.replaceAll("\\?.*", "");
                        BrowserFrame browserFrame = new BrowserFrame(
                                "Another UI", new ExternalResource(url));
                        addComponent(browserFrame);
                    }
                }));
    }

    @Override
    protected int getLogMessageCount() {
        return 10;
    }

    @Override
    protected String getTestDescription() {
        return "Test for seeing when access start and end events are fired.";
    }

    @Override
    protected Integer getTicketNumber() {
        return Integer.valueOf(12256);
    }

}
