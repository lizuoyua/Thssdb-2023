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
import cn.edu.thssdb.type.ColumnType;

public class ColumnDefPlan extends LogicalPlan {

    private String columnName;
    private TypeNamePlan typeNamePlan;
    private boolean primary;
    private boolean notNull;

    public ColumnDefPlan(String name, TypeNamePlan typeNamePlan, boolean isPrimary, boolean isNotNull) {
        super(LogicalPlanType.COL_DEF);
        this.columnName = name;
        this.typeNamePlan = typeNamePlan;
        this.primary = isPrimary;
        this.notNull = isNotNull;
    }


    public void setPrimary(boolean isPrimary){primary = isPrimary;}

    public void setNotNull(boolean isNotNull){notNull = isNotNull;}

    public String getColumnName() {
        return columnName;
    }

    public TypeNamePlan getTypeNamePlan() {
        return typeNamePlan;
    }

    public boolean getPrimary() {
        return primary;
    }

    public boolean getNotNull() {
        return notNull;
    }

    @Override
    public String toString() {
        return null;
    }
}
