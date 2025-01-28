package kr.hhplus.be.server.domain.coupon.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.BaseEntity;
import kr.hhplus.be.server.domain.coupon.exception.InvalidCouponDateException;
import kr.hhplus.be.server.domain.coupon.exception.InvalidMaxIssuableCountException;
import kr.hhplus.be.server.domain.coupon.exception.MaxIssuableCountExceededException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Column(name = "discount_type")
    @Enumerated(EnumType.STRING)
    private CouponDiscountType discountType;

    private int discountAmount;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private int issuedCount;

    private int maxIssuableCount;

    public Coupon(
            Long id,
            String code,
            CouponDiscountType discountType,
            int discountAmount ,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int maxIssuableCount
    ) {
        this(code, discountType, discountAmount, startDate, endDate, maxIssuableCount);
        this.id = id;
    }

    public Coupon(
            String code,
            CouponDiscountType discountType,
            int discountAmount ,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int maxIssuableCount
    ) {
        validateCouponDates(startDate, endDate);
        validateMaxIssuableCount(maxIssuableCount);
        this.code = code;
        this.discountType = discountType;
        this.discountAmount = discountAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.issuedCount = 0;
        this.maxIssuableCount = maxIssuableCount;
    }

    private void validateCouponDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidCouponDateException();
        }
    }

    private void validateMaxIssuableCount(int maxIssuableCount) {
        if (maxIssuableCount < 0) {
            throw new InvalidMaxIssuableCountException();
        }
    }

    public void issue() {
        if (this.issuedCount >= this.maxIssuableCount) {
            throw new MaxIssuableCountExceededException();
        }
        issuedCount++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Coupon coupon)) {
            return false;
        }
        return Objects.equals(id, coupon.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
