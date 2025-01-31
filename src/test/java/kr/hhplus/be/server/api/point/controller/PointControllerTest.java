package kr.hhplus.be.server.api.point.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import kr.hhplus.be.server.api.point.controller.dto.request.ChargeRequest;
import kr.hhplus.be.server.domain.point.model.Point;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.ControllerTest;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

@DisplayName("포인트 API 통합 테스트")
class PointControllerTest extends ControllerTest {

    private static final FieldDescriptor[] CHARGE_POINT_REQUEST_FIELD_DESCRIPTORS = {
            fieldWithPath("amount").description("추가할 포인트")
    };

    private static final FieldDescriptor[] CHARGE_POINT_RESPONSE_FIELD_DESCRIPTORS = {
            fieldWithPath("userId").description("포인트 잔액 조회 사용자 ID"),
            fieldWithPath("balance").description("충전 후, 포인트 잔액")
    };

    private static final FieldDescriptor[] GET_BALANCE_RESPONSE_FIELD_DESCRIPTORS = {
            fieldWithPath("userId").description("포인트 잔액 조회 사용자 ID"),
            fieldWithPath("balance").description("포인트 잔액")
    };

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @DisplayName("포인트 잔액 조회 성공 시, 200을 응답한다.")
    @Test
    void getPointBalance() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user.getId())
                .filter(document("point/get",
                        responseFields(GET_BALANCE_RESPONSE_FIELD_DESCRIPTORS)
                ))
                .when().get("/v1/points")
                .then().log().all().statusCode(200);
    }

    @DisplayName("포인트 잔액 조회 시, 인증 토큰이 유효하지 않은 경우 401 에러가 발생한다.")
    @Test
    void getPointBalance_invalidUserToken() {
        // given
        long invalidUserToken = 0L;

        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + invalidUserToken)
                .filter(document("point/get"
                ))
                .when().get("/v1/points")
                .then().log().all().statusCode(401);
    }

    @DisplayName("포인트 잔액 조회 시, 인증 토큰이 누락된 경우 401 에러가 발생한다.")
    @Test
    void getPointBalance_doesNotExistToken() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .filter(document("point/get"
                ))
                .when().get("/v1/points")
                .then().log().all().statusCode(401);
    }

    @DisplayName("포인트 충전 성공 시, 200을 응답한다.")
    @Test
    void addPoints() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        ChargeRequest request = new ChargeRequest(100_000);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user.getId())
                .body(request)
                .filter(document("point/add",
                        requestFields(CHARGE_POINT_REQUEST_FIELD_DESCRIPTORS),
                        responseFields(CHARGE_POINT_RESPONSE_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/points")
                .then().log().all().statusCode(200);
    }

    @DisplayName("포인트 충전 시, 인증 토큰이 유효하지 않은 경우 401 에러가 발생한다.")
    @Test
    void addPoints_invalidUserToken() {
        // given
        long invalidUserToken = 0L;

        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        ChargeRequest request = new ChargeRequest(100_000);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + invalidUserToken)
                .body(request)
                .filter(document("point/add",
                        requestFields(CHARGE_POINT_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/points")
                .then().log().all().statusCode(401);
    }

    @DisplayName("포인트 충전 시, 인증 토큰이 누락된 경우 401 에러가 발생한다.")
    @Test
    void addPoints_doesNotExistToken() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        ChargeRequest request = new ChargeRequest(100_000);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .filter(document("point/add",
                        requestFields(CHARGE_POINT_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/points")
                .then().log().all().statusCode(401);
    }

    @DisplayName("포인트 충전 시, 최소 충전 포인트를 만족하지 않은 경우 400 에러가 발생한다.")
    @Test
    void addPoints_invalidMinimumAmount() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        ChargeRequest request = new ChargeRequest(0);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user.getId())
                .body(request)
                .filter(document("point/add",
                        requestFields(CHARGE_POINT_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/points")
                .then().log().all().statusCode(400);
    }

    @DisplayName("포인트 충전 시, 충전 포인트 단위를 만족하지 않은 경우 400 에러가 발생한다.")
    @Test
    void addPoints_invalidAmountUnit() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        ChargeRequest request = new ChargeRequest(1_500);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user.getId())
                .body(request)
                .filter(document("point/add",
                        requestFields(CHARGE_POINT_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/points")
                .then().log().all().statusCode(400);
    }
}
