
package jp.co.sss.shop.repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.UserCoupon;

/**
 * 会員保有クーポンテーブル用リポジトリ
 */
@Repository
public interface UserCouponsRepository extends JpaRepository<UserCoupon, Integer> {

    // 指定会員の未使用クーポンを取得
    List<UserCoupon> findByUser_IdAndIsUsed(
            Integer userId,
            Integer isUsed
    );

    // 指定会員の未使用かつ有効期限内のクーポンを取得
    List<UserCoupon> findByUser_IdAndIsUsedAndCoupon_ExpirationDateGreaterThanEqual(
            Integer userId,
            Integer isUsed,
            Date today
    );

    // 指定会員が保有する未使用クーポンを1件取得
    Optional<UserCoupon> findByIdAndUser_IdAndIsUsed(
            Integer id,
            Integer userId,
            Integer isUsed
    );

    // 指定注文で使用されたクーポンを取得
    Optional<UserCoupon> findByOrder_Id(Integer orderId);
}