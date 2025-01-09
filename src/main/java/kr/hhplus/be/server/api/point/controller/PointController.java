package kr.hhplus.be.server.api.point.controller;

import kr.hhplus.be.server.api.point.controller.request.PointAddRequest;
import kr.hhplus.be.server.api.point.controller.request.PointDeductRequest;
import kr.hhplus.be.server.api.point.controller.response.PointResponse;
import kr.hhplus.be.server.api.point.facade.PointFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private final PointFacade pointFacade;

    @GetMapping("/{userId}")
    public ResponseEntity<PointResponse> getPointBalance(
        @PathVariable long userId
    ) {
        PointResponse response = pointFacade.getPointBalance(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<PointResponse> addPoints(
        @RequestBody PointAddRequest request
    ) {
        PointResponse response = pointFacade.addPoints(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deduct")
    public ResponseEntity<PointResponse> deductPoints(
            @RequestBody PointDeductRequest request
    ) {
        PointResponse response = pointFacade.deductPoints(request);
        return ResponseEntity.ok(response);
    }
}
