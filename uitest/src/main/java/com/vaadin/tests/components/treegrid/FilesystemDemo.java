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
package com.vaadin.tests.components.treegrid;

import java.io.File;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.data.hierarchical.HierarchicalDataSource;
import com.vaadin.tests.components.AbstractTestUI;
import com.vaadin.ui.TreeGrid;

@Widgetset(VaadinServlet.DEFAULT_WIDGETSET)
@Theme("valo")
public class FilesystemDemo extends AbstractTestUI {

    @Override
    protected void setup(VaadinRequest request) {
        HierarchicalDataSource<File> dataSource = new FileSystemDataSource(
                new File("./"));

        TreeGrid<File> treeGrid = new TreeGrid<>(dataSource);
        treeGrid.setHierachyColValueProvider(File::getName);
        treeGrid.setAdditionalCol("Size", file -> {
            if (file.isDirectory()) {
                return "-";
            }
            return String.valueOf(file.length());
        });
        treeGrid.setSizeFull();
        treeGrid.setHeight("600px");

        addComponent(treeGrid);

    }

}
