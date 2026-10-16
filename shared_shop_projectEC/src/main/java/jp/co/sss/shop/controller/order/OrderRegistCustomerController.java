
package jp.co.sss.shop.controller.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.bean.OrderItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Coupon;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.Order;
import jp.co.sss.shop.entity.OrderItem;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.entity.UserCoupon;
import jp.co.sss.shop.form.AddressForm;
import jp.co.sss.shop.form.BasketForm;
import jp.co.sss.shop.form.OrderForm;
import jp.co.sss.shop.repository.CouponsRepository;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.repository.OrderItemRepository;
import jp.co.sss.shop.repository.OrderRepository;
import jp.co.sss.shop.repository.UserCouponsRepository;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.util.PriceCalc;

/**
 * 注文管理 注文受付機能のコントローラクラス
 * クーポン機能対応版
 */
@Controller
public class OrderRegistCustomerController {

    @Autowired
    UserRepository userRpository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    UserCouponsRepository userCouponsRepository;
    
    @Autowired
    CouponsRepository couponsRepository;

    @Autowired
    HttpSession session;

    /**
     * ログイン会員IDを取得
     */
    private Integer getLoginUserId() {
        UserBean userBean =
                (UserBean) session.getAttribute("user");

        if (userBean == null) {
            return null;
        }

        return userBean.getId();
    }

    /**
     * 利用可能なクーポン一覧を画面に渡す
     */
    private void addAvailableCoupons(Model model) {

        Integer userId = getLoginUserId();

        if (userId == null) {
            model.addAttribute(
                    "availableCoupons",
                    new ArrayList<UserCoupon>());
            return;
        }

        List<UserCoupon> coupons =
                userCouponsRepository.findByUser_IdAndControl(userId, 0);

        model.addAttribute("availableCoupons", coupons);
    }


/**
 * 選択クーポンの使用可否を確認
 */

private Optional<UserCoupon> findValidCoupon(
        Integer userCouponId,
        Integer userId,
        int subtotal) {

    if (userCouponId == null || userId == null || subtotal <= 0) {
        return Optional.empty();
    }

    Optional<UserCoupon> result =
            userCouponsRepository.findByIdAndUser_IdAndControl(
                    userCouponId, userId, 0);

    if (result.isEmpty()) {
        return Optional.empty();
    }

    UserCoupon userCoupon = result.get();

    java.math.BigDecimal discountRate =
            userCoupon.getCoupon().getDiscountRate();

    if (discountRate == null
            || discountRate.compareTo(java.math.BigDecimal.ZERO) <= 0
            || discountRate.compareTo(new java.math.BigDecimal("100")) > 0) {
        return Optional.empty();
    }

    return Optional.of(userCoupon);
}

    /**
     * お届け先入力処理
     */
    @RequestMapping(
            path = "/address/input",
            method = RequestMethod.POST)
    public String inputAddress(
            boolean backFlg,
            String postalCode,
            String address,
            String name,
            String phoneNumber,
            Model model,
            @ModelAttribute BasketForm form) {

        Integer userId = getLoginUserId();

        if (userId == null) {
            return "redirect:/login";
        }

        if (form.getItemId() != null) {

            List<BasketBean> basketBeanList =
                    new ArrayList<>();

            for (int i = 0; i < form.getItemId().length; i++) {

                Item item = itemRepository
                        .getById(form.getItemId()[i]);

                BasketBean basketBean = new BasketBean(
                        item.getId(),
                        item.getName(),
                        item.getStock(),
                        form.getOrderNum()[i]);

                basketBeanList.add(basketBean);
            }

            session.setAttribute(
                    "basketBeans", basketBeanList);
        }

        AddressForm addressForm = new AddressForm();

        if (backFlg) {

            addressForm.setPostalCode(postalCode);
            addressForm.setAddress(address);
            addressForm.setName(name);
            addressForm.setPhoneNumber(phoneNumber);

        } else {

            User user = userRpository.getById(userId);

            BeanUtils.copyProperties(user, addressForm);
        }

        model.addAttribute("addressForm", addressForm);

        return "order/regist/order_address_input";
    }

    /**
     * お届け先入力エラー時
     */
    @RequestMapping(
            path = "/address/input",
            method = RequestMethod.GET)
    public String inputAddressError() {

        return "order/regist/order_address_input";
    }

    /**
     * 支払い方法・クーポン選択画面
     */
    @RequestMapping(
            path = "/payment/input",
            method = RequestMethod.POST)
    public String inputPayment(
            boolean backFlg,
            Integer payMethod,
            Model model,
            @Valid @ModelAttribute AddressForm form,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (getLoginUserId() == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {

            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.addressForm",
                    result);

            redirectAttributes.addFlashAttribute(
                    "addressForm", form);

            return "redirect:/address/input";
        }

        OrderForm orderForm = new OrderForm();

        BeanUtils.copyProperties(form, orderForm);

        if (backFlg) {
            orderForm.setPayMethod(payMethod);
        }

        Integer userId = getLoginUserId();

        User user = userRpository.getById(userId);

        BeanUtils.copyProperties(user, orderForm);
        BeanUtils.copyProperties(form, orderForm);

        model.addAttribute("orderForm", orderForm);

        // 利用可能クーポン一覧を取得
        addAvailableCoupons(model);

        return "order/regist/order_payment_input";
    }

    /**
     * 注文内容確認処理
     */
    @RequestMapping(
            path = "/order/check",
            method = RequestMethod.POST)
    public String checkOrder(
            Model model,
            @Valid @ModelAttribute OrderForm form,
            BindingResult result) {

        Integer userId = getLoginUserId();

        if (userId == null) {
            return "redirect:/login";
        }

        if (form.getPayMethod() == null
                || result.hasErrors()) {

            return inputPaymentBack(model, form);
        }

        @SuppressWarnings("unchecked")
        List<BasketBean> basketBeanList =
                (List<BasketBean>)
                session.getAttribute("basketBeans");

        if (basketBeanList == null
                || basketBeanList.isEmpty()) {
            return "redirect:/basket/list/0";
        }

        List<OrderItemBean> orderItemBeanList =
                new ArrayList<>();

        List<String> itemNameListLessThan =
                new ArrayList<>();

        List<String> itemNameListZero =
                new ArrayList<>();

        List<BasketBean> itemListZero =
                new ArrayList<>();

        // 在庫数の確認
        for (BasketBean basketBean : basketBeanList) {
        	
        	if (basketBean.getOrderNum() == null
        	        || basketBean.getOrderNum() <= 0) {

        	    model.addAttribute(
        	            "quantityError",
        	            "注文数量は1以上を指定してください。");

        	    return inputPaymentBack(model, form);
        	}

            Item itemStockLessThanOrderNum =
                    itemRepository.findByIdAndStockLessThan(
                            basketBean.getId(),
                            basketBean.getOrderNum());

            if (itemStockLessThanOrderNum != null) {

                if (itemStockLessThanOrderNum.getStock() > 0) {

                    itemNameListLessThan.add(
                            itemStockLessThanOrderNum.getName());

                    basketBean.setOrderNum(
                            itemStockLessThanOrderNum.getStock());

                    basketBean.setStock(
                            itemStockLessThanOrderNum.getStock());

                } else {

                    itemNameListZero.add(
                            basketBean.getName());

                    itemListZero.add(basketBean);
                }
            }
        }

        for (BasketBean itemZero : itemListZero) {
            basketBeanList.remove(itemZero);
        }

        // 注文商品情報を生成
        for (BasketBean basketBean : basketBeanList) {

            Item item = itemRepository
                    .getById(basketBean.getId());

            OrderItemBean orderItemBean =
                    new OrderItemBean();

            BeanUtils.copyProperties(item, orderItemBean);

            orderItemBean.setOrderNum(
                    basketBean.getOrderNum());

            int subtotal =
                    item.getPrice() * basketBean.getOrderNum();

            orderItemBean.setSubtotal(subtotal);

            orderItemBeanList.add(orderItemBean);
        }

        int subtotal =
                PriceCalc.orderItemBeanPriceTotal(
                        orderItemBeanList);

        int discountAmount = 0;
        String couponName = null;

        // 選択したクーポンを確認
        if (form.getUserCouponId() != null) {

            Optional<UserCoupon> selectedCoupon =
                    findValidCoupon(
                            form.getUserCouponId(),
                            userId,
                            subtotal);

            if (selectedCoupon.isEmpty()) {

                model.addAttribute(
                        "couponError",
                        "選択したクーポンは利用できません。");

                return inputPaymentBack(model, form);
            }

            UserCoupon userCoupon = selectedCoupon.get();


discountAmount = java.math.BigDecimal.valueOf(subtotal)
        .multiply(userCoupon.getCoupon().getDiscountRate())
        .divide(new java.math.BigDecimal("100"))
        .setScale(0, java.math.RoundingMode.DOWN)
        .intValue();

couponName = userCoupon.getCoupon().getCouponsName();

        }

        int total = subtotal - discountAmount;

        model.addAttribute(
                "orderItemBeans", orderItemBeanList);

        model.addAttribute("subtotal", subtotal);
        model.addAttribute(
                "discountAmount", discountAmount);
        model.addAttribute("couponName", couponName);
        model.addAttribute("total", total);
        model.addAttribute("orderForm", form);

        model.addAttribute(
                "itemNameListLessThan",
                itemNameListLessThan);

        model.addAttribute(
                "itemNameListZero",
                itemNameListZero);

        if (basketBeanList.isEmpty()) {
            session.removeAttribute("basketBeans");
        }

        return "order/regist/order_check";
    }

    /**
     * 支払い方法選択画面への戻る処理
     */
    @RequestMapping(
            path = "/payment/input/back",
            method = RequestMethod.POST)
    public String inputPaymentBack(
            Model model,
            @ModelAttribute OrderForm form) {

        if (getLoginUserId() == null) {
            return "redirect:/login";
        }

        model.addAttribute("orderForm", form);

        // 戻った際もクーポン一覧を表示
        addAvailableCoupons(model);

        return "order/regist/order_payment_input";
    }

    /**
     * 注文確定処理
     */
    @Transactional
    @RequestMapping(
            path = "/order/complete",
            method = RequestMethod.POST)
    public String completeOrder(
            Model model,
            @ModelAttribute OrderForm form,
            RedirectAttributes redirectAttributes) {

        Integer userId = getLoginUserId();

        if (userId == null) {
            return "redirect:/login";
        }

        @SuppressWarnings("unchecked")
        List<BasketBean> basketBeanList =
                (List<BasketBean>)
                session.getAttribute("basketBeans");

        if (basketBeanList == null
                || basketBeanList.isEmpty()) {
            return "redirect:/basket/list/0";
        }

        // 注文確定時に在庫を再確認
        Item errorItem = null;

        int subtotal = 0;

        for (BasketBean basketBean : basketBeanList) {

            Item item = itemRepository
                    .getById(basketBean.getId());

            if (basketBean.getOrderNum() == null
                    || basketBean.getOrderNum() <= 0
                    || item.getStock() < basketBean.getOrderNum()) {
                errorItem = item;
                break;
            }

            subtotal += item.getPrice()
                    * basketBean.getOrderNum();
        }

        if (errorItem != null) {

            redirectAttributes.addFlashAttribute(
                    "errorItem", errorItem.getName());

            return "redirect:/basket/list/0";
        }

        int discountAmount = 0;
        UserCoupon selectedUserCoupon = null;

        // クーポンの再確認
        if (form.getUserCouponId() != null) {

        	Optional<UserCoupon> couponResult =
        	        userCouponsRepository.findUnusedCouponForUpdate(
        	                form.getUserCouponId(),
        	                userId);

        	if (couponResult.isPresent()) {
        	    UserCoupon coupon = couponResult.get();

        	    java.math.BigDecimal rate =
        	            coupon.getCoupon().getDiscountRate();

        	    if (subtotal <= 0
        	            || rate == null
        	            || rate.compareTo(java.math.BigDecimal.ZERO) <= 0
        	            || rate.compareTo(new java.math.BigDecimal("100")) > 0) {
        	        couponResult = Optional.empty();
        	    }
        	}

            if (couponResult.isEmpty()) {

                redirectAttributes.addFlashAttribute(
                        "couponError",
                        "クーポンを利用できません。"
                        + "もう一度選択してください。");

                return "redirect:/basket/list/0";
            }

            selectedUserCoupon = couponResult.get();


discountAmount = java.math.BigDecimal.valueOf(subtotal)
        .multiply(selectedUserCoupon.getCoupon().getDiscountRate())
        .divide(new java.math.BigDecimal("100"))
        .setScale(0, java.math.RoundingMode.DOWN)
        .intValue();

        }

        // 在庫数を更新
        for (BasketBean basketBean : basketBeanList) {

            Item item = itemRepository
                    .getById(basketBean.getId());

            item.setStock(
                    item.getStock() - basketBean.getOrderNum());

            itemRepository.save(item);
        }

        // 注文情報を保存
        Order order = new Order();

        order.setPostalCode(form.getPostalCode());
        order.setAddress(form.getAddress());
        order.setName(form.getName());
        order.setPhoneNumber(form.getPhoneNumber());
        order.setPayMethod(form.getPayMethod());
        order.setDiscountAmount(discountAmount);

        User user = userRpository.getById(userId);

        order.setUser(user);

        // 保存した注文をそのまま使用する
        order = orderRepository.save(order);

        // 注文商品情報を保存
        for (BasketBean basketBean : basketBeanList) {

            Item item = itemRepository
                    .getById(basketBean.getId());

            OrderItem orderItem = new OrderItem();

            orderItem.setQuantity(
                    basketBean.getOrderNum());

            orderItem.setItem(item);
            orderItem.setOrder(order);
            orderItem.setPrice(item.getPrice());

            orderItemRepository.save(orderItem);
        }

        // クーポンを使用済みに変更
        if (selectedUserCoupon != null) {

        	selectedUserCoupon.setControl(1);
            selectedUserCoupon.setOrder(order);

            userCouponsRepository.save(
                    selectedUserCoupon);
        }
        

        // 商品合計5,000円以上で20%クーポンを付与
        if (subtotal >= 5000) {

            Coupon repeatCoupon = couponsRepository.findById(7)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "リピート会員クーポンが見つかりません。"));

            UserCoupon newUserCoupon = new UserCoupon();
            newUserCoupon.setUser(user);
            newUserCoupon.setCoupon(repeatCoupon);
            newUserCoupon.setControl(0);

            userCouponsRepository.save(newUserCoupon);
        }


        // 注文完了後に買い物かごを初期化
        session.removeAttribute("basketBeans");

        model.addAttribute("orderForm", form);
        model.addAttribute(
                "discountAmount", discountAmount);
        model.addAttribute(
                "total", subtotal - discountAmount);

        return "order/regist/order_complete";
    }
}
