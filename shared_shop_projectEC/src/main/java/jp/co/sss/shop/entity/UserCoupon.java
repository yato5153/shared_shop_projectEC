package jp.co.sss.shop.entity;

import jakarta.persistence.Column;
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
 * 会員保有クーポン情報のエンティティクラス
 */
@Entity
@Table(name = "user_coupons")
public class UserCoupon {

    /**
     * 会員保有クーポンID
     */
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "seq_user_coupons_gen"
    )
    @SequenceGenerator(
        name = "seq_user_coupons_gen",
        sequenceName = "seq_user_coupons",
        allocationSize = 1
    )
    private Integer id;

    /**
     * クーポンを保有している会員
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * クーポン情報
     */
    @ManyToOne
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    /**
     * 使用状況（0：未使用、1：使用済み）
     */
    @Column(name = "is_used", nullable = false)
    private Integer isUsed = 0;

    /**
     * クーポンを使用した注文
     */
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

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

    public Integer getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Integer isUsed) {
        this.isUsed = isUsed;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}