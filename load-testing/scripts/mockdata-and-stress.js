import http from 'k6/http';
import { check } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.1.0/index.js';
import { randomItem, randomIntBetween, randomString } from 'https://jslib.k6.io/k6-utils/1.1.0/index.js';

let accountIds = [];

export let options = {
    stages: [
        { duration: '10s', target: 200 },
        { duration: '5s', target: 200 },
        { duration: '10s', target: 0 },
    ],
};

export default function () {
    // Generate mock account data
    const account = {
        accountNumber: randomString(10),
        currency: 'USD',
    };

    // Create account
    let res = http.post('http://localhost:8080/account', JSON.stringify(account), {
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
        'service unavailable': (r) => r.status === 503
    });
    if (res.status !== 201 && res.status !== 200) {
        return;
    }

    accountIds.push(res.json().id);

    if (accountIds.length >= 10) {
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
        res = http.post('http://localhost:8080/account:transfer', JSON.stringify(transaction), {
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
            'transaction service unavailable': (r) => r.status === 503
        });
    }
}
