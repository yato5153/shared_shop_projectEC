package jp.co.sss.shop.controller.basket;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.form.ItemForm;
import jp.co.sss.shop.repository.ItemRepository;

@Controller
public class BasketCreateController {
	
	@Autowired
	ItemRepository itemRepository;
	@RequestMapping(path ="/client/basket/add", method = RequestMethod.POST)
	public String addBasket(@ModelAttribute("item") ItemForm itemForm ,HttpSession session) {
		
//		商品のID取得
		Integer id = Integer.parseInt(itemForm.getId());
		
//		DBから商品取得
		Item item = itemRepository.findById(id).orElse(null);
		
//		セッションからカートを取得
		List<BasketBean> basketBeans = (List<BasketBean>)session.getAttribute("basketBeans");
		
//		無ければ生成
		
			if(basketBeans==null){
				basketBeans = new ArrayList<>();
			
			}
		
//		同じ商品があるか確認
			
		for (int i = 0; i < basketBeans.size();i++) {
			BasketBean basket = basketBeans.get(i);
			
			if (basket.getId().equals(item.getId())) {
				
			basket.setOrderNum(basket.getOrderNum() + 1);
			
			session.setAttribute("basketBeans",basketBeans);
			
			return "redirect:/basket/list";
					
			}
		}
		
//		無ければ新規追加
		
		BasketBean basket = new BasketBean();
		
		basket.setId(item.getId());
		basket.setName(item.getName());
		basket.setStock(item.getStock());
		
		basketBeans.add(basket);
		
		
//		セッションへ保存
		
		session.setAttribute("basketBeans", basketBeans);
		
		
		return "redirect:/basket/list";
	}
	
	@RequestMapping(path ="/basket/list", method = RequestMethod.GET)
	public  String list() {
		
		return "basket/list";
	}
	

	
	
	@RequestMapping(path ="/client/basket/delete", method = RequestMethod.POST)
	public String delete(@ModelAttribute("item") ItemForm itemForm,HttpSession session) {
		
//		商品のIDを取得
		
		Integer id = Integer.parseInt(itemForm.getId());
		
//		セッションからカートを取得
		
		List<BasketBean> basketBeans = (List<BasketBean>)session.getAttribute("basketBeans");
		
//		処理： 数が2以上のとき：該当商品の数を減らす
		
		
		
//		 数が1のとき:買い物かごから該当商品を削除
		
		for (int i = 0; i < basketBeans.size(); i++) {
			
			BasketBean basket =basketBeans.get(i);
			
			if(basket.getId().equals(id)) {
				
				if(basket.getOrderNum() >= 2) {
					
					basket.setOrderNum(basket.getOrderNum() -1);
				
				} else {
				
					basketBeans.remove(i);
					
				}
				
				break;
				
			}
		}
		
		return "redirect:/basket/list";
	}
	
	@RequestMapping(path ="/client/basket/allDelete", method = RequestMethod.POST)
	public String allDelete(HttpSession session) {
		
//		セッションからカートを取得
		
		session.removeAttribute("basketBeans");
		
		return "redirect:/basket/list";
	}


}
