package kr.hhplus.be.server.api.goods;

import kr.hhplus.be.server.api.goods.dto.BestSellerGoodsResponseDto;
import kr.hhplus.be.server.api.goods.dto.GoodsResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GoodsController {

    @GetMapping("/goods")
    public List<GoodsResponseDto> getGoods() {
        return List.of(
                new GoodsResponseDto(
                        1L,
                        "T-shirt",
                        19900,
                        100,
                        List.of("Red", "Blue", "Green"),
                        List.of("S", "M", "L")
                ),
                new GoodsResponseDto(
                        2L,
                        "Jeans",
                        49900,
                        50,
                        List.of("Black", "Blue"),
                        List.of("M", "L", "XL")
                ),
                new GoodsResponseDto(
                        3L,
                        "Sneakers",
                        59900,
                        200,
                        List.of("White", "Black"),
                        List.of("42", "43", "44")
                )
        );
    }

    @GetMapping("/goods/best-sellers")
    public List<BestSellerGoodsResponseDto> getBestSellers() {
        return List.of(
                new BestSellerGoodsResponseDto(
                        1L,
                        "T-shirt",
                        150,
                        19900,
                        "Red",
                        "M"
                ),
                new BestSellerGoodsResponseDto(
                        2L,
                        "Jeans",
                        95,
                        49900,
                        "Black",
                        "L"
                )
        );
    }
}
