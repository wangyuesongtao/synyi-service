package com.synyi.edc.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.synyi.edc.pojo.Parameter;
import com.synyi.edc.service.ISqlbuilderService;
import com.synyi.edc.util.SqlBuilderUtils;
/**
 * sql语句生成主要contorller
 * @author wy
 *
 */
@Controller
@RequestMapping("/sqlbuilder")
public class SqlbuilderController {
	private Logger log = Logger.getLogger(SqlbuilderController.class);
	
	@Resource
	private ISqlbuilderService sqlbuilderService;
	
	
	@RequestMapping("/toIndex")
	public String toJ(HttpServletRequest request,Model model){
//		int userId = Integer.parseInt(request.getParameter("id"));
//		User user = this.userService.getUserById(userId);
//		model.addAttribute("user", user);
		return "index";
	}
	/**
	 * sql生成方法
	 * @param request
	 * @param model
	 * @param param
	 * @param res
	 * @return
	 */
	@RequestMapping("/generate")
	public String generate(HttpServletRequest request,Model model,Parameter param,HttpServletResponse res){
		log.info("参数传递:"+param);
		String jsonStr = param.getEventData();
    	String stepStr = param.getGroupData();
    	
    	String returnSql = SqlBuilderUtils.genGroupSql(jsonStr,stepStr);
		log.info("生成sql:"+returnSql);
		
		printJson(res,returnSql);
		 return null;
	}
	
	/**
	 * 打印返回参数
	 * @param res
	 * @param str
	 */
	public void printJson(HttpServletResponse res,String str){
		
		res.setContentType("text/plain");
		 res.setCharacterEncoding("utf-8");
		 res.setHeader("Access-Control-Allow-Origin", "*");  
		 res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");  
		 res.setHeader("Access-Control-Max-Age", "3600");  
		 res.setHeader("Access-Control-Allow-Headers", "x-requested-with");  
		 PrintWriter pw = null;
		try {
			pw = res.getWriter();
			 pw.write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			 pw.flush();
			 pw.close();
		}
	}
	/**
	 * 获取所有的医院列表
	 * @param request
	 * @param model
	 * @param res
	 * @return
	 */
	@RequestMapping("/getAllOrg")
	public String getAllOrg(HttpServletRequest request,Model model,HttpServletResponse res){
		
		List<Parameter> par = sqlbuilderService.getAllOrgInfo();
		List list = new ArrayList();
		for(Parameter p:par){
			Map map1 = new HashMap();
			map1.put("value", p.getOrgCode());
			map1.put("label", p.getOrgName());
			list.add(map1);
		}
		
		String jsonString = JSONObject.toJSON(list).toString();
		printJson(res,jsonString);
		return null;
	}
	/**
	 * 模糊匹配获取所有的检查名称
	 * @param request
	 * @param model
	 * @param res
	 * @param param
	 * @return
	 */
	@RequestMapping("/getAlllabInfo")
	public String getAllLabInfo(HttpServletRequest request,Model model,HttpServletResponse res,Parameter param){
		
		List<Parameter> par = sqlbuilderService.getAllLabInfo(param);
		List list = new ArrayList();
		for(Parameter p:par){
			Map map1 = new HashMap();
			map1.put("value", p.getItemId());
			map1.put("label", p.getItemName());
			list.add(map1);
		}
		
		String jsonString = JSONObject.toJSON(list).toString();
		printJson(res,jsonString);
		return null;
	}
	
	/**
	 * 获取检验结果,文本结果和数字结果等suggestion列表
	 * @param request
	 * @param model
	 * @param res
	 * @param param
	 * @return
	 */
	@RequestMapping("/getSuggestionList")
	public String getSuggestionList(HttpServletRequest request,Model model,HttpServletResponse res,Parameter param){
		
		List<Parameter> par = sqlbuilderService.getSuggestionList(param);
		List list = new ArrayList();
		for(Parameter p:par){
			//c.item_name,text_value,numerical_value,reference_range,abnormal_flag_name
			Map map1 = new HashMap();
			map1.put("itemName", p.getItemName());
			map1.put("textValue", p.getTextValue());
			map1.put("numericalValue", p.getNumericalValue());
			map1.put("referenceRange", p.getReferenceRange());
			map1.put("abnormalFlagName", p.getAbnormalFlagName());
			list.add(map1);
		}
		
		String jsonString = JSONObject.toJSON(list).toString();
		printJson(res,jsonString);
		return null;
	}
	
	/**
	 * 模糊匹配获取所有的药品名称
	 * @param request
	 * @param model
	 * @param res
	 * @param param
	 * @return
	 */
	@RequestMapping("/getAllDrugInfo")
	public String getAllDrugInfo(HttpServletRequest request,Model model,HttpServletResponse res,Parameter param){
		
		List<Parameter> par = sqlbuilderService.getAllDrugInfo(param);
		List list = new ArrayList();
		for(Parameter p:par){
			Map map1 = new HashMap();
			map1.put("value", p.getDrugId());
			map1.put("label", p.getDrugName());
			list.add(map1);
		}
		
		String jsonString = JSONObject.toJSON(list).toString();
		printJson(res,jsonString);
		return null;
	}
	/**
	 * 获取所有的药品大类名称
	 * @param request
	 * @param model
	 * @param res
	 * @param param
	 * @return
	 */
	@RequestMapping("/getAllDrugCategory")
	public String getAllDrugCategory(HttpServletRequest request,Model model,HttpServletResponse res){
		
		List<Parameter> par = sqlbuilderService.getAllDrugCategory();
		List list = new ArrayList();
		for(Parameter p:par){
			Map map1 = new HashMap();
			map1.put("value", p.getDrugId());
			map1.put("label", p.getDrugName());
			list.add(map1);
		}
		
		String jsonString = JSONObject.toJSON(list).toString();
		printJson(res,jsonString);
		return null;
	}
	/**
	 * 根据药品名称获取模糊匹配的suggestion
	 * @param request
	 * @param model
	 * @param res
	 * @param param
	 * @return
	 */
	@RequestMapping("/getSuggestionByDrug")
	public String getSuggestionByDrug(HttpServletRequest request,Model model,HttpServletResponse res,Parameter param){
		
		List<Parameter> par = sqlbuilderService.getSuggestionByDrug(param);
		List list = new ArrayList();
		for(Parameter p:par){
			Map map1 = new HashMap();
			map1.put("value", p.getDrugId());
			map1.put("label", p.getDrugName());
			list.add(map1);
		}
		
		String jsonString = JSONObject.toJSON(list).toString();
		printJson(res,jsonString);
		return null;
	}
	/**
	 * 根据药品父节点名称获取子节点药品名称
	 * @param request
	 * @param model
	 * @param res
	 * @param param
	 * @return
	 */
	@RequestMapping("/getChildDrug")
	public String getChildDrug(HttpServletRequest request,Model model,HttpServletResponse res,Parameter param){
		
		List<Parameter> par = sqlbuilderService.getChildDrug(param);
		List list = new ArrayList();
		for(Parameter p:par){
			Map map1 = new HashMap();
			map1.put("value", p.getDrugId());
			map1.put("label", p.getDrugName());
			map1.put("parent", p.getParent());
			list.add(map1);
		}
		
		String jsonString = JSONObject.toJSON(list).toString();
		printJson(res,jsonString);
		return null;
	}
	
	/**
	 * 递归通过子类别将所有父类药品列出来
	 * @param request
	 * @param model
	 * @param res
	 * @param param
	 * @return
	 */
	@RequestMapping("/getParentPath")
	public String getParentPath(HttpServletRequest request,Model model,HttpServletResponse res,Parameter param){
		
		List<Parameter> par = sqlbuilderService.getParentPath(param);
		List list = new ArrayList();
		for(Parameter p:par){
			Map map1 = new HashMap();
			map1.put("value", p.getDrugId());
			map1.put("label", p.getDrugName());
			map1.put("parent", p.getParent());
			list.add(map1);
		}
		
		String jsonString = JSONObject.toJSON(list).toString();
		printJson(res,jsonString);
		return null;
	}
	
	/**
	 * 递归通过子类别将所有父类药品列出来并且所有父类的同类列出来
	 * @param request
	 * @param model
	 * @param res
	 * @param param
	 * @return
	 */
	@RequestMapping("/getAllDrugPathInfo")
	public String getAllDrugPathInfo(HttpServletRequest request,Model model,HttpServletResponse res,Parameter param){
		
		List<Parameter> par = sqlbuilderService.getParentPath(param);
		List<String> strList = new ArrayList();
		for(Parameter p:par){
			strList.add(p.getDrugId());
		}
		param.setStrList(strList);
		param.setStrSize(strList.size());
		List<Parameter> pathInfo = sqlbuilderService.getAllDrugPathInfo(param);
		List list = new ArrayList();
		for(Parameter p:pathInfo){
			Map map1 = new HashMap();
			map1.put("value", p.getDrugId());
			map1.put("label", p.getDrugName());
			map1.put("parent", p.getParent());
			list.add(map1);
		}
		String jsonString = JSONObject.toJSON(list).toString();
		printJson(res,jsonString);
		return null;
	}
	
}
