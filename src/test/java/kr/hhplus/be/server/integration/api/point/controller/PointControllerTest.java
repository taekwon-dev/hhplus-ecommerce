package kr.hhplus.be.server.integration.api.point.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import kr.hhplus.be.server.api.point.controller.request.PointAddRequest;
import kr.hhplus.be.server.domain.point.domain.Point;
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

class PointControllerTest extends ControllerTest {

    private static final FieldDescriptor[] POINT_ADD_REQUEST_FIELD_DESCRIPTORS = {
            fieldWithPath("userId").description("포인트 추가 요청 사용자 ID"),
            fieldWithPath("amount").description("추가할 포인트 금액")
    };

    private static final FieldDescriptor[] POINT_RESPONSE_FIELD_DESCRIPTORS = {
            fieldWithPath("userId").description("포인트 잔액 조회 사용자 ID"),
            fieldWithPath("balance").description("사용자의 현재 포인트 잔액")
    };

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @DisplayName("현재 포인트 잔액 조회 성공 시, 200을 응답한다.")
    @Test
    void getPointBalance() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user, initialBalance));

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .filter(document("point/get",
                        responseFields(POINT_RESPONSE_FIELD_DESCRIPTORS)
                ))
                .when().get("/v1/points?userId={userId}", user.getId())
                .then().log().all().statusCode(200);
    }

    @DisplayName("포인트 충전 성공 시, 200을 응답한다.")
    @Test
    void addPoints() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user, initialBalance));

        PointAddRequest request = new PointAddRequest(user.getId(), 100_000);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .filter(document("point/add",
                        requestFields(POINT_ADD_REQUEST_FIELD_DESCRIPTORS),
                        responseFields(POINT_RESPONSE_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/points")
                .then().log().all().statusCode(200);
    }

    @DisplayName("포인트 충전 시, 최소 충전 포인트를 만족하지 않은 경우 400 에러가 발생한다.")
    @Test
    void addPoints_invalidMinimumAmount() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user, initialBalance));

        PointAddRequest request = new PointAddRequest(user.getId(), 0);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .filter(document("point/add",
                        requestFields(POINT_ADD_REQUEST_FIELD_DESCRIPTORS)
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
        pointRepository.save(new Point(user, initialBalance));

        PointAddRequest request = new PointAddRequest(user.getId(), 1_500);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .filter(document("point/add",
                        requestFields(POINT_ADD_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/points")
                .then().log().all().statusCode(400);
    }
}
