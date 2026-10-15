package jp.co.sss.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ClientBasketController {
	
	@RequestMapping(path ="/basket/list", method = RequestMethod.GET)
	public  String a() {
		
		return "basket/list";
	}
	
	@RequestMapping(path ="/client/basket/add", method = RequestMethod.POST)
	public String b() {
		return "";
	}
	
	@RequestMapping(path ="/client/basket/delete", method = RequestMethod.POST)
	public String c() {
		return "";
	}
	
	@RequestMapping(path ="/client/basket/allDelete", method = RequestMethod.POST)
	public String d() {
		return "";
	}


}
