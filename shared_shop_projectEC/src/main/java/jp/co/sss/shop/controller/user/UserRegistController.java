
package jp.co.sss.shop.controller.user;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Coupon;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.entity.UserCoupon;
import jp.co.sss.shop.form.UserForm;
import jp.co.sss.shop.repository.CouponsRepository;
import jp.co.sss.shop.repository.UserCouponsRepository;
import jp.co.sss.shop.repository.UserRepository;

@Controller
public class UserRegistController {
	
	// 新規会員登録特典クーポンのID
    private static final int NEW_USER_COUPON_ID = 1;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
    private CouponsRepository couponsRepository;
	
	@Autowired
    private UserCouponsRepository userCouponsRepository;
	
	@Autowired
	HttpSession session;
	
	/**
	 * 会員情報入力画面表示処理
	 *
	 * @param Model Viewとの値受渡し
	 * @return "user/regist/regist_input" 会員情報 登録入力画面へ
	 */
	@RequestMapping(path = "/user/regist/input")
	public String showInputUser(@ModelAttribute("userForm") UserForm userForm, Model model) {
		
		model.addAttribute("userForm", userForm);
		
		return "user/regist/regist_input";
	}
	
	/**
	 * 会員情報 登録確認処理
	 *
	 * @param userform   会員情報フォーム
	 * @param result 入力チェック結果
	 * @return 入力値エラーあり："user/regist/regist_input" 会員情報登録画面へ
	 *         入力値エラーなし："user/regist/regist_check" 会員情報 登録確認画面へ
	 */
	@RequestMapping(path = "/client/user/regist/check", method = RequestMethod.POST)
	public String regitCheckUser(
			@Valid @ModelAttribute("userForm") UserForm userForm,
			BindingResult result,
			RedirectAttributes redirectAttributes) {
		
		// 入力値にエラーがあった場合、会員情報 入力画面表示処理に戻る
		if (result.hasErrors()) {
			
			return "user/regist/regist_input";
		}
		
		return "user/regist/regist_check";
	}
	
	/**
	 * 会員情報登録完了処理
	 *
	 * @param form 会員情報
	 * @return "redirect:/user/regist/complete" 会員情報 登録完了画面へ
	 */
	@RequestMapping(path = "/client/user/regist/complete", method = RequestMethod.POST)
	public String registComplete(@ModelAttribute UserForm form, HttpSession session) {
		
		// 会員情報の生成
		User user = new User();
		user.setAuthority(2);

		// 入力値を会員情報にコピー
		BeanUtils.copyProperties(form, user);

		// 会員情報を保存
		userRepository.save(user);
		
		//ログイン情報をセッションに保存
		UserBean userBean = new UserBean();
		BeanUtils.copyProperties(user, userBean);
		session.setAttribute("user", userBean);
		
		// クーポン付与情報を作成
        UserCoupon userCoupon = new UserCoupon();
        
        // 新規登録特典クーポンを取得
        Coupon coupon = couponsRepository.findById(NEW_USER_COUPON_ID).orElseThrow(() ->
        	new IllegalStateException("新規会員登録特典クーポンが存在しません。"));
		
		userCoupon.setUser(user);
        userCoupon.setCoupon(coupon);
        userCoupon.setStatus(0);

        // クーポンを会員に付与
        userCouponsRepository.save(userCoupon);

		return "redirect:/user/regist/complete";
	}

	/**
	 * 会員情報登録完了画面表示
	 *
	 * @param form 会員情報
	 * @return "user/regist/regist_complete" 会員情報 登録完了画面へ
	 */
	@RequestMapping(path = "/user/regist/complete", method = RequestMethod.GET)
	public String registCompleteRedirect() {
		
		return "user/regist/regist_complete";
	}

}
