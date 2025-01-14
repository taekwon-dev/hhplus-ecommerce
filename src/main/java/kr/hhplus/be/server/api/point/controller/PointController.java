package kr.hhplus.be.server.api.point.controller;

import kr.hhplus.be.server.api.point.controller.request.PointAddRequest;
import kr.hhplus.be.server.api.point.controller.response.PointResponse;
import kr.hhplus.be.server.api.point.facade.PointFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/points")
@RequiredArgsConstructor
public class PointController {

    private final PointFacade pointFacade;

    @GetMapping
    public ResponseEntity<PointResponse> getPointBalance(@RequestParam final Long userId) {
        PointResponse response = pointFacade.getPointBalance(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PointResponse> addPoints(@RequestBody PointAddRequest request) {
        PointResponse response = pointFacade.addPoints(request);
        return ResponseEntity.ok(response);
    }
}
