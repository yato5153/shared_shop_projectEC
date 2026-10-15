//ユーザークーポンテーブル用エンティティ

package jp.co.sss.shop.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_coupons")
public class UserCoupon {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_user_coupons_gen")
	@SequenceGenerator(name = "seq_user_coupons_gen", sequenceName = "seq_user_coupons", allocationSize = 1)
	/**
	 * ユーザークーポンID
	 */
	private Integer id;

	/**
	 * クーポンを所持しているユーザーのID
	 */
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;
	
	/**
	 * ユーザーが所持しているクーポンのID
	 */
	@ManyToOne
	@JoinColumn(name = "coupons_id", referencedColumnName = "id")
	private Coupon coupon;



}
