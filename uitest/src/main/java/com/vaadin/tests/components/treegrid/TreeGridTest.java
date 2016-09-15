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

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.appengine.repackaged.com.google.common.base.Objects;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.data.hierarchical.HierarchicalDataSource;
import com.vaadin.server.data.hierarchical.HierarchicalQuery;
import com.vaadin.tests.components.AbstractTestUI;
import com.vaadin.ui.TreeGrid;

@Widgetset(VaadinServlet.DEFAULT_WIDGETSET)
public class TreeGridTest extends AbstractTestUI {

    public static class Node {
        private final String id;

        public Node(String id) {
            this.id = id;
        }

        public Node(Node parent, int childId) {
            if (parent == null) {
                id = String.valueOf(childId);
            } else {
                id = parent.id + "." + childId;
            }
        }

        public String getId() {
            return id;
        }

        public int getChildCount() {
            return new Random(hashCode()).nextInt(9) + 1;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Node) {
                Node other = (Node) obj;
                return Objects.equal(other.id, id);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "Node " + id;
        }
    }

    @Override
    protected void setup(VaadinRequest request) {
        HierarchicalDataSource<Node> dataSource = new HierarchicalDataSource<Node>() {
            @Override
            public Stream<Node> apply(HierarchicalQuery<Node> query) {
                int limit = Math.min(query.getLimit(),
                        size(query) - query.getOffset());

                Node parent = query.getParent().orElse(null);
                return IntStream
                        .range(query.getOffset(), query.getOffset() + limit)
                        .mapToObj(id -> new Node(parent, id));
            }

            @Override
            public int size(HierarchicalQuery<Node> query) {
                return query.getParent().map(Node::getChildCount)
                        .orElse(Integer.valueOf(32)).intValue();
            }
        };
        TreeGrid<Node> grid = new TreeGrid<>(dataSource);

        grid.setAdditionalColValueProvider(
                node -> String.valueOf(node.getChildCount()));

        addComponent(grid);
    }

}
