package kr.hhplus.be.server.api.product.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import kr.hhplus.be.server.api.order.application.dto.request.CreateOrderParam;
import kr.hhplus.be.server.api.order.application.OrderFacade;
import kr.hhplus.be.server.api.payment.application.dto.request.PaymentParam;
import kr.hhplus.be.server.api.payment.application.PaymentFacade;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

@DisplayName("상품 API 통합 테스트")
class ProductControllerTest extends ControllerTest {

    private static final FieldDescriptor[] PRODUCTS_RESPONSE_FIELD_DESCRIPTORS = {
            fieldWithPath("products[].productId").description("상품 ID"),
            fieldWithPath("products[].name").description("상품 이름"),
            fieldWithPath("products[].price").description("상품 가격"),
            fieldWithPath("products[].stockQuantity").description("재고 수량"),
            fieldWithPath("totalPages").description("전체 페이지 수"),
            fieldWithPath("page").description("현재 페이지 번호"),
            fieldWithPath("size").description("한 페이지에 포함된 데이터 개수")
    };

    private static final FieldDescriptor[] BEST_SELLING_PRODUCTS_RESPONSE_FIELD_DESCRIPTORS = {
            fieldWithPath("products[].productId").description("상품 ID"),
            fieldWithPath("products[].name").description("상품 이름"),
            fieldWithPath("products[].price").description("상품 가격"),
            fieldWithPath("products[].stockQuantity").description("재고 수량"),
            fieldWithPath("products[].soldQuantity").description("판매 수량")
    };

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private PaymentFacade paymentFacade;

    @MockitoBean
    private Clock clock;

    @DisplayName("판매 가능한 모든 상품 목록 조회 성공 시, 200을 응답한다.")
    @Test
    void findSellableProducts() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        productRepository.save(new Product("라넌큘러스 오버핏 맨투맨1", category, 10_000, 50));
        productRepository.save(new Product("라넌큘러스 오버핏 맨투맨2", category, 10_000, 50));
        productRepository.save(new Product("라넌큘러스 오버핏 맨투맨3", category, 10_000, 50));

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .filter(document("product/best-selling",
                        responseFields(PRODUCTS_RESPONSE_FIELD_DESCRIPTORS)
                ))
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when().get("/v1/products")
                .then().log().all().statusCode(200);
    }

    @DisplayName("지난 3일 동안 가장 많이 팔린 상위 5개 상품 목록 조회 성공 시, 200을 응답한다.")
    @Test
    void findBestSellingProducts() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product1 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨1", category, 10_000, 50));
        Product product2 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨2", category, 10_000, 50));
        Product product3 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨3", category, 10_000, 50));
        Product product4 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨4", category, 10_000, 50));
        Product product5 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨5", category, 10_000, 50));
        Product product6 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨6", category, 10_000, 50));
        List<Product> products = List.of(product1, product2, product3, product4, product5, product6);

        int initialBalance = 1_000_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        for (int i = 1; i <= products.size(); i++) {
            List<CreateOrderParam.Detail> createOrderParamDetails = List.of(new CreateOrderParam.Detail(products.get(i - 1).getId(), products.get(i - 1).getSalesPrice(), i));
            CreateOrderParam createOrderParam = new CreateOrderParam(createOrderParamDetails);
            long savedOrderId = orderFacade.order(user.getId(), createOrderParam);

            PaymentParam paymentParam = new PaymentParam(savedOrderId, products.get(i - 1).getSalesPrice(), PaymentMethod.POINT_PAYMENT);
            paymentFacade.pay(user.getId(), paymentParam);
        }

        LocalDateTime now = LocalDateTime.now();
        Instant instant = now.plusDays(1).atZone(ZoneId.systemDefault()).toInstant();
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .filter(document("product/best-selling",
                        responseFields(BEST_SELLING_PRODUCTS_RESPONSE_FIELD_DESCRIPTORS)
                ))
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when().get("/v1/products/best-selling")
                .then().log().all().statusCode(200);
    }
}
