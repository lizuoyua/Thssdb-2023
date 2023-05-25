package cn.edu.thssdb.schema;

import cn.edu.thssdb.type.ColumnType;

public class Column implements Comparable<Column> {
  private String name;
  private ColumnType type;
  private boolean primary;
  private boolean notNull;
  private int maxLength;

  /** [method] 构造方法 */
  public Column(String name, ColumnType type, boolean primary, boolean notNull, int maxLength) {
    this.name = name;
    this.type = type;
    this.primary = primary;
    this.notNull = notNull;
    this.maxLength = maxLength;
  }

  /** [method] 拷贝构造方法 */
  public Column(Column column) {
    this.name = column.getName();
    this.type = column.getType();
    this.primary = column.isPrimary();
    this.notNull = column.isNotNull();
    this.maxLength = column.getMaxLength();
  }

  @Override
  public int compareTo(Column e) {
    return name.compareTo(e.name);
  }

  public String toString(char delimiter) {
    return name + delimiter + type + delimiter + primary + delimiter + notNull + delimiter + maxLength;
  }

  public String getName() { return name;}

  public int getMaxLength() {
    return maxLength;
  }

  public ColumnType getType() {
    return type;
  }

  public boolean isNotNull() {
    return notNull;
  }

  public boolean isPrimary() { return primary; }
}

