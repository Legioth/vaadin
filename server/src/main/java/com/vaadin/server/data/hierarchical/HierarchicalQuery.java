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
package com.vaadin.server.data.hierarchical;

import java.util.Optional;

public class HierarchicalQuery<T> {
    private T parent;
    private int limit;
    private int offset;

    public HierarchicalQuery(T parent, int offset, int limit) {
        this.parent = parent;
        this.limit = limit;
        this.offset = offset;
    }

    public Optional<T> getParent() {
        return Optional.ofNullable(parent);
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }
}
