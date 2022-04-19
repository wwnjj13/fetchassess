
curl -H "Content-Type: application/json" -d "{""payer"":""DANNON"",""points"":1000,""timestamp"":""2020-11-02T14:00:00Z""}" -X POST http://localhost:8080/fetch/dispatch/fetch/v1/transaction
curl -H "Content-Type: application/json" -d "{""payer"":""UNILEVER"",""points"":200,""timestamp"":""2020-10-31T11:00:00Z""}" -X POST http://localhost:8080/fetch/dispatch/fetch/v1/transaction
curl -H "Content-Type: application/json" -d "{""payer"":""DANNON"",""points"":-200,""timestamp"":""2020-10-31T15:00:00Z""}" -X POST http://localhost:8080/fetch/dispatch/fetch/v1/transaction
curl -H "Content-Type: application/json" -d "{""payer"":""MILLER COORS"",""points"":10000,""timestamp"":""2020-11-01T14:00:00Z""}" -X POST http://localhost:8080/fetch/dispatch/fetch/v1/transaction
curl -H "Content-Type: application/json" -d "{""payer"":""DANNON"",""points"":300,""timestamp"":""2020-10-31T10:00:00Z""}" -X POST http://localhost:8080/fetch/dispatch/fetch/v1/transaction

curl -H "Content-Type: application/json"  -X GET http://localhost:8080/fetch/dispatch/fetch/v1/balances
curl -H "Content-Type: application/json" -d "{""points"":50000}" -X POST http://localhost:8080/fetch/dispatch/fetch/v1/spend
curl -H "Content-Type: application/json"  -X GET http://localhost:8080/fetch/dispatch/fetch/v1/balances
curl -H "Content-Type: application/json" -d "{""points"":11301}" -X POST http://localhost:8080/fetch/dispatch/fetch/v1/spend
curl -H "Content-Type: application/json"  -X GET http://localhost:8080/fetch/dispatch/fetch/v1/balances
curl -H "Content-Type: application/json" -d "{""points"":11299}" -X POST http://localhost:8080/fetch/dispatch/fetch/v1/spend
curl -H "Content-Type: application/json"  -X GET http://localhost:8080/fetch/dispatch/fetch/v1/balances
curl -H "Content-Type: application/json" -d "{""points"":5000}" -X POST http://localhost:8080/fetch/dispatch/fetch/v1/spend
curl -H "Content-Type: application/json"  -X GET http://localhost:8080/fetch/dispatch/fetch/v1/balances
curl -H "Content-Type: application/json"  -X POST http://localhost:8080/fetch/dispatch/fetch/v1/cleartransactions