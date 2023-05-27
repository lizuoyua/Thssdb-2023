package cn.edu.thssdb.service;

import cn.edu.thssdb.plan.LogicalGenerator;
import cn.edu.thssdb.plan.LogicalPlan;
import cn.edu.thssdb.plan.impl.DropDatabasePlan;
import cn.edu.thssdb.plan.impl.UseDatabasePlan;
import  cn.edu.thssdb.schema.Logger;

import cn.edu.thssdb.exception.DatabaseOccupiedException;

import cn.edu.thssdb.rpc.thrift.ConnectReq;
import cn.edu.thssdb.rpc.thrift.ConnectResp;
import cn.edu.thssdb.rpc.thrift.DisconnectReq;
import cn.edu.thssdb.rpc.thrift.DisconnectResp;
import cn.edu.thssdb.rpc.thrift.ExecuteStatementReq;
import cn.edu.thssdb.rpc.thrift.ExecuteStatementResp;
import cn.edu.thssdb.rpc.thrift.GetTimeReq;
import cn.edu.thssdb.rpc.thrift.GetTimeResp;
import cn.edu.thssdb.rpc.thrift.IService;
import cn.edu.thssdb.rpc.thrift.Status;
import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.utils.Global;
import cn.edu.thssdb.utils.StatusUtil;
import org.apache.thrift.TException;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class IServiceHandler implements IService.Iface {

  private static final AtomicInteger sessionCnt = new AtomicInteger(0);

  /** 暂时没有进行用户定义时的权宜之计 */
  public String activeDatabaseName = "";

  @Override
  public GetTimeResp getTime(GetTimeReq req) throws TException {
    GetTimeResp resp = new GetTimeResp();
    resp.setTime(new Date().toString());
    resp.setStatus(new Status(Global.SUCCESS_CODE));
    return resp;
  }

  @Override
  public ConnectResp connect(ConnectReq req) throws TException {
    return new ConnectResp(StatusUtil.success(), sessionCnt.getAndIncrement());
  }
  @Override
  public DisconnectResp disconnect(DisconnectReq req) throws TException {
    return new DisconnectResp(StatusUtil.success());
  }

  @Override
  public ExecuteStatementResp executeStatement(ExecuteStatementReq req) throws TException {
    if (req.getSessionId() < 0) {
      return new ExecuteStatementResp(
          StatusUtil.fail("You are not connected. Please connect first."), false);
    }
    // TODO: implement execution logic
    LogicalPlan plan = LogicalGenerator.generate(req.statement);
    switch (plan.getType()) {
      case CREATE_DB:
      case CREATE_TBL:
        System.out.println("[DEBUG] " + plan);
        plan.setCurrentDatabase(activeDatabaseName);
        plan.exec();
        return new ExecuteStatementResp(StatusUtil.success(), false);
      case DROP_DB:
        System.out.println("[DEBUG] " + plan);
        for(Table table :
                Manager.getInstance()
                        .getDatabaseByName(((DropDatabasePlan) plan).getDatabaseName())
                        .getTables()){
          if(table.lock.isWriteLocked()) throw new DatabaseOccupiedException();
        }
        plan.exec();
        return new ExecuteStatementResp(StatusUtil.success(), false);
      case USE_DB:
        System.out.println("[DEBUG] " + plan);
        plan.setCurrentDatabase(activeDatabaseName);
        plan.exec();
        activeDatabaseName = ((UseDatabasePlan)plan).getDatabaseName();
        return new ExecuteStatementResp(StatusUtil.success(), false);

      //default:
    }
    return null;
  }
}
