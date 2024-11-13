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
        "transactionId": uuidv4().toString(),
        "fromAccount": "000354eb-2d86-4c0d-86ca-6d3635fdd062",
        "toAccount": "00041d8c-d5a6-4f91-bbbc-3f07c502f4fb",
        "amount": Math.random(),
        "timestamp": new Date().toISOString()
    }

    let res = http.post('http://localhost:8080/account:transfer', JSON.stringify(request), params);
    check(res, {
        'status was 200': r => r.status === 200,
        'status was 429': r => r.status === 429,
    });
    sleep(0.05)
}
