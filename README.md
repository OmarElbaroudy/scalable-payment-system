# payment-system

### A scalable blockchain based payment system.

In this project we create a decentralized payment system that does not require a bank as an indermediary.
When a user buys something from an e-comerce website using the payment system, the system withdraws money from the user's debit card and deposit it into the vendor's wallet. This is done in a number of steps to ensure decentralization and security:

1. The buyer and the vendor must first create their own wallet which has an address and controlled using a public and private key.
2. The buyer can deposit money into his wallet or choose to transfer money to the vendor directly.
3. The system will withdraw money from the buyer's debit or credit card and provide him 1:1 stable cryptocurrency tokens which now can be transfered to the vendor's wallet.
4. At any time a user can cash-out his wallet balance to his bank account.

##### note that all transactions are stored in the blockchain in a peer to peer network and not on a centralized server.

### Technical details

To acheive scalability a sharding protocol was used where the network was divided into smaller commitees each commitee stores it's own part of the decentralized ledger and process a fraction of the incoming transactions.

cross-shard transactions are supported to verify transactions that should be validated by several comittees.

The server interacting with the blockchain to provide some services such as generating a wallet, send and receive transactions and provide the balance for a specific user.

The server providing these services to the end-user is not in control of the blockchain and is not a node in the network. It merely communicates with the blockchain to provide the user with several services to make it easier for user to interact with the blockchain.

The server is also highly scalable so as not to be the bottleneck in the system.

![image](https://user-images.githubusercontent.com/47888993/116227264-3d272880-a754-11eb-9efa-4856ebdd53a7.png)


### Authors
This is the work of [Omar Elbaroudy](https://github.com/OmarElbaroudy) and [Ahmed Medhat](https://github.com/ahmedhat1) for our bachelor thesis.
