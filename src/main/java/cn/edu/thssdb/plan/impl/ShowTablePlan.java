/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cn.edu.thssdb.plan.impl;

import cn.edu.thssdb.exception.DatabaseNotExistException;
import cn.edu.thssdb.exception.TableNotExistException;
import cn.edu.thssdb.plan.LogicalPlan;
import cn.edu.thssdb.schema.Column;
import  cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.type.ColumnType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShowTablePlan extends LogicalPlan {

    private String tableName;
    private String stmt;
    private List<List<String>> showTable;
    private List<String> columnNames;

    /** [method] 构造方法 */
    public ShowTablePlan(String name) {
        super(LogicalPlanType.SHOW_TBL);
        this.tableName = name;
        this.stmt = "show table " + this.tableName;
    }

    public void exec(){
        if (currentDatabaseName.length()==0) {
            throw new DatabaseNotExistException();
        }
        Table table = database.get(tableName);
        if (table == null) {
            throw new TableNotExistException();
        }
        showTable = new ArrayList<>();
        columnNames = new ArrayList<>();
        ArrayList<Column> columns = table.getColumns();

        for (int i = 0; i < columns.size(); i++) {
            columnNames.add(columns.get(i).getName());
        }

        ArrayList<String> columnTypes = new ArrayList<>();
        ArrayList<String> is_null = new ArrayList<>();
        ArrayList<String> is_primary = new ArrayList<>();
        for (Column column : columns) {

            String type = ColumnType.columnType2String(column.getType());
            if (column.getType() == ColumnType.STRING)
                type += " (MAX_LENGTH: " + column.getMaxLength() + ")";
            columnTypes.add(type);
            if (column.isNotNull()) {
                is_null.add("NOT NULL");
            } else {
                is_null.add("");
            }
            if (column.isPrimary()) {
                is_primary.add("PRIMARY KEY");
            } else {
                is_primary.add("");
            }
        }
        showTable.add(columnTypes);
        showTable.add(is_null);
        showTable.add(is_primary);
    }

    public String getTableName() {
        return tableName;
    }

    public ArrayList<String> getTableName(int i) {
        return new ArrayList<>(Arrays.asList(this.tableName));
    }

    public String getStmt() {
        return this.stmt;
    }

    public List<List<String>> getShowTable() {
        return showTable;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    @Override
    public String toString() {
        return "ShowTablePlan{" + "tableName='" + tableName + '\'' + '}';
    }
}
