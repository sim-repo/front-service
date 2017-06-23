package com.simple.server.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.RedirectRouting;
import com.simple.server.domain.contract.StatusMsg;
import com.simple.server.mediators.Mediator;
import com.simple.server.mediators.Subscriber;
import com.simple.server.service.BusMsgService;
import com.simple.server.service.LogBusMsgService;
import com.simple.server.service.remote.IRemoteLogService;
import com.simple.server.service.remote.IRemoteService;
import com.simple.server.statistics.PerfomancerStat;


@Service("appConfig")
@Scope("singleton")
public class AppConfig {	
			
	public final static String ACC = "SIMPLE\\jservice";
	public final static String PSW = "j123Service";
	
	public final static String DATEFORMAT = "dd.MM.yyyy HH:mm:ss";
	
	
	private Subscriber subscriber = new Subscriber();
	
	@Autowired
	private ApplicationContext ctx;
	
	@Autowired
    private MessageChannel channelBusBridge;
	
	@Autowired
    private MessageChannel channelBusLog;
	
	@Autowired
	private MessageChannel channelSrvLog;
	
	@Autowired
	private MessageChannel channelAdminLog;

	@Autowired
	private PerfomancerStat perfomancerStat;
	
	@Autowired
	private BusMsgService busMsgService;
	
	@Autowired
	private LogBusMsgService logBusMsgService;
	
	ConcurrentHashMap<String,RedirectRouting> redirectRoutingsHashMap = new ConcurrentHashMap<String, RedirectRouting>();
	
    private LinkedBlockingQueue<IContract> queueDirtyPlainText = new LinkedBlockingQueue<>(100);
    private LinkedBlockingQueue<IContract> queueDirtyMsg = new LinkedBlockingQueue<>(100);
    private LinkedBlockingQueue<IContract> queueClientMsg = new LinkedBlockingQueue<>(100);
    private LinkedBlockingQueue<IContract> queueAdminMsg = new LinkedBlockingQueue<>(10);
         
	private Mediator mediator = new Mediator();    	        
    private StatusMsg successStatus = new StatusMsg("202","Accepted");   


	
	public ConcurrentHashMap<String, RedirectRouting> getRedirectRoutingsHashMap() {
		return redirectRoutingsHashMap;
	}

	public void setRedirectRoutingsHashMap(ConcurrentHashMap<String, RedirectRouting> redirectRoutingsHashMap) {
		this.redirectRoutingsHashMap = redirectRoutingsHashMap;
	}
	
	public void setRedirectRoutingHashMap(RedirectRouting routing){
		this.redirectRoutingsHashMap.put(routing.getMethodName(), routing);		
	}

	public LinkedBlockingQueue<IContract> getQueueAdminMsg() {  
  		return queueAdminMsg;
  	}

	public LinkedBlockingQueue<IContract> getQueueDirtyPlainText() {
		return queueDirtyPlainText;
	}

	public LinkedBlockingQueue<IContract> getQueueDirtyMsg() {
		return queueDirtyMsg;
	}		

	public LinkedBlockingQueue<IContract> getQueueClientMsg() {
		return queueClientMsg;
	}

	public IRemoteService getRemoteService(){
		return (IRemoteService)ctx.getBean("remoteService");
	}
	
	public IRemoteLogService getRemoteLogService(){
		return (IRemoteLogService)ctx.getBean("remoteLogService");
	}
	
	public MessageChannel getChannelAdminLog() {
		return channelAdminLog;
	}
	
	public MessageChannel getChannelBusLog() {
		return channelBusLog;
	}

	public MessageChannel getChannelBusBridge() {
		return channelBusBridge;
	}

	public MessageChannel getChannelSrvLog() {
		return channelSrvLog;
	}	

	public Mediator getMediator() {
		return mediator;
	}

	public Subscriber getSubscriber() {
		return subscriber;
	}

	public PerfomancerStat getPerfomancerStat() {
		return perfomancerStat;
	}

	public LogBusMsgService getLogBusMsgService() {
		return logBusMsgService;
	}

	public BusMsgService getBusMsgService() {
		return busMsgService;
	}		
	
	public StatusMsg getSuccessStatus() {
		return successStatus;
	}

	public void initQueueDirtyMsg(int size){
		this.queueDirtyMsg = new LinkedBlockingQueue<>(size);
	}
	
	public void initQueueAdminMsg(int size){
		this.queueAdminMsg = new LinkedBlockingQueue<>(size);
	}
	
	public void initQueueDirtyPlainText(int size){
		this.queueDirtyPlainText = new LinkedBlockingQueue<>(size);
	}
	
	public void initQueueClientMsg(int size){
		this.queueClientMsg = new LinkedBlockingQueue<>(size);
	}
		
}
