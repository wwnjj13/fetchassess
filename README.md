# fetchassess

Download `fetch.jar`

Navigate to downloaded file.

run `java -jar fetch.jar`

This will start a server at localhost:8080

The base URL for the endpoint is `http://localhost:8080/fetch/dispatch/fetch/v1/`

The `/transaction` route adds a new transaction.

The `/balances` route returns current balances.

The `/spend` route initiates a spend operations.

I also included a `/cleartransactions` route to ease the testing process.

`curlTesting.bat` is an automaed testing script pointing to the local endpoint.
