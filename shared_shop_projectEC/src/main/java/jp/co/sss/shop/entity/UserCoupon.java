package jp.co.sss.shop.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * クーポン情報のエンティティクラス
 */

@Entity
@Table(name = "user_coupons")
public class UserCoupon {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_user_coupons_gen")
	@SequenceGenerator(name = "seq_user_coupons_gen", sequenceName = "seq_user_coupons", allocationSize = 1)

	private Integer id;

	/**
	 * クーポンを所持しているユーザーのID
	 */
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
	private User user;
	
	/**
	 * ユーザーが所持しているクーポンのID
	 */
	@ManyToOne
	@JoinColumn(name = "coupons_id", referencedColumnName = "id", nullable = false)
	private Coupon coupon;

	/**
	 * クーポン使用判定
	 */
	private Integer status;
	
	/**
	 * クーポンを使用した注文
	 */
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

	/**
	 * getterとsetter
	 */
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Coupon getCoupon() {
		return coupon;
	}

	public void setCoupon(Coupon coupon) {
		this.coupon = coupon;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

}
