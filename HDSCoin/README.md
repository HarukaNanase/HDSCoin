# SEC - Sistemas de Elevada Confiabilidade (2017-2018)

### Contributors
- [@jtf16](https://github.com/jtf16) - João Freitas
- [@HarukaNanase](https://github.com/HarukaNanase) - André Soares
- [@rubenjpmartins](https://github.com/rubenjpmartins) - Rúben Martins

### About

Cryptocurrencies have become extremely popular with many new cryptocurrencies appearing every day. 
Following this trend, the goal of the project is to create a new cryptocurrency – HDS Coin - which system maintains a set of ledgers, each ledger being associated with a distinct account and uniquely identified by a public key. 


### How to run:

- Open a terminal and run - "mvn clean install" at the root project /HDSCoin 
- On /HDSCoin/HDSCoinServer run "mvn exec:java"
- On /HDSCoin/HDSCoinWallet run "mvn exec:java"
  - To run client with predefined keys - On /HDSCoin/HDSCoinWallet run "mvn exec:java -Dexec.args="client1" where client1 is the folder where the keys are. The keys must be named client.priv and client.pub




