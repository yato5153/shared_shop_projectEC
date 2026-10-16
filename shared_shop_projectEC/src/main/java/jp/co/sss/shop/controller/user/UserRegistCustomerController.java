
package jp.co.sss.shop.controller.user;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jp.co.sss.shop.entity.Coupon;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.entity.UserCoupon;
import jp.co.sss.shop.form.UserForm;
import jp.co.sss.shop.repository.CouponsRepository;
import jp.co.sss.shop.repository.UserCouponsRepository;
import jp.co.sss.shop.repository.UserRepository;

@Controller
public class UserRegistCustomerController {

    // 新規会員登録特典クーポンのID
    private static final int NEW_USER_COUPON_ID = 1;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponsRepository couponsRepository;

    @Autowired
    private UserCouponsRepository userCouponsRepository;

    @Autowired
    private HttpSession session;

    // 会員登録入力画面
    @RequestMapping(
        path = "/client/user/regist/input",
        method = RequestMethod.GET
    )
    public String registInput(Model model) {

        UserForm userForm =
            (UserForm) session.getAttribute("registUserForm");

        if (userForm == null) {
            userForm = new UserForm();
        }

        model.addAttribute("userForm", userForm);

        return "user/regist/regist_input";
    }

    // 確認画面から入力画面に戻る
    @RequestMapping(
        path = "/client/user/regist/input",
        method = RequestMethod.POST
    )
    public String registInputBack(Model model) {

        UserForm userForm =
            (UserForm) session.getAttribute("registUserForm");

        if (userForm == null) {
            return "redirect:/client/user/regist/input";
        }

        model.addAttribute("userForm", userForm);

        return "user/regist/regist_input";
    }

    // 会員登録確認画面
    @RequestMapping(
        path = "/client/user/regist/check",
        method = RequestMethod.POST
    )
    public String registCheck(
            @Valid @ModelAttribute("userForm") UserForm form,
            BindingResult result) {

        if (result.hasErrors()) {
            return "user/regist/regist_input";
        }

        // メールアドレスの重複確認
        if (userRepository.findByEmail(form.getEmail()) != null) {
            result.rejectValue(
                "email",
                "email.duplicate",
                "このメールアドレスは既に登録されています。"
            );

            return "user/regist/regist_input";
        }

        // 一般会員の権限を設定
        form.setAuthority(2);

        // 登録内容をセッションに保存
        session.setAttribute("registUserForm", form);

        return "user/regist/regist_check";
    }

    // 会員登録確定処理
    @Transactional
    @RequestMapping(
        path = "/client/user/regist/complete",
        method = RequestMethod.POST
    )
    public String registComplete() {

        UserForm form =
            (UserForm) session.getAttribute("registUserForm");

        if (form == null) {
            return "redirect:/client/user/regist/input";
        }

        // 登録確定時にも重複を確認
        if (userRepository.findByEmail(form.getEmail()) != null) {
            return "redirect:/client/user/regist/input";
        }

        // 会員情報を作成
        User user = new User();

        BeanUtils.copyProperties(form, user);

        // 一般会員として登録
        user.setId(null);
        user.setAuthority(2);

        // 会員情報を保存
        User savedUser = userRepository.save(user);

        // 新規登録特典クーポンを取得
        Coupon coupon = couponsRepository
                .findById(NEW_USER_COUPON_ID)
                .orElseThrow(() ->
                    new IllegalStateException(
                        "新規会員登録特典クーポンが存在しません。"
                    )
                );

        // クーポン付与情報を作成
        UserCoupon userCoupon = new UserCoupon();

        userCoupon.setUser(savedUser);
        userCoupon.setCoupon(coupon);
        userCoupon.setIsUsed(0);

        // クーポンを会員に付与
        userCouponsRepository.save(userCoupon);

        // 登録情報をセッションから削除
        session.removeAttribute("registUserForm");

        return "redirect:/client/user/regist/complete";
    }

    // 登録完了画面
    @RequestMapping(
        path = "/client/user/regist/complete",
        method = RequestMethod.GET
    )
    public String registCompleteView() {

        return "user/regist/regist_complete";
    }
}
