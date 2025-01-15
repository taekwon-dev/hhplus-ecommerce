package kr.hhplus.be.server.integration.api.coupon.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import kr.hhplus.be.server.api.coupon.controller.request.CouponIssueRequest;
import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.CouponDiscountType;
import kr.hhplus.be.server.domain.coupon.domain.user.UserCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.ControllerTest;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.FieldDescriptor;

import java.time.LocalDateTime;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

class CouponControllerTest extends ControllerTest {

    private static final FieldDescriptor[] COUPON_ISSUE_REQUEST_FIELD_DESCRIPTORS = {
            fieldWithPath("couponId").description("유저에게 발급할 쿠폰 ID")
    };

    private static final FieldDescriptor[] ISSUED_COUPON_RESPONSE_FIELD_DESCRIPTORS = {
            fieldWithPath("couponId").description("쿠폰 ID"),
            fieldWithPath("code").description("쿠폰 코드"),
            fieldWithPath("startDate").description("쿠폰 사용 시작일 (UTC 표준)"),
            fieldWithPath("endDate").description("쿠폰 사용 종료일 (UTC 표준)")
    };

    private static final FieldDescriptor[] AVAILABLE_COUPONS_RESPONSE_FIELD_DESCRIPTORS = {
            fieldWithPath("[].couponId").description("쿠폰 ID"),
            fieldWithPath("[].code").description("쿠폰 코드"),
            fieldWithPath("[].startDate").description("쿠폰 사용 시작일 (UTC 표준)"),
            fieldWithPath("[].endDate").description("쿠폰 사용 종료일 (UTC 표준)")
    };

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @DisplayName("사용 가능한 쿠폰 목록 조회 성공 시, 200을 응답한다.")
    @Test
    void findAvailableCoupons() {
        // given
        User user = userRepository.save(UserFixture.USER());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        UserCoupon userCoupon = new UserCoupon(user, coupon);
        userCouponRepository.save(userCoupon);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user.getId())
                .filter(document("coupon/getAvailableCoupons",
                        responseFields(AVAILABLE_COUPONS_RESPONSE_FIELD_DESCRIPTORS)
                ))
                .when().get("/v1/coupons")
                .then().log().all().statusCode(200);
    }

    @DisplayName("사용 가능한 쿠폰 목록 조회 시, 인증 토큰이 유효하지 않은 경우 401 에러가 발생한다.")
    @Test
    void findAvailableCoupons_invalidUserToken() {
        // given
        long invalidUserToken = 0L;

        User user = userRepository.save(UserFixture.USER());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        UserCoupon userCoupon = new UserCoupon(user, coupon);
        userCouponRepository.save(userCoupon);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + invalidUserToken)
                .filter(document("coupon/getAvailableCoupons"
                ))
                .when().get("/v1/coupons")
                .then().log().all().statusCode(401);
    }

    @DisplayName("사용 가능한 쿠폰 목록 조회 시, 인증 토큰이 누락된 경우 401 에러가 발생한다.")
    @Test
    void findAvailableCoupons_doesNotExistToken() {
        // given
        User user = userRepository.save(UserFixture.USER());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        UserCoupon userCoupon = new UserCoupon(user, coupon);
        userCouponRepository.save(userCoupon);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .filter(document("coupon/getAvailableCoupons"
                ))
                .when().get("/v1/coupons")
                .then().log().all().statusCode(401);
    }

    @DisplayName("쿠폰 발급 성공 시, 200을 응답한다.")
    @Test
    void issue() {
        // given
        User user = userRepository.save(UserFixture.USER());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        CouponIssueRequest request = new CouponIssueRequest(coupon.getId());

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user.getId())
                .body(request)
                .filter(document("coupon/issue",
                        requestFields(COUPON_ISSUE_REQUEST_FIELD_DESCRIPTORS),
                        responseFields(ISSUED_COUPON_RESPONSE_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/coupons")
                .then().log().all().statusCode(200);
    }

    @DisplayName("쿠폰 발급 시, 인증 토큰이 유효하지 않은 경우 401 에러가 발생한다.")
    @Test
    void issue_invalidUserToken() {
        // given
        long invalidUserToken = 0L;

        User user = userRepository.save(UserFixture.USER());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        CouponIssueRequest request = new CouponIssueRequest(coupon.getId());

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + invalidUserToken)
                .body(request)
                .filter(document("coupon/issue",
                        requestFields(COUPON_ISSUE_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/coupons")
                .then().log().all().statusCode(401);
    }

    @DisplayName("쿠폰 발급 시, 인증 토큰이 누락된 경우 401 에러가 발생한다.")
    @Test
    void issue_doesNotExistToken() {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        CouponIssueRequest request = new CouponIssueRequest(coupon.getId());

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .filter(document("coupon/issue",
                        requestFields(COUPON_ISSUE_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/coupons")
                .then().log().all().statusCode(401);
    }

    @DisplayName("쿠폰 발급 시, 해당 쿠폰이 모두 소진된 경우 400 에러가 발생한다.")
    @Test
    void issue_exceededMaxIssuableCount() {
        // given
        User user = userRepository.save(UserFixture.USER());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 0));
        CouponIssueRequest request = new CouponIssueRequest(coupon.getId());

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user.getId())
                .body(request)
                .filter(document("coupon/issue",
                        requestFields(COUPON_ISSUE_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/coupons")
                .then().log().all().statusCode(400);
    }

    @DisplayName("쿠폰 발급 시, 이미 발급된 쿠폰을 중복해서 발급 요청한 경우 400 에러가 발생한다.")
    @Test
    void issue_alreadyIssuedCoupon() {
        // given
        User user = userRepository.save(UserFixture.USER());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 1));
        CouponIssueRequest request = new CouponIssueRequest(coupon.getId());

        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user.getId())
                .body(request)
                .filter(document("coupon/issue",
                        requestFields(COUPON_ISSUE_REQUEST_FIELD_DESCRIPTORS),
                        responseFields(ISSUED_COUPON_RESPONSE_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/coupons")
                .then().log().all().statusCode(200);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user.getId())
                .body(request)
                .filter(document("coupon/issue",
                        requestFields(COUPON_ISSUE_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/coupons")
                .then().log().all().statusCode(400);
    }
}
