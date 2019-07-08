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
import com.simple.server.util.MyLogger;


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
			MyLogger.warnStartBlock(getClass(), "SERVICE START CONFIG...");
			MyLogger.warnSingleHeader(getClass(),"PROPERTY:;SIZE:;");													
			res = appConfig.getRemoteLogService().getAllMsg(new RedirectRouting());						
			if(res != null && res.size() > 0) {
				MyLogger.warn(getClass(), "Redirect Routes;"+res.size());
				
				for(IContract msg: res){
					redirect = (RedirectRouting)msg;
					appConfig.setRedirectRoutingHashMap(redirect);				
				}
			} else {
				MyLogger.error(getClass(), "SERVICE START CONFIG: redirect routes is null!");
			}
				
			res2 = appConfig.getRemoteLogService().getAllMsg(new SessionFactory());
			if(res2 != null && res2.size() > 0) {
				MyLogger.warn(getClass(),"Session Factories;"+res2.size());
				for(IContract msg: res2){
					sf = (SessionFactory)msg;
					if(sf.getDefaultEndpointId())
						appConfig.setSessionFactories(sf.getEndpointGroupId(), sf.getEndpointId());			
				}
			} else {
				MyLogger.error(getClass(), "SERVICE START CONFIG: session factories is null!");
			}
			
						
			res3 = appConfig.getRemoteLogService().getAllMsg(new TimeoutPolicies());
			if(res3 != null && res3.size() > 0) {
				for(IContract msg: res3){			
					appConfig.timeoutPolicies = (TimeoutPolicies)msg;
					MyLogger.warn(getClass(), "Timeout Policies;"+res3.size());				
				}
			} else {
				MyLogger.error(getClass(), "SERVICE START CONFIG: timeout policies is null!");
			}
			
			
			res4 = appConfig.getRemoteLogService().getAllMsg(new DbUniGetter());
			if(res4 != null && res4.size() > 0) {
				MyLogger.warn(getClass(),"DB Uni Getter;"+res4.size());
				for(IContract msg: res4){
					dbUniGetter = (DbUniGetter)msg;
					appConfig.setdbUniGetHashMap(dbUniGetter);	
					dbUniGetter.setHibernateParamsMap(dbUniGetter.getHibernateParamsMap());
					dbUniGetter.setAppConfig(appConfig);									
				}
			} else {
				MyLogger.error(getClass(), "SERVICE START CONFIG: db uni getter is null!");
			}
			
			
	
			res6 = appConfig.getRemoteLogService().getAllMsg(new DbSecureUniGetter());			
			if(res6 != null && res6.size() > 0) {
				MyLogger.warn(getClass(),"DB Secure Uni Getter;"+res6.size());
				for(IContract msg: res6){
					dbSecureUniGetter = (DbSecureUniGetter)msg;
					appConfig.setSecureUniGetter(dbSecureUniGetter);	
					dbSecureUniGetter.setHibernateParamsMap(dbSecureUniGetter.getHibernateParamsMap());
					dbSecureUniGetter.setAppConfig(appConfig);			
				}
			} else {
				MyLogger.error(getClass(), "SERVICE START CONFIG: db secure uni getter is null!");
			}
			
			
			
			res5 = appConfig.getRemoteLogService().getAllMsg(new Login());
			if(res5 != null && res5.size() > 0) {
				MyLogger.warn(getClass(),"Logins;"+res5.size());
				for(IContract msg: res5){
					login = (Login)msg;					
					appConfig.setLoginHashMap(login.getLogin(), login);				
					customFilter.setAppConfig(appConfig);					
				}
			} else {
				MyLogger.error(getClass(), "SERVICE START CONFIG: logins is null!");
			}
			
			
			MyLogger.warnEndBlock(getClass(), "SERVICE COMPLETE CONFIG...");
		} catch (Exception e) {
			MyLogger.error(getClass(), e);			
		}		      			        
        list.clear();
    }
}
