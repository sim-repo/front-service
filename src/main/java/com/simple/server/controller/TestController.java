package com.simple.server.controller;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.simple.server.config.AppConfig;
import com.simple.server.config.OperationType;
import com.simple.server.domain.contract.BusSubMsg;
import com.simple.server.domain.contract.BusWriteMsg;
import com.simple.server.domain.contract.ConfirmMsg;
import com.simple.server.domain.contract.ErrPubMsg;
import com.simple.server.domain.contract.ErrSubMsg;
import com.simple.server.domain.contract.IncomingBufferMsg;
import com.simple.server.domain.contract.StatusMsg;
import com.simple.server.domain.contract.SuccessPubMsg;
import com.simple.server.domain.contract.UniMinMsg;
import com.simple.server.statistics.time.Timing;
import com.simple.server.util.MyLogger;

@SuppressWarnings("static-access")
@RestController
@RequestMapping("/test")
public class TestController {
	@Autowired
	private AppConfig appConfig;

	@RequestMapping(value = "/log/error", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public @ResponseBody String DividedByZeroGet() {
		try {
			MyLogger.info(getClass(), "Hello Moscow!");
			System.out.println(1/0);
		} catch(Exception e) {
			 MyLogger.error(getClass(), e);			
		}
		
		return "ok";
	}
	
	
	
	
	@RequestMapping(value = "nav/pub/success", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public StatusMsg jsonNavPubSuccess(@RequestBody SuccessPubMsg msg) {
		try {
			Thread.currentThread().sleep(Timing.getTimeMaxSleep());			
			msg.setMethodHandler("nav/pub/success");
			msg.setChannel(appConfig.getChannelBusBridge());
			msg.setLogClass(BusWriteMsg.class);
			msg.setOperationType(OperationType.WRITE);
			msg.setEndPointId(msg.getEndPointId());
			msg.setIsDirectInsert(true);
			appConfig.getQueueDirtyMsg().put(msg);
			return appConfig.getSuccessStatus();
		} catch (Exception e) {
			MyLogger.error(getClass(), e);				
			return new StatusMsg("406", e.toString());
		}
	}

	@RequestMapping(value = "nav/pub/err", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public StatusMsg jsonNavPubErr(@RequestBody ErrPubMsg msg) {
		try {
			Thread.currentThread().sleep(Timing.getTimeMaxSleep());			
			msg.setMethodHandler("nav/pub/err");
			msg.setChannel(appConfig.getChannelBusBridge());
			msg.setLogClass(BusWriteMsg.class);
			msg.setOperationType(OperationType.WRITE);
			msg.setEndPointId(msg.getEndPointId());
			msg.setIsDirectInsert(true);
			appConfig.getQueueDirtyMsg().put(msg);
			return appConfig.getSuccessStatus();
		} catch (Exception e) {
			MyLogger.error(getClass(), e);	
			return new StatusMsg("406", e.toString());
		}
	}

	@RequestMapping(value = "nav/pub/confirm", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public StatusMsg jsonConfirm(@RequestBody ConfirmMsg confirm) {
		try {
			Thread.currentThread().sleep(Timing.getTimeMaxSleep());			
			confirm.setMethodHandler("/async/json/sub/confirm");
			confirm.setChannel(appConfig.getChannelBusBridge());
			confirm.setLogClass(BusSubMsg.class);
			confirm.setOperationType(OperationType.WRITE);
			confirm.setEndPointId(confirm.getEndPointId());
			confirm.setIsDirectInsert(true);
			appConfig.getQueueDirtyMsg().put(confirm);
			return appConfig.getSuccessStatus();
		} catch (Exception e) {
			MyLogger.error(getClass(), e);	
			return new StatusMsg("406", e.toString());
		}
	}

	@RequestMapping(value = "oktell/uni/listener", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public StatusMsg jsonOktellListener(@RequestBody UniMinMsg msg) {
		try {
			Thread.currentThread().sleep(Timing.getTimeMaxSleep());			
			IncomingBufferMsg in = new IncomingBufferMsg();
			in.copyFrom(msg);

			in.setMethodHandler("oktell/uni/listener");
			in.setChannel(appConfig.getChannelBusBridge());
			in.setLogClass(BusWriteMsg.class);
			in.setOperationType(OperationType.WRITE);
			in.setEndPointId("OKTELL");
			in.setIsDirectInsert(false);
			appConfig.getQueueDirtyMsg().put(in);
			return appConfig.getSuccessStatus();
		} catch (Exception e) {
			MyLogger.error(getClass(), e);	
			return new StatusMsg("406", e.toString());
		}
	}

	@RequestMapping(value = "btx/uni/listener", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public StatusMsg jsonBtxListener(@RequestBody UniMinMsg msg) {
		try {
			Thread.currentThread().sleep(Timing.getTimeMaxSleep());			
			IncomingBufferMsg in = new IncomingBufferMsg();
			in.copyFrom(msg);

			in.setMethodHandler("btx/uni/listener");
			in.setChannel(appConfig.getChannelBusBridge());
			in.setLogClass(BusWriteMsg.class);
			in.setOperationType(OperationType.WRITE);
			in.setEndPointId("BTX");
			in.setIsDirectInsert(false);
			appConfig.getQueueDirtyMsg().put(in);
			return appConfig.getSuccessStatus();
		} catch (Exception e) {
			MyLogger.error(getClass(), e);	
			return new StatusMsg("406", e.toString());
		}
	}

	@RequestMapping(value = "btx/uni/listener2", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void jsonBtxListener2(@RequestBody UniMinMsg msg) {
		try {
			Thread.currentThread().sleep(Timing.getTimeMaxSleep());			

		} catch (Exception e) {
			MyLogger.error(getClass(), e);	
		}
	}

	@RequestMapping(value = "oktell/sub/err", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public StatusMsg jsonOktellSubErr(@RequestBody ErrSubMsg msg) {
		try {
			Thread.currentThread().sleep(Timing.getTimeMaxSleep());			
			msg.setMethodHandler("oktell/sub/err");
			msg.setChannel(appConfig.getChannelBusBridge());
			msg.setLogClass(BusWriteMsg.class);
			msg.setOperationType(OperationType.WRITE);
			msg.setEndPointId("OKTELL");
			msg.setIsDirectInsert(true);
			appConfig.getQueueDirtyMsg().put(msg);
			return appConfig.getSuccessStatus();
		} catch (Exception e) {
			MyLogger.error(getClass(), e);	
			return new StatusMsg("406", e.toString());
		}
	}

	@RequestMapping(value = "btx/sub/err", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public StatusMsg jsonBtxSubErr(@RequestBody ErrSubMsg msg) {
		try {
			Thread.currentThread().sleep(Timing.getTimeMaxSleep());
			msg.setMethodHandler("btx/sub/err");
			msg.setChannel(appConfig.getChannelBusBridge());
			msg.setLogClass(BusWriteMsg.class);
			msg.setOperationType(OperationType.WRITE);
			msg.setEndPointId("BTX");
			msg.setIsDirectInsert(true);
			appConfig.getQueueDirtyMsg().put(msg);
			return appConfig.getSuccessStatus();
		} catch (Exception e) {
			MyLogger.error(getClass(), e);	
			return new StatusMsg("406", e.toString());
		}
	}
	

	@RequestMapping(value = "/sync/get/json/nav/execOutcoming", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public @ResponseBody String jsonNavExecOutcomingGet(
			@RequestParam(value = "eventId", required = false) String eventId,		
			@RequestParam(value = "juuid", required = false) String juuid,
			@RequestParam(value = "endpointId", required = false) String endpointId) {
		
		
		StringBuilder sql = new StringBuilder("EXEC [dbo].[web_execOutcomingBuffer] ");
		

		
		 
		if (juuid != null) {
				sql.append("@_juuid = '" + juuid + "'");
		} else if (eventId != null) {
			sql.append("@_eventId = '" + eventId + "'");
		}	
		
		String res = null;
		try {
			res = appConfig.getRemoteService().getFlatJson(sql.toString(),
					endpointId != null ? endpointId : appConfig.getDefaultEndpointByGroupId(appConfig.navGroupId));
		} catch (Exception e) {
			MyLogger.error(getClass(), e);	
			return e.getMessage();
		}
		return res;
	}
}
