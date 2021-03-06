package com.synyi.edc.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;

public class SqlBuilderUtils {

	/**
	 * sql生成分组方法
	 * @param jsonStr
	 * @param stepStr
	 * @return
	 */
	public static String genGroupSql(String jsonStr,String stepStr){
		Map<String,Map> temp1 = null;  //入排条件1
		Map temp2 = null;  //入排条件2
		List<Map> lis = (List)JSON.parse(jsonStr);
		if(!CollectionUtils.isEmpty(lis)){
			temp1 = lis.get(0);
			if(lis.size()>1){
				temp2 = lis.get(1);
			}
		}
		
		System.out.println(stepStr);
		Map stepMap = (Map)JSON.parse(stepStr);;
		
		int maxValue = 0;
		int minValue = 0;
		int interval = 0;
		
		try{
			if(StringUtils.isNotBlank((String)stepMap.get("maxValue"))){
				maxValue = Integer.parseInt((String)stepMap.get("maxValue"));
			}
			if(StringUtils.isNotBlank((String)stepMap.get("minValue"))){
				minValue = Integer.parseInt((String)stepMap.get("minValue"));
			}
			if(StringUtils.isNotBlank((String)stepMap.get("interval"))){
				interval = Integer.parseInt((String)stepMap.get("interval"));
			}
		}catch(Exception e){}
		
		String groupType = stepMap.get("groupType")==null?"":(String)stepMap.get("groupType");
		String event1 = (String)temp1.get("事件").get("event_type");
		String event2 = temp2!=null?(String)((Map)temp2.get("事件")).get("event_type"):"";
		
		String returnSql = "";
		switch(event1){
			case "diagnosis":returnSql = "with temp1 as ("+event_diagnose(temp1,stepMap)+")";break;
			case "laboratoryExamination":returnSql = "with temp1 as ("+event_lab(temp1,stepMap)+")";break;
			case "drugUse":returnSql = "with temp1 as ("+event_drug(temp1,stepMap)+")";break;
			case "operation":returnSql = "with temp1 as ("+event_operation(temp1,stepMap)+")";break;
		}
		
		
		if(!CollectionUtils.isEmpty(temp2)){
			switch(event2){
				case "diagnosis":returnSql += ", <br/> temp2 as ("+event_diagnose(temp2,stepMap)+")";break;
				case "laboratoryExamination":returnSql += ", <br/> temp2 as ("+event_lab(temp2,stepMap)+")";break;
				case "drugUse":returnSql += ", <br/> temp2 as ("+event_drug(temp2,stepMap)+")";break;
				case "operation":returnSql += ", <br/> temp2 as ("+event_operation(temp2,stepMap)+")";break;
			}
			
			String rel = (String)temp2.get("关系");
			if("or".equals(rel)){
				returnSql += " <br/> select ";
				
				if(StringUtils.isNotBlank(groupType)){
					switch((String)stepMap.get("groupType")){
						case "年龄段":returnSql += generateAgePart(minValue,maxValue,interval)+" 年龄段,";break;
						case "年份":returnSql += "to_char(event_time,'yyyy') 年份,";break;
						case "医院":returnSql += "t.org_code,org.org_name,";break;
						case "就诊类型":returnSql += "visit_type,";break;
						case "性别":returnSql += "case when c.sex_code in ('2','9') then '女' when c.sex_code ='1' then '男' else '未知' end 性别,";break;
					}
				}
				
				if("patientsNumber".equals((String)stepMap.get("queryType"))){
					returnSql += "count(distinct t.patient_id) 人数";
				}else{
					returnSql += "count(t.patient_id) 人次";
				}
				returnSql += " <br/> from (select * from temp1 union all  select * from temp2 )t ";
				if("性别".equals((String)stepMap.get("groupType"))||"年龄段".equals((String)stepMap.get("groupType"))){
					returnSql += " join patient.patient_master_info c on t.patient_id=c.patient_id <br/>";
				}
				if("医院".equals((String)stepMap.get("groupType"))){
					returnSql += " join mdm.organization org on t.org_code=org.org_code  <br/>";
				}
				if(StringUtils.isNotBlank(groupType)){
					returnSql += " group by ";
					switch((String)stepMap.get("groupType")){
						case "年龄段":returnSql += generateAgePart(minValue,maxValue,interval);break;
						case "年份":returnSql += "to_char(event_time,'yyyy') ";break;
						case "医院":returnSql += "t.org_code,org.org_name";break;
						case "就诊类型":returnSql += "visit_type";break;
						case "性别":returnSql += "case when c.sex_code in ('2','9') then '女' when c.sex_code ='1' then '男' else '未知' end";break;
					}
				}
			}else if("and".equals(rel)){
				returnSql += "select count(1) 人数 from (select distinct temp1.patient_id from temp1)a join (select distinct temp2.patient_id from temp2 ) b on a.patient_id=b.patient_id";
			}
		}else{//只有temp1的情况
			
			returnSql += "<br/> select ";
			if(StringUtils.isNotBlank(groupType)){
				switch((String)stepMap.get("groupType")){
					case "年龄段":returnSql += generateAgePart(minValue,maxValue,interval)+" 年龄段,";break;
					case "年份":returnSql += "to_char(diag_time,'yyyy') 年份,";break;
					case "医院":returnSql += "temp1.org_code,org.org_name,";break;
					case "就诊类型":returnSql += "visit_type,";break;
					case "性别":returnSql += "case when c.sex_code in ('2','9') then '女' when c.sex_code ='1' then '男' else '未知' end 性别,<br/>";break;
				}
			}
			if("patientsNumber".equals((String)stepMap.get("queryType"))){
				returnSql += "count(distinct temp1.patient_id) 人数";
			}else{  //visitsNumber 人次
				returnSql += "count(temp1.patient_id) 人次";
			}
			returnSql += " from temp1 ";
			
			if("性别".equals((String)stepMap.get("groupType"))||"年龄段".equals((String)stepMap.get("groupType"))){
				returnSql += " join patient.patient_master_info c on temp1.patient_id=c.patient_id <br/> ";
			}
			if("医院".equals((String)stepMap.get("groupType"))){
				returnSql += " join mdm.organization org on temp1.org_code=org.org_code  <br/>";
			}
			
			if(StringUtils.isNotBlank(groupType)){
				returnSql += "group by ";
				
				switch((String)stepMap.get("groupType")){
					case "年龄段":returnSql += generateAgePart(minValue,maxValue,interval);break;
					case "年份":returnSql += "to_char(diag_time,'yyyy') ";break;
					case "医院":returnSql += "temp1.org_code,org.org_name";break;
					case "就诊类型":returnSql += "visit_type";break;
					case "性别":returnSql += "case when c.sex_code in ('2','9') then '女' when c.sex_code ='1' then '男' else '未知' end";break;
				}
			}
		}
		return returnSql;
	}
	/**
	 * 按照年龄段分组方法
	 * @param min  最小年龄
	 * @param max  最大年龄
	 * @param interval  年龄间隔
	 * @return
	 */
	  public static String generateAgePart(int min,int max,int interval){
	    	String str = " case";
	    	
	    	int temp = min;
	    	for(int i=min;i<max;i+=interval){
	    		if(i>min){
	    			//System.out.println(temp+":"+i);
	    			if(temp==min){
	    				str += " when extract(year from age(diag_time,c.birth_date))  >="+temp +" and extract(year from age(diag_time,c.birth_date)) <"+i+" then '"+temp+"到"+i+"'<br/>";
//	    			
	    				}else if(i==max){
//	    				str += " when extract(year from age(temp1.diag_time,c.birth_date)) <="+max+" then '"+temp+"到"+max+"' end  ";
	    			}else{
	    				str += " when extract(year from age(diag_time,c.birth_date)) <"+i+" then '"+temp+"到"+i+"'<br/>";
	    			}	
	    			temp = i;
	    		}
	    	}
	    	str += " when extract(year from age(diag_time,c.birth_date)) <="+max+" then '"+temp+"到"+max+"' end <br/> ";
	    //	System.out.println(str);
	    	return str;
	    }
	/**
	 * 按照事件  为  诊断 的sql语句生成方法
	 * @param temp1
	 * @param groupMap
	 * @return
	 */
	  public static String event_diagnose(Map<String,Map> temp1,Map groupMap){
	    	/*{"事件":{"diag_standard":"I21,I22,I23","event_type":"诊断"},"医院":{"org_code":"9983838x,111999441,2224589985,112445580"},
			 * "就诊类型":{"visit_type":"门诊"},"时间":{"time_from":"绝对时间:2017-01-01","time_to":"绝对时间:2017-01-10"}}
			 * 
			 * {\"事件\":{\"diag_standard\":[\"I220\",\"I230\"],\"diag_primitive\":[\"I220\",\"I230\"],\"event_type\":\"diagnosis\"},
			 * \"医院\":{\"org_code\":[\"111111\",\"222222\"]},\"就诊类型\":{\"visit_type\":\"hospitalization\"},\"时间\":{\"time_from\":\"2018-7-2\",\"time_to\":\"2018-8-12\"},
			 * \"关系\":\"or\"}
			 * 
			 * */
			String resultSql = "";
			String condition = "";
			//String eventType = "";
			
			for(String str:temp1.keySet()){
				if("事件".equals(str)){  //"事件":{"diag_standard":"I21,I22,I23","event_type":"诊断"}
					Map temp = temp1.get(str);
					//if(temp.get("event_type").equals("diagnosis")){
						//eventType = (String)temp.get("event_type");
						//,diag_time,org_code,case when visit_type='O' then '门诊' when visit_type='I' then '住院' when is_emergency=true then '急诊' else '未知' end visit_type
						resultSql += " select * from(select a.patient_id";
								String groupType = groupMap.get("groupType")==null?"":(String)groupMap.get("groupType");
								if(StringUtils.isNotBlank(groupType)){//"{\"groupType\":\"医院\",\"interval\":\"\",\"maxValue\":\"\",\"minValue\":\"\"}";
									switch(groupType){
										case "年龄段":resultSql += ",coalesce(a.diag_time,b.visit_time) diag_time";break;
										case "年份":resultSql += ",coalesce(a.diag_time,b.visit_time) diag_time";break;
										case "医院":resultSql += ",a.org_code";break;
										case "就诊类型":resultSql += ",case when b.visit_type='O' then '门诊' when b.visit_type='I' then '住院' when is_emergency=true then '急诊' else '未知' end visit_type";break;
										case "性别":resultSql += "";break;
									}
								}
								
								resultSql += ",jsonb_array_elements(diag_sycode_set) "+
										"as sycode  <br/> from diag.patient_diagnose a ";
								
								String time_from = temp1.get("时间")==null?"":(String)temp1.get("时间").get("time_from");
								String visit_type =  temp1.get("就诊类型")==null?"":(String)temp1.get("就诊类型").get("visit_type");
								if(StringUtils.isNotBlank(time_from)||StringUtils.isNotBlank(visit_type)){
									resultSql += " left join visit.visit_record b on a.visit_id = b.visit_id ";
								}else{
									if(StringUtils.isNotBlank(groupType)){
										switch((String)groupMap.get("groupType")){
											case "年龄段":resultSql += " left join visit.visit_record b on a.visit_id = b.visit_id ";break;
											case "年份":resultSql += " left join visit.visit_record b on a.visit_id = b.visit_id ";break;
											case "就诊类型":resultSql += " left join visit.visit_record b on a.visit_id = b.visit_id ";break;
											//case "性别":resultSql += "";break;
										}
									}
								}									
								resultSql += "where 1=1 $condition$ ) t1  <br/> ";
						
						List<String> diag_list = (List)temp.get("diag_standard");
						List<String> diag_primitive = (List)temp.get("diag_primitive");
						if(diag_list!=null&&diag_list.size()>0){
							resultSql += " join (select diag_code,diag_code2,diag_name from mdm.diagnose a where a.code_sys_id=30 <br/> ";
							
							if(diag_list.size()>1){
								for(int i = 0;i<diag_list.size();i++){
									String diag_c = diag_list.get(i);
									if(i==0){
										resultSql += " and ( diag_code2 like '"+diag_c+"%'";
									}else if(i==diag_list.size()-1){
										resultSql += " or diag_code2 like '"+diag_c+"%')";
									}else{
										resultSql += " or diag_code2 like '"+diag_c+"%'";
									}
								}
							}else{
								resultSql += " and diag_code2 like '"+diag_list.get(0)+"%'";
							}
							resultSql += " <br/> )t2 on replace(t1.sycode::varchar(50),'\"','')  =t2.diag_code";
						}else if(diag_primitive!=null&&diag_primitive.size()>0){
							
							resultSql += " join (select diag_code,diag_code2,diag_name from mdm.diagnose a where a.code_sys_id=30 <br/> ";
							
							if(diag_primitive.size()>1){
								for(int i = 0;i<diag_primitive.size();i++){
									String diag_c = diag_primitive.get(i);
									if(i==0){
										resultSql += " and ( diag_name like '%"+diag_c+"%'";
									}else if(i==diag_primitive.size()-1){
										resultSql += " or diag_name like '%"+diag_c+"%')";
									}else{
										resultSql += " or diag_name like '%"+diag_c+"%'";
									}
								}
							}else{
								resultSql += " and diag_name like '%"+diag_primitive.get(0)+"%'";
							}
							resultSql += " <br/> )t2 on replace(t1.sycode::varchar(50),'\"','')  =t2.diag_code";
						}
					//}
				}else if("医院".equals(str)){ //"医院":{"org_code":"9983838x,111999441,2224589985,112445580"}
					Map temp = temp1.get(str);
					try{
						List<String> org_list = (List)temp.get("org_code");
						if(!CollectionUtils.isEmpty(org_list)){
							if(org_list.size()>1){
								for(int i = 0;i<org_list.size();i++){
									String org_code = org_list.get(i);
									if(i==0){
										condition += " and  a.org_code in ('"+org_code+"',";
									}else if(i==org_list.size()-1){
										condition += "'"+org_code+"')";
									}else{
										condition += "'"+org_code+"',";
									}
								}
							}else{
								condition += " and a.org_code = '"+org_list.get(0)+"'";
							}
						}
					}catch(java.lang.ClassCastException e){}
					
				}else if("时间".equals(str)){//"时间":{"time_from":"绝对时间:2017-01-01","time_to":"绝对时间:2017-01-10","time_type":"诊断时间"}
					Map temp = temp1.get(str);
					String time_from = (String)temp.get("time_from");
					String time_to = (String)temp.get("time_to"); 
					condition += " <br/> ";
					if(StringUtils.isNoneBlank(time_from)&&StringUtils.isNoneBlank(time_to)){
						condition += " and coalesce(a.diag_time,b.visit_time) between '"+time_from +"' and '"+time_to+"'";
					}
					
//					switch(eventType){
//						case "诊断":condition+= "and coalesce(a.diag_time,b.visit_time) ";break;
//						case "检验时间":condition+= "and report_time ";break;
//						case "用药时间":condition+= "and drug_time ";break;
//						case "手术时间":condition+= "and oper_time ";break;
//					}
				}else if("就诊类型".equals(str)){//"就诊类型":{"visit_type":"门诊"}
					Map temp = temp1.get(str);
					String visit_type = (String)temp.get("visit_type");
					condition += " <br/> ";
					switch(visit_type){
						case "outpat":condition+= " and b.visit_type='O' ";break;
						case "inpat":condition+= " and b.visit_type='I' ";break;
						case "emergency":condition+= " and b.is_emergency=true ";break;
					}
				}
			}
			
			resultSql = resultSql.replace("$condition$", condition);
			return resultSql;
	    }
	  public static String event_lab(Map<String,Map> temp1,Map groupMap){
	    	/*{"事件":{"diag_standard":"I21,I22,I23","event_type":"诊断"},"医院":{"org_code":"9983838x,111999441,2224589985,112445580"},
			 * "就诊类型":{"visit_type":"门诊"},"时间":{"time_from":"绝对时间:2017-01-01","time_to":"绝对时间:2017-01-10"}}
			 * 
			 * {\"事件\":{\"diag_standard\":[\"I220\",\"I230\"],\"diag_primitive\":[\"I220\",\"I230\"],\"event_type\":\"diagnosis\"},
			 * \"医院\":{\"org_code\":[\"111111\",\"222222\"]},\"就诊类型\":{\"visit_type\":\"hospitalization\"},\"时间\":{\"time_from\":\"2018-7-2\",\"time_to\":\"2018-8-12\"},
			 * \"关系\":\"or\"}
			 * 
			 * */
			String resultSql = "";
			String condition = "";
			//String eventType = "";
			
			for(String str:temp1.keySet()){
				if("事件".equals(str)){  //"事件":{"diag_standard":"I21,I22,I23","event_type":"诊断"}
					Map temp = temp1.get(str);
					//if(temp.get("event_type").equals("diagnosis")){
						//eventType = (String)temp.get("event_type");
						//,diag_time,org_code,case when visit_type='O' then '门诊' when visit_type='I' then '住院' when is_emergency=true then '急诊' else '未知' end visit_type
						resultSql += " select d.patient_id";
								String groupType = groupMap.get("groupType")==null?"":(String)groupMap.get("groupType");
								if(StringUtils.isNotBlank(groupType)){//"{\"groupType\":\"医院\",\"interval\":\"\",\"maxValue\":\"\",\"minValue\":\"\"}";
									switch(groupType){
										case "年龄段":resultSql += ",d.report_time event_time";break;
										case "年份":resultSql += ",d.report_time event_time";break;
										case "医院":resultSql += ",d.org_code";break;
										case "就诊类型":resultSql += ",case when e.visit_type='O' then '门诊' when e.visit_type='I' then '住院' when is_emergency=true then '急诊' else '未知' end visit_type";break;
										case "性别":resultSql += "";break;
									}
								}
								
								resultSql += " from lab.lab_report_result a join  mdm.mdm_map b on a.test_item_id=b.source_id"+
												" <br/> join mdm.lis_item c on  b.map_id=c.item_id"+ 
												" join lab.lab_report d on a.report_id=d.report_id ";
								
								String visit_type =  temp1.get("就诊类型")==null?"":(String)temp1.get("就诊类型").get("visit_type");
								if(StringUtils.isNotBlank(visit_type)){
									resultSql += " left join visit.visit_record e  <br/> on d.visit_id = e.visit_id ";
								}								
								resultSql += " where b.md_type = 'lis_item' and c.code_sys_id =197  $condition$  ";
						
			//{\"lab_standard\":[\"220\",\"230\"],\"character_result\":[\"偏高\",\"异常\"],\"exception_symbol\":[\"1\",\"2\"],\"number_result\":[\">=1\"],\"event_type\":\"lab\"}
						List<String> lab_list = (List)temp.get("lab_standard");
						List<String> character_result = (List)temp.get("character_result");
						List<String> exception_symbol = (List)temp.get("exception_symbol");
						List<String> number_result = (List)temp.get("number_result");
						if(lab_list!=null&&lab_list.size()>0){
							resultSql += " and c.item_id in (";
							String tempLab = "";
							for(String lab:lab_list){
								tempLab += "'"+lab+"',";
							}
							resultSql += tempLab.substring(0,tempLab.length()-1)+")";
							
						}
						if(character_result!=null&&character_result.size()>0){
							resultSql += " and a.text_value in (";
							String tempLab = "";
							for(String lab:character_result){
								tempLab += "'"+lab+"',";
							}
							resultSql += tempLab.substring(0,tempLab.length()-1)+")";
						}
						if(exception_symbol!=null&&exception_symbol.size()>0){
							resultSql += " and a.abnormal_flag_name in (";
							String tempLab = "";
							for(String lab:exception_symbol){
								tempLab += "'"+lab+"',";
							}
							resultSql += tempLab.substring(0,tempLab.length()-1)+")";
						}
						if(number_result!=null&&number_result.size()>0){
							resultSql += " and a.numerical_value "+number_result.get(0);
						}
					//}
				}else if("医院".equals(str)){ //"医院":{"org_code":"9983838x,111999441,2224589985,112445580"}
					Map temp = temp1.get(str);
					try{
						List<String> org_list = (List)temp.get("org_code");
						if(!CollectionUtils.isEmpty(org_list)){
							if(org_list.size()>1){
								for(int i = 0;i<org_list.size();i++){
									String org_code = org_list.get(i);
									if(i==0){
										condition += " and  d.org_code in ('"+org_code+"',";
									}else if(i==org_list.size()-1){
										condition += "'"+org_code+"')";
									}else{
										condition += "'"+org_code+"',";
									}
								}
							}else{
								condition += " and d.org_code = '"+org_list.get(0)+"'";
							}
						}
					}catch(java.lang.ClassCastException e){}
					
				}else if("时间".equals(str)){//"时间":{"time_from":"绝对时间:2017-01-01","time_to":"绝对时间:2017-01-10","time_type":"诊断时间"}
					Map temp = temp1.get(str);
					String time_from = (String)temp.get("time_from");
					String time_to = (String)temp.get("time_to");
					condition += " <br/> ";
					if(StringUtils.isNoneBlank(time_from)&&StringUtils.isNoneBlank(time_to)){
						condition += " and d.report_time between '"+time_from +"' and '"+time_to+"'";
					}
					
//					switch(eventType){
//						case "诊断":condition+= "and coalesce(a.diag_time,b.visit_time) ";break;
//						case "检验时间":condition+= "and report_time ";break;
//						case "用药时间":condition+= "and drug_time ";break;
//						case "手术时间":condition+= "and oper_time ";break;
//					}
				}else if("就诊类型".equals(str)){//"就诊类型":{"visit_type":"门诊"}
					Map temp = temp1.get(str);
					String visit_type = (String)temp.get("visit_type");
					condition += " <br/> ";
					switch(visit_type){
						case "outpat":condition+= " and e.visit_type='O' ";break;
						case "inpat":condition+= " and e.visit_type='I' ";break;
						case "emergency":condition+= " and e.is_emergency=true ";break;
					}
				}
			}
			
			resultSql = resultSql.replace("$condition$", condition);
			return resultSql;
	    }
	  
	  public static String event_drug(Map<String,Map> temp1,Map groupMap){
	    	/*{\"事件\":{\"drug_standard\":[\"220\",\"230\"],\"drug_category\":[\"330\",\"543\"],\"event_type\":\"drug\"},\"医院\":{\"org_code\":[\"111111\",\"222222\"]},
	    	 * \"就诊类型\":{\"visit_type\":\"inpat\"},\"时间\":{\"time_from\":\"2018-7-2\",\"time_to\":\"2018-8-12\"},\"关系\":\"or\"},
			 * 
			 * */
			String resultSql1 = "";
			String resultSql2 = "";
			String condition1 = "";
			String condition2 = "";
			//String eventType = "";
			
			for(String str:temp1.keySet()){
				if("事件".equals(str)){  //"事件":{"diag_standard":"I21,I22,I23","event_type":"诊断"}
					Map temp = temp1.get(str);
					//if(temp.get("event_type").equals("diagnosis")){
						//eventType = (String)temp.get("event_type");
						//,diag_time,org_code,case when visit_type='O' then '门诊' when visit_type='I' then '住院' when is_emergency=true then '急诊' else '未知' end visit_type
						resultSql1 += " select t1.patient_id";
						resultSql2 += " select t1.patient_id";
								String groupType = groupMap.get("groupType")==null?"":(String)groupMap.get("groupType");
								if(StringUtils.isNotBlank(groupType)){//"{\"groupType\":\"医院\",\"interval\":\"\",\"maxValue\":\"\",\"minValue\":\"\"}";
									switch(groupType){
										case "年龄段":resultSql1 += ",t1.begin_time event_time";resultSql2 += " , t4.recipe_time event_time";break;
										case "年份":resultSql1 += ",t1.begin_time event_time";resultSql2 += ",t4.recipe_time event_time";break;
										case "医院":resultSql1 += ",t1.org_code";resultSql2 += ",t1.org_code";break;
										case "就诊类型":resultSql1 += ",'住院' visit_type";resultSql2 += ",'门诊' visit_type";break;
										case "性别":resultSql1 += "";break;
									}
								}
								
								List<String> drug_category = (List)temp.get("drug_category");
								
								resultSql1 += " FROM orders.inpat_drug_order t1  join mdm.mdm_map t2 on t1.drug_id=t2.source_id "+
												" <br/> join mdm.drug t3 on t2.map_id=t3.drug_id";
								
								resultSql2 += " FROM orders.outpat_recipe_detail t1 join mdm.mdm_map t2 on t1.drug_id=t2.source_id  "+
										" <br/> join mdm.drug t3 on t2.map_id=t3.drug_id join orders.outpat_recipe t4 on t1.recipe_id=t4.recipe_id ";
								
								if(drug_category!=null&&drug_category.size()>0){
									
									String tempLab = " in (";
									for(String cat:drug_category){
										tempLab += "'"+cat+"',";
									}
									tempLab = tempLab.substring(0,tempLab.length()-1)+")";
									
									resultSql1 +=" join ( <br/> with RECURSIVE  temp1 as (  select id from drug_new where id "+tempLab+" union ALL "+
											" select d.id from drug_new d ,temp1 where d.parent=temp1.id )select * from temp1) ca <br/> on  t3.drug_code=ca.id";
									resultSql2 +=" join ( <br/> with RECURSIVE  temp1 as (  select id from drug_new where id "+tempLab+" union ALL "+
											" select d.id from drug_new d ,temp1 where d.parent=temp1.id )select * from temp1) ca <br/> on  t3.drug_code=ca.id";
								}
								
								resultSql1 += " where t2.md_type='drug' and t3.code_sys_id = 51  $condition$  <br/> ";
								resultSql2 += " where t2.md_type='drug' and t3.code_sys_id = 51  $condition$  <br/> ";
						
			//{\"lab_standard\":[\"220\",\"230\"],\"character_result\":[\"偏高\",\"异常\"],\"exception_symbol\":[\"1\",\"2\"],\"number_result\":[\">=1\"],\"event_type\":\"lab\"}
						List<String> lab_list = (List)temp.get("drug_standard");
						
						if(lab_list!=null&&lab_list.size()>0){
							resultSql1 += " and t3.drug_id in (";
							resultSql2 += " and t3.drug_id in (";
							String tempLab = "";
							for(String lab:lab_list){
								tempLab += "'"+lab+"',";
							}
							resultSql1 += tempLab.substring(0,tempLab.length()-1)+")";
							resultSql2 += tempLab.substring(0,tempLab.length()-1)+")";
						}
						
						
					//}
				}else if("医院".equals(str)){ //"医院":{"org_code":"9983838x,111999441,2224589985,112445580"}
					Map temp = temp1.get(str);
					try{
						List<String> org_list = (List)temp.get("org_code");
						if(!CollectionUtils.isEmpty(org_list)){
							if(org_list.size()>1){
								for(int i = 0;i<org_list.size();i++){
									String org_code = org_list.get(i);
									if(i==0){
										condition1 += " and  t1.org_code in ('"+org_code+"',";
										condition2 += " and  t1.org_code in ('"+org_code+"',";
									}else if(i==org_list.size()-1){
										condition1 += "'"+org_code+"')";
										condition2 += "'"+org_code+"')";
									}else{
										condition1 += "'"+org_code+"',";
										condition2 += "'"+org_code+"',";
									}
								}
							}else{
								condition1 += " and t1.org_code = '"+org_list.get(0)+"'";
								condition2 += " and t1.org_code = '"+org_list.get(0)+"'";
							}
						}
					}catch(java.lang.ClassCastException e){}
					
				}else if("时间".equals(str)){//"时间":{"time_from":"绝对时间:2017-01-01","time_to":"绝对时间:2017-01-10","time_type":"诊断时间"}
					Map temp = temp1.get(str);
					String time_from = (String)temp.get("time_from");
					String time_to = (String)temp.get("time_to");
					if(StringUtils.isNoneBlank(time_from)&&StringUtils.isNoneBlank(time_to)){
						condition1 += " and t1.begin_time between '"+time_from +"' and '"+time_to+"'";
						condition2 += " and t4.recipe_time between '"+time_from +"' and '"+time_to+"'";
					}
					
//					switch(eventType){
//						case "诊断":condition+= "and coalesce(a.diag_time,b.visit_time) ";break;
//						case "检验时间":condition+= "and report_time ";break;
//						case "用药时间":condition+= "and drug_time ";break;
//						case "手术时间":condition+= "and oper_time ";break;
//					}
				}
			}
			
			resultSql1 = resultSql1.replace("$condition$", condition1);
			resultSql2 = resultSql2.replace("$condition$", condition2);
			
			String returnSql = "";
			Map temp = temp1.get("就诊类型");
			String visit_type = (String)temp.get("visit_type");
			if("inpat".equals(visit_type)){
				returnSql = resultSql1;
			}else if("outpat".equals(visit_type)){
				returnSql+= resultSql2;
			}else{
				returnSql+= resultSql1 +" union all "+ resultSql2;
			}
			
			
			return returnSql;
	    }
	  public static String event_operation(Map<String,Map> temp1,Map groupMap){
			String resultSql = "";
			String condition = "";
			//String eventType = "";
			
			for(String str:temp1.keySet()){
				if("事件".equals(str)){  //"事件":{"diag_standard":"I21,I22,I23","event_type":"诊断"}
					Map temp = temp1.get(str);
						resultSql += " select t1.patient_id";
								String groupType = groupMap.get("groupType")==null?"":(String)groupMap.get("groupType");
								if(StringUtils.isNotBlank(groupType)){//"{\"groupType\":\"医院\",\"interval\":\"\",\"maxValue\":\"\",\"minValue\":\"\"}";
									switch(groupType){
										case "年龄段":resultSql += ",t2.operation_time event_time";break;
										case "年份":resultSql += ",t2.operation_time event_time";break;
										case "医院":resultSql += ",t2.org_code";break;
										case "就诊类型":resultSql += ",'住院' visit_type";break;
										case "性别":resultSql += "";break;
									}
								}
								
								resultSql += " from cases.case_base t1 join cases.case_operation t2 on t1.case_id=t2.case_id " ;
								
								resultSql += "where 1=1 $condition$  <br/> ";
						
			//{\"lab_standard\":[\"220\",\"230\"],\"character_result\":[\"偏高\",\"异常\"],\"exception_symbol\":[\"1\",\"2\"],\"number_result\":[\">=1\"],\"event_type\":\"lab\"}
						List<String> opeartion_standard = (List)temp.get("opeartion_standard");
						if(opeartion_standard!=null&&opeartion_standard.size()>0){
							resultSql += " and t2.operation_code in (";
							String tempLab = "";
							for(String lab:opeartion_standard){
								tempLab += "'"+lab+"',";
							}
							resultSql += tempLab.substring(0,tempLab.length()-1)+")";
						}
					//}
				}else if("医院".equals(str)){ //"医院":{"org_code":"9983838x,111999441,2224589985,112445580"}
					Map temp = temp1.get(str);
					try{
						List<String> org_list = (List)temp.get("org_code");
						if(!CollectionUtils.isEmpty(org_list)){
							if(org_list.size()>1){
								for(int i = 0;i<org_list.size();i++){
									String org_code = org_list.get(i);
									if(i==0){
										condition += " and  t2.org_code in ('"+org_code+"',";
									}else if(i==org_list.size()-1){
										condition += "'"+org_code+"')";
									}else{
										condition += "'"+org_code+"',";
									}
								}
							}else{
								condition += " and t2.org_code = '"+org_list.get(0)+"'";
							}
						}
					}catch(java.lang.ClassCastException e){}
					
				}else if("时间".equals(str)){//"时间":{"time_from":"绝对时间:2017-01-01","time_to":"绝对时间:2017-01-10","time_type":"诊断时间"}
					Map temp = temp1.get(str);
					String time_from = (String)temp.get("time_from");
					String time_to = (String)temp.get("time_to");
					condition += " <br/> ";
					if(StringUtils.isNoneBlank(time_from)&&StringUtils.isNoneBlank(time_to)){
						condition += " and t2.operation_time between '"+time_from +"' and '"+time_to+"'";
					}
					
				}else if("就诊类型".equals(str)){//"就诊类型":{"visit_type":"门诊"}
					
				}
			}
			
			resultSql = resultSql.replace("$condition$", condition);
			return resultSql;
	    }
}
