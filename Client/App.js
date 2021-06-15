import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import Regfrom from './components/Regform';
import Loginform from './components/Loginform';

export default function App() {
    return (
        <View style={styles.container}>
            <Loginform/>
            {/*<Text>Open up App.js to start working on your app!</Text>*/}
            {/*<StatusBar style="auto" />*/}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#171f2b',
        alignItems: 'center',
        justifyContent: 'center',
    },
});
