# SEC - Sistemas de Elevada Confiabilidade (2017-2018)

### Contributors
- [@jtf16](https://github.com/jtf16) - João Freitas
- [@HarukaNanase](https://github.com/HarukaNanase) - André Soares
- [@rubenjpmartins](https://github.com/rubenjpmartins) - Rúben Martins

### About

Cryptocurrencies have become extremely popular with many new cryptocurrencies appearing every day.
Following this trend, the goal of the project is to create a new cryptocurrency – HDS Coin - which system maintains a set of ledgers, each ledger being associated with a distinct account and uniquely identified by a public key.

### Software Needed
In order to run this project you will need the following:

- JDK 1.8
- Maven 3


### Compiling

- Open a terminal and navigate to the HDSCoin folder.
- run "mvn compile"
- Both modules should automatically compile with all the dependencies needed.

### Testing
To run the tests, you can use the 'mvn test' command at the HDSCoin folder or individually in each of the modules folders.

Special attention to the SecurityManagerTest class where attempts to tamper with information tests are done, simulating Replay Attacks, Tampering of data and others.


### How to run:

- Open a terminal per ledger and run - "mvn clean install" at the root project /HDSCoin
- On each terminal run:
	- "mvn compile exec:java -Dexec.args="ledger1"
	- "mvn compile exec:java -Dexec.args="ledger2"
	- "mvn compile exec:java -Dexec.args="ledger3"
	- "mvn compile exec:java -Dexec.args="ledger4"


- On /HDSCoin/HDSCoinWallet run "mvn exec:java"
  - To run client with predefined keys - On /HDSCoin/HDSCoinWallet run "mvn compile exec:java -Dexec.args="client1" where client is the folder containing the keystore for the client.


  - Two pre-generated key pairs are presented in the client1 and client2 folders. Those can be used to test the system and run demos on the software

- After launching wallet, you can run the following commands:
 	- create_account - Creates an account on the server with the current keys loaded into the wallet
 	- check_account - Checks current wallet account in the server and prints account information
 	- create_transaction - Allows user to send a transaction of HDSCoins to another user by using the recipient's public key as the address and stipulating an ammount.
 	- receive_transaction - Allows recipient of a transaction to claim the coins previously sent into their balance.
 	- audit - Allows an auditor to view any account information as long as they have the account's public key. This command can be run by any user, registered or not into the system.

 	Other commands:

 	- request_chain - Requests the whole chain to the server, which includes all transactions since the start.

 ### Demo:

 A quick demo that you can run is the following:
 - Open 3 terminals, 1 in the HDSCoinServer folder and 2 in the HDSCoinWallet folder
 - In the terminal for the server run 'mvn exec:java'
 - In one of the wallet terminals, run 'mvn exec:java -Dexec.args:"client1"'
 - In the other wallet terminal, run 'mvn exec:java -Dexec.args:"client2"'
 - In both wallet terminals run 'create_account' to register both accounts
 - Copy the public key from one of the wallet terminals and in the other terminal run 'create_transaction'
 - Use the copied public key as the input for the transaction operation and try different values (initial balance is set to 50), we recommend using a value lower or equal to 50 (the others will error out)
 - After this, you can run 'check_account' and see that your balance is now changed.
 - Run 'check_account' in the second wallet and check that the balance is still the same, but you now have a pending transaction.
 - In the second wallet run 'receive_transaction' and input the first wallet's public keys
 - After this run 'check_account' again and you will now see that you have the balance from the transaction added to your initial balance
 - You can now run audit in any of the accounts to get the transaction history (you could also run it while pending as audit shows both pending and completed transactions)
 - You can also request the blockchain using 'request_chain' in any wallet, and it will show you the current blocks and their info.
 - You can now kill the server and re-run it.
 - You will see that all the information is still available and you can re-connect the wallets and continue operations.


 If you want to test the Replay Attacks and Tampering with any request, please refer to LedgerTest class under the test folder of the server as those are not possible to test under the client.
