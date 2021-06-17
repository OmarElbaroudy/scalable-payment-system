import * as React from 'react';
import {useState} from 'react';
import {StyleSheet, Text, TextInput, TouchableOpacity, View} from 'react-native';
import {fetcher} from "../API/Fetcher";

export default function () {
    const [userName, setUserName] = useState('');
    const [password, setPassword] = useState('');
    const onPress = async () => {
        const data = await fetcher.login(userName, password);
        console.log(data.message);
    };

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
