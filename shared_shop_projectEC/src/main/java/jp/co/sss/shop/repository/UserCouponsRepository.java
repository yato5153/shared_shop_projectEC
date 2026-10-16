package jp.co.sss.shop.repository;

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
	
    // 一般会員が所持しているクーポンの一覧を取得
    List<UserCoupon> findByUser_Id(Integer userId);

    // 指定会員の未使用クーポンを取得
    List<UserCoupon> findByUser_IdAndStatus(
            Integer userId,
            Integer status
    );

    // 指定会員が保有する未使用クーポンを1件取得
    Optional<UserCoupon> findByIdAndUser_IdAndStatus(
            Integer id,
            Integer userId,
            Integer status
    );

    // 指定注文で使用されたクーポンを取得
    Optional<UserCoupon> findByOrder_Id(Integer orderId);
}