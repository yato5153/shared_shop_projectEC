package jp.co.sss.shop.controller.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.OrderBean;
import jp.co.sss.shop.bean.OrderItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Coupon;
import jp.co.sss.shop.entity.Order;
import jp.co.sss.shop.entity.OrderItem;
import jp.co.sss.shop.entity.UserCoupon;
import jp.co.sss.shop.form.OrderShowForm;
import jp.co.sss.shop.repository.OrderRepository;
import jp.co.sss.shop.repository.UserCouponsRepository;
import jp.co.sss.shop.util.PriceCalc;

/**
 * 注文管理 一覧・詳細表示機能（一般会員用）
 */
@Controller
public class OrderShowController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    UserCouponsRepository userCouponsRepository;

    @Autowired
    HttpSession session;

    /**
     * 注文一覧表示処理
     */
    @RequestMapping(path = "/order/list", method = RequestMethod.GET)
    public String showOrderList(
            Model model,
            @ModelAttribute OrderShowForm form) {

        UserBean userBean =
                (UserBean) session.getAttribute("user");

        if (userBean == null) {
            return "redirect:/login";
        }

        List<Order> orderList =
                orderRepository.findByUserIdOrderByInsertDateDescIdAsc(
                        userBean.getId());

        List<OrderBean> orderBeanList = new ArrayList<>();

        for (Order order : orderList) {

            OrderBean orderBean = new OrderBean();

            orderBean.setId(order.getId());
            orderBean.setUserName(order.getUser().getName());
            orderBean.setInsertDate(
                    order.getInsertDate().toString());
            orderBean.setPayMethod(order.getPayMethod());

            int total = 0;

            for (OrderItem orderItem : order.getOrderItemsList()) {
                total += orderItem.getPrice()
                        * orderItem.getQuantity();
            }

            //クーポン利用履歴の検索
            Optional<UserCoupon> orderCoupns = userCouponsRepository.findByOrder_Id(orderBean.getId());
            
            Integer discountAmount = 0;
            
            if (orderCoupns.isPresent()) {
            	UserCoupon userCoupon = orderCoupns.get();
            	
            	Coupon couppon = userCoupon.getCoupon();
            	
            	if (couppon != null) {
            		BigDecimal discountRate = couppon.getDiscountRate();
            		
            		BigDecimal discount = BigDecimal.valueOf(total)
            				.multiply(discountRate)
            				.divide(BigDecimal.valueOf(100));
            		
            		discountAmount = discount.intValue();
            	}
            }
            
            orderBean.setTotal(Math.max(0, total - discountAmount));

            orderBeanList.add(orderBean);
        }

        model.addAttribute("orders", orderBeanList);
        model.addAttribute("url", "/order/list");

        return "order/list/order_list";
    }

    /**
     * 注文詳細表示処理
     */
    @RequestMapping(
            path = "/order/detail/{id}",
            method = RequestMethod.GET)
    public String showOrder(
            @PathVariable int id,
            Model model,
            @ModelAttribute OrderShowForm form) {

        // ログイン会員情報
        UserBean userBean =
                (UserBean) session.getAttribute("user");

        if (userBean == null) {
            return "redirect:/login";
        }

        // 本人の注文だけ取得
        Optional<Order> optionalOrder =
                orderRepository.findByIdAndUserId(
                        id, userBean.getId());

        if (optionalOrder.isEmpty()) {
            return "redirect:/order/list";
        }

        Order order = optionalOrder.get();

        // 注文情報
        OrderBean orderBean = new OrderBean();
        BeanUtils.copyProperties(order, orderBean);

        orderBean.setInsertDate(
                order.getInsertDate().toString());

        // 注文商品情報
        List<OrderItemBean> orderItemBeanList =
                new ArrayList<>();

        for (OrderItem orderItem : order.getOrderItemsList()) {

            OrderItemBean orderItemBean =
                    new OrderItemBean();

            orderItemBean.setName(
                    orderItem.getItem().getName());

            orderItemBean.setPrice(
                    orderItem.getPrice());

            orderItemBean.setOrderNum(
                    orderItem.getQuantity());

            int subtotal =
                    orderItem.getPrice()
                    * orderItem.getQuantity();

            orderItemBean.setSubtotal(subtotal);

            orderItemBeanList.add(orderItemBean);
        }

        // 商品合計金額
        int subtotal = PriceCalc.orderItemPriceTotal(orderItemBeanList);
        
        //クーポン利用履歴の検索
        Optional<UserCoupon> orderCoupns = userCouponsRepository.findByOrder_Id(id);
        
        Integer discountAmount = 0;
        
        if (orderCoupns.isPresent()) {
        	UserCoupon userCoupon = orderCoupns.get();
        	
        	Coupon couppon = userCoupon.getCoupon();
        	
        	if (couppon != null) {
        		BigDecimal discountRate = couppon.getDiscountRate();
        		
        		BigDecimal discount = BigDecimal.valueOf(subtotal)
        				.multiply(discountRate)
        				.divide(BigDecimal.valueOf(100));
        		
        		discountAmount = discount.intValue();
        	}
        }

        // クーポン適用後の支払金額
        int total = Math.max(0, subtotal - discountAmount);

        // 使用クーポン情報
        String couponName = null;

        // HTMLへ渡す情報
        model.addAttribute("order", orderBean);
        model.addAttribute("orderItemBeans", orderItemBeanList);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("couponName", couponName);
        model.addAttribute("total", total);

        return "order/detail/order_detail";
    }
}