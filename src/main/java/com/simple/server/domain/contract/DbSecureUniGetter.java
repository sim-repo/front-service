package com.simple.server.domain.contract;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.simple.server.config.AppConfig;
import com.simple.server.config.HandlerResultType;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(as = DbSecureUniGetter.class)
public class DbSecureUniGetter extends AContract{
	
private AppConfig appConfig;
	
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("id")
	protected Integer id;
	
	
	String method;
	String endpointId;
	Map<String, List<String>> webParamsByMethod;
	String executedFunctionName;
	Map<String, String> functParamByWebParam;
	
	
	String hibernateParamsMap;
	String resultHandlerType;
	String description;
	
	
	
	@Override
	public String getClazz() {
		return DbSecureUniGetter.class.getName();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEndpointId() {
		return endpointId;
	}


	public void setEndpointId(String endpointId) {
		this.endpointId = endpointId;
	}


	public String getMethod() {
		return method;
	}


	public void setMethod(String method) {
		this.method = method;
	}



	public AppConfig getAppConfig() {
		return appConfig;
	}

	public void setAppConfig(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	public Map<String, List<String>> getWebParamsByMethod() {
		return webParamsByMethod;
	}


	public void setWebParamsByMethod(Map<String, List<String>> webParamsByMethod) {
		this.webParamsByMethod = webParamsByMethod;
	}


	public String getExecutedFunctionName() {
		return executedFunctionName;
	}


	public void setExecutedFunctionName(String executedFunctionName) {
		this.executedFunctionName = executedFunctionName;
	}
	

	public HandlerResultType getResultHandlerType() {
		return HandlerResultType.fromValue(resultHandlerType);
	}


	public void setResultHandlerType(HandlerResultType resultHandlerType) {
		this.resultHandlerType = resultHandlerType.toValue();
	}
	

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, String> getFunctParamByWebParam() {
		return functParamByWebParam;
	}
	
	public String getFunctParamByWebParam(String webParam) {
		return functParamByWebParam.get(webParam);
	}


	public void setFunctParamByWebParam(Map<String, String> functParamByWebParam) {
		this.functParamByWebParam = functParamByWebParam;
	}


	public String getHibernateParamsMap() {
		return hibernateParamsMap;
	}

	public void setHibernateParamsMap(String hibernateParamsMap) {
		this.hibernateParamsMap = hibernateParamsMap;
		this.webParamsByMethod = parseWebParamsByMethod(hibernateParamsMap);
		this.functParamByWebParam = parseFunctionParamByWebParam(hibernateParamsMap);	
	} 
	
	
	
	private Map<String, List<String>> parseWebParamsByMethod(String hibernateParamsMap) {
		Map<String, List<String>> res = new HashMap<>();

	    if (StringUtils.isEmpty(hibernateParamsMap)) {
	        return res;
	    }
	    String[] parameters = hibernateParamsMap.split(";");

	    List<String> webParamNames = new ArrayList<String>();
	    	    
	    for (String parameter : parameters) {	    	
	       String[] keyValuePair = parameter.split(":");
	       webParamNames.add(keyValuePair[0]);
	    }
	    res.put(this.method, webParamNames);
	    
	    return res;
	}

	
	
	
	private Map<String, String> parseFunctionParamByWebParam(String hibernateParamsMap) {
	    Map<String, String> res = new HashMap<>();
	    if (StringUtils.isEmpty(hibernateParamsMap)) {
	        return res;
	    }

	    String[] parameters = hibernateParamsMap.split(";");

	    for (String parameter : parameters) {
	        String[] keyValuePair = parameter.split(":");
	        if (keyValuePair.length == 2) {
	        	res.put(keyValuePair[0], keyValuePair[1]);
	        } else {
	        	throw new IllegalArgumentException(hibernateParamsMap);
	        }
	    }
	    return res;
	}
	
	
	
	
	
	public static String runDbStatement(DbSecureUniGetter getter, String method, String params) {
		Map<String, String> queryParameters = getQueryParameters(params);

		List<String> funcParamKeys = new ArrayList<String>();
		List<String> funcParamValues = new ArrayList<String>();
		for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
			String webParam = entry.getKey();
			String functionParam = getter.getFunctParamByWebParam(webParam);
			if (functionParam != null && functionParam != "") {
				funcParamKeys.add(functionParam);
				funcParamValues.add(entry.getValue());         
			}
    	} 
		StringBuilder sqlStatement = new StringBuilder();
		sqlStatement.append(getter.executedFunctionName + " ");
			
		if (funcParamKeys.size() > 0) {
			for(int i=0; i < funcParamKeys.size(); i++){
				sqlStatement.append(funcParamKeys.get(i)+"="+funcParamValues.get(i)+","); 
	        }
			sqlStatement.deleteCharAt(sqlStatement.length()-1);
		}		
		sqlStatement.append(", @_secured = 1");
		System.out.println(sqlStatement.toString());		
		return execStatement(getter, sqlStatement.toString(), getter.endpointId);		
	}
	

	
	public static String runDbStatement(DbSecureUniGetter getter, String method, Map<String, String> params) {

		List<String> funcParamKeys = new ArrayList<String>();
		List<String> funcParamValues = new ArrayList<String>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String webParam = entry.getKey();
			String functionParam = getter.getFunctParamByWebParam(webParam);
			if (functionParam != null && functionParam != "") {
				funcParamKeys.add(functionParam);
				funcParamValues.add(entry.getValue());         
			}
    	} 
		StringBuilder sqlStatement = new StringBuilder();
		sqlStatement.append(getter.executedFunctionName + " ");
			
		if (funcParamKeys.size() > 0) {
			for(int i=0; i < funcParamKeys.size(); i++){			
				sqlStatement.append(funcParamKeys.get(i)+"="+funcParamValues.get(i)+","); 
	        }
		}
		sqlStatement.append(", @_secured = 1");
		System.out.println(sqlStatement.toString());		
		return execStatement(getter, sqlStatement.toString(), getter.endpointId);		
	}
	
	
	

	private static String execStatement(DbSecureUniGetter getter, String sql, String endpointId) {
		 switch (getter.getResultHandlerType()) {
	         case flatXML:  
	        	 return runFlatXML(getter, sql, endpointId);	
	         
	         case flatJSON:  
	        	 return runFlatJSON(getter,sql, endpointId);	        	 
	        	 	        	 
	         case complexJSON:  
	        	 return runComplexJSON(getter,sql, endpointId);
	        	 
	         case firstFlatJSON:  
	        	 return runFirstFlatJson(getter,sql, endpointId);
	         
	         default: break;
		 }
		 return "Something Wrong: check [routing db exec].[result handler]";
	}
	
	
	
	
	private static String runFlatJSON(DbSecureUniGetter getter, String sql, String endpointId) {
		String res = null;
		System.out.println(endpointId+ " : "+sql);
		try {
			res = getter.appConfig.getRemoteService().getFlatJson(sql,
					endpointId != null ? endpointId : getter.appConfig.getDefaultEndpointByGroupId(getter.appConfig.navGroupId));
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return res;
	}
	
	
	
	
	private static String runFlatXML(DbSecureUniGetter getter, String sql, String endpointId) {
		String res = null;
		try {
			res = getter.appConfig.getRemoteService().getFlatXml(sql,
					endpointId != null ? endpointId : getter.appConfig.getDefaultEndpointByGroupId(getter.appConfig.navGroupId));
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return res;
	}
	
	
	
	private static String runComplexJSON(DbSecureUniGetter getter, String sql, String endpointId) {
		String res = null;
		try {
			res = getter.appConfig.getRemoteService().getComplexJson(sql,
					endpointId != null ? endpointId : getter.appConfig.getDefaultEndpointByGroupId(getter.appConfig.navGroupId));
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return res;
	}
	
	
	
	private static String runFirstFlatJson(DbSecureUniGetter getter, String sql, String endpointId) {
		String res = null;
		try {
			res = getter.appConfig.getRemoteService().getFlatJsonFirstObj(sql,
					endpointId != null ? endpointId : getter.appConfig.getDefaultEndpointByGroupId(getter.appConfig.navGroupId));
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return res;
	}
	
	
	
	
	
	private static  Map<String, String> getQueryParameters(String queryString) {
	    Map<String, String> queryParameters = new HashMap<>();
	    String encoded = "";
	    try {
	    	encoded = URLDecoder.decode( queryString, "UTF-8" );	    	
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    if (StringUtils.isEmpty(encoded)) {
	        return queryParameters;
	    }

	    String[] parameters = encoded.split("&");

	    for (String parameter : parameters) {
	        String[] keyValuePair = parameter.split("=");	        
	        queryParameters.put(keyValuePair[0], keyValuePair[1]);
	    }
	    return queryParameters;
	}
	
}
