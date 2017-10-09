# individual-income-des-stub

[![Build Status](https://travis-ci.org/hmrc/individual-income-des-stub.svg)](https://travis-ci.org/hmrc/individual-income-des-stub) [ ![Download](https://api.bintray.com/packages/hmrc/releases/individual-income-des-stub/images/download.svg) ](https://bintray.com/hmrc/releases/individual-income-des-stub/_latestVersion)

This is the stateful Stub of the Data Exchange Service (DES) for retrieving/storing test individual income details.
It allows third party developers to create their own records for test individuals on the External Test Environment.

It is a shared Stub for both openid-connect-userinfo and citizen-details.

### Running tests

Unit, integration and component tests can be run with the following:

    sbt test it:test component:test

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")