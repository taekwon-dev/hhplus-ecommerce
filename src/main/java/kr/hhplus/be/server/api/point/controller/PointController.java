package kr.hhplus.be.server.api.point.controller;

import kr.hhplus.be.server.api.point.application.dto.ChargeResult;
import kr.hhplus.be.server.api.point.application.dto.GetBalanceResult;
import kr.hhplus.be.server.api.point.controller.dto.request.ChargeRequest;
import kr.hhplus.be.server.api.point.controller.dto.response.ChargeResponse;
import kr.hhplus.be.server.api.point.application.PointFacade;
import kr.hhplus.be.server.api.point.controller.dto.response.GetBalanceResponse;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/points")
@RequiredArgsConstructor
public class PointController {

    private final PointFacade pointFacade;

    @GetMapping
    public ResponseEntity<GetBalanceResponse> getBalance(User user) {
        GetBalanceResult result = pointFacade.getBalance(user.getId());
        return ResponseEntity.ok(GetBalanceResponse.from(result));
    }

    @PostMapping
    public ResponseEntity<ChargeResponse> chargePoint(User user, @RequestBody ChargeRequest request) {
        ChargeResult result = pointFacade.charge(user.getId(), request.amount());
        return ResponseEntity.ok(ChargeResponse.from(result));
    }
}
