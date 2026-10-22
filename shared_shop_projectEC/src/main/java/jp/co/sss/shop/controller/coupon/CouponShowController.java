//クーポン一覧表示用のコントローラー

package jp.co.sss.shop.controller.coupon;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Coupon;
import jp.co.sss.shop.entity.UserCoupon;
import jp.co.sss.shop.repository.UserCouponsRepository;

@Controller
public class CouponShowController {

	@Autowired
	UserCouponsRepository userCouponsRepository;
	
	@Autowired
	HttpSession session;
	
	/**
	 * 一般会員所持クーポン一覧表示処理
	 *
	 * @return "login" ログイン画面へ
	 */
	@RequestMapping(path = "/coupon/list", method = RequestMethod.GET)
	public String list(Model model) {
		
		Object user = session.getAttribute("user");
		Integer userId = ((UserBean) user).getId();
		
		List<UserCoupon> userCoupons = userCouponsRepository.findByUser_Id(userId);
		
		List<String> couponNames = new ArrayList<>();
		
		for (UserCoupon userCoupon : userCoupons) {
			if (userCoupon.getStatus() == 0) {
				Coupon coupon = userCoupon.getCoupon();
				couponNames.add(coupon.getCouponsName());
			}
		}
		
		boolean couponFalse = couponNames.isEmpty();
		
		model.addAttribute("couponNames", couponNames);
		model.addAttribute("couponFalse", couponFalse);
		
		return "coupon/show";
	}
}
