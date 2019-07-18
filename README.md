# Revolut transfer service

The service allows to transfer funds between two accounts. The service has been written with thinking of being run on 
more than one instance in an unreliable network. Therefore it supports failures on different levels and allows to not to lose
money in such cases. Please see [Ledger documentation](#ledger) for more details.

#### Build:

-   Normal build  
    ```bash
    gradle build
    ```
    
-   To produce a complete JAR file with all dependencies:  
    ```bash
    gradle fatJar
    ```

#### Run:

Please note all logs are printed to the `STDIN`

-   Pre-generated jar:
    ```bash
    java -jar transfer.jar
    ```

-   After running `fradle fatJar`:
    ```bash
    java -jar ./build/libs/transfer-all-1.0-SNAPSHOT.jar
    ```
#### Tests:
```bash
gradle test
```

Please have a look on [TransferServiceTest](/src/test/java/com/revolut/transfer/TransferServiceTest.java) and on [TransferCommandTest](/src/test/java/com/revolut/transfer/TransferCommandTest.java) for concurrency tests 

<a name="overview"></a>
## Overview
-   `SparkJava` microframework, `jUnit5` + `rest-assured` + `mockito` for testing, `gradle`, `slf4j` + `log4j`
-   All IDs are always in UUIDv4
-   Amounts are considered to be always in the same currency and in minor units

<a name="api"></a>
## API

The API documentation is provided in the [swagger spec](/transfer.yaml) format, or in the [rendered version](/transfer.html).
It is important to note that all API responses are always in the JSON format with partially "fixed" schema. In case of errors
a **trace id** (UUIDv4) is provided in the `msg` field which could be used to search for related log messages. See [Logger documentation](#logger) for details


Example of a request:
```bash
curl -X POST 'http://localhost:4567/transfer?from=5ab59fdf-997f-4a20-ab33-67272b840a19&to=326608e5-5fbf-4505-871d-d0ec830e1994&amount=1'
```

Example of a successful response:
```json
{"responseCode":"NO_ERROR","msg":""}
```

Example of an unsuccessful response:
```json
{"responseCode":"INTERNAL_ERROR","msg":"8a7fc047-e3fc-41e5-a9d0-95372dd9e938","data":""}
```

For all possible `responseCode` values please see [Response class](/src/main/java/com/revolut/router/Response.java)

<a name="project_structure"></a>
## Project structure

Project can be logically split into two parts:

-   "Shared" Revolut codebase: definitions (and some partial implementations) which are most likely to be required in 
other microservices. For example, [AccountService](/src/main/java/com/revolut/account/AccountService.java) to manage accounts
or [CommandExecutor](/src/main/java/com/revolut/command/executor/CommandExecutor.java) to execute financial commands.
They are the packages inside `com.revolut` without `transfer` package

-   `com.revolut.transfer` package which contains _local_ implementations of the required _shared_ services and the actual transferring business logic

## Table of contents
1.  [Account service](#account-service)
2.  [Command & CommandExecutor](#command-and-commandexecutor)  
3.  [Logger](#logger)  
4.  [Router](#router)  
5.  [Locks](#locks)       
    5.1 [Acquiring a lock](#locks-acquiring)  
    5.2 [Releasing a lock](#locks-releasing)  
6.  [Exceptions](#exceptions)  
7.  [Ledger](#ledger)  
    7.1 [Ledger transaction](#ledger-transaction)  
    7.2 [Ledger entry](#ledger-entry)  
8.  [Transfer](#transfer)  
9.  [TODO](#todo)
10.  [Things to improve](#things-to-improve)

<a name="account-service"></a>
### 1. Account service

[Source](/src/main/java/com/revolut/account/AccountService.java)

To manage accounts. [Local implementation](/src/main/java/com/revolut/transfer/LocalAccountService.java) saves accounts in a `HashMap` and has following pre-populated accounts:

| id                                   | Name     |
| ------------------------------------ | --------:|
| 326608e5-5fbf-4505-871d-d0ec830e1994 | John     |
| 5ab59fdf-997f-4a20-ab33-67272b840a19 | Smith    |
| d2febbaf-0edb-4f19-824e-588b712c8c29 | Angelina |

<a name="command-and-commandexecutor"></a>
### 2. Command & CommandExecutor

Each financial command (like [TransferCommand](/src/main/java/com/revolut/transfer/TransferCommand.java)) is represented via the [Command](/src/main/java/com/revolut/command/Command.java) interface. 
[CommandExecutor](/src/main/java/com/revolut/command/executor/CommandExecutor.java) provides a basic way of executing commands with the following lifecycle:
![lifecycle](/docs/CommandExecutor.png)

<a name="logger"></a>
### 3. Logger
[Source](/src/main/java/com/revolut/logger/Logger.java)


`slf4j` plus `log4j`. Logs go to the stdout and each log message has a **trace id** (UUIDv4) which could be used to correlate the relevant (**per API request**) log messages together and/or with the error returned by API. See [API documentation](#api) for more details

Example of a log message:

```bash
08:03:39.840 7fa20d26-5780-45f9-85f2-14f4f48c5519 [main] INFO  Main - Transfer service has been loaded
```

<a name="router"></a>
### 4. Router
[Source](/src/main/java/com/revolut/router/Router.java)

A simple router which allows to add a POST handler. It also generates a **trace id** per request and catches exceptions.

![router](/docs/Router.png)

<a name="locks"></a>
### 5. Locks

[Lock](/src/main/java/com/revolut/lock/Lock.java) and [Lock service](/src/main/java/com/revolut/lock/LockService.java) provide basics for distributed locks.
They are deliberately made not compatible with Java locks for two reasons*:
1. They behave a bit differently than local locks (even than concurrent locks)
2. It is **extremely hard** to implement distributed locks correctly and they always come with different Ts&Cs which could have been not known/considered at the time of development.

A Java ReentrantLock is used in the acquiring process with a spinnig, so it would be fare to say it is a basic simple spinning lock.   

*: subject to discussion

[Local implementation](/src/main/java/com/revolut/transfer/LocalLockService.java) saves locks in a `HashMap` and uses UUIDv4 tokens to track locks ownership.

<a name="locks-acquiring"></a>
#### 5.1 Acquiring a lock
![Locks.Acquire](/docs/Locks.Acquire.png)

<a name="locks-releasing"></a>
#### 5.2 Releasing a lock
![Locks.Release](/docs/Locks.Release.png)

<a name="exceptions"></a>
### 6. Exceptions

A base [RevolutException](/src/main/java/com/revolut/exception/RevolutException.java) is provided for convenience.
It introduces ability to make an exception `displayable` - in that case the `toDisplayable()` will be sent back in the API response. 



<a name="ledger"></a>
### 7. Ledger

Keep track of activity for an account

*   Is for anything you need to track operations on
*   Is append only
*   Provides a complete record of financial transactions of a company
*   A record for a ledger operation (ledger entry) is immutable
*   Double entry rule: every financial transaction has equal and opposite effects in at least two different ledgers.
    The debit and credit amounts must balance, ie. <i>credit - debit = 0</i>  
    ![Double entry](/docs/double_entry.png)
*   Having that we achieve a 2-staged transaction process (which is similar to 2PC), where any error on any step of the process can be easily recovered  

So, per each account we create a [Ledger](/src/main/java/com/revolut/ledger/Ledger.java) with the following fields:

| id   | type     | accountId   | balance.actual   | balance.obligating   | balance.receiving   | 
| :---: | :-------:| :----------:| :---------------:| :-------------------:| :------------------:|
| UUID | There can be many types of ledgers, for now there is only one `INTERNAL`     | ledger owner | available funds to spend | amount obliged to send | amount expected to receive |

[Local implementation](/src/main/java/com/revolut/transfer/LocalLedgerService.java) saves accounts in a `HashMap` and has following pre-populated ledgers:

| id   | type     | accountId   | balance.actual   | balance.obligating   | balance.receiving   | 
| :---: | :-------:| :----------:| :---------------:| :-------------------:| :------------------:|
| 326608e5-5fbf-4505-871d-d0ec830e1994 | `INTERNAL` | 326608e5-5fbf-4505-871d-d0ec830e1994 | 1000 | 0 | 0 |
| ef43bea7-8723-4f14-bab1-6b48ef8cb4fb | `INTERNAL` | 5ab59fdf-997f-4a20-ab33-67272b840a19 | 500 | 0 | 0 |
| 530d0897-36dd-4045-bc1c-89f9dc41c0f2 | `INTERNAL` | d2febbaf-0edb-4f19-824e-588b712c8c29 | 50 | 0 | 0 |
 

We will talk about `Transaction` and `LedgerEntry` later, for now let's quickly look on the logical diagram:

![Logical](/docs/logical.png)

<a name="ledger-transaction"></a>
#### 7.2 Ledger transaction

A [Transaction](/src/main/java/com/revolut/ledger/Transaction/Transaction.java) can be either [Debit](/src/main/java/com/revolut/ledger/Transaction/DebitTransaction.java) or [Credit](/src/main/java/com/revolut/ledger/Transaction/CreditTransaction.java).


Due to the **double entry** rule each debit transaction has a corresponding credit transaction and vice-versa. Each transaction consists of at least 2 steps:
*   Debit:  
    * Obligating  
    Is a legal liability to disburse funds immediately or at a later date as a result of a series of actions  
    * Actual  
*   Credit:  
    * Receiving  
    * Actual      
 
In case of failure there can be one step - `Cancel`.
![Obligating](/docs/obligating.png)


Each step from the same transaction shares the same transaction id and each step from the **opposite** transaction refers to the original transaction

<a name="ledger-entry"></a>
#### 7.2 Ledger entry

[Ledger entry](/src/main/java/com/revolut/ledger/LedgerEntry.java) is an immutable record in the ledger about any activity with funds. Combined together they represent:
-   Who (account id) requested the operation
-   When
-   Current moving status of the funds
-   Users (from & to) actual balance
-   Current status of the transactions (original & opposite)

| id    | type     | subtype     | globalId         | fromLedgerId         | toLedgerId          | amount | createdAt | createdBy
| :---: | :-------:| :----------:| :---------------:| :-------------------:| :------------------:| :----: | :-------: | :-------: |
| UUID | `DEBIT`, `CREDIT` | `OBLIGATION`, `RECEIVING`, `CANCEL`, `ACTUAL` | Global transaction UUID | Ledger UUID of the source | Ledger UUID of the target | transaction amount | Timestamp of creation | Transaction owner |


<a name="transfer"></a>
### 8. Transfer

To conclude, the following "shared" Revolut services have been implemented locally:
-   [AccountService](/src/main/java/com/revolut/account/AccountService.java) -> [LocalAccountService](/src/main/java/com/revolut/transfer/LocalAccountService.java)
-   [LedgerService](/src/main/java/com/revolut/ledger/LedgerService.java) -> [LocalLedgerService](/src/main/java/com/revolut/transfer/LocalLedgerService.java)
-   [LockService](/src/main/java/com/revolut/lock/LockService.java) -> [LocalLockService](/src/main/java/com/revolut/transfer/LocalLockService.java)


With the [TransferCommand](/src/main/java/com/revolut/transfer/TransferCommand.java) as an implementation of a [FinancialCommand](/src/main/java/com/revolut/command/Command.java) 

<a name="todo"></a>
### 9. TODO

Some things have not been done due to the time constraints and their complexity:

-   A (cron) process to "cleanup" broken transactions. Each broken transaction must be cancelled within a specific time constraint

-   When loading a list of the ledger entries all the incomplete transaction have not to be accounted in the balance changes 

<a name="things-to-improve"></a>
### 10. Things to improve

It is crucial to understand what could be the next steps of the service improvements. The following items could be good candidates for discussions:


-   Idempotency keys: there is absolutely no protection from a situation when a caller decided that his transfer request failed somewhere in the middle of the network (i.e. has not reached at all the service) and sending exactly the same request again. Idempotency keys are to the resque

-   Separate ledgers: one for completed transactions, one for in-flight ones
