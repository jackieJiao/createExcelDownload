package com.houtai.controller;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.houtai.entitys.CreditNum;
import com.houtai.entitys.Credit_body;
import com.houtai.entitys.PageInfo;
import com.houtai.entitys.Stock;
import com.houtai.dao.StockMapper;

@Controller
public class TestDownload {
	@Autowired
	private StockMapper stockMapper;
	/*
	 * 下载机构查询的所有数据
	 * 3.14
	 * jiao
	 * */
	@RequestMapping("/downloadPro")
	@ResponseBody
	public  void downloadPro(HttpServletRequest request, HttpServletResponse response) throws Exception {
		PageInfo page=new PageInfo();
		page.setInsti(request.getParameter("einsti"));
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date sinotime = null;
		String sinotimeString=request.getParameter("edate");
		try {
		 sinotime=format.parse(sinotimeString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("error:sinotime为空");
		}
		page.setSinotime(sinotime);
		System.out.println(page.getSinotime());
		List<Stock> list=stockMapper.getByInsti(page);
		
		//生成excel
	   File file = new File("C:\\Users\\dell\\Documents");
	   if (!file.exists()) {
		  file.mkdirs();
	   }
	   String path = "C:\\Users\\dell\\Documents";  
	   String fileName = "普惠金融风控报告专业版输出样例";  
	   String fileType = "xlsx";  
	   String title[] = {"姓名","身份证号","手机号","信贷记录情况","近2周申请(条)","近2周申请成功(条)","近2周申请失败(条)",
			   			 "近2周还款记录(条)","近2周逾期记录(条)",
			   			 "近1个月申请(条)","近1个月申请成功(条)","近1个月申请失败(条)","近1个月还款记录(条)",
			   			 "近1个月逾期记录(条)","黑名单"
	   					};  
	  /* String con="交1";
	   Stock stock=new Stock(con, con, con, con, 1, con, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, con, con);
	   List<Stock> list=new ArrayList<>();
	   list.add(stock);*/
	   String docPath = CreateExcelUtil.writerPro(path, fileName, fileType,list,title); 
	   DownLoadFile.checkDoc(request,response,docPath);
	}
	
	/*
	 * 下载个人查询的所有数据
	 * 3.14
	 * jiao
	 * */
	@RequestMapping("/downloadPerson")
	@ResponseBody
	public  void downloadPerson(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		String name=req.getParameter("name");
		String cardid=req.getParameter("cardid");
		String phone=req.getParameter("phone");
		//String num=req.getParameter("pageNum");
		String sinotimeString= req.getParameter("sinotime");
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date sinotime = null;
		try {
		 sinotime=format.parse(sinotimeString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("error:sinotime为空");
		}
		Credit_body creditbody =new Credit_body();
		if(name!=null&&!name.equals("")){creditbody.setName(name);}
		if(cardid!=null&&!cardid.equals("")){creditbody.setCardid(cardid);}
		if(phone!=null&&!phone.equals("")){creditbody.setCellPhoneNum(phone);}
		if(sinotime!=null&&!sinotime.equals("")){creditbody.setSinotime(sinotime);}
		Stock stock=getStock(creditbody);
		List<Stock> list= new ArrayList<>();
		list.add(stock);
		//生成excel
	   File file = new File("C:\\Users\\dell\\Documents");
	   if (!file.exists()) {
		  file.mkdirs();
	   }
	   String path = "C:\\Users\\dell\\Documents";  
	   String fileName = "普惠金融风控报告专业版输出样例";  
	   String fileType = "xlsx";  
	   String title[] = {"姓名","身份证号","手机号","信贷记录情况","近7天申请(条)","近7天申请成功(条)","近7天申请失败(条)",
			   			 "近7天还款记录(条)","近7天逾期记录(条)",
			   			 "近1个月申请(条)","近1个月申请成功(条)","近1个月申请失败(条)","近1个月还款记录(条)",
			   			 "近1个月逾期记录(条)","黑名单"
	   					};  
	  
	   String docPath = CreateExcelUtil.writerPerson(path, fileName, fileType,list,title); 
	   DownLoadFile.checkDoc(req,resp,docPath);
	}
	
	public Stock getStock(Credit_body creditbody) {
		String sinoid = creditbody.getSinoid();
	    String name= creditbody.getName();
	    String cardid= creditbody.getCardid();
	    String cellPhoneNum= creditbody.getCellPhoneNum();
	    String instiName= creditbody.getInstiName();
		//spring模板方法调用接口
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1000);
        requestFactory.setReadTimeout(1000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
	    String url = "http://123.57.245.74:8080/CISPMonitor/query/{cardid}/{name}/{phone}/5";
	    String result =  restTemplate.getForObject(url, String.class,cardid, name,cellPhoneNum);
	    //System.out.println(result);
	    //解析json
	    Gson gson=new Gson();
	    JsonObject jobj=gson.fromJson(result, JsonObject.class);
	    System.out.println(jobj.get("ONEWEEKDATA"));
	    CreditNum week=gson.fromJson(jobj.get("ONEWEEKDATA"), CreditNum.class);
	    CreditNum month=gson.fromJson(jobj.get("ONEMONTHDATA"), CreditNum.class);
	    System.out.println(week.getAPPFAILEDNUM());
	    
	    //目标Stock对象变量
	     Integer nOp=1;
	     String ISCREDITRECORD=jobj.get("ISCREDITRECORD").toString();
	     ISCREDITRECORD=ISCREDITRECORD.substring(1, ISCREDITRECORD.length()-1);
	     String ISBLACKLIST=jobj.get("ISBLACKLIST").toString();
	     ISBLACKLIST=ISBLACKLIST.substring(1, ISBLACKLIST.length()-1);
	     System.out.println(ISCREDITRECORD);
	     Integer numW=week.getAPPLICATIOSNNUM();
	     Integer sucNumW=week.getAPPSUCNUM();
	     Integer failNumW=week.getAPPFAILEDNUM();
	     Integer repayNumW=week.getREPAYMENTNUM();
	     Integer overdueW=week.getOVERDUENUM();

	     Integer numM=month.getAPPLICATIOSNNUM();
	     Integer sucNumM=month.getAPPSUCNUM();
	     Integer failNumM=month.getAPPFAILEDNUM();
	     Integer repayNumM=month.getREPAYMENTNUM();
	     Integer overdueMM=month.getOVERDUENUM();
	     
	     return new Stock(sinoid, name, cardid, cellPhoneNum, nOp, instiName, numW, sucNumW, failNumW, repayNumW, overdueW, numM, sucNumM, failNumM, repayNumM, overdueMM, ISCREDITRECORD, ISBLACKLIST);
	}
}
