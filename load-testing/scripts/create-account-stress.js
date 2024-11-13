import http from 'k6/http';
import { uuidv4 } from "https://jslib.k6.io/k6-utils/1.0.0/index.js";
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '10s', target: 200 },
        { duration: '5s', target: 200 },
        { duration: '10s', target: 0 },
    ],
};

export default function() {
    let params = {
        headers: {
            'Content-Type': 'application/json',
            'X-Request-ID': uuidv4().toString()
        },
    };
    let request = {
        "accountNumber": "test",
        "currency": "USD",
        "status": "OPEN"
    }

    let res = http.post('http://localhost:8080/account', JSON.stringify(request), params);
    check(res, {
        'status was 200': r => r.status === 200
    });
    sleep(0.05)
}
