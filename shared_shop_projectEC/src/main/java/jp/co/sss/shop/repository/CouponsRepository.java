package jp.co.sss.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.Coupon;

/**
 * クーポンテーブル用リポジトリ
 */
@Repository
public interface CouponsRepository extends JpaRepository<Coupon, Integer> {

}