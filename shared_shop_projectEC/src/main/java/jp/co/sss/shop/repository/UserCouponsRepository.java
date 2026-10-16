
package jp.co.sss.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jp.co.sss.shop.entity.UserCoupon;

/**
 * 会員保有クーポンテーブル用リポジトリ
 */
@Repository
public interface UserCouponsRepository
        extends JpaRepository<UserCoupon, Integer> {

    // 指定会員の未使用クーポンを取得
    List<UserCoupon> findByUser_IdAndControl(
            Integer userId,
            Integer control
    );

    // 指定会員が保有する未使用クーポンを1件取得
    Optional<UserCoupon> findByIdAndUser_IdAndControl(
            Integer id,
            Integer userId,
            Integer control
    );

    // 指定注文で使用されたクーポンを取得
    Optional<UserCoupon> findByOrder_Id(Integer orderId);
 // 注文確定時にクーポンをロックして取得
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT uc FROM UserCoupon uc
        WHERE uc.id = :id
          AND uc.user.id = :userId
          AND uc.control = 0
        """)
    Optional<UserCoupon> findUnusedCouponForUpdate(
            @Param("id") Integer id,
            @Param("userId") Integer userId
    );
}
