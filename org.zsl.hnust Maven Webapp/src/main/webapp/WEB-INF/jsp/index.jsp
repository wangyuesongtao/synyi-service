<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	
 <head>
 	<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery-1.11.3.js"></script>
 	<script type="text/javascript" src="<%=request.getContextPath() %>/js/datepicker/WdatePicker.js"></script>
 	<script type="text/javascript">
 	function confirmMethod(){
 		var checks = document.getElementsByName("orgArray1");
 		var ids = "";
 		if(checks&&checks.length>0){
 		for(var i=0;i<checks.length;i++){
 		if(checks[i].checked){
 		if(ids.length>0)ids+="','";
 		ids+=checks[i].value;
 		}
 		}
 		}
 		ids = "'"+ids+"'";
 		return ids;
 	}
 	function generate(){
 		var ids = confirmMethod();
 		$("#orgArray").val(ids);
 		document.forms[0].submit();
 	}
 	
 	</script>
 </head>
<body>
<h2></h2>
<form action="<%=request.getContextPath() %>/sqlbuilder/generate" >
<label>类型：</label>
<select name="type" style="width: 120px;">
	<option value="hospital">医院</option>
	<option value="datePart">时间</option>
	<option value="diagnose">事件</option>
	<option value="visitType">就诊类型</option>
</select>
<br/>
<label>医院类型：</label>
<select name="orgType" style="width: 120px;">
	<option value="allOrg">所有医院</option>
	<option value="manyOrg">多家医院</option>
	<option value="oneOrg">一家医院</option>
</select>
<br/>
<label>医院选择：</label>
<input type="hidden" id="orgArray" name="orgArray" value="" />
<input name='orgArray1' type="checkbox" value="488099744" /><label>福州一院</label>
<input name='orgArray1' type="checkbox" value="48809974x" /><label>福州儿院</label>
<input name='orgArray1' type="checkbox" value="488099743"/><label>福州中医院</label>
<input name='orgArray1' type="checkbox" value="488099757"/><label>福州传染病医院</label>
<br/>

时间 from <input type="text" id="beginDate" name="beginDate"  onclick="WdatePicker({dateFmt:'yyyy-MM-dd'})"   value="" />
to <input type="text" id="endDate" name="endDate"  onclick="WdatePicker({dateFmt:'yyyy-MM-dd'})" value="" />
<br/>
<label>查询内容：</label>
<select name="queryType" style="width: 120px;">
	<option value="diagTime">统计就诊次数</option>
	<option value="diagCount">统计就诊人数</option>
</select>
<br/>
</form>
<button onclick="generate();">generate</button>
<br/>
<br/>
<textarea rows="15" cols="50">${resultSql }</textarea>


</body>
