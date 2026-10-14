package jp.co.sss.shop.controller.item;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jp.co.sss.shop.bean.ItemBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.util.BeanCopy;
import jp.co.sss.shop.util.Constant;

/**
 * 商品管理 一覧表示機能(一般会員用)のコントローラクラス
 *
 * @author SystemShared
 */
@Controller
public class ItemShowCustomerController {
	/**
	 * 商品情報
	 */
	@Autowired
	ItemRepository itemRepository;

	/**
	 * トップ画面 表示処理
	 *
	 * @param model Viewとの値受渡し
	 * @return "/" トップ画面へ
	 * @since 1.5 / 2019/07/09
	 */
	@RequestMapping(path = "/")
	public String index(Model model) {
		// 商品の並び順情報（2：売れ筋順）
		int sortType = 2;

		// 商品情報を全件検索(売れ筋順)
		List<Item> itemList = itemRepository.findAllOrderById();

		// エンティティ内の検索結果をJavaBeansにコピー
		List<ItemBean> itemBeanList = BeanCopy.copyEntityToItemBean(itemList);

		// 商品情報をViewへ渡す
		model.addAttribute("items", itemBeanList);
		model.addAttribute("url", "/");
		model.addAttribute("sortType", sortType);

		return "index";
	}

	/**
	 * 商品情報一覧 表示処理
	 *
	 * @param sortType   並べ替え条件
	 * @param categoryId カテゴリID
	 * @param model      Viewとの値受渡し
	 * @return "item/list/item_list" 商品情報 一覧画面へ
	 * @since 1.2 / 2019/06/22
	 */
	@RequestMapping(path = "/item/list/{sortType}")
	public String showItemList(@PathVariable Integer sortType, Integer categoryId, Model model) {

		if (categoryId != null) {
			// カテゴリIDが含まれていた場合、カテゴリ検索機能を実行
			return "redirect:/item/list/category/" + sortType + "/?categoryId=" + categoryId;
		}

		List<Item> itemList = null;
		if (sortType == 1) {
			// 商品情報を全件検索(新着順)
			itemList = itemRepository.findByDeleteFlagOrderByInsertDateDescIdAsc(Constant.NOT_DELETED);
		} else {
			// 商品情報を全件検索(売れ筋順)
			itemList = itemRepository.findAllOrderById();
		}

		// エンティティ内の検索結果をJavaBeansにコピー
		List<ItemBean> itemBeanList = BeanCopy.copyEntityToItemBean(itemList);

		// 商品情報をViewへ渡す
		model.addAttribute("items", itemBeanList);
		model.addAttribute("sortType", sortType);
		model.addAttribute("url", "/item/list/" + sortType);

		return "item/list/item_list";
	}

	/**
	 * 商品情報一覧カテゴリ別 表示処理
	 *
	 * @param sortType   並べ替え条件
	 * @param categoryId カテゴリID
	 * @param model      Viewとの値受渡し
	 * @return "item/list/item_list" 商品情報 一覧画面へ
	 * @since 1.2 / 2019/06/22
	 */
	@RequestMapping(path = "/item/list/category/{sortType}", method = RequestMethod.GET)
	public String showItemListByCategoryId(@PathVariable Integer sortType, Integer categoryId, Model model) {

		List<Item> itemList = null;

		if (sortType == 1) {
			// 商品情報をカテゴリーIDで条件検索(新着順)
			itemList = itemRepository.findByCategoryIdOrderByInsertDate(categoryId);
		} else {
			// 商品情報をカテゴリーIDで条件検索(売れ筋順)
			itemList = itemRepository.findByCategoryIdOrderById(categoryId);
		}

		// エンティティ内の検索結果をJavaBeansにコピー
		List<ItemBean> itemBeanList = BeanCopy.copyEntityToItemBean(itemList);

		// 商品情報をViewへ渡す
		model.addAttribute("items", itemBeanList);
		model.addAttribute("sortType", sortType);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("url", "/item/list/category/" + sortType + "?categoryId=" + categoryId);

		return "item/list/item_list";
	}

	/**
	 * 商品情報詳細表示処理
	 *
	 * @param id      商品ID
	 * @param model   Viewとの値受渡し
	 * @param session セッション情報
	 * @return "/item/detail/item_detail" 商品情報 詳細画面へ
	 */
	@RequestMapping(path = "/item/detail/{id}")
	public String showItem(@PathVariable int id, Model model) {

		// 商品IDに該当する商品情報を取得する
		Item item = itemRepository.getById(id);

		ItemBean itemBean = new ItemBean();

		// Itemエンティティの各フィールドの値をItemBeanにコピー
		BeanUtils.copyProperties(item, itemBean);

		// 商品情報にカテゴリ名を設定
		itemBean.setCategoryName(item.getCategory().getName());

		// 商品情報をViewへ渡す
		model.addAttribute("item", itemBean);

		return "item/detail/item_detail";
	}
}
