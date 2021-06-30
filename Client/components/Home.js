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
    Button,
    TouchableOpacity,
    Image,
    Alert, ActivityIndicator
} from 'react-native';
import {useState} from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {fetcher} from "../API/Fetcher";
import Profile from "./Profile";

export default function ({navigation}) {

    const [userName, setUserName] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const storeData = async (property, value) => {
        try {
            await AsyncStorage.setItem(property, value);
        } catch (e) {
            console.log(e)
        }
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
            navigation.navigate('Profile');
        }
    };

    const login = async () => {
        try {
            setIsLoading(true);
            const data = await fetcher.login(userName, password);
            if (userName === "" || password === "") {
                setMessage("Please enter valid username and password!");
                setIsLoading(false);
                return;
            }
            if (data.message === "invalid") {
                setMessage("invalid username or password");
            } else {
                await storeData("userId", data.userId);
                await storeData("userName", data.userName);
                navigation.navigate('Profile');
            }
            setIsLoading(false);
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
                <Text style={styles.loginText}>Login</Text>
            </TouchableOpacity>
            <ActivityIndicator animating={isLoading}/>
            {/*<TouchableHighlight style={styles.buttonContainer} onPress={() => forgotPassword()}>*/}
            {/*    <Text>Forgot your password?</Text>*/}
            {/*</TouchableHighlight>*/}

            <TouchableOpacity style={styles.buttonContainer} onPress={() => register()}>
                <Text>Register</Text>
            </TouchableOpacity>
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
    },
    inputIcon: {
        width: 30,
        height: 30,
        marginLeft: 15,
        justifyContent: 'center'
    },
    TextStyle: {
        color: 'white',
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
    loginText: {
        color: 'white',
    }
});

