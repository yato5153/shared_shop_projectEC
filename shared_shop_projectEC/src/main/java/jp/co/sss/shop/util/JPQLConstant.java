package jp.co.sss.shop.util;

/**
 * 独自JPQLを定義するためのクラス
 *
 * @author System Shared
 */
public class JPQLConstant {
	// 商品情報を全件検索(新着順)
	public static final String FIND_ORDER_BY_INSERT_DATE = "SELECT i FROM Item i INNER JOIN i.category c WHERE i.deleteFlag = 0 ORDER BY i.insertDate DESC,i.id ASC";

	// 商品情報を全件検索(売れ筋順)
	public static final String FIND_ORDER_BY_ORDER_COUNT = "SELECT new Item(i.id, i.name, i.price,i.description, i.image, c.name) FROM Item i INNER JOIN i.category c INNER JOIN i.orderItemList oi WHERE i.deleteFlag = 0 GROUP BY i.id, i.name, i.price,i.description, i.image, c.name ORDER BY COUNT(i.id) DESC,i.id ASC";

	// 商品情報をカテゴリーIDで条件検索(新着順)
	public static final String FIND_BY_CATEGORY_ID_ORDER_BY_INSERT_DATE = "SELECT i FROM Item i INNER JOIN i.category c WHERE i.deleteFlag = 0 AND c.id = :categoryId ORDER BY i.insertDate DESC,i.id ASC";

	// 商品情報をカテゴリーIDで条件検索(売れ筋順)
	public static final String FIND_BY_CATEGORY_ID_ORDER_BY_ORDER_COUNT = "SELECT new Item(i.id, i.name, i.price, i.description, i.image, c.name) FROM Item i INNER JOIN i.category c INNER JOIN i.orderItemList oi WHERE i.deleteFlag = 0 AND c.id = :categoryId GROUP BY i.id, i.name, i.price, i.description, i.image, c.name ORDER BY COUNT(i.id) DESC,i.id ASC";

	// 注文情報を全件検索(新着順)
	public static final String FIND_ALL_ORDERS_ORDER_BY_INSERT_DATE = "SELECT o FROM Order o ORDER BY o.insertDate DESC, o.id ASC";

}
