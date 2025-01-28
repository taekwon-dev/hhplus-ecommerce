package kr.hhplus.be.server.api.payment.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import kr.hhplus.be.server.api.order.application.OrderFacade;
import kr.hhplus.be.server.api.order.application.dto.request.CreateOrderParam;
import kr.hhplus.be.server.api.payment.controller.dto.request.PaymentRequest;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.point.model.Point;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.product.model.Category;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.repository.CategoryRepository;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.ControllerTest;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.FieldDescriptor;

import java.util.List;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

@DisplayName("결제 API 통합 테스트")
class PaymentControllerTest extends ControllerTest {

    private static final FieldDescriptor[] PAYMENT_REQUEST_FIELD_DESCRIPTORS = {
            fieldWithPath("orderId").description("결제 대상 주문 ID"),
            fieldWithPath("paymentAmount").description("결제 금액"),
            fieldWithPath("paymentMethod").description("결제 방식(POINT_PAYMENT, ...)")
    };

    private static final FieldDescriptor[] PAYMENT_RESPONSE_FIELD_DESCRIPTORS = {
            fieldWithPath("orderId").description("결제 대상 주문 ID"),
            fieldWithPath("paymentId").description("결제 ID"),
            fieldWithPath("paymentAmount").description("총 결제 금액"),
            fieldWithPath("paymentMethod").description("결제 방식(POINT_PAYMENT, ..."),
            fieldWithPath("paymentStatus").description("결제 상태(CONFIRMED, PENDING, ...)")
    };

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderFacade orderFacade;

    @DisplayName("결제 성공 시, 200을 응답한다.")
    @Test
    void createPayment() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        List<CreateOrderParam.Detail> createOrderParamDetails = List.of(new CreateOrderParam.Detail(product.getId(), product.getSalesPrice(), 1));
        CreateOrderParam createOrderParam = new CreateOrderParam(createOrderParamDetails);
        long savedOrderId = orderFacade.order(user.getId(), createOrderParam);

        PaymentRequest request = new PaymentRequest(savedOrderId, product.getSalesPrice(), PaymentMethod.POINT_PAYMENT);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user.getId())
                .body(request)
                .filter(document("payment",
                        requestFields(PAYMENT_REQUEST_FIELD_DESCRIPTORS),
                        responseFields(PAYMENT_RESPONSE_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/payments")
                .then().log().all().statusCode(200);
    }

    @DisplayName("결제 시, 인증 토큰이 누락된 경우 401 에러가 발생한다.")
    @Test
    void createPayment_doesNotExistToken() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 50));

        List<CreateOrderParam.Detail> createOrderParamDetails = List.of(new CreateOrderParam.Detail(product.getId(), product.getSalesPrice(), 1));
        CreateOrderParam createOrderParam = new CreateOrderParam(createOrderParamDetails);
        long savedOrderId = orderFacade.order(user.getId(), createOrderParam);

        PaymentRequest request = new PaymentRequest(savedOrderId, product.getSalesPrice(), PaymentMethod.POINT_PAYMENT);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .filter(document("payment",
                        requestFields(PAYMENT_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/payments")
                .then().log().all().statusCode(401);
    }

    @DisplayName("결제 시, 인증 토큰이 유효하지 않은 경우 401 에러가 발생한다.")
    @Test
    void createPayment_invalidUserToken() {
        // given
        long invalidUserToken = 0L;

        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 100_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 50));

        List<CreateOrderParam.Detail> createOrderParamDetails = List.of(new CreateOrderParam.Detail(product.getId(), product.getSalesPrice(), 1));
        CreateOrderParam createOrderParam = new CreateOrderParam(createOrderParamDetails);
        long savedOrderId = orderFacade.order(user.getId(), createOrderParam);

        PaymentRequest request = new PaymentRequest(savedOrderId, product.getSalesPrice(), PaymentMethod.POINT_PAYMENT);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + invalidUserToken)
                .body(request)
                .filter(document("payment",
                        requestFields(PAYMENT_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/payments")
                .then().log().all().statusCode(401);
    }

    @DisplayName("결제 시, 포인트 잔액이 부족할 경우 400 에러가 발생한다.")
    @Test
    void createPayment_insufficientPoint() {
        // given
        int initialBalance = 10_000;
        int orderQuantity = 1;
        int productPrice = 100_000;

        User user = userRepository.save(UserFixture.USER());
        pointRepository.save(new Point(user.getId(), initialBalance));

        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, productPrice, 10));

        List<CreateOrderParam.Detail> createOrderParamDetails = List.of(new CreateOrderParam.Detail(product.getId(), product.getSalesPrice(), 1));
        CreateOrderParam createOrderParam = new CreateOrderParam(createOrderParamDetails);
        long savedOrderId = orderFacade.order(user.getId(), createOrderParam);

        PaymentRequest request = new PaymentRequest(savedOrderId, product.getSalesPrice(), PaymentMethod.POINT_PAYMENT);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user.getId())
                .body(request)
                .filter(document("payment",
                        requestFields(PAYMENT_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/payments")
                .then().log().all().statusCode(400);
    }
}
