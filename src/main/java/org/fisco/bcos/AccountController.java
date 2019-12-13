package org.fisco.bcos;

import org.fisco.bcos.asset.contract.Asset;

import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.fisco.bcos.asset.client.AssetClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.fisco.bcos.web3j.tuples.generated.Tuple3;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

@Controller
public class AccountController {

  public List<String> loginUser;
  public String[] users = {"Jane", "QiQi", "Tom", "Jack", "Bank", "Car", "Hub", "Tyre", "BankCopy", "HubCompany", "TyreCompany", "CarCompany"};
  public AssetClient client;

  AccountController()throws Exception{
	System.out.println("<----------- Constructor ------------->");

	client = new AssetClient();
	client.initialize();
	client.deployAssetAndRecordAddr();

	loginUser = new ArrayList<String>();

	System.out.println("<----------- Constructor End ------------->");
  }

  String removeSlash(String str){
	int len = str.length();
	int idx = 0;
	for(int i = 0; i < len; ++i, ++idx){
	  if(str.charAt(i) == '/')
		break;
	}
	return str.substring(0, idx);
  }

  boolean findUser(String user){
	  int len = loginUser.size();
	  for(int i = 0; i < len; ++i){
		  if(user.equals(loginUser.get(i))){
			  return true;
		  }
	  }
	  return false;
  }

  @RequestMapping(value = "/login")
  public String indexPage() {
	return "index";
  }

  /**
   * @brief 用户页面, 先查找Map中是否存在相应的用户正在登录状态
   *        如果存在则不需要密码, 否则需要密码
   * 
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(value = "/user")
  public String login(HttpServletRequest request, Model model) {
	String username = request.getParameter("username");
	String pwd;
	username = removeSlash(username);
	if(findUser(username)){
	  model.addAttribute("username", username);
	  return "Account";
	}

	pwd = request.getParameter("password");
	System.out.println("username: " + username);

	int len = users.length;
	boolean is_exist = false;
	for(int i = 0; i < len; ++i){
	  if(username.equals(users[i])){
		is_exist = true;
		break;
	  }
	}
	if(!is_exist){
	  return "usernoexist";
	}else if(!pwd.equals("x123456")){
	  return "pwdwrong";
	}
	model.addAttribute("username", username);
	loginUser.add(username);
	return "Account";
  }

  // @brief Select index
  @RequestMapping(value = "/user/select")
  public String select(HttpServletRequest request, Model model) throws Exception{
	String username = request.getParameter("username");
	username = removeSlash(username);
	System.out.println("<----------- Select ------------->");

	System.out.println("<----------- Select End------------->");
	model.addAttribute("username", username);
	return "select";
  }

  @RequestMapping(value = "/user/select/result")
  @ResponseBody
  public Account selectResult(HttpServletRequest request) throws Exception{
	System.out.println("<----------- Select Result ------------->");
	String username = request.getParameter("username");
	System.out.println(username);
	username = removeSlash(username);

	Account res = new Account();
	List<String> str = new ArrayList<String>();
	str = client.Select(username);
	for(int i = 0; i < str.size(); ++i){
		String tmp = str.get(i);
		String name = new String();
		String count = new String();
		String oweName = new String();
		int choice = 0;
		int idx = 0;
		for(int j = 0; j < tmp.length(); ++j){
			if(tmp.charAt(j) == '$'){
				if(choice == 0){
					name = tmp.substring(idx, j);
					idx = j + 1;
				}else if(choice == 1){
					count = tmp.substring(idx, j);
					idx = j + 1;
					oweName = tmp.substring(idx, tmp.length());
					break;
				}
				++choice;
			}
		}
		name = name.replaceAll("\u0000", "");
		oweName = oweName.replaceAll("\u0000", "");
		res.data.add(new UserData(name, count, oweName));
		System.out.println(str.get(i));
	}

	System.out.println("<----------- Select Result End------------->");
	return res;
  }

  // @brief Buy
  @RequestMapping(value = "/user/buy")
  public String buy(HttpServletRequest request, Model model) {
	String username = request.getParameter("username");
	username = removeSlash(username);
	model.addAttribute("username", username);
	model.addAttribute("method", "buy");
	return "buy";
  }

  // @brief BuyWithLoan
  @RequestMapping(value = "/user/buyWithLoan")
  public String buyWithLoan(HttpServletRequest request, Model model) {
	String username = request.getParameter("username");
	username = removeSlash(username);
	model.addAttribute("username", username);
	model.addAttribute("method", "buyWithLoan");
	return "buyWithLoan";
  }

  // @brief LoanWithBank
  @RequestMapping(value = "/user/loanWithBank")
  public String loanWithBank(HttpServletRequest request, Model model) {
	String username = request.getParameter("username");
	username = removeSlash(username);
	model.addAttribute("username", username);
	model.addAttribute("method", "loanWithBank");
	return "loanWithBank";
  }

  // @brief PayMoney
  @RequestMapping(value = "/user/payMoney")
  public String payMoney(HttpServletRequest request, Model model) {
	String username = request.getParameter("username");
	username = removeSlash(username);
	model.addAttribute("username", username);
	model.addAttribute("method", "payMoney");
	return "payMoney";
  }

  /**
   * @brief 处理
   * 
   */
  @RequestMapping(value = "/user/success")
  public String success(HttpServletRequest request, Model model) {
	String username = request.getParameter("username");
	String count = request.getParameter("count");
	String company = request.getParameter("company");
	String oweCompany = request.getParameter("owecompany");
	String partMoney = request.getParameter("partmoney");
	String bossCompany = request.getParameter("bosscompany");


	String tradeMethod = request.getParameter("method");

	username = removeSlash(username);
	tradeMethod = removeSlash(tradeMethod);

	model.addAttribute("username", username);
	System.out.println("<----------- username: " + username + " ------------->");
	System.out.println("<----------- method: " + tradeMethod + " ------------->");

	if(tradeMethod.equals("buy")){
		client.Buy(username, new BigInteger(count), company);
	}else if(tradeMethod.equals("buyWithLoan")){
		client.BuyWithLoan(username, new BigInteger(count), company, oweCompany, new BigInteger(partMoney));
	}else if(tradeMethod.equals("loanWithBank")){
		client.LoanWithBank(username, company, new BigInteger(count));
	}else if(tradeMethod.equals("payMoney")){
		client.PayMoney(username, new BigInteger(count), bossCompany);
	}else{
		System.out.println("<----------- No Method ------------->");
	}

	return "success";
  }

}
