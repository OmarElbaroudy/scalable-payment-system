import 'react-native-gesture-handler';
import React from 'react';
import Home from './components/Home';
import Profile from "./components/Profile";
import {NavigationContainer} from "@react-navigation/native";
import {createStackNavigator} from '@react-navigation/stack'

export default function App() {
    const Stack = createStackNavigator();
    return (
        <NavigationContainer>
            <Stack.Navigator>
                <Stack.Screen
                    name=" "
                    component={Home}
                />
                <Stack.Screen
                    name="Profile"
                    component={Profile}
                />
            </Stack.Navigator>
        </NavigationContainer>
    );
}

