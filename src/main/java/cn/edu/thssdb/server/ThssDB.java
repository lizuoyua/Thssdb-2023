package cn.edu.thssdb.server;

import cn.edu.thssdb.rpc.thrift.IService;
import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.service.IServiceHandler;
import cn.edu.thssdb.utils.Global;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThssDB {

  private static final Logger logger = LoggerFactory.getLogger(ThssDB.class);

  private static IServiceHandler handler;
  private static IService.Processor processor;
  private static TServerSocket transport;
  private static TServer server;

  private Manager manager;

  public static ThssDB getInstance() {
    return ThssDBHolder.INSTANCE;
  }

  public static void main(String[] args) {
    if (args.length > 0) {
      //设置隔离级别并打印
      String level = args[0];
      switch (level) {
        case "U":
          Global.DATABASE_ISOLATION_LEVEL = Global.ISOLATION_LEVEL.READ_UNCOMMITTED;
          break;
        case "C":
          Global.DATABASE_ISOLATION_LEVEL = Global.ISOLATION_LEVEL.READ_COMMITTED;
          break;
        case "S":
          Global.DATABASE_ISOLATION_LEVEL = Global.ISOLATION_LEVEL.SERIALIZABLE;
          break;
      }
    }
    System.out.println("[ISOLATION LEVEL]: " + Global.DATABASE_ISOLATION_LEVEL);
    ThssDB server = ThssDB.getInstance();
    server.start();
  }

  private void start() {
    handler = new IServiceHandler();
    processor = new IService.Processor(handler);
    Runnable setup = () -> setUp(processor);
    new Thread(setup).start();
  }

  private static void setUp(IService.Processor processor) {
    try {
      transport = new TServerSocket(Global.DEFAULT_SERVER_PORT);
      server = new TThreadPoolServer(new TThreadPoolServer.Args(transport).processor(processor));
      logger.info("Starting ThssDB ...");
      server.serve();
    } catch (TTransportException e) {
      logger.error(e.getMessage());
    }
  }

  private static class ThssDBHolder {
    private static final ThssDB INSTANCE = new ThssDB();

    private ThssDBHolder() {}
  }
}
