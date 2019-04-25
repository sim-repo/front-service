package com.simple.server.service.remote;

import java.util.List;

import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.Login;


public interface IRemoteLogService {
	List<IContract> getAllMsg(IContract msg) throws Exception;
	Boolean putLogin(Login msg) throws Exception;
	List<IContract> getMsgBySqlCriteria(IContract msg, String sql) throws Exception;
}
