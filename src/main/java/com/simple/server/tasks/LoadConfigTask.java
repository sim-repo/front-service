package com.simple.server.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.simple.server.config.AppConfig;
import com.simple.server.controller.CustomFilter;
import com.simple.server.domain.contract.DbSecureUniGetter;
import com.simple.server.domain.contract.DbUniGetter;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.Login;
import com.simple.server.domain.contract.RedirectRouting;
import com.simple.server.domain.contract.SessionFactory;
import com.simple.server.domain.contract.TimeoutPolicies;
import com.simple.server.mediators.CommandType;


@SuppressWarnings("static-access")
@Service("LoadConfigTask")
@Scope("prototype")
public class LoadConfigTask  extends AbstractTask {
	  
	@Autowired
	private AppConfig appConfig;
	
	@Autowired
	private CustomFilter customFilter;
	
	
	
	
    private List<IContract> list = new ArrayList<>();
	
	
    @Override
    public void update(Observable o, Object arg) {

        if(arg.getClass().equals(CommandType.class)) {
            switch ((CommandType) arg) {
                case WAKEUP_PROCESSING:
                case WAKEUP_ALL:
                    super.update(o, CommandType.WAKEUP_ALLOW);
                    break;
                case AWAIT_PROCESSING:
                case AWAIT_ALL:
                    super.update(o, CommandType.AWAIT_ALLOW);
                    break;
            }
        }      
    }
	
	
	@Override
    public void task() throws Exception {  
		      
		List<IContract> res = null;
		List<IContract> res2 = null;
		List<IContract> res3 = null;
		List<IContract> res4 = null;
		List<IContract> res5 = null;
		List<IContract> res6 = null;
		RedirectRouting redirect = null;
		SessionFactory sf = null;
		DbUniGetter dbUniGetter = null;
		DbSecureUniGetter dbSecureUniGetter = null;
		Login login = null;
				
		setDeactivateMySelfAfterTaskDone(true);
		
		Thread.currentThread().sleep(5000);		
		
		try {			
			
			System.out.println(">>>> FRONT-SERVICE::::LOADING CONFIG >>>>");
			
			System.out.println("Waiting for redirect routings ..");	
			res = appConfig.getRemoteLogService().getAllMsg(new RedirectRouting());
			System.out.println("redirect routings size: "+res.size());
			for(IContract msg: res){
				redirect = (RedirectRouting)msg;
				appConfig.setRedirectRoutingHashMap(redirect);				
			}
			
			System.out.println("Waiting for session factories ..");		
			res2 = appConfig.getRemoteLogService().getAllMsg(new SessionFactory());
			System.out.println("session factories size: "+res2.size());
			for(IContract msg: res2){
				sf = (SessionFactory)msg;
				if(sf.getDefaultEndpointId())
					appConfig.setSessionFactories(sf.getEndpointGroupId(), sf.getEndpointId());			
			}
			
			System.out.println("Waiting for timeout policies..");
			res3 = appConfig.getRemoteLogService().getAllMsg(new TimeoutPolicies());
			System.out.println("timeout policies size: "+res3.size());
			for(IContract msg: res3){			
				appConfig.timeoutPolicies = (TimeoutPolicies)msg;	
				System.out.println(String.format("timeout: %s %s %s %s %s %s", 
						appConfig.timeoutPolicies.getFrontSyncReadTimeout(), 
						appConfig.timeoutPolicies.getFrontSyncConnectionRequestTimeout(),
						appConfig.timeoutPolicies.getFrontSyncConnectionTimeout(),
						appConfig.timeoutPolicies.getBackAsyncReadTimeout(),
						appConfig.timeoutPolicies.getBackAsyncConnectionTimeout(),
						appConfig.timeoutPolicies.getBackAsyncConnectionRequestTimeout()
						));
			}
			
			System.out.println("Waiting for db uni getter..");
			res4 = appConfig.getRemoteLogService().getAllMsg(new DbUniGetter());
			System.out.println("db uni getter size: "+res4.size());
			for(IContract msg: res4){
				dbUniGetter = (DbUniGetter)msg;
				appConfig.setdbUniGetHashMap(dbUniGetter);	
				dbUniGetter.setHibernateParamsMap(dbUniGetter.getHibernateParamsMap());
				dbUniGetter.setAppConfig(appConfig);
				System.out.println(String.format("detail: %s %s %s %s", 
						dbUniGetter.getMethod(), 
						dbUniGetter.getEndpointId(),
						dbUniGetter.getExecutedFunctionName(),
						dbUniGetter.getFunctParamByWebParam()
						));
			}
			
			
			System.out.println("Waiting for db secure uni getter..");
			res6 = appConfig.getRemoteLogService().getAllMsg(new DbSecureUniGetter());
			System.out.println("db secure uni getter size: "+res6.size());
			for(IContract msg: res6){
				dbSecureUniGetter = (DbSecureUniGetter)msg;
				appConfig.setSecureUniGetter(dbSecureUniGetter);	
				dbSecureUniGetter.setHibernateParamsMap(dbSecureUniGetter.getHibernateParamsMap());
				dbSecureUniGetter.setAppConfig(appConfig);
				System.out.println(String.format("detail: %s %s %s %s", 
						dbSecureUniGetter.getMethod(), 
						dbSecureUniGetter.getEndpointId(),
						dbSecureUniGetter.getExecutedFunctionName(),
						dbSecureUniGetter.getFunctParamByWebParam()
						));
			}
			
			System.out.println("Waiting for logins ..");
			res5 = appConfig.getRemoteLogService().getAllMsg(new Login());
			System.out.println("login size: "+res5.size());
			for(IContract msg: res5){
				login = (Login)msg;
				
				appConfig.setLoginHashMap(login.getLogin(), login);				
				customFilter.setAppConfig(appConfig);
				System.out.println(String.format("detail: %s", 
						login.getLogin() 						
						));
			}
			
			System.out.println("<<<< FRONT-SERVICE::::LOADING CONFIG COMPLETE <<<<<");
		} catch (Exception e) {
			e.printStackTrace();
		}		      			
        throwToStatistic(list.size());
        list.clear();
    }
   	
}
