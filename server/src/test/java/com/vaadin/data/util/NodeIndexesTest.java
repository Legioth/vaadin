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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class NodeIndexesTest {
    private NodeIndexes indexes = new NodeIndexes();

    @Test
    public void rootSizes() {
        Assert.assertEquals(1, indexes.getTotalSize(null));

        indexes.setChildren(null, Arrays.asList("0", "1", "2"));

        Assert.assertEquals(1, indexes.getTotalSize("0"));
        Assert.assertEquals(1, indexes.getTotalSize("1"));
        Assert.assertEquals(1, indexes.getTotalSize("2"));

        Assert.assertEquals(4, indexes.getTotalSize(null));
    }

    @Test
    public void rootIndexes() {
        indexes.setChildren(null, Arrays.asList("0", "1", "2"));

        Assert.assertEquals(0, indexes.getIndexOf("0"));
        Assert.assertEquals(1, indexes.getIndexOf("1"));
        Assert.assertEquals(2, indexes.getIndexOf("2"));
    }

    @Test
    public void indexOfRoot() {
        indexes.setChildren(null, Arrays.asList("0", "1", "2"));

        Assert.assertEquals("0", indexes.getNodeAt(0));
        Assert.assertEquals("1", indexes.getNodeAt(1));
        Assert.assertEquals("2", indexes.getNodeAt(2));
    }

    @Test
    public void rootIndexes_afterReadd() {
        indexes.setChildren(null, Arrays.asList("0", "1", "2"));

        indexes.setChildren(null, Arrays.asList("3", "4"));

        Assert.assertEquals(0, indexes.getIndexOf("3"));
        Assert.assertEquals(1, indexes.getIndexOf("4"));
    }

    @Test
    public void rootSizes_withChildren() {
        indexes.setChildren(null, Arrays.asList("0", "1", "2"));
        indexes.setChildren("0", Arrays.asList("0.0"));
        indexes.setChildren("1", Arrays.asList("1.0"));

        Assert.assertEquals(2, indexes.getTotalSize("0"));

        Assert.assertEquals(6, indexes.getTotalSize(null));
    }

    @Test
    public void rootIndexes_withChildren() {
        indexes.setChildren(null, Arrays.asList("0", "1", "2"));
        indexes.setChildren("0", Arrays.asList("0.0"));
        indexes.setChildren("1", Arrays.asList("1.0"));

        Assert.assertEquals(0, indexes.getIndexOf("0"));
        Assert.assertEquals(2, indexes.getIndexOf("1"));
        Assert.assertEquals(4, indexes.getIndexOf("2"));
    }

    @Test
    public void childIndex() {
        indexes.setChildren(null, Arrays.asList("0", "1", "2"));
        indexes.setChildren("0", Arrays.asList("0.0"));
        indexes.setChildren("1", Arrays.asList("1.0", "1.1", "1.2", "1.3"));

        Assert.assertEquals(0, indexes.getIndexOf("0"));
        Assert.assertEquals(1, indexes.getIndexOf("0.0"));
        Assert.assertEquals(2, indexes.getIndexOf("1"));
        Assert.assertEquals(3, indexes.getIndexOf("1.0"));
        Assert.assertEquals(4, indexes.getIndexOf("1.1"));
        Assert.assertEquals(5, indexes.getIndexOf("1.2"));
        Assert.assertEquals(6, indexes.getIndexOf("1.3"));
        Assert.assertEquals(7, indexes.getIndexOf("2"));
    }

    @Test
    public void childAtIndex() {
        indexes.setChildren(null, Arrays.asList("0", "1", "2"));
        indexes.setChildren("0", Arrays.asList("0.0"));
        indexes.setChildren("1", Arrays.asList("1.0", "1.1", "1.2", "1.3"));

        Assert.assertEquals("0", indexes.getNodeAt(0));
        Assert.assertEquals("0.0", indexes.getNodeAt(1));
        Assert.assertEquals("1", indexes.getNodeAt(2));
        Assert.assertEquals("1.0", indexes.getNodeAt(3));
        Assert.assertEquals("1.1", indexes.getNodeAt(4));
        Assert.assertEquals("1.2", indexes.getNodeAt(5));
        Assert.assertEquals("1.3", indexes.getNodeAt(6));
        Assert.assertEquals("2", indexes.getNodeAt(7));
    }

    @Test
    public void recursivelyDelete() {
        indexes.setChildren(null, Arrays.asList("0", "1", "2"));
        indexes.setChildren("0", Arrays.asList("0.0", "0.1"));

        Assert.assertEquals(3, indexes.getTotalSize("0"));

        // Remove 0 and then add it back with only one child
        indexes.setChildren(null, Arrays.asList("1", "2"));
        indexes.setChildren(null, Arrays.asList("0", "1", "2"));
        indexes.setChildren("0", Arrays.asList("0.0"));

        Assert.assertEquals(2, indexes.getTotalSize("0"));
    }

    @Test
    public void sizesAfterExpandedDelete() {
        indexes.setChildren(null, Arrays.asList("0", "1"));
        Assert.assertEquals(3, indexes.getTotalSize(null));

        indexes.setChildren("0", Arrays.asList("0.0", "0.1"));
        indexes.setChildren("0.0", Arrays.asList("0.0.0"));

        indexes.setChildren(null, Arrays.asList("1"));
        Assert.assertEquals(2, indexes.getTotalSize(null));
    }
}
