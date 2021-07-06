import 'react-native-gesture-handler';
import React from 'react';
import Home from './components/Home';
import About from "./components/About";
import Login from './components/Login';
import Profile from "./components/Profile";
import {View} from "react-native";
import {NavigationContainer} from "@react-navigation/native";
import {createStackNavigator} from '@react-navigation/stack';


export default function App() {
    const Stack = createStackNavigator();
    return (
        <NavigationContainer>
            <Stack.Navigator>
                <></>
                <Stack.Screen
                    name=" "
                    component={Home}
                />
                <Stack.Screen
                    name="About"
                    component={About}
                />
                <Stack.Screen
                    name="Login"
                    component={Login}
                />
                <Stack.Screen
                    name="Profile"
                    component={Profile}
                />
            </Stack.Navigator>
        </NavigationContainer>
    );
}

