package kr.hhplus.be.server.api.order.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import kr.hhplus.be.server.api.order.controller.dto.request.CreateOrderRequest;
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

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

@DisplayName("주문 API 통합 테스트")
class OrderControllerTest extends ControllerTest {

    private static final FieldDescriptor[] CREATE_ORDER_REQUEST_FIELD_DESCRIPTORS = {
            fieldWithPath("createOrderRequestDetails").description("주문에 포함된 상품 상세 정보 목록"),
            fieldWithPath("createOrderRequestDetails[].productId").description("상품 ID"),
            fieldWithPath("createOrderRequestDetails[].salesPrice").description("개당 상품 가격"),
            fieldWithPath("createOrderRequestDetails[].quantity").description("주문 수량")
    };

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @DisplayName("주문 성공 시, 201을 응답한다.")
    @Test
    void createOrder() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 1));
        int quantity = 1;

        List<CreateOrderRequest.Detail> createOrderRequestDetails = List.of(new CreateOrderRequest.Detail(product.getId(), product.getSalesPrice(), quantity));
        CreateOrderRequest request = new CreateOrderRequest(createOrderRequestDetails);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user.getId())
                .body(request)
                .filter(document("order",
                        requestFields(CREATE_ORDER_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/orders")
                .then().log().all().statusCode(201);
    }

    @DisplayName("주문 시, 인증 토큰이 유효하지 않은 경우 401 에러가 발생한다.")
    @Test
    void createOrder_invalidUserToken() {
        // given
        long invalidUserToken = 0L;

        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));
        int quantity = 1;

        List<CreateOrderRequest.Detail> createOrderRequestDetails = List.of(new CreateOrderRequest.Detail(product.getId(), product.getSalesPrice(), quantity));
        CreateOrderRequest request = new CreateOrderRequest(createOrderRequestDetails);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + invalidUserToken)
                .body(request)
                .filter(document("order",
                        requestFields(CREATE_ORDER_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/orders")
                .then().log().all().statusCode(401);
    }

    @DisplayName("주문 시, 인증 토큰이 누락된 경우 401 에러가 발생한다.")
    @Test
    void createOrder_doesNotExistToken() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));
        int quantity = 1;

        List<CreateOrderRequest.Detail> createOrderRequestDetails = List.of(new CreateOrderRequest.Detail(product.getId(), product.getSalesPrice(), quantity));
        CreateOrderRequest request = new CreateOrderRequest(createOrderRequestDetails);
        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .filter(document("order",
                        requestFields(CREATE_ORDER_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/orders")
                .then().log().all().statusCode(401);
    }

    @DisplayName("주문 시, 재고가 부족할 경우 400 에러가 발생한다.")
    @Test
    void createOrder_insufficientStockQuantity() {
        // given
        int stockQuantity = 1;
        int orderQuantity = 5;

        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, stockQuantity));

        List<CreateOrderRequest.Detail> createOrderRequestDetails = List.of(new CreateOrderRequest.Detail(product.getId(), product.getSalesPrice(), orderQuantity));
        CreateOrderRequest request = new CreateOrderRequest(createOrderRequestDetails);

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + user.getId())
                .body(request)
                .filter(document("order",
                        requestFields(CREATE_ORDER_REQUEST_FIELD_DESCRIPTORS)
                ))
                .when().post("/v1/orders")
                .then().log().all().statusCode(400);
    }
}
