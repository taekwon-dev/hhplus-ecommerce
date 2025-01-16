package kr.hhplus.be.server.integration.api.product.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import kr.hhplus.be.server.api.order.controller.request.OrderCreateRequest;
import kr.hhplus.be.server.api.order.controller.request.OrderProductDetail;
import kr.hhplus.be.server.api.order.facade.OrderFacade;
import kr.hhplus.be.server.api.payment.controller.request.PaymentRequest;
import kr.hhplus.be.server.api.payment.facade.PaymentFacade;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
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

class ProductControllerTest extends ControllerTest {

    private static final FieldDescriptor[] PRODUCT_RESPONSE_FIELD_DESCRIPTORS = {
            fieldWithPath("productId").description("상품 ID"),
            fieldWithPath("name").description("상품 이름"),
            fieldWithPath("price").description("상품 가격"),
            fieldWithPath("stockQuantity").description("재고 수량")
    };

    private static final FieldDescriptor[] PRODUCT_ALL_RESPONSE_FIELD_DESCRIPTORS = {
            fieldWithPath("products").description("상품 목록"),
            fieldWithPath("currentPage").description("현재 페이지 번호"),
            fieldWithPath("totalPages").description("총 페이지 수"),
            fieldWithPath("pageSize").description("페이지당 상품 수")
    };

    private static final FieldDescriptor[] BEST_SELLING_PRODUCT_RESPONSE_FIELD_DESCRIPTORS = {
            fieldWithPath("[].productId").description("상품 ID"),
            fieldWithPath("[].name").description("상품 이름"),
            fieldWithPath("[].price").description("상품 가격"),
            fieldWithPath("[].stockQuantity").description("재고 수량"),
            fieldWithPath("[].soldQuantity").description("판매 수량")
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

    @DisplayName("Product 모든 목록 조회 성공 시, 200을 응답한다.")
    @Test
    void findAllProducts() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        productRepository.save(new Product("라넌큘러스 오버핏 맨투맨1", category, 10_000, 50));
        productRepository.save(new Product("라넌큘러스 오버핏 맨투맨2", category, 10_000, 50));
        productRepository.save(new Product("라넌큘러스 오버핏 맨투맨3", category, 10_000, 50));

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .filter(document("product/best-selling",
                        responseFields(PRODUCT_ALL_RESPONSE_FIELD_DESCRIPTORS)
                                .andWithPrefix("products[].", PRODUCT_RESPONSE_FIELD_DESCRIPTORS)
                ))
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when().get("/v1/products")
                .then().log().all().statusCode(200);
    }

    @DisplayName("가장 많이 팔린 상위 5개 Product 조회 성공 시, 200을 응답한다.")
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
        pointRepository.save(new Point(user, initialBalance));

        for (int i = 1; i <= products.size(); i++) {
            OrderProductDetail orderProductDetail = new OrderProductDetail(products.get(i - 1).getId(), i);
            List<OrderProductDetail> orderProductDetails = List.of(orderProductDetail);
            OrderCreateRequest request = new OrderCreateRequest(orderProductDetails);
            long orderId = orderFacade.order(user, request);
            PaymentRequest paymentRequest = new PaymentRequest(orderId, PaymentMethod.POINT_PAYMENT);
            paymentFacade.pay(user, paymentRequest);
        }

        LocalDateTime now = LocalDateTime.now();
        Instant instant = now.plusDays(1).atZone(ZoneId.systemDefault()).toInstant();
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // when & then
        RestAssured.given(spec).log().all()
                .contentType(ContentType.JSON)
                .filter(document("product/best-selling",
                        responseFields(BEST_SELLING_PRODUCT_RESPONSE_FIELD_DESCRIPTORS)
                ))
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when().get("/v1/products/best-selling")
                .then().log().all().statusCode(200);
    }
}
