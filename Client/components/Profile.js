import React, {Component, useState, useEffect} from 'react';
import {
    StyleSheet,
    Text,
    View,
    Image,
    TouchableOpacity
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import Button from 'react-bootstrap/Button';
import Modal from 'react-bootstrap/Modal';
import Form from 'react-bootstrap/Form';
import Alert from 'react-bootstrap/Alert';
import 'bootstrap/dist/css/bootstrap.min.css';
import {fetcher} from "../API/Fetcher";

export default function Profile() {
    const [showBalance, setShowBalance] = useState(false);
    const [showTransfer, setShowTransfer] = useState(false);
    const [showBuy, setShowBuy] = useState(false);
    const [showSell, setShowSell] = useState(false);
    const [showAlert, setShowAlert] = useState(false);

    const [userName, setUserName] = useState('');
    const [balance, setBalance] = useState('0');
    const [transferReceiver, setTransferReceiver] = useState('');
    const [transferAmount, setTransferAmount] = useState('');
    const [transferStatus, setTransferStatus] = useState('');
    const [variant, setVariant] = useState('');
    const [buyAmount, setBuyAmount] = useState('');
    const [sellAmount, setSellAmount] = useState('');


    const handleClose1 = () => setShowBalance(false);
    const handleShow1 = () => setShowBalance(true);
    const handleClose2 = () => {
        setShowTransfer(false);
        setShowAlert(false);
    }
    const handleShow2 = () => setShowTransfer(true);
    const handleClose3 = () => {
        setShowBuy(false);
        setShowAlert(false);
    }
    const handleShow3 = () => setShowBuy(true);
    const handleClose4 = () => {
        setShowSell(false);
        setShowAlert(false);
    }
    const handleShow4 = () => setShowSell(true);


    useEffect(() => {
        (async () => {
            try {
                const userName = await AsyncStorage.getItem('userName');
                setUserName(userName);
            } catch (e) {
                console.log(e);
            }
        })()
    }, [])
    const getUserId = async () => {
        try {
            return await AsyncStorage.getItem('userId');
        } catch (e) {
            console.log(e);
        }
    }

    const handleBalance = async () => {
        handleShow1();
        try {
            const userId = await getUserId();
            const data = await fetcher.balance(userId);
            console.log(data.amount);
            setBalance(data.amount);
        } catch (e) {
            console.log(e);
        }
    }
    const handleShowTransfer = () => {
        handleShow2();
    }
    const handleShowBuy = () => {
        handleShow3();
    }
    const handleShowSell = () => {
        handleShow4();
    }
    const handleTransferTransaction = async (receiver, amount) => {
        try {
            const userId = await getUserId();
            const data = await fetcher.transfer(userId, receiver, amount);
            if (data.message === "transaction created!") {
                setVariant("success");
                setTransferStatus("Transfer in process!");
                setShowAlert(true);
            }
        } catch (e) {
            setVariant("danger");
            setTransferStatus("Transfer Failed! Make sure this username exists");
            setShowAlert(true);
            console.log(e);
        }
    }

    const handleBuy = async () => {
        try {
            setShowAlert(false);
            const userId = await getUserId();
            const data = await fetcher.buy(userId, buyAmount);
            if (data.message === "transaction created!") {
                setShowAlert(true);
            }
        } catch (e) {
            console.log(e);
        }
    }
    const handleSell = async () => {
        try {
            setShowAlert(false);
            const userId = await getUserId();
            const data = await fetcher.sell(userId, sellAmount);
            console.log(data.message);
            if (data.message === "transaction created!") {
                setShowAlert(true);
            }
        } catch (e) {
            console.log(e);
        }
    }

    return (
        <View style={styles.container}>
            <View style={styles.header}></View>
            <Image style={styles.avatar} source={{uri: 'https://bootdey.com/img/Content/avatar/avatar6.png'}}/>
            <View style={styles.body}>
                <View style={styles.bodyContent}>
                    <Text style={styles.name}>{userName}</Text>
                    <Text style={styles.info}>some info about the user</Text>
                    <Text style={styles.description}>we could use here some notes,advices or any quote...</Text>

                    <TouchableOpacity variant="primary" onPress={() => handleBalance()} style={styles.buttonContainer}>
                        <Text>Show Balance</Text>
                    </TouchableOpacity>
                    <Modal show={showBalance} onHide={handleClose1}>
                        <Modal.Header closeButton>
                            <Modal.Title>Balance</Modal.Title>
                        </Modal.Header>
                        <Modal.Body>your balance is {balance}</Modal.Body>
                        <Modal.Footer>
                            <Button variant="secondary" onClick={handleClose1}>
                                Close
                            </Button>
                        </Modal.Footer>
                    </Modal>
                    <TouchableOpacity onPress={() => handleShowTransfer()} style={styles.buttonContainer}>
                        <Text>Transfer Crypto</Text>
                    </TouchableOpacity>
                    <Modal show={showTransfer} onHide={handleClose2}>
                        <Modal.Header closeButton>
                            <Modal.Title>Transfer Money</Modal.Title>
                        </Modal.Header>
                        <Modal.Body><Form>
                            <Form.Group controlId="formReceiverUserName">
                                <Form.Label>Receiver User Name</Form.Label>
                                <Form.Control type="username" placeholder="Enter username"
                                              onChange={e => setTransferReceiver(e.target.value)}/>
                            </Form.Group>

                            <Form.Group controlId="formBasicAmount">
                                <Form.Label>Amount</Form.Label>
                                <Form.Control type="amount" placeholder="Enter amount"
                                              onChange={e => setTransferAmount(e.target.value)}/>
                            </Form.Group>
                            <Button onClick={() => handleTransferTransaction(transferReceiver, transferAmount)}
                                    variant="primary">
                                Submit
                            </Button>
                            <Alert show={showAlert} variant={variant}>
                                <Alert.Heading> {transferStatus} </Alert.Heading>
                            </Alert>
                        </Form></Modal.Body>
                        <Modal.Footer>
                            <Button variant="secondary" onClick={handleClose2}>
                                Close
                            </Button>
                        </Modal.Footer>
                    </Modal>
                    <TouchableOpacity onPress={() => handleShowBuy()} style={styles.buttonContainer}>
                        <Text>Buy Crypto</Text>
                    </TouchableOpacity>
                    <Modal show={showBuy} onHide={handleClose3}>
                        <Modal.Header closeButton>
                            <Modal.Title>Buy</Modal.Title>
                        </Modal.Header>
                        <Modal.Body><Form>
                            <Form.Group controlId="formBuyAmount">
                                <Form.Label>Amount</Form.Label>
                                <Form.Control type="amount" onChange={e => setBuyAmount(e.target.value)}
                                              placeholder="Enter amount"/>
                            </Form.Group>
                            <Button onClick={() => handleBuy()} variant="primary">
                                Buy Now
                            </Button>
                            <Alert show={showAlert} variant="success">
                                <Alert.Heading> Transaction done! </Alert.Heading>
                            </Alert>
                        </Form></Modal.Body>
                        <Modal.Footer>
                            <Button variant="secondary" onClick={handleClose3}>
                                Close
                            </Button>
                        </Modal.Footer>
                    </Modal>
                    <TouchableOpacity onPress={() => handleShowSell()} style={styles.buttonContainer}>
                        <Text>Sell Crypto</Text>
                    </TouchableOpacity>
                    <Modal show={showSell} onHide={handleClose3}>
                        <Modal.Header closeButton>
                            <Modal.Title>Sell</Modal.Title>
                        </Modal.Header>
                        <Modal.Body><Form>
                            <Form.Group controlId="formSellAmount">
                                <Form.Label>Amount</Form.Label>
                                <Form.Control type="amount" onChange={e => setSellAmount(e.target.value)}
                                              placeholder="Enter amount"/>
                            </Form.Group>
                            <Button onClick={() => handleSell()} variant="primary">
                                Sell Now
                            </Button>
                            <Alert show={showAlert} variant="success">
                                <Alert.Heading> Sold! </Alert.Heading>
                            </Alert>
                        </Form></Modal.Body>
                        <Modal.Footer>
                            <Button variant="secondary" onClick={handleClose4}>
                                Close
                            </Button>
                        </Modal.Footer>
                    </Modal>
                </View>
            </View>
        </View>
    );

}

const styles = StyleSheet.create({
    header: {
        backgroundColor: "#00BFFF",
        height: 200,
    },
    avatar: {
        width: 130,
        height: 130,
        borderRadius: 63,
        borderWidth: 4,
        borderColor: "white",
        marginBottom: 10,
        alignSelf: 'center',
        position: 'absolute',
        marginTop: 130
    },
    name: {
        fontSize: 26,
        color: "#23135c",
        fontWeight: '600',
    },
    body: {
        marginTop: 40,
    },
    bodyContent: {
        flex: 1,
        alignItems: 'center',
        padding: 30,
    },
    info: {
        fontSize: 16,
        color: "#00BFFF",
        marginTop: 10
    },
    description: {
        fontSize: 16,
        color: "#696969",
        marginTop: 10,
        textAlign: 'center'
    },
    buttonContainer: {
        marginTop: 10,
        height: 45,
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        marginBottom: 20,
        width: 250,
        borderRadius: 30,
        backgroundColor: "#00BFFF",
    },
});
