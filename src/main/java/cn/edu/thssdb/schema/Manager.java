package cn.edu.thssdb.schema;

import cn.edu.thssdb.exception.*;
import cn.edu.thssdb.utils.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Manager {
  private HashMap<String, Database> databases;//数据库哈希表
  private static ReentrantReadWriteLock lock;//可重入读写锁

  /** 新增属性 */
  private Meta meta;//meta类管理元数据文件的创建、读取、写入和删除

  private ArrayList<String> databasesList; // 数据库名称列表

  private HashMap<String, Integer> onlineDatabases; // 正在使用的数据库哈希表，记录了几个客户端正在使用

  public static Manager getInstance() {
    return Manager.ManagerHolder.INSTANCE;
  }

  public Manager() {
    databases = new HashMap<>();
    lock = new ReentrantReadWriteLock();
    databasesList = new ArrayList<>();
    onlineDatabases = new HashMap<>();
    meta = new Meta(Global.DATA_ROOT_FOLDER, "manager.meta");
    ArrayList<String[]> db_list = this.meta.readFromFile();
    System.out.println(db_list);
    for (String[] db_info : db_list) {
      databases.put(db_info[0], new Database(db_info[0]));
      databasesList.add(db_info[0]);
    }
  }

  /** [method] 判断数据库是否存在 */
  public boolean contains(String name) {
    return databases.containsKey(name);
  }

  /** [method] 通过名称获取数据库 return {Database},失败返回null */
  public Database getDatabaseByName(String name) {
    if (!contains(name)) return null;
    return databases.get(name);
  }

  /** [method] 通过名称获取表 return {Table} 表，失败则返回null */
  public Table getTableByName(String databaseName, String tableName) {
    if (!contains(databaseName)) return null;
    return databases.get(databaseName).get(tableName);
  }

  /** [method] 写元数据 */
  public void writeMeta() {
    ArrayList<String> db_list = new ArrayList<>();
    for (String name : databasesList) {
      db_list.add(name);
    }
    this.meta.writeToFile(db_list);
  }

  /** [method] 创建数据库 */
  private void createDatabaseIfNotExists(String name) {
    if(contains(name)) throw new DuplicateDatabaseException();
    databases.put(name, new Database(name));
    databasesList.add(name);
    writeMeta();
  }

  /** [method] 删除数据库 */
  private void deleteDatabase(String name) {
    if(!contains(name)) throw new DatabaseNotExistException();
    databases.get(name).clearData();
    databases.remove(name);
    databasesList.remove(name);
    onlineDatabases.remove(name);
    writeMeta();
  }

  /** [method] 切换数据库 */
  public void switchDatabase(String name) {
    if (contains(name)) {
      if (!onlineDatabases.containsKey(name)) {
        databases.get(name).recover();
        onlineDatabases.put(name, 1);
      } else {
        onlineDatabases.replace(name, onlineDatabases.get(name) + 1);
      }
    } else {
      throw new DatabaseNotExistException();
    }
  }

  /** [method] 退出数据库 */
  public void quitDatabase(String name) {
    if (onlineDatabases.containsKey(name)) {
      int count = onlineDatabases.get(name);
      if (count > 1) onlineDatabases.replace(name, count - 1);
      else {
        onlineDatabases.remove(name);
        getDatabaseByName(name).persist();
      }
    }
  }

  /** [method] 展示所有数据库名称 */
  public String showAllDatabases() {
    StringBuffer info = new StringBuffer();
    for (String name : databasesList) {
      info.append(name + '\n');
    }
    return info.toString();
  }

  private static class ManagerHolder {
    private static final Manager INSTANCE = new Manager();

    private ManagerHolder() {}
  }
}
