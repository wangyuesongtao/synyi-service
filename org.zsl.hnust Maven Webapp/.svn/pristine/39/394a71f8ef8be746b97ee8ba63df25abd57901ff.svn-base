package com.synyi.edc.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.synyi.edc.dao.ISqlbuilderDao;
import com.synyi.edc.pojo.Parameter;
import com.synyi.edc.service.ISqlbuilderService;
/**
 * 连接数据库查询service
 * @author wy
 *
 */
@Service("sqlbuilderService")
public class SqlbuilderServiceImpl implements ISqlbuilderService {
	@Resource
	private ISqlbuilderDao dao;
	/**
	 * 获取所有的医院列表
	 */
	@Override
	public List<Parameter> getAllOrgInfo() {
		// TODO Auto-generated method stub
		return this.dao.getAllOrgInfo();
	}
	/**
	 * 获取检验名称
	 */
	@Override
	public List<Parameter> getAllLabInfo(Parameter labname) {
		// TODO Auto-generated method stub
		return this.dao.getAllLabInfo( labname);
	}

	/**
     * 获取检验结果,文本结果和数字结果等suggestion列表
     * @param par
     * @return
     */
    public List<Parameter> getSuggestionList(Parameter par){
    	return this.dao.getSuggestionList(par);
    }
    /**
     * 根据药品名称 模糊穷举所有药品名称
     * @param par 参数传递为 labName
     * @return
     */
    public List<Parameter> getAllDrugInfo(Parameter par){
    	return this.dao.getAllDrugInfo(par);
    }
    
    /**
     * 获取所有的药品大类名称
     * @return
     */
	public List<Parameter> getAllDrugCategory(){
		return this.dao.getAllDrugCategory();
	}
	/**
	 * 根据药品名称获取模糊匹配的suggestion
	 * @param param
	 * @return
	 */
	public List<Parameter> getSuggestionByDrug(Parameter param){
		return this.dao.getSuggestionByDrug(param);
	}
	/**
	 * 根据药品父节点名称获取子节点药品名称
	 * @param param
	 * @return
	 */
	public List<Parameter> getChildDrug(Parameter param){
		return this.dao.getChildDrug(param);
	}
	/**
	 * 递归通过子类别将所有父类药品列出来
	 * @param param
	 * @return
	 */
	public List<Parameter> getParentPath(Parameter param){
		return this.dao.getParentPath(param);
	}
	/**
	 * 递归通过子类别将所有父类药品列出来并且所有父类的同类列出来
	 * @param param
	 * @return
	 */
	public List<Parameter> getAllDrugPathInfo(Parameter param){
		return this.dao.getAllDrugPathInfo(param);
	}
}
