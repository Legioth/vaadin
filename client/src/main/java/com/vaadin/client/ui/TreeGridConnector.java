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
package com.vaadin.client.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.data.AbstractRemoteDataSource;
import com.vaadin.client.renderers.ComplexRenderer;
import com.vaadin.client.widget.grid.CellReference;
import com.vaadin.client.widget.grid.RendererCellReference;
import com.vaadin.client.widgets.Grid;
import com.vaadin.client.widgets.Grid.Column;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.treegrid.Node;
import com.vaadin.shared.ui.treegrid.TreeGridClientRpc;
import com.vaadin.shared.ui.treegrid.TreeGridServerRpc;
import com.vaadin.shared.ui.treegrid.TreeGridState;
import com.vaadin.ui.TreeGrid;

@Connect(TreeGrid.class)
public class TreeGridConnector extends AbstractComponentConnector {

    @Override
    protected void init() {
        getWidget().addColumn(new Column<Node, Node>() {
            @Override
            public Node getValue(Node row) {
                return row;
            }
        }).setRenderer(new ComplexRenderer<Node>() {
            @Override
            public void render(RendererCellReference cell, Node data) {
                String string = "";
                for (int i = 0; i < data.getLevel(); i++) {
                    string += "  ";
                }
                string += data.isExpandable() ? (data.isExpanded() ? "-" : "+")
                        : " ";
                string += " ";
                string += data.getC1();

                cell.getElement().setInnerText(string);
            }

            @Override
            public void init(RendererCellReference cell) {
                cell.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
            }

            @Override
            public Collection<String> getConsumedEvents() {
                return Arrays.asList("click");
            }

            @Override
            public boolean onBrowserEvent(CellReference<?> cell,
                    NativeEvent event) {
                Node node = (Node) cell.getRow();
                if (!node.isExpandable()) {
                    return false;
                }

                getRpc().setExpanded(node.getKey(), !node.isExpanded());

                return true;
            }
        }).setHeaderCaption("Name").setWidth(300);

        getWidget().addColumn(new Column<String, Node>() {
            @Override
            public String getValue(Node row) {
                return row.getC2();
            }
        }).setHeaderCaption("Child count");

        getWidget().setDataSource(new AbstractRemoteDataSource<Node>() {
            {
                registerRpc(TreeGridClientRpc.class, new TreeGridClientRpc() {
                    @Override
                    public void setRows(int firstRow, List<Node> nodes,
                            int totalSize) {
                        if (size() != totalSize) {
                            resetDataAndSize(totalSize);
                        }
                        setRowData(firstRow, nodes);
                    }

                    @Override
                    public void addRows(int index, int count) {
                        insertRowData(index, count);
                    }

                    @Override
                    public void removeRows(int index, int count) {
                        removeRowData(index, count);
                    }
                });
            }

            @Override
            protected void requestRows(int firstRowIndex, int numberOfRows,
                    RequestRowsCallback<Node> callback) {
                getRpc().requestRows(firstRowIndex, numberOfRows);
            }

            @Override
            public Object getRowKey(Node row) {
                return row.getC1();
            }
        });
    }

    @Override
    public TreeGridState getState() {
        return (TreeGridState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        getWidget().getColumn(1).setHeaderCaption(getState().additionalColname);
    }

    private TreeGridServerRpc getRpc() {
        return getRpcProxy(TreeGridServerRpc.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Grid<Node> getWidget() {
        return (Grid<Node>) super.getWidget();
    }
}
