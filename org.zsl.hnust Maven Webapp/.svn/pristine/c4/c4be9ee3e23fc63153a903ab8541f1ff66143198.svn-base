package com.synyi.edc.dao;

import java.util.List;

import com.synyi.edc.pojo.Parameter;
/**
 * sqlbuilder 语句生成dao
 * @author wy
 *
 */
public interface ISqlbuilderDao {
	/**
	 * 获取所有的医院列表
	 * @return
	 */
    List<Parameter> getAllOrgInfo();
    /**
     * 根据检查名称 模糊穷举所有检查名称
     * @param par 参数传递为 labName
     * @return
     */
    List<Parameter> getAllLabInfo(Parameter par);
    /**
     * 获取检验结果,文本结果和数字结果等suggestion列表
     * @param par
     * @return
     */
    List<Parameter> getSuggestionList(Parameter par);
    
    /**
     * 根据药品名称 模糊穷举所有药品名称
     * @param par 参数传递为 labName
     * @return
     */
    List<Parameter> getAllDrugInfo(Parameter par);
    
    /**
     * 获取所有的药品大类名称
     * @return
     */
	public List<Parameter> getAllDrugCategory();
	/**
	 * 根据药品名称获取模糊匹配的suggestion
	 * @param param
	 * @return
	 */
	public List<Parameter> getSuggestionByDrug(Parameter param);
	/**
	 * 根据药品父节点名称获取子节点药品名称
	 * @param param
	 * @return
	 */
	public List<Parameter> getChildDrug(Parameter param);
	/**
	 * 递归通过子类别将所有父类药品列出来
	 * @param param
	 * @return
	 */
	public List<Parameter> getParentPath(Parameter param);
	/**
	 * 递归通过子类别将所有父类药品列出来并且所有父类的同类列出来
	 * @param param
	 * @return
	 */
	public List<Parameter> getAllDrugPathInfo(Parameter param);
}