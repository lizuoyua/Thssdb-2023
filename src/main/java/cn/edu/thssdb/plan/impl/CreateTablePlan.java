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

import cn.edu.thssdb.plan.LogicalPlan;
import  cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.exception.DatabaseNotExistException;
import cn.edu.thssdb.exception.WrongCreateTableException;
import cn.edu.thssdb.schema.Column;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class CreateTablePlan extends LogicalPlan {

    private String tableName;

    private Column[] columns;

    private int primaryIndex; // 主键索引

    private String stmt;

    static final String keyNotDefined = "Exception: create table failed (primary key not defined) !";
    static final String moreThanOneKey =
            "Exception: create table failed (more than one primary key defined) !";
    static final String duplicateColumnName =
            "Exception: create table failed (duplicate column name) !";

    /** [method] 构造方法 */
    public CreateTablePlan(String name, Column[] columns, int primaryIndex, String stmt) {
        super(LogicalPlanType.CREATE_TBL);
        this.tableName = name;
        this.columns = columns;
        this.primaryIndex = primaryIndex;
        this.stmt = stmt;
    }

    /** [method] 执行操作 */
    public void exec(){
        if (currentDatabaseName == null) {
            throw new DatabaseNotExistException();
        }

        for (int i = 0; i < columns.length; i++) {
            for (int j = 0; j < i; j++) {
                if (columns[i].getName().equals(columns[j].getName())) {
                    throw new WrongCreateTableException(duplicateColumnName);
                }
            }
        }

        if (primaryIndex == -1) {
            throw new WrongCreateTableException(keyNotDefined);
        }

        int keys = 0;

        for (Column column : columns) {
            if (column.isPrimary()) {
                keys++;
            }
        }

        if (keys != 1) {
            throw new WrongCreateTableException(moreThanOneKey);
        }

        Manager.getInstance().getDatabaseByName(currentDatabaseName).create(tableName, columns, primaryIndex);
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public String toString() {
        return "CreateTablePlan{" + "tableName='" + tableName + '\'' + '}';
    }
}
