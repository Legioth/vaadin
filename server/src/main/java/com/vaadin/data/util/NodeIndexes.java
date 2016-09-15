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
package com.vaadin.data.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class NodeIndexes {

    private class Node {
        private final String key;
        private final Node parent;
        private final List<Node> children = new ArrayList<>();

        private int totalSize = 1;

        public Node(Node parent, String key) {
            assert parent == null || idToNode.containsKey(parent.key);
            assert !idToNode.containsKey(key);

            this.key = key;
            this.parent = parent;
        }

        public void setChildren(List<String> newChildren) {
            int oldChildCount = children.size();
            int sizeChange = newChildren.size() - oldChildCount;

            children.stream().forEach(child -> {
                child.setChildren(Collections.emptyList());
                idToNode.remove(child.key);
            });
            children.clear();

            newChildren.stream().map(childId -> getOrCreateNode(this, childId))
                    .forEach(children::add);

            adjustSize(sizeChange);
        }

        private void adjustSize(int sizeChange) {
            totalSize += sizeChange;
            if (parent != null) {
                parent.adjustSize(sizeChange);
            }
        }

        public String getKey() {
            return key;
        }

        public int getLevel() {
            if (parent == null) {
                return 0;
            } else {
                return 1 + parent.getLevel();
            }
        }

        public Stream<String> getDescendants() {
            return children.stream().flatMap(child -> Stream
                    .concat(Stream.of(child.key), child.getDescendants()));
        }
    }

    private Map<String, Node> idToNode = new HashMap<>();

    public NodeIndexes() {
        idToNode.put(null, new Node(null, null));
    }

    public void setChildren(String parentId, List<String> children) {
        getNode(parentId).setChildren(children);
    }

    private Node getOrCreateNode(Node parent, String id) {
        Node node = idToNode.computeIfAbsent(id, foo -> new Node(parent, id));

        assert node.parent.equals(parent);

        return node;
    }

    private Node getNode(String parentId) {
        Node parent = idToNode.get(parentId);
        assert parent != null;
        return parent;
    }

    public int getChildCount(String id) {
        return getNode(id).children.size();
    }

    public int getTotalSize(String id) {
        return getNode(id).totalSize;
    }

    public int getIndexOf(String id) {
        Node node = getNode(id);
        Node parent = node.parent;
        if (parent == null) {
            // Invisible root is 1 before the first actual node
            return -1;
        }

        int index = 1;

        // find index inside parent
        for (Node sibling : parent.children) {
            if (sibling == node) {
                break;
            }

            index += sibling.totalSize;
        }

        // Find index of parent
        index += getIndexOf(parent.key);

        return index;
    }

    public String getNodeAt(int index) {
        return getNodeAt(getNode(null), index + 1).key;
    }

    private Node getNodeAt(Node node, int index) {
        if (index == 0) {
            return node;
        }

        assert index < node.totalSize;

        int seenNodes = 1;
        for (Node child : node.children) {
            seenNodes += child.totalSize;
            if (seenNodes > index) {
                int startOfNode = seenNodes - child.totalSize;
                int indexInChild = index - startOfNode;
                return getNodeAt(child, indexInChild);
            }
        }

        throw new RuntimeException("No more nodes");
    }

    public int getLevel(String key) {
        return getNode(key).getLevel();
    }

    public Stream<String> getDescendants(String id) {
        return getNode(id).getDescendants();
    }
}
