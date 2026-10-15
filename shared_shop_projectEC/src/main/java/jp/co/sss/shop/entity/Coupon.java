package jp.co.sss.shop.entity;

import java.sql.Date;

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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer discountAmount;

    @Column(nullable = false)
    private Date expirationDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}