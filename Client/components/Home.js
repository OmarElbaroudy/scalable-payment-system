import React, {useEffect} from "react";
import Navbar from 'react-bootstrap/Navbar';
import Nav from 'react-bootstrap/Nav';
import {ImageBackground, StyleSheet, Text, TouchableOpacity} from "react-native";
import image from '../images/slide-01.jpg';
import Login from './Login';
import About from "./About";

export default function Home({navigation}) {
    useEffect(() => {
        //window.location.reload(false);
    });

    return (
        <>
            <Navbar bg="light" variant="light" style={{minWidth: 700}}>
                <Navbar.Brand href="#">Crypto Platform</Navbar.Brand>
                <Nav className="ml-auto">
                    <Nav.Link href="#">Home</Nav.Link>
                    <Nav.Link href="#" onSelect={() => navigation.push("About")}>About</Nav.Link>
                    <Nav.Link to="#">Documentation</Nav.Link>
                </Nav>
            </Navbar>
            <ImageBackground source={image} style={styles.image}>
                <Text style={styles.title}>INVESTING IN CRYPTOCURRENCY<br/> SEEMS TOO EASY</Text>
                <Text style={styles.text}>By enabling this easy and secure way to invest in cryptocurrency<br/> we hope
                    to have eliminated the boundaries that earlier prevented individuals and companies.</Text>
                <TouchableOpacity style={styles.buttonStyle} onPress={() => navigation.push("Login")}>Get Started
                </TouchableOpacity>
            </ImageBackground>
        </>
    )
}
const styles = StyleSheet.create({
    container: {
        flex: 1,
        flexDirection: "column"
    },
    title: {
        // flex:1,
        // flexWrap: 'wrap',
        // flexDirection: 'row',
        // flexShrink: 1,
        color: "#FFFFFF",
        fontSize: 40,
        fontWeight: 'bold',
        textAlign: 'center',
        paddingTop: 50,
        paddingBottom: 20
    },
    image: {
        flex: 1,
        resizeMode: "cover",
        justifyContent: "center"
    },
    text: {
        color: "white",
        fontSize: 20,
        textAlign: "center",
        paddingBottom: 20
    },
    buttonStyle: {
        backgroundColor: '#00FF00',
        color: '#000000',
        fontsize: 28,
        height: 50,
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        alignSelf: 'center',
        marginBottom: 120,
        width: 250,
        borderRadius: 30,
    }
});
