package jp.co.sss.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.Order;
import jp.co.sss.shop.util.JPQLConstant;

/**
 * ordersテーブル用リポジトリ
 *
 * @author System Shared
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // 最新の注文情報を1件取得
    Order findTop1ByOrderByInsertDateDesc();

    // 会員IDに該当する注文情報を注文日付順で検索
    List<Order> findByUserIdOrderByInsertDateDescIdAsc(int userId);

    // 注文日付順で注文情報すべてを検索
    @Query(JPQLConstant.FIND_ALL_ORDERS_ORDER_BY_INSERT_DATE)
    List<Order> findAllOrderByInsertDateDesc();

    // 注文IDと会員IDに該当する注文情報を取得
    Optional<Order> findByIdAndUserId(int id, int userId);

//    // 川上さんのOrderShowCustomerControllerの変更 注文詳細表示処理
//    Optional<UserCoupon> findByOrder_id(int id);
}