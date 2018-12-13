package com.simple.server.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.simple.server.config.AppConfig;

import com.simple.server.domain.contract.IContract;
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
		RedirectRouting redirect = null;
		SessionFactory sf = null;
		
		setDeactivateMySelfAfterTaskDone(true);
		
		Thread.currentThread().sleep(5000);		
		
		try {			
			
			System.out.println(">>>> FRONT-SERVICE::::LOADING CONFIG >>>>");
			
			res = appConfig.getRemoteLogService().getAllMsg(new RedirectRouting());
			System.out.println("redirect routings size: "+res.size());
			for(IContract msg: res){
				redirect = (RedirectRouting)msg;
				appConfig.setRedirectRoutingHashMap(redirect);				
			}
			
					
			res2 = appConfig.getRemoteLogService().getAllMsg(new SessionFactory());
			System.out.println("session factories size: "+res2.size());
			for(IContract msg: res2){
				sf = (SessionFactory)msg;
				if(sf.getDefaultEndpointId())
					appConfig.setSessionFactories(sf.getEndpointGroupId(), sf.getEndpointId());			
			}
			
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
			
			System.out.println("<<<< FRONT-SERVICE::::LOADING CONFIG COMPLETE <<<<<");
		} catch (Exception e) {
			e.printStackTrace();
		}		      			
        throwToStatistic(list.size());
        list.clear();
    }
   	
}
