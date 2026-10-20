package jp.co.sss.shop.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * クーポン情報のエンティティクラス
 */
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "seq_coupons_gen"
    )
    @SequenceGenerator(
        name = "seq_coupons_gen",
        sequenceName = "seq_coupons",
        allocationSize = 1
    )

    private Integer id;

    /**
     * クーポン名
     */
    @Column(name = "coupons_name", nullable = false, length = 256)
    private String couponsName;
    /**
     * クーポン割引率
     */
    @Column(name = "discount_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountRate;

    /**
     * getterとsetter
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCouponsName() {
        return couponsName;
    }

    public void setCouponsName(String couponsName) {
        this.couponsName = couponsName;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

}
