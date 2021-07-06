// import * as React from 'react';
// import {useState} from 'react';
// import {StyleSheet, Text, TextInput, TouchableOpacity, View} from 'react-native';
// import {fetcher} from "../API/Fetcher";
// import Profile from "./Profile";
// import AsyncStorage from '@react-native-async-storage/async-storage';
//
// export default function ({navigation}) {
//     const [userName, setUserName] = useState('');
//     const [password, setPassword] = useState('');
//     const [message, setMessage] = useState('');
//     const storeData = async (property, value) => {
//         try {
//             await AsyncStorage.setItem(property, value);
//         } catch (e) {
//             console.log(e)
//         }
//     }
//     const onPress = async () => {
//         try {
//             const data = await fetcher.login(userName, password);
//             console.log(data.message);
//             if (data.message === "invalid") {
//                 setMessage("invalid username or password");
//             } else {
//                 await storeData("userId", data.userId);
//                 await storeData("userName", data.userName);
//                 navigation.navigate('Profile');
//             }
//         } catch (e) {
//             console.log(e);
//         }
//
//     };
//
//     return (
//         <View style={styles.Wrapper}>
//             <View style={styles.headerWrapper}>
//                 <Text style={styles.heading}> Login </Text>
//             </View>
//             <TextInput style={styles.textInput}
//                        underlineColorAndroid="transparent"
//                        placeholder="Enter User Name"
//                        placeholderTextColor='black'
//                        onChangeText={(value) => setUserName(value)}
//             />
//             <TextInput style={styles.textInput}
//                        underlineColorAndroid="transparent"
//                        placeholder="Enter Password"
//                        placeholderTextColor='black'
//                        autoCapitalize="none"
//                        keyboardType="default"
//                        secureTextEntry={true}
//                        onChangeText={(value) => setPassword(value)}
//             />
//
//             <TouchableOpacity onPress={onPress} style={styles.ButtonStyle}>
//                 <Text style={styles.TextStyle}> Login </Text>
//             </TouchableOpacity>
//             <Text style={styles.TextStyle2}>{message}</Text>
//         </View>
//
//
//     );
//
// }
//
//
// const styles = StyleSheet.create({
//
//     Wrapper: {
//         backgroundColor: '#9999FF',
//         padding: 90,
//     },
//     textInput: {
//         fontSize: 18,
//         alignSelf: 'stretch',
//         color: 'black',
//         marginBottom: 30,
//         borderBottomColor: 'grey',
//         borderBottomWidth: 2
//     },
//     headerWrapper: {
//         //borderBottomColor: 'red',
//         borderBottomWidth: 2,
//         marginBottom: 30,
//     },
//     heading: {
//         textAlign: 'center',
//         fontSize: 28
//     },
//     TextStyle: {
//         color: 'white',
//         fontWeight: 'bold',
//         textAlign: 'center',
//         fontSize: 28
//     },
//     TextStyle2: {
//         color: 'white',
//         flexDirection: "row",
//         textAlign: 'center',
//         fontWeight: 'bold',
//         fontSize: 12
//     },
//     ButtonStyle: {
//         padding: 10,
//         borderRadius: 5,
//         width: '100%',
//         backgroundColor: 'grey'
//     }
// });

import React, {Component} from 'react';
import {
    StyleSheet,
    Text,
    View,
    TextInput,
    TouchableOpacity,
    Image,
} from 'react-native';
import Spinner from 'react-bootstrap/Spinner';
import Modal from 'react-bootstrap/Modal';
import Button from 'react-bootstrap/Button';
import Alert from 'react-bootstrap/Alert';
import {useState} from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import Cards from 'react-credit-cards';
import 'react-credit-cards/es/styles-compiled.css';
import Form from "react-bootstrap/Form";
import {fetcher} from "../API/Fetcher";
import Profile from "./Profile";

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

            {/*<TouchableHighlight style={styles.buttonContainer} onPress={() => forgotPassword()}>*/}
            {/*    <Text>Forgot your password?</Text>*/}
            {/*</TouchableHighlight>*/}

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
            <Text style={styles.TextStyle}>{message}</Text>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#DCDCDC',
    },
    inputContainer: {
        borderBottomColor: '#F5FCFF',
        backgroundColor: '#FFFFFF',
        borderRadius: 30,
        borderBottomWidth: 1,
        width: 250,
        height: 45,
        marginBottom: 20,
        flexDirection: 'row',
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
        // width: 30,
        // height: 30,
        // marginLeft: 15,
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
        backgroundColor: "#00b5ec",
    },
    registerButton: {
        backgroundColor: "#00f50c",
    },
    loginText: {
        color: 'white',
        textAlign: 'center'
    }
});

