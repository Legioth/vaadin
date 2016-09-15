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
package com.vaadin.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.data.util.NodeIndexes;
import com.vaadin.server.KeyMapper;
import com.vaadin.server.data.hierarchical.HierarchicalDataSource;
import com.vaadin.server.data.hierarchical.HierarchicalQuery;
import com.vaadin.shared.ui.treegrid.Node;
import com.vaadin.shared.ui.treegrid.TreeGridClientRpc;
import com.vaadin.shared.ui.treegrid.TreeGridServerRpc;

public class TreeGrid<T> extends AbstractComponent {

    private HierarchicalDataSource<T> dataSource;

    private Function<T, String> hierachyColValueProvider = String::valueOf;
    private Function<T, String> additionalColValueProvider = value -> String
            .valueOf(value.hashCode());

    private final NodeIndexes nodeIndexes = new NodeIndexes();
    private final KeyMapper<T> keyMapper = new KeyMapper<>();

    public TreeGrid(HierarchicalDataSource<T> dataSource) {
        this.dataSource = dataSource;

        registerRpc(new TreeGridServerRpc() {
            @Override
            public void requestRows(int firstRowIndex, int numberOfRows) {
                sendRows(firstRowIndex, numberOfRows);
            }

            @Override
            public void setExpanded(String id, boolean expanded) {
                int toggledNodeIndex = nodeIndexes.getIndexOf(id);

                if (expanded) {
                    T parentRow = keyMapper.get(id);
                    List<String> childKeys = loadChildren(parentRow);

                    nodeIndexes.setChildren(id, childKeys);

                    getRpc().addRows(toggledNodeIndex + 1, childKeys.size());
                    sendRows(toggledNodeIndex + 1, childKeys.size());
                } else {
                    int removeSize = nodeIndexes.getTotalSize(id) - 1;
                    nodeIndexes.getDescendants(id).forEach(childKey -> keyMapper
                            .remove(keyMapper.get(childKey)));
                    nodeIndexes.setChildren(id, Collections.emptyList());

                    getRpc().removeRows(toggledNodeIndex + 1, removeSize);
                }
                // Push expansion update for toggled node
                sendRows(toggledNodeIndex, 1);
            }
        });

        // "Expand" root nodes
        List<String> rootKeys = loadChildren(null);
        nodeIndexes.setChildren(null, rootKeys);
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);
        if (initial) {
            sendRows(0, nodeIndexes.getTotalSize(null) - 1);
        }
    }

    private List<String> loadChildren(T parent) {
        // XXX Always loading all children even though it might not be needed
        Stream<T> apply = dataSource
                .apply(new HierarchicalQuery<>(parent, 0, Integer.MAX_VALUE));
        List<T> items = apply.collect(Collectors.toList());

        return items.stream().map(keyMapper::key).collect(Collectors.toList());
    }

    private void sendRows(int firstRow, int numberOfRows) {
        List<Node> nodes = new ArrayList<>();

        for (int i = 0; i < numberOfRows; i++) {
            int index = firstRow + i;
            String key = nodeIndexes.getNodeAt(index);
            T row = keyMapper.get(key);

            // XXX Assuming that there are no leaf nodes
            boolean expanded = nodeIndexes.getChildCount(key) != 0;

            int level = nodeIndexes.getLevel(key);

            Node node = new Node(level, key,
                    hierachyColValueProvider.apply(row),
                    additionalColValueProvider.apply(row), expanded);

            nodes.add(node);
        }

        // Ignore the invisible root node
        int totalSize = nodeIndexes.getTotalSize(null) - 1;

        getRpc().setRows(firstRow, nodes, totalSize);
    }

    private TreeGridClientRpc getRpc() {
        return getRpcProxy(TreeGridClientRpc.class);
    }

    public void setAdditionalColValueProvider(
            Function<T, String> additionalColValueProvider) {
        this.additionalColValueProvider = additionalColValueProvider;
    }

}
