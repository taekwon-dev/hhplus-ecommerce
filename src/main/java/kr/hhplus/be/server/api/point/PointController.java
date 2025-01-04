package kr.hhplus.be.server.api.point;

import kr.hhplus.be.server.api.point.dto.PointRequestDto;
import kr.hhplus.be.server.api.point.dto.PointResponseDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PointController {

    @GetMapping("/points")
    public PointResponseDto getPoints() {
        return new PointResponseDto(10_000L);
    }

    @PostMapping("/points")
    public PointResponseDto chargePoints(@RequestBody PointRequestDto pointRequestDto) {
        return new PointResponseDto(20_000L);
    }
}
