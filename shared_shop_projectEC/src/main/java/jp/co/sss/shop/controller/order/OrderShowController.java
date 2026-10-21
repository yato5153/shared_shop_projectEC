package jp.co.sss.shop.controller.order;

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
import jp.co.sss.shop.entity.Order;
import jp.co.sss.shop.entity.OrderItem;
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

            //変更箇所
            Integer discountAmount =
                    (Integer) session.getAttribute("discountAmount");

                model.addAttribute("discountAmount", discountAmount);
                
            int discount = discountAmount == null
                    ? 0 : discountAmount;
            //ここまで
            
            orderBean.setTotal(Math.max(0, total - discount));

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
        int subtotal =
                PriceCalc.orderItemPriceTotal(
                        orderItemBeanList);

        // 注文時点の割引金額
        //int discountAmount =
        		//discountAmount == null
                       //? 0 : discountAmount;
        
        //変更箇所
        Integer discountAmount =
                (Integer) session.getAttribute("discountAmount");

            model.addAttribute("discountAmount", discountAmount);
            
        discountAmount = discountAmount == null
                ? 0 : discountAmount;
        //ここまで

        // クーポン適用後の支払金額
        int total =
                Math.max(0, subtotal - discountAmount);

        // 使用クーポン情報
        String couponName = null;

//        Optional<UserCoupon> userCoupon =userCouponsRepository.findB(id);
//
//        if (userCoupon.isPresent()) {
//            couponName =
//                    userCoupon.get().getCoupon().getCouponsName();
//        }

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