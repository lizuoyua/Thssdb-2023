package cn.edu.thssdb.schema;

import cn.edu.thssdb.exception.*;
import cn.edu.thssdb.query.QueryResult;
import cn.edu.thssdb.query.QueryTable;
import cn.edu.thssdb.utils.Global;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {

  private String name;
  private HashMap<String, Table> tables;//表的哈希表
  private ReentrantReadWriteLock lock;//可重入读写锁

  /** 新增属性 */
  private Meta meta;//meta类管理元数据文件的创建、读取、写入和删除

  private Logger logger; // 日志管理

  private ArrayList<Table> droppedTables; // 删除的表

  /** [method] 构造方法 */
  public Database(String name) throws CustomIOException{
    this.name = name;
    this.tables = new HashMap<>();
    this.lock = new ReentrantReadWriteLock();
    String folder = Paths.get(Global.DATA_ROOT_FOLDER, name).toString();
    String meta_name = name + ".meta";
    this.meta = new Meta(folder, meta_name);
    String logger_name = name + ".log";
    this.logger = new Logger(folder, logger_name);
    this.droppedTables = new ArrayList<>();

  }

  /** [method] 读取日志 */
  public Logger getLogger() {
    return logger;
  }

  /** [method] 判断表是否存在 */
  public boolean contains(String name) {
    return tables.containsKey(name);
  }

  /** [method] 获取表 */
  public Table get(String name) {
    if (!contains(name)) return null;
    return tables.get(name);
  }

  /** [method] 存储数据库（持久化） [note] 将数据库持久化存储 */
  public synchronized void persist() {
    ArrayList<String> keys = new ArrayList<>();
    for (String key : tables.keySet()) {
      tables.get(key).persist();
      keys.add(key);
    }
    for (Table table : droppedTables) {
      table.drop();
    }
    droppedTables.clear();
    this.meta.writeToFile(keys); // 目前 一行一个table名
    this.logger.eraseFile();
  }

  /** [method] 创建表 */
  public void create(String name, Column[] columns, int primaryIndex) {
    if (contains(name)) throw new DuplicateTableException();
    tables.put(name, new Table(this.name, name, columns, primaryIndex));
  }

  /** [method] 删除表 */
  public void drop(String name) {
    if(!contains(name)) throw new TableNotExistException();
    if (tables.get(name).lock.isWriteLocked()) throw new TableOccupiedException();
    droppedTables.add(tables.remove(name));
  }

  /** [method] 查询表 */
  public String select(QueryTable[] queryTables) {
    // TODO
    QueryResult queryResult = new QueryResult(queryTables);
    return null;
  }

  /** [method] 恢复数据库 [note] 从持久化数据中恢复数据库 */
  public synchronized void recover() {
    ArrayList<String[]> table_list = this.meta.readFromFile();
    // 目前 一行一个table名
    for (String[] table_info : table_list) {
      tables.put(table_info[0], new Table(this.name, table_info[0]));
    }
  }

  /** [method] 清除数据库中数据 */
  public void clearData(){
    for (Table table : tables.values()) {
      table.drop();
    }
    tables.clear();
    this.logger.deleteFile();
    this.meta.deleteFile();
    Paths.get(Global.DATA_ROOT_FOLDER, name).toFile().delete();
  }

  /** [method] 获取数据库中所有表 */
  public ArrayList<Table> getTables() {
    ArrayList<Table> res = new ArrayList<>();
    for (Table table : tables.values()) {
      res.add(table);
    }
    return res;
  }

  public void quit() {
    // TODO
  }
}
