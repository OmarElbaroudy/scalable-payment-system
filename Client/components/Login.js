import React, {useState} from 'react';
import {Image, ImageBackground, StyleSheet, Text, TextInput, TouchableOpacity, View,} from 'react-native';
import Spinner from 'react-bootstrap/Spinner';
import Modal from 'react-bootstrap/Modal';
import Button from 'react-bootstrap/Button';
import Alert from 'react-bootstrap/Alert';
import AsyncStorage from '@react-native-async-storage/async-storage';
import Cards from 'react-credit-cards';
import 'react-credit-cards/es/styles-compiled.css';
import Form from "react-bootstrap/Form";
import {fetcher} from "../API/Fetcher";
import Profile from "./Profile";
import image from "../images/logImg.jpg";

export default function ({navigation}) {

    const [userName, setUserName] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const [loging, setLoging] = useState('Login');
    const [cvc, setCvc] = useState('');
    const [expiry, setExpiry] = useState('');
    const [name, setName] = useState('');
    const [number, setNumber] = useState('');
    const [spinner, setSpinner] = useState(false);

    const [showRegister, setShowRegister] = useState(false);
    const [showAlert, setShowAlert] = useState(false);
    const storeData = async (property, value) => {
        try {
            await AsyncStorage.setItem(property, value);
        } catch (e) {
            console.log(e)
        }
    }

    const showRegisterFunc = () => {
        setShowRegister(true);
    }
    const handleClose = () => {
        setShowRegister(false);
        setShowAlert(false);
    }

    const register = async () => {
        const data = await fetcher.register(userName, password);
        if (userName === "" || password === "") {
            setMessage("Please enter valid username and password!");
            return;
        }
        if (data.message === "username already exists") {
            setMessage("username already exists");
        } else {
            await storeData("userId", data.userId);
            await storeData("userName", data.userName);
            setShowAlert(true);
        }
    };

    const login = async () => {
        try {
            setSpinner(true);
            setLoging('');
            const data = await fetcher.login(userName, password);
            if (userName === "" || password === "") {
                setSpinner(false);
                setMessage("Please enter valid username and password!");
                setLoging('Login');
                return;
            }
            if (data.message === "invalid") {
                setSpinner(false);
                setLoging('Login');
                setMessage("invalid username or password");
            } else {
                await storeData("userId", data.userId);
                await storeData("userName", data.userName);
                navigation.push('Profile');
                setSpinner(false);
                setLoging('Login');
            }
        } catch (e) {
            console.log(e);
        }

    };

    return (
        <View style={styles.container}>
            <ImageBackground source={image} style={styles.image}>
                <View style={styles.inputContainer}>
                    <Image style={styles.inputIcon}/>
                    <TextInput style={styles.inputs}
                               placeholder="Username"
                               underlineColorAndroid='transparent'
                               onChangeText={(value) => setUserName(value)}/>
                </View>

                <View style={styles.inputContainer}>
                    <Image style={styles.inputIcon}/>
                    <TextInput style={styles.inputs}
                               placeholder="Password"
                               keyboardType="default"
                               secureTextEntry={true}
                               underlineColorAndroid='transparent'
                               onChangeText={(value) => setPassword(value)}/>
                </View>

                <TouchableOpacity style={[styles.buttonContainer, styles.loginButton]}
                                  onPress={() => login()}>
                    {spinner ? (<Spinner
                        as="span"
                        animation="border"
                        size="sm"
                        role="status"
                        aria-hidden="true"
                    />) : null}
                    <Text style={styles.loginText}>{loging}</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.buttonContainer, styles.registerButton]}
                                  onPress={() => showRegisterFunc()}>
                    <Text style={styles.loginText}>Register</Text>
                </TouchableOpacity>

                <Modal show={showRegister} onHide={handleClose}>
                    <Modal.Header closeButton>
                        <Modal.Title>REGISTRATION</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <div id="PaymentForm">
                            <Cards
                                cvc={cvc}
                                expiry={expiry}
                                name={name}
                                number={number}
                            />
                        </div>
                        <Form>
                            <Form.Group controlId="username">
                                <Form.Label>User Name</Form.Label>
                                <Form.Control type="username" placeholder="Enter username"
                                              onChange={e => setUserName(e.target.value)}/>
                            </Form.Group>

                            <Form.Group controlId="password">
                                <Form.Label>Password</Form.Label>
                                <Form.Control type="password" placeholder="Enter password"
                                              onChange={e => setPassword(e.target.value)}/>
                            </Form.Group>
                            <Form.Group controlId="cardNumber">
                                <Form.Label>Card Number</Form.Label>
                                <Form.Control type="tel" placeholder="Enter card number"
                                              onChange={e => setNumber(e.target.value)}/>
                            </Form.Group>
                            <Form.Group controlId="cvc">
                                <Form.Label>CVC</Form.Label>
                                <Form.Control type="text" maxLength="3" placeholder="Enter CVC"
                                              onChange={e => setCvc(e.target.value)}/>
                            </Form.Group>
                            <Form.Group controlId="expiry">
                                <Form.Label>Expiry</Form.Label>
                                <Form.Control type="text" placeholder="Enter expiry"
                                              onChange={e => setExpiry(e.target.value)}/>
                            </Form.Group>
                            <Form.Group controlId="cardName">
                                <Form.Label>Card Name</Form.Label>
                                <Form.Control type="text" placeholder="Enter card name"
                                              onChange={e => setName(e.target.value)}/>
                            </Form.Group>
                            <Alert show={showAlert} variant="success">
                                <Alert.Heading> Registered! </Alert.Heading>
                            </Alert>
                        </Form>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="primary" onClick={() => register()}>
                            Register
                        </Button>
                        <Button variant="secondary" onClick={handleClose}>
                            Close
                        </Button>
                    </Modal.Footer>
                    <Text style={styles.TextStyle}>{message}</Text>
                </Modal>
                <Text style={styles.TextStyle2}>{message}</Text>
            </ImageBackground>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    inputContainer: {
        borderBottomColor: '#F5FCFF',
        backgroundColor: '#FFFFFF',
        flexDirection: 'row',
        borderRadius: 30,
        borderBottomWidth: 1,
        width: '25%',
        height: 45,
        marginBottom: 20,
        marginLeft: '37.5%',
        alignItems: 'center'
    },
    inputs: {
        height: 45,
        marginLeft: 16,
        borderBottomColor: '#FFFFFF',
        flex: 1,
        textAlign: 'center'
    },
    inputIcon: {
        justifyContent: 'center'
    },
    TextStyle: {
        color: 'black',
        fontWeight: 'bold',
        textAlign: 'center',
        fontSize: 20
    },
    buttonContainer: {
        height: 45,
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        marginBottom: 20,
        width: 250,
        borderRadius: 30,
    },
    loginButton: {
        width: '25%',
        marginLeft: '37.5%',
        backgroundColor: "#00b5ec",
    },
    registerButton: {
        width: '25%',
        marginLeft: '37.5%',
        backgroundColor: "#00f50c",
    },
    loginText: {
        color: 'white',
        textAlign: 'center',
        fontWeight: 'bold',
        fontSize: 17
    },
    image: {
        height: '100%',
        width: '100%',
        resizeMode: "cover",
        justifyContent: "center"
    },
    TextStyle2: {
        color: 'white',
        fontWeight: 'bold',
        textAlign: 'center',
        fontSize: 20
    }
});

