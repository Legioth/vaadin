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
package com.vaadin.shared.ui.treegrid;

public class Node {
    private int level;
    private String key;
    private String c1;
    private String c2;

    private boolean expanded;

    @Deprecated
    public Node() {
        // Bean constructor
    }

    public Node(int level, String key, String c1, String c2, boolean expanded) {
        this.level = level;
        this.key = key;
        this.c1 = c1;
        this.c2 = c2;
        this.expanded = expanded;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setC1(String c1) {
        this.c1 = c1;
    }

    public void setC2(String c2) {
        this.c2 = c2;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public int getLevel() {
        return level;
    }

    public String getC1() {
        return c1;
    }

    public String getC2() {
        return c2;
    }
}