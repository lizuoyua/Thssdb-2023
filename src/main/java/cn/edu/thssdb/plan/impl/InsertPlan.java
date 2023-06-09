package cn.edu.thssdb.plan.impl;

import cn.edu.thssdb.exception.DatabaseNotExistException;
import cn.edu.thssdb.exception.TableNotExistException;
import cn.edu.thssdb.exception.WrongInsertException;
import cn.edu.thssdb.plan.LogicalPlan;
import cn.edu.thssdb.schema.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class InsertPlan extends LogicalPlan {
    private String tableName;
    private ArrayList<String> columnNames = null;
    private ArrayList<ArrayList<LiteralValuePlan>> values;
    private ArrayList<Row> rowsHasInsert;
    private ArrayList<Row> rowsToInsert;
    private int[] columnMatch;

    private Table table;

    static final String wrongColumnNum =
            "Exception: wrong insert operation (columns unmatched)!"; // 列数不匹配
    static final String wrongColumnType =
            "Exception: wrong insert operation (type unmatched)!"; // 类型不匹配
    static final String wrongValueNum =
            "Exception: wrong insert operation (number of columns and values unmatched)!"; // 列数与值数不匹配
    static final String duplicateValueType =
            "Exception: wrong insert operation (duplicate name of columns)!"; // 列名重复
    static final String wrongColumnName =
            "Exception: wrong insert operation (wrong column name)!"; // 属性名不在列定义中
    static final String duplicateKey =
            "Exception: wrong insert operation (insertion causes duplicate key)!"; // 主键重复
    static final String wrongStringLength =
            "Exception: wrong insert operation (string exceeds length limit)!"; // 字符串过长

    public InsertPlan(String tableName, ArrayList<ArrayList<LiteralValuePlan>> values) {
        super(LogicalPlanType.INSERT);
        this.tableName = tableName;
        this.values = values;
        rowsHasInsert = new ArrayList<>();
        rowsToInsert = new ArrayList<>();
    }

    public InsertPlan(String tableName, ArrayList<String> columnNames,ArrayList<ArrayList<LiteralValuePlan>> values) {
        super(LogicalPlanType.INSERT);
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.values = values;
        rowsHasInsert = new ArrayList<>();
        rowsToInsert = new ArrayList<>();
    }

    /** [method] 执行操作 */
    public void exec(){
        if(currentDatabaseName.length()==0) throw new DatabaseNotExistException();
        table = Manager.getInstance().getDatabaseByName(currentDatabaseName).get(tableName);
        if (table == null) {
            throw new TableNotExistException();
        }
        ArrayList<Column> columns = table.getColumns();
        int primaryKeyIndex = table.primaryIndex;
        String primaryKey = columns.get(primaryKeyIndex).getName();
        if (columnNames == null) {
            for (ArrayList<LiteralValuePlan> value : values) {
                if (value.size() != columns.size()) {
                    throw new WrongInsertException(wrongColumnNum);
                }
                ArrayList<Entry> entries = new ArrayList<>();

                // 类型检查
                Iterator<Column> column_it = columns.iterator();
                Iterator<LiteralValuePlan> value_it = value.iterator();
                while (column_it.hasNext()) {
                    matchType(column_it.next(), value_it.next(), primaryKey, entries);
                }
                Row newRow = new Row(entries);
                rowsToInsert.add(newRow);
            }
        } else {
            columnMatch = new int[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                columnMatch[i] = -1;
            }
            if (columnNames.size() > columns.size()) {
                throw new WrongInsertException(wrongColumnNum);
            }
            for (ArrayList<LiteralValuePlan> value : values) {
                if (value.size() != columnNames.size()) {
                    throw new WrongInsertException(wrongValueNum);
                }
            }

            // 列名重复或不存在
            for (int i = 0; i < columnNames.size(); i++) {
                for (int j = 0; j < i; j++) {
                    if (columnNames.get(i).equals(columnNames.get(j))) {
                        throw new WrongInsertException(duplicateValueType);
                    }
                }
                boolean hasMatched = false;
                for (int j = 0; j < columns.size(); j++) {
                    if (columnNames.get(i).equals(table.getColumns().get(j).getName())) {
                        hasMatched = true;
                        columnMatch[j] = i;
                        break;
                    }
                }
                if (hasMatched == false) {
                    throw new WrongInsertException(wrongColumnName);
                }
            }

            for (ArrayList<LiteralValuePlan> value : values) {

                ArrayList<Entry> entries = new ArrayList<>();

                Iterator<Column> column_it = columns.iterator();
                int i = 0;
                while (column_it.hasNext()) {
                    Column c = column_it.next();
                    int match = columnMatch[i];

                    // 将没匹配到的列的值置为null
                    if (match != -1) {
                        matchType(c, value.get(match), primaryKey, entries);
                    } else {
                        if (c.isNotNull()) {
                            throw new WrongInsertException(
                                    "Exception: wrong insert operation ( column "
                                            + c.getName()
                                            + " cannot be null )");
                        } else {
                            entries.add(new Entry(null));
                        }
                    }
                    i++;
                }
                Row newRow = new Row(entries);
                rowsToInsert.add(newRow);
            }
        }
        insert();
    }

    /** [method] 撤销操作 */
    public void undo() {
        for (Row row : rowsHasInsert) {
            table.delete(row);
        }
    }

    /** [method] 获取记录 */
    public LinkedList<String> getLog() {
        LinkedList<String> log = new LinkedList<>();
        for (Row row : rowsHasInsert) {
            log.add("INSERT " + tableName + " " + row.toString());
        }
        return log;
    }

    /** [method] 确认无异常后插入 */
    private void insert() {
        try {
            for (Row row : rowsToInsert) {
                table.insert(row);
                rowsHasInsert.add(row);
            }
        } catch (Exception e) {
            undo();
            throw new WrongInsertException(duplicateKey);
        }

        rowsToInsert.clear();
    }

    private void matchType(
            Column column, LiteralValuePlan value, String primaryKey, ArrayList<Entry> entries) {
        LiteralValuePlan.LiteralType value_type = value.getLiteralType();
        switch (column.getType()) {
            case INT:
                if (value_type == LiteralValuePlan.LiteralType.FLOAT_OR_DOUBLE) {
                    try {
                        int tmp = Integer.parseInt(value.getString());
                        entries.add(new Entry(tmp));
                    } catch (NumberFormatException e) {
                        throw e;
                    }
                } else if (value_type == LiteralValuePlan.LiteralType.NULL) {
                    if (column.isNotNull()) {
                        throw new WrongInsertException(
                                "Exception: wrong insert operation ( " + column.getName() + " cannot be null)");
                    }
                    entries.add((new Entry(null)));
                } else {
                    throw new WrongInsertException(wrongColumnType);
                }
                break;
            case LONG:
                if (value_type == LiteralValuePlan.LiteralType.FLOAT_OR_DOUBLE) {
                    try {
                        long tmp = Long.parseLong(value.getString());
                        entries.add(new Entry(tmp));
                    } catch (NumberFormatException e) {
                        throw e;
                    }
                } else if (value_type == LiteralValuePlan.LiteralType.NULL) {
                    if (column.isNotNull()) {
                        throw new WrongInsertException(
                                "Exception: wrong insert operation ( " + column.getName() + " cannot be null)");
                    }
                    entries.add((new Entry(null)));
                } else {
                    throw new WrongInsertException(wrongColumnType);
                }
                break;
            case DOUBLE:
                if (value_type == LiteralValuePlan.LiteralType.FLOAT_OR_DOUBLE
                        || value_type == LiteralValuePlan.LiteralType.INT_OR_LONG) {
                    try {
                        double tmp = Double.parseDouble(value.getString());
                        entries.add(new Entry(tmp));
                    } catch (NumberFormatException e) {
                        throw e;
                    }
                } else if (value_type == LiteralValuePlan.LiteralType.NULL) {
                    if (column.isNotNull()) {
                        throw new WrongInsertException(
                                "Exception: wrong insert operation ( " + column.getName() + " cannot be null)");
                    }
                    entries.add((new Entry(null)));
                } else {
                    throw new WrongInsertException(wrongColumnType);
                }
                break;
            case FLOAT:
                if (value_type == LiteralValuePlan.LiteralType.FLOAT_OR_DOUBLE
                        || value_type == LiteralValuePlan.LiteralType.INT_OR_LONG) {
                    try {
                        float tmp = Float.parseFloat(value.getString());
                        entries.add(new Entry(tmp));
                    } catch (NumberFormatException e) {
                        throw e;
                    }
                } else if (value_type == LiteralValuePlan.LiteralType.NULL) {
                    if (column.isNotNull()) {
                        throw new WrongInsertException(
                                "Exception: wrong insert operation ( " + column.getName() + " cannot be null)");
                    }
                    entries.add((new Entry(null)));
                } else {
                    throw new WrongInsertException(wrongColumnType);
                }
                break;
            case STRING:
                if (value_type == LiteralValuePlan.LiteralType.STRING) {
                    if (value.getString().length() > column.getMaxLength()) {
                        throw new WrongInsertException(wrongStringLength);
                    }
                    entries.add(new Entry(value.getString()));
                } else if (value_type == LiteralValuePlan.LiteralType.NULL) {
                    if (column.isNotNull()) {
                        throw new WrongInsertException(
                                "Exception: wrong insert operation ( " + column.getName() + " cannot be null)");
                    }
                    entries.add((new Entry(null)));
                } else {
                    throw new WrongInsertException(wrongColumnType);
                }
                break;
        }
    }

    public String getTableName(){return tableName;}
    public ArrayList<String> getTableName(int i) {
        return new ArrayList<>(Arrays.asList(this.tableName));
    }

    @Override
    public String toString() {
        return "InsertPlan{" + "tableName='" + tableName + '\'' + '}';
    }
}
