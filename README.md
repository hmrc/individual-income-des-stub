# individual-income-ip-stub

This is the stateful Stub of the Integration Platform and abstracts connectivity to HoDs via downstream connector (DES or HIP). This stub is for retrieving/storing test individual income details.
It allows third party developers to create their own records for test individuals on the External Test Environment.

It is a shared Stub for both individuals-income-api and individuals-employments-api.

### Running tests

Unit, integration and component tests can be run with the following:

    sbt test it:test component:test

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
