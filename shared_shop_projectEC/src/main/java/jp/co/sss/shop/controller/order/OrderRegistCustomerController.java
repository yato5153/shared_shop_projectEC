package jp.co.sss.shop.controller.order;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.bean.OrderItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.Order;
import jp.co.sss.shop.entity.OrderItem;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.AddressForm;
import jp.co.sss.shop.form.BasketForm;
import jp.co.sss.shop.form.OrderForm;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.repository.OrderItemRepository;
import jp.co.sss.shop.repository.OrderRepository;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.util.PriceCalc;

/**
 * 注文管理 注文受付機能のコントローラクラス
 *
 * @author SystemShared
 */
@Controller
public class OrderRegistCustomerController {

	/**
	 * 会員情報
	 */
	@Autowired
	UserRepository userRpository;

	/**
	 * 商品情報
	 */
	@Autowired
	ItemRepository itemRepository;

	/**
	 * 注文情報
	 */
	@Autowired
	OrderRepository orderRepository;

	/**
	 * 注文商品情報
	 */
	@Autowired
	OrderItemRepository orderItemRepository;

	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;

	/**
	 * お届け先入力処理
	 *
	 * @param model Viewとの値受渡し
	 * @param form  買い物かご情報
	 * @return "order/regist/order_address_input" 送付先入力画面へ
	 * @since 1.2 / 2019/06/21
	 */
	@RequestMapping(path = "/address/input", method = RequestMethod.POST)
	public String inputAddress(boolean backFlg, String postalCode, String address, String name, String phoneNumber,
			Model model, @ModelAttribute BasketForm form) {

		if (form.getItemId() != null) {
			// 買い物リストを生成
			List<BasketBean> basketBeanList = new ArrayList<BasketBean>();

			for (int i = 0; i < form.getItemId().length; i++) {
				// 選択された商品情報から買い物リストを生成
				Item item = itemRepository.getById(form.getItemId()[i]);
				BasketBean basketBean = new BasketBean(item.getId(), item.getName(), item.getStock(),
						form.getOrderNum()[i]);
				basketBeanList.add(basketBean);
			}

			// セッションスコープに買い物リストを入れる
			session.setAttribute("basketBeans", basketBeanList);
		}

		// 届け先情報の取得
		AddressForm addressForm = new AddressForm();
		if (backFlg) {
			// 支払い方法画面から遷移した場合、前回届け先入力画面で入力した値を取得
			addressForm.setPostalCode(postalCode);
			addressForm.setAddress(address);
			addressForm.setName(name);
			addressForm.setPhoneNumber(phoneNumber);
		} else {
			// 買い物かご画面から遷移した場合、データベース上からログイン中のユーザの住所情報を取得
			// セッションスコープから会員情報を取得
			Integer userId = ((UserBean) session.getAttribute("user")).getId();
			User user = userRpository.getById(userId);

			// 送付先フォームを生成し、会員情報を初期値として設定
			BeanUtils.copyProperties(user, addressForm);
		}

		// 送付先情報をViewに渡す
		model.addAttribute("addressForm", addressForm);

		return "order/regist/order_address_input";
	}

	/**
	 * お届け先情報入力エラー時
	 *
	 * @return "order/regist/order_address_input" お届け先入力画面
	 */
	@RequestMapping(path = "/address/input", method = RequestMethod.GET)
	public String inputAddressError() {

		return "order/regist/order_address_input";
	}

	/**
	 * 支払い方法選択処理
	 *
	 * @param model  Viewとの値受渡し
	 * @param form   送付先情報フォーム
	 * @param result 入力チェック結果
	 * @return 入力値エラーあり：送付先入力画面へ 入力値エラーなし："item/regist/item_regist_check"
	 *         カテゴリ情報登録確認画面へ
	 * @since 1.2 / 2019/06/21
	 */
	@RequestMapping(path = "/payment/input", method = RequestMethod.POST)
	public String inputPayment(boolean backFlg, Integer payMethod, Model model, @Valid @ModelAttribute AddressForm form,
			BindingResult result, RedirectAttributes redirectAttributes) {
		// 入力値エラーありの場合、送付先入力画面へ
		if (result.hasErrors()) {

			redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.addressForm", result);

			redirectAttributes.addFlashAttribute("addressForm", form);

			return "redirect:/address/input";
		}

		OrderForm orderForm = new OrderForm();
		// 送付先情報を注文情報にコピー
		BeanUtils.copyProperties(form, orderForm);

		if (backFlg) {
			orderForm.setPayMethod(payMethod);
		}

		// 会員情報を取得
		Integer userId = ((UserBean) session.getAttribute("user")).getId();
		User user = userRpository.getById(userId);

		// 会員情報を注文情報にコピー
		BeanUtils.copyProperties(user, orderForm);
		// 送付先情報を注文情報にコピー（注文情報の送付先情報をユーザが入力した値に上書き）
		BeanUtils.copyProperties(form, orderForm);

		// 注文情報をViewに渡す
		model.addAttribute("orderForm", orderForm);

		return "order/regist/order_payment_input";
	}

	/**
	 * 注文内容確認処理
	 *
	 * @param model  Viewとの値受渡し
	 * @param form   注文情報
	 * @param result 入力値チェック結果
	 * @return 支払方法未選択：支払方法選択画面へ エラーなし："order/regist/order_regist_check" 注文情報
	 *         登録確認画面へ
	 * @since 1.2 / 2019/06/22
	 */
	@RequestMapping(path = "/order/check", method = RequestMethod.POST)
	public String checkOrder(Model model, @Valid @ModelAttribute OrderForm form, BindingResult result) {

		if (form.getPayMethod() == null) {
			// 支払方法が選択されていなかった場合、支払方法選択画面に戻る処理へ
			return inputPaymentBack(model, form);
		}

		// 注文商品リストを生成
		@SuppressWarnings("unchecked")
		List<BasketBean> basketBeanList = ((List<BasketBean>) session.getAttribute("basketBeans"));
		List<OrderItemBean> orderItemBeanList = new ArrayList<OrderItemBean>();

		// 在庫数の確認
		// 在庫不足の商品名リスト（在庫数1以上）
		List<String> itemNameListLessThan = new ArrayList<String>();
		// 在庫不足の商品名リスト（在庫数0）
		List<String> itemNameListZero = new ArrayList<String>();
		// 在庫不足の商品リスト（在庫数0）
		List<BasketBean> itemListZero = new ArrayList<BasketBean>();
		for (BasketBean basketBean : basketBeanList) {
			// 在庫不足の商品を取得する
			Item itemStockLessThanOrderNum = itemRepository.findByIdAndStockLessThan(basketBean.getId(),
					basketBean.getOrderNum());
			if (itemStockLessThanOrderNum != null) {
				// 在庫不足の場合、買い物かごの情報を更新する
				if (itemStockLessThanOrderNum.getStock() > 0) {
					// 在庫数が1以上の場合の処理
					// 在庫不足の商品名リスト（在庫数1以上）に該当の商品を追加する
					itemNameListLessThan.add(itemStockLessThanOrderNum.getName());
					// 買い物かごの注文数を在庫数に上書きする
					basketBean.setOrderNum(itemStockLessThanOrderNum.getStock());
					// 買い物かごで記録されている在庫数を現在の在庫数に上書きする
					basketBean.setStock(itemStockLessThanOrderNum.getStock());
				} else if (itemStockLessThanOrderNum.getStock() == 0) {
					// 在庫不足の商品名リスト（在庫数0）に該当の商品を追加する
					itemNameListZero.add(itemStockLessThanOrderNum.getName());
					// 在庫数が0の商品をリストに追加する
					itemListZero.add(basketBean);
				}
			}
		}

		// 在庫数が0の商品を買い物かごから削除する
		for (BasketBean itemZero : itemListZero) {
			basketBeanList.remove(itemZero);
		}

		// 注文商品情報に、該当商品の情報をコピー
		for (BasketBean basketBean : basketBeanList) {
			Item item = itemRepository.getById(basketBean.getId());

			OrderItemBean orderItemBean = new OrderItemBean();
			BeanUtils.copyProperties(item, orderItemBean);

			orderItemBean.setOrderNum(basketBean.getOrderNum());

			int subtotal = item.getPrice() * basketBean.getOrderNum();
			orderItemBean.setSubtotal(subtotal);

			orderItemBeanList.add(orderItemBean);
		}

		// 合計金額を算出
		int total = PriceCalc.orderItemBeanPriceTotal(orderItemBeanList);

		// カード情報、注文商品情報をViewに渡す
		model.addAttribute("orderItemBeans", orderItemBeanList);
		model.addAttribute("total", total);
		model.addAttribute("orderForm", form);

		// 在庫不足の商品名をVeiwに渡す
		model.addAttribute("itemNameListLessThan", itemNameListLessThan);
		model.addAttribute("itemNameListZero", itemNameListZero);

		// 注文確認処理中に在庫数が0になった場合は、セッション中の買い物かご情報を削除する
		if (basketBeanList.size() == 0) {
			session.removeAttribute("basketBeans");
		}

		return "order/regist/order_check";
	}

	/**
	 * 支払い方法選択画面への戻る処理
	 *
	 * @param model Viewとの値受渡し
	 * @param form  注文情報
	 * @return "order/regist/order_payment_input" 支払方法選択・入力画面へ
	 */
	@RequestMapping(path = "/payment/input/back", method = RequestMethod.POST)
	public String inputPaymentBack(Model model, @ModelAttribute OrderForm form) {

		// 注文情報をViewに渡す
		model.addAttribute("orderForm", form);

		return "order/regist/order_payment_input";
	}

	/**
	 * 注文登録完了の処理
	 *
	 * @param model              Viewとの値受渡し
	 * @param form               注文情報
	 * @param redirectAttributes リダイレクト後情報保持
	 * @return 在庫切れありの場合："redirect:/basket/list/0" 買い物かご画面へ
	 *         在庫切れなしの場合："order/regist/order_complete" 注文完了画面へ
	 */
	@RequestMapping(path = "/order/complete", method = RequestMethod.POST)
	public String completeOrder(Model model, @ModelAttribute OrderForm form, RedirectAttributes redirectAttributes) {

		// 買い物かごの中身を取得
		@SuppressWarnings("unchecked")
		List<BasketBean> basketBeanList = ((List<BasketBean>) session.getAttribute("basketBeans"));

		// 在庫数の確認
		// ※注文確定時に在庫数が変化した場合を想定した処理（詳細設計書のp31を参照）
		Item errorItem = null;
		for (BasketBean basketBean : basketBeanList) {
			errorItem = itemRepository.findByIdAndStockLessThan(basketBean.getId(), basketBean.getOrderNum());
			if (errorItem != null) {
				break;
			}
		}

		// 商品の在庫数から購入数を引く
		if (errorItem == null) {
			for (BasketBean basketBean : basketBeanList) {
				Item item = itemRepository.getById(basketBean.getId());
				int stock = item.getStock() - basketBean.getOrderNum();
				item.setStock(stock);
				itemRepository.save(item);
			}
		} else {
			// 在庫切れ商品がある場合、エラーメッセージを設定し買い物かごに戻る
			redirectAttributes.addFlashAttribute("errorItem", errorItem.getName());
			return "redirect:/basket/list/0";
		}

		// 注文テーブルに登録する（1件）
		Order order = new Order();
		order.setPostalCode(form.getPostalCode());
		order.setAddress(form.getAddress());
		order.setName(form.getName());
		order.setPhoneNumber(form.getPhoneNumber());
		order.setPayMethod(form.getPayMethod());
		Integer userId = ((UserBean) session.getAttribute("user")).getId();
		User user = userRpository.getById(userId);
		order.setUser(user);
		orderRepository.save(order);

		// 注文商品テーブルに登録する（商品の個数分）
		order = orderRepository.findTop1ByOrderByInsertDateDesc();
		for (BasketBean basketBean : basketBeanList) {
			OrderItem orderItem = new OrderItem();
			orderItem.setQuantity(basketBean.getOrderNum());
			Item item = itemRepository.getById(basketBean.getId());
			orderItem.setItem(item);
			orderItem.setOrder(order);
			// 購入時点の商品単価をsetしてDBへINSERTする
			orderItem.setPrice(item.getPrice());
			orderItemRepository.save(orderItem);
		}

		// セッションスコープの買い物かご情報を初期化
		session.setAttribute("basketBeans", null);

		// 注文情報をViewに渡す
		model.addAttribute("orderForm", form);

		return "order/regist/order_complete";
	}
}
