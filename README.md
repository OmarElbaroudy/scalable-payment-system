# payment-system

### A scalable blockchain based payment system.

In this project we create a decentralized payment system that does not require a bank as an intermediary.

When a user buys a product from an e-commerce website, the system transfers money from the buyer to the vendor in a number of steps to ensure decentralization, transparency and security:

1. Any user must first create their own wallet which stores public-private key pairs that control cryptocurrency tokens.
2. A user can then buy 1:1 stable cryptocurrency using his debit card. Cryptocurrency ownership is added to the user's wallet.
3. The buyer could then use the balance in his wallet to buy products and transfer cryptocurrency to the vendors
4. At any time a user can cash-out his wallet balance to his bank account.

##### note that all transactions are stored in the blockchain in a peer to peer network and not on a centralized server.

### Technical details

To achieve scalability a sharding protocol was used where the network was divided into smaller committees each committee stores its own part of the decentralized ledger and process a fraction of the incoming transactions.

Cross-shard transactions are supported to verify transactions that should be validated by several committees.

A server interacting with the blockchain to provide some services such as generating a wallet, send and receive transactions and provide the balance for a specific user.

The server providing these services to the end-user is not in control of the blockchain and is not a node in the network. It merely communicates with the blockchain to provide the user with several services to make it easier for user to interact with the blockchain.

A signaling server works as a controller and organizer of the whole system to allow synchronization of mining phases and consistency within all nodes.

The signaling server is not a node in the blockchain network and merely signals the committees to ensure synchronization and epoch segmentation

All servers in the system send messages of their current tasks to a demo app which presents how the system is functioning.

The demo app also plots graphs and charts representing the load on each node and committee as well as give stats about each service




Message Brokers, Caching, Multithreading, Cryptography and decentralization are used to achieve scalability.
Nodes are loosely coupled and independent to achieve fault tolerance.
![image](https://user-images.githubusercontent.com/47888993/116227264-3d272880-a754-11eb-9efa-4856ebdd53a7.png)


### Authors
This is the work of [Omar Elbaroudy](https://github.com/OmarElbaroudy) and [Ahmed Medhat](https://github.com/ahmedhat1) for our bachelor thesis.
