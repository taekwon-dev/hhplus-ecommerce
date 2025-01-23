import http from 'k6/http';
import { check } from 'k6';

export const options = {
    batchPerHost: 10,
    scenarios: {
        coupon_scenario: {
            vus: 1000,
            // exec: 'coupon_scenario',
            executor: 'per-vu-iterations',
            iterations: 1
        }
    }
};

export default function () {
    const body = JSON.stringify({
        couponId: 1
    });

    // HTTP POST 요청 설정
    const params = {
        headers: {
            'Authorization': `Bearer ${__VU}`,
            'Content-Type': 'application/json',
        },
    };

    // POST 요청 보내기
    let response = http.post('http://localhost:8080/v1/coupons', body, params);

    // 응답 확인
    check(response, {
        'is status 200': (r) => r.status === 200,
    });
}
