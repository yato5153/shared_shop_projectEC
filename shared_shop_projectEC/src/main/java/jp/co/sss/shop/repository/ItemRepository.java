package jp.co.sss.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.util.JPQLConstant;

/**
 * itemsテーブル用リポジトリ
 *
 * @author System Shared
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

	/** 商品情報をすべて取得 */
	@Query(JPQLConstant.FIND_ORDER_BY_ORDER_COUNT)
	public List<Item> findAllOrderById();

	/** 商品情報を新着順で検索 */
	public List<Item> findByDeleteFlagOrderByInsertDateDescIdAsc(int deleteFlag);

	/** 商品情報をカテゴリIDで条件検索(新着順) */
	@Query(JPQLConstant.FIND_BY_CATEGORY_ID_ORDER_BY_INSERT_DATE)
	public List<Item> findByCategoryIdOrderByInsertDate(@Param("categoryId") int categoryId);

	/** 商品情報をカテゴリIDで条件検索(売れ筋順) */
	@Query(JPQLConstant.FIND_BY_CATEGORY_ID_ORDER_BY_ORDER_COUNT)
	public List<Item> findByCategoryIdOrderById(@Param("categoryId") int categoryId);

	/** 注文数より在庫数の少ない商品情報を検索 */
	public Item findByIdAndStockLessThan(int id, int orderNum);

}
