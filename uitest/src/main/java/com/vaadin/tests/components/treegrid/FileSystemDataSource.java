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
import java.util.stream.Stream;

import com.vaadin.server.data.hierarchical.HierarchicalDataSource;
import com.vaadin.server.data.hierarchical.HierarchicalQuery;

public class FileSystemDataSource implements HierarchicalDataSource<File> {
    private final File root;

    public FileSystemDataSource(File root) {
        this.root = root;
    }

    @Override
    public Stream<File> fetchChildren(HierarchicalQuery<File> query) {
        File[] children = query.getContext().listFiles();

        return Stream.of(children).skip(query.getOffset())
                .limit(query.getLimit());
    }

    @Override
    public int countChildren(HierarchicalQuery<File> query) {
        return query.getContext().list().length;
    }

    @Override
    public boolean isExpandable(File file) {
        return file.isDirectory();
    }

    @Override
    public File getRoot() {
        return root;
    }
}