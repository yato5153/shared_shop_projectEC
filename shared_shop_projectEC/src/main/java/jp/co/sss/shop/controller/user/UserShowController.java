package jp.co.sss.shop.controller.user;


import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.UserForm;
import jp.co.sss.shop.repository.UserRepository;

/**
 * 会員管理 表示機能(運用管理者、システム管理者)のコントローラクラス
 *
 * @author SystemShared
 */
@Controller
public class UserShowController {
	/**
	 * 会員情報
	 */
	@Autowired
	UserRepository userRepository;
	
	/**
	 * セッション情報
	 */
	@Autowired
	HttpSession	session;
	
	@RequestMapping(path = "/client/user/detail/{id}")
	public String showLoginUser(@PathVariable int id, Model model, @ModelAttribute UserForm form, HttpSession session) {
		// 表示対象の会員情報を取得
		User user = userRepository.getById(form.getId());
		
		UserBean userBean = new UserBean();

		// Userエンティティの各フィールドの値をUserBeanにコピー
		BeanUtils.copyProperties(user, userBean);

		// 会員情報をViewに渡す
		model.addAttribute("user", userBean);

		return "user/detail/detail";
	}

}
