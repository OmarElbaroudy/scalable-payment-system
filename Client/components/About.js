import * as React from 'react';
import {useState} from 'react';
import {Text, ImageBackground, View, StyleSheet} from 'react-native';
import img2 from '../images/transactioncycle.jpg';
import img1 from '../images/advantages.jpg';

export default function About() {
    return (
        <View>
            <ImageBackground
                style={{
                    flex: 1,
                    justifyContent: "center",
                    resizeMode: "center",
                    margin: 20,
                    width: 1300,
                    height: 690
                }}
                source={img2}
            />
            <ImageBackground
                style={{
                    marginTop: 675,
                    flex: 1,
                    justifyContent: "center",
                    resizeMode: "center",
                    margin: 20,
                    width: 1300,
                    height: 680
                }}
                source={img1}
            />
        </View>
    )
}
const styles = StyleSheet.create({
    image: {
        flex: 1,
        resizeMode: "cover",
        justifyContent: "center"
    }
})
