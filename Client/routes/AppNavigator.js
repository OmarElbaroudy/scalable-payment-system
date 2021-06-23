// import React from 'react';
// import {NavigationContainer} from '@react-navigation/native'
// import {createStackNavigator} from '@react-navigation/stack';
// import {createAppContainer} from '@react-navigation/native';
// import Login from "../components/Loginform";
// import Profile from "../components/Profile";
//
// const {Navigator, Screen} = createStackNavigator();
//
// const HomeNavigator = () => (
//     <Navigator headerMode="none">
//         {/*//other options: "float", "screen"*/}
//         <Screen name="Login" component={Login}/>
//         <Screen name="Profile" component={Profile}/>
//     </Navigator>
// );
// export const AppNavigator = () => (
//     <NavigationContainer>
//         <HomeNavigator/>
//     </NavigationContainer>
// );
// // const Stack = createStackNavigator();
// // export default function AppNavigator() {
// //     return (
// //         <NavigationContainer initialRouteName='Login'>
// //             <Stack.AppNavigator>
// //                 <Stack.Screen name='Login' component={Login}/>
// //                 <Stack.Screen name='Profile' component={Profile}/>
// //
// //             </Stack.AppNavigator>
// //         </NavigationContainer>
// //     )
// // }
//
// // const screens = {
// //     Login: {
// //         screen: Login
// //     },
// //     Profile: {
// //         screen: Profile
// //     }
// // }
// // const HomeStack = createStackNavigator(screens);
// //
// // export default createAppContainer(HomeStack);
