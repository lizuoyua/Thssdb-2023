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
package cn.edu.thssdb.parser;

import cn.edu.thssdb.exception.IllegalTypeException;
import cn.edu.thssdb.plan.LogicalPlan;
import cn.edu.thssdb.plan.impl.*;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.sql.SQLBaseVisitor;
import cn.edu.thssdb.sql.SQLParser;
import cn.edu.thssdb.type.ColumnType;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.sound.midi.SysexMessage;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ThssDBSQLVisitor extends SQLBaseVisitor<LogicalPlan> {

  public String getFullText(ParseTree tree) {
    ParserRuleContext context = (ParserRuleContext) tree;
    if (context.children == null) {
      return "";
    }
    Token startToken = context.start;
    Token stopToken = context.stop;
    Interval interval = new Interval(startToken.getStartIndex(), stopToken.getStopIndex());
    String result = context.start.getInputStream().getText(interval);
    return result;
  }

  @Override
  public LogicalPlan visitCreateDbStmt(SQLParser.CreateDbStmtContext ctx) {
    return new CreateDatabasePlan(ctx.databaseName().getText());
  }

  @Override
  public LogicalPlan visitDropDbStmt(SQLParser.DropDbStmtContext ctx) {
    return new DropDatabasePlan(ctx.databaseName().getText());
  }

  @Override
  public LogicalPlan visitUseDbStmt(SQLParser.UseDbStmtContext ctx) {
    return new UseDatabasePlan(ctx.databaseName().getText());
  }

  @Override
  public LogicalPlan visitCreateTableStmt(SQLParser.CreateTableStmtContext ctx) {
    String tableName = ctx.tableName().getText();
    int n = ctx.getChildCount();
    ArrayList<ColumnDefPlan> columnDefPlans = new ArrayList<>();
    String primaryKey = null;
    for (int i = 4; i < n; i += 2) {
      if (visit(ctx.getChild(i)) instanceof ColumnDefPlan) {
        columnDefPlans.add((ColumnDefPlan) visit(ctx.getChild(i)));
      } else if (visit(ctx.getChild(i)) instanceof TableConstraintPlan) {
        primaryKey = ((TableConstraintPlan)visit(ctx.getChild(i))).getColumnName();
      }
    }
    ArrayList<Column> columns = new ArrayList<>();
    int primaryKeyIndex = -1;

    for (int i = 0; i < columnDefPlans.size(); i++) {
      ColumnDefPlan c = columnDefPlans.get(i);

      if (c.getPrimary()) {
        primaryKeyIndex = i;
        c.setNotNull(true);
      }

      if (primaryKey != null) {
        if (primaryKey.equalsIgnoreCase(c.getColumnName())) {
          primaryKeyIndex = i;
          c.setPrimary(true);
          c.setNotNull(true);
        }
      }

      Column column =
              new Column(
                      c.getColumnName(),
                      c.getTypeNamePlan().getColumnType(),
                      c.getPrimary(),
                      c.getNotNull(),
                      c.getTypeNamePlan().getMaxLength());
      columns.add(column);
    }
    Column[] pColumns = new Column[columns.size()];
    for (int i = 0; i < columns.size(); i++) {
      pColumns[i] = columns.get(i);
    }
    return new CreateTablePlan(tableName, pColumns, primaryKeyIndex, getFullText(ctx));
  }

  @Override
  public LogicalPlan visitColumnDef(SQLParser.ColumnDefContext ctx) {
    String columnName = ctx.columnName().getText();
    TypeNamePlan typeNamePlan = (TypeNamePlan) visit(ctx.typeName());
    int n =ctx.getChildCount();
    boolean isPrimary = false;
    boolean isNotNull = false;
    if(n>2) {
      isPrimary = ((ColumnConstraintPlan) visit(ctx.getChild(2))).getPrimary();
      isNotNull = ((ColumnConstraintPlan) visit(ctx.getChild(2))).getNotNull();
    }
    return new ColumnDefPlan(columnName,typeNamePlan,isPrimary,isNotNull);
  }

  @Override
  public LogicalPlan visitColumnName(SQLParser.ColumnNameContext ctx) {
    return new ColumnNamePlan(ctx.getChild(0).getText().toUpperCase());
  }

  @Override
  public LogicalPlan visitTypeName(SQLParser.TypeNameContext ctx) {
    if (ctx.getChildCount() == 1) {
      try {
        return new TypeNamePlan(ColumnType.string2ColumnType(ctx.getChild(0).getText().toUpperCase()));
      } catch (Exception e) {
        throw new IllegalTypeException();
      }
    } else {
      try {
        int maxLength = Integer.parseInt(ctx.getChild(2).getText());
        return new TypeNamePlan(
                ColumnType.string2ColumnType(ctx.getChild(0).getText().toUpperCase()), maxLength);
      } catch (Exception e) {
        throw new IllegalTypeException();
      }
    }
  }

  @Override
  public LogicalPlan visitColumnConstraint(SQLParser.ColumnConstraintContext ctx) {
    boolean isPrimary = false;
    boolean isNotNull = false;
    if(ctx.getChild(0).getText().toUpperCase().equals("PRIMARY")){
      isPrimary = true;
    }
    else if(ctx.getChild(0).getText().toUpperCase().equals("NOT")){
      isNotNull = true;
    }
    return new ColumnConstraintPlan(isPrimary, isNotNull);
  }

  @Override
  public LogicalPlan visitTableConstraint(SQLParser.TableConstraintContext ctx) {
    return new TableConstraintPlan(ctx.getChild(3).getText());
  }

  @Override
  public LogicalPlan visitDropTableStmt(SQLParser.DropTableStmtContext ctx) {
    return new DropTablePlan(ctx.tableName().getText());
  }

  @Override
  public LogicalPlan visitShowTableStmt(SQLParser.ShowTableStmtContext ctx) {
    return new ShowTablePlan(ctx.tableName().getText());
  }

  @Override
  public LogicalPlan visitLiteralValue(SQLParser.LiteralValueContext ctx) {
    String str = ctx.getChild(0).getText();
    Object child = ctx.getChild(0);
    if (str.equalsIgnoreCase("NULL")) {
      return new LiteralValuePlan(LiteralValuePlan.LiteralType.NULL, "null");
    } else if (str.charAt(0) == '\'') {
      return new LiteralValuePlan(LiteralValuePlan.LiteralType.STRING, str.substring(1, str.length() - 1));
    } else return new LiteralValuePlan(LiteralValuePlan.LiteralType.FLOAT_OR_DOUBLE, str);
  }

  @Override
  public LogicalPlan visitInsertStmt(SQLParser.InsertStmtContext ctx) {
    String tableName = ctx.tableName().getText();
    int n = ctx.getChildCount();
    ArrayList<String> columnNames = new ArrayList<>();
    ArrayList<ArrayList<LiteralValuePlan>> values = new ArrayList<>();
    String text = ctx.getChild(3).getText();

    if (text.equalsIgnoreCase("VALUES")) {
      for (int i = 4; i < n; i += 2) {
        LiteralValuePlan plan = (LiteralValuePlan)visit(ctx.getChild(i));
        ArrayList<LiteralValuePlan> tmp = new ArrayList<>();
        tmp.add(plan);
        values.add(tmp);
      }
      return new InsertPlan(tableName, values);
    } else {
      int i;
      for (i = 4; i < n; i++) {
        if (ctx.getChild(i).getText().equalsIgnoreCase("VALUES")) break;
        else if (ctx.getChild(i).getText().equals(",") || ctx.getChild(i).getText().equals(")"))
          continue;
        else {
          LiteralValuePlan plan = (LiteralValuePlan) visit(ctx.getChild(i));
          columnNames.add(plan.getString());
        }
      }
      i++;
      for (; i < n; i += 2) {
        LiteralValuePlan plan = (LiteralValuePlan)visit(ctx.getChild(i));
        ArrayList<LiteralValuePlan> tmp = new ArrayList<>();
        tmp.add(plan);
        values.add(tmp);
      }
      return new InsertPlan(tableName, columnNames, values);
    }
  }

  // TODO: parser to more logical plan
}
