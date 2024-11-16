import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.1.0/index.js';
import { randomItem, randomIntBetween, randomString } from 'https://jslib.k6.io/k6-utils/1.1.0/index.js';

let accountIds = [];

export let options = {
    stages: [
        { duration: '10s', target: 100 },
        { duration: '45s', target: 100 },
        { duration: '5s', target: 0 },
    ],
};

export let host = __ENV.TARGET_HOST || 'http://localhost:8080';
export let chaosMonkeyUrl = `${host}/actuator/chaosmonkey/assaults/runtime/attack`;
export let enableChaosMonkey = __ENV.ENABLE_CHAOS_MONKEY || 'false';

export function setup() {
    // Start a delayed task to call the Chaos Monkey attack endpoint after 10 seconds
    if (enableChaosMonkey === 'true') {
        // Start a background thread to call the Chaos Monkey attack endpoint after 10 seconds
        new Promise((resolve) => {
            sleep(10);
            let chaosRes = http.post(chaosMonkeyUrl);
            check(chaosRes, {
                'chaos monkey attack triggered': (r) => r.status === 200,
            });
            if (chaosRes.status !== 200) {
                console.error(`Chaos Monkey attack failed: ${chaosRes.status}`);
            } else {
                console.warn('Chaos Monkey attack triggered');
            }
            resolve();
        });
    }
}

export default function () {
    // Generate mock account data
    const account = {
        accountNumber: randomString(10),
        currency: 'USD',
    };

    // Create account
    let res = http.post(`${host}/account`, JSON.stringify(account), {
        headers: {
            'Content-Type': 'application/json',
            'X-Request-ID': uuidv4().toString()
        },
    });
    check(res, {
        'account created': (r) => r.status === 201 || r.status === 200,
        'bad request': (r) => r.status === 400,
        'api not found': (r) => r.status === 404,
        'overloaded': (r) => r.status === 429,
        'server error': (r) => r.status === 500,
        'service gateway error': (r) => r.status === 502,
        'service unavailable': (r) => r.status === 503,
        'service timeout': (r) => r.status === 504,
    });
    if (res.status < 200 || res.status >= 400) {
        console.error(`Account creation failed: ${res.status}`);
    }
    if (res.status !== 201 && res.status !== 200) {
        return;
    }

    accountIds.push(res.json().id);

    if (accountIds.length >= 3) {
        // Randomly select two accounts
        // TODO: simulate hot accounts.
        let fromAccount = randomItem(accountIds);
        let toAccount;
        do {
            toAccount = randomItem(accountIds);
        } while (toAccount === fromAccount);

        // Generate mock transaction data
        let transaction = {
            "transactionId": uuidv4().toString(),
            "fromAccount": fromAccount,
            "toAccount": toAccount,
            "amount": randomIntBetween(10, 1000),
            "timestamp": new Date().toISOString(),
        };

        // Create transaction
        res = http.post(`${host}/account:transfer`, JSON.stringify(transaction), {
            headers: {
                'Content-Type': 'application/json',
                'X-Request-ID': uuidv4().toString()
            },
        });
        check(res, {
            'transaction created': (r) => r.status === 200 || r.status === 201 || r.status === 202,
            'transaction bad request': (r) => r.status === 400,
            'transaction api not found': (r) => r.status === 404,
            'transaction overloaded': (r) => r.status === 429,
            'transaction server error': (r) => r.status === 500,
            'transaction gateway error': (r) => r.status === 502,
            'transaction service unavailable': (r) => r.status === 503,
            'transaction timeout': (r) => r.status === 504,
        });
        if (res.status < 200 || res.status >= 400) {
            console.error(`Transaction creation failed: ${res.status}`);
        }
    }
}
