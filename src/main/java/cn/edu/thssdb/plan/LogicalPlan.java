package cn.edu.thssdb.plan;

import cn.edu.thssdb.schema.Database;
import java.util.LinkedList;

public abstract class LogicalPlan {

  protected LogicalPlanType type;

  protected String currentDatabaseName = "";
  protected Database database;
  protected  String username = "";

  public LogicalPlan(LogicalPlanType type) {
    this.type = type;
  }

  public LogicalPlanType getType() {
    return type;
  }

  public void setCurrentDatabase(String name){this.currentDatabaseName=name;}

  public void exec(){}

  /** [method] 获取日志 */
  public LinkedList<String> getLog() { return null; }

  public enum LogicalPlanType {
    // TODO: add more LogicalPlanType
    CREATE_DB,
    DROP_DB,
    USE_DB,
    CREATE_TBL,
    DROP_TBL,
    SHOW_TBL,
    INSERT,
    DELETE,
    UPDATE,
    COL_DEF,
    COL_CONSTRAINT,
    TBL_CONSTRAINT,
    COL_NAME,
    TYPE_NAME,
  }
}
