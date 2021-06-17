import * as React from 'react';
import {useEffect, useState} from "react";
import {
    Text,
    View,
    StyleSheet,
    TextInput,
    TouchableOpacity
} from 'react-native';
import {fetches} from "../API/fetches";

export default function () {
    const [userName, setUserName] = useState('');
    const [password, setPassword] = useState('');
    const [pressed, setPressed] = useState(false);
    const onPress = async () => {
        const data = await fetches.login(userName, password);
    };
    useEffect(() => {
        if (pressed) {
            const requestOptions = {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({userName: userName, password: password})
            };
            fetch('http://localhost:5000/login', requestOptions)
                .then(response => response.json());
        }
    }, [userName, password, pressed]);

    return (
        <View style={styles.Wrapper}>
            <View style={styles.headerWrapper}>
                <Text style={styles.heading}> Login </Text>
            </View>
            <TextInput style={styles.textinput}
                       underlineColorAndroid="transparent"
                       placeholder="Enter User Name"
                       placeholderTextColor='black'
                       onChangeText={(value) => setUserName(value)}
            />

            <TextInput style={styles.textinput}
                       underlineColorAndroid="transparent"
                       placeholder="Enter Password"
                       placeholderTextColor='black'
                       autoCapitalize="none"
                       secureTextEntry="true"
                       onChangeText={(value) => setPassword(value)}
            />

            <TouchableOpacity onPress={onPress} style={styles.ButtonStyle}>
                <Text style={styles.TextStyle}> Login </Text>
            </TouchableOpacity>

        </View>


    );

}


const styles = StyleSheet.create({
    Wrapper: {
        backgroundColor: '#9999FF',
        padding: 80
    },
    textinput: {
        fontSize: 18,
        alignSelf: 'stretch',
        color: 'black',
        marginBottom: 30,
        borderBottomColor: 'grey',
        borderBottomWidth: 2
    },
    headerWrapper: {
        //borderBottomColor: 'red',
        borderBottomWidth: 2,
        marginBottom: 30,
    },
    heading: {
        textAlign: 'center',
        fontSize: 28
    },
    TextStyle: {
        color: 'white',
        fontWeight: 'bold',
        textAlign: 'center',
        fontSize: 28
    },
    ButtonStyle: {
        padding: 10,
        borderRadius: 5,
        width: '100%',
        backgroundColor: 'grey'
    }
});
