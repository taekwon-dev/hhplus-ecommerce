CREATE TABLE user
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    address_line1 VARCHAR(100) NOT NULL,
    address_line2 VARCHAR(100),
    postal_code   VARCHAR(10)
);

CREATE TABLE category
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    name      VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    FOREIGN KEY (parent_id) REFERENCES category (id) ON DELETE SET NULL
);

CREATE TABLE product
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id    BIGINT      NOT NULL,
    name           VARCHAR(50) NOT NULL,
    price          INT         NOT NULL,
    stock_quantity INT         NOT NULL,
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE CASCADE
);

CREATE TABLE point
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    balance INT    NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

CREATE TABLE cart
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
);

CREATE TABLE `order`
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'PAYMENT_PENDING' COMMENT 'PAYMENT_PENDING, PAYMENT_COMPLETED, CANCELLED',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

CREATE TABLE order_product
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT    NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order` (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
);

CREATE TABLE coupon
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    code        VARCHAR(50) NOT NULL,
    discount_type      VARCHAR(20) NOT NULL COMMENT 'FIXED, RATE',
    discount_amount     INT         NOT NULL,
    start_date         DATETIME    NOT NULL,
    end_date           DATETIME    NOT NULL,
    issued_count       INT         NOT NULL,
    max_issuable_count INT         NOT NULL
);

CREATE TABLE coupon_usage
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id  BIGINT      NOT NULL,
    coupon_id BIGINT      NOT NULL,
    status    VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPLIED, CANCELLED',
    FOREIGN KEY (order_id) REFERENCES `order` (id) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES coupon (id) ON DELETE CASCADE
);

CREATE TABLE user_coupon
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id   BIGINT      NOT NULL,
    coupon_id BIGINT      NOT NULL,
    status    VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE, USED, EXPIRED',
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES coupon (id) ON DELETE CASCADE
);

CREATE TABLE point_transaction
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT      NOT NULL,
    transaction_type VARCHAR(20) NOT NULL COMMENT 'CHARGE, USAGE',
    amount           INT         NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

CREATE TABLE payment
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id       BIGINT      NOT NULL,
    method VARCHAR(20) NOT NULL COMMENT 'CREDIT_CARD, MOBILE_PAYMENT, POINT_PAYMENT',
    amount          INT         NOT NULL,
    status         VARCHAR(20) NOT NULL COMMENT 'COMPLETED, CANCELLED, FAILED',
    FOREIGN KEY (order_id) REFERENCES `order` (id) ON DELETE CASCADE
);
