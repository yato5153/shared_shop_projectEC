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
public interface UserCouponsRepository extends JpaRepository<UserCoupon, Integer> {
	
    // 一般会員が所持しているクーポンの一覧を取得
    List<UserCoupon> findByUser_Id(Integer userId);
    
    // 一般会員が所持しているクーポンの中から使用可能なものを取得
    List<UserCoupon> findByUser_IdAndStatus(Integer userId, Integer status);

    // 指定会員が保有する未使用クーポンを1件取得
    Optional<UserCoupon> findByIdAndUser_IdAndStatus(
            Integer id,
            Integer userId,
            Integer status
    );
    
    // 注文確定時にクーポンをロックして取得
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT uc FROM UserCoupon uc
        WHERE uc.id = :id
          AND uc.user.id = :userId
          AND uc.status = 0
        """)
    Optional<UserCoupon> findUnusedCouponForUpdate(
            @Param("id") Integer id,
            @Param("userId") Integer userId
    );

    // 指定注文で使用されたクーポンを取得
    Optional<UserCoupon> findByOrder_Id(Integer orderId);
}
