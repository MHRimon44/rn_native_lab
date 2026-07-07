import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import HomeScreen from '../screens/HomeScreen';
import NativeDebugScreen from '../screens/NativeDebugScreen';
import NativeDeviceScreen from '../screens/NativeDeviceScreen';
import type { RootStackParamList } from '../types/navigator';
import NativeSecureStorageScreen from '../screens/NativeSecureStorageScreen';

const Stack = createNativeStackNavigator<RootStackParamList>();

function AppNavigator(): React.JSX.Element {
  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName="Home"
        screenOptions={{
          headerTitleAlign: 'center',
        }}
      >
        <Stack.Screen
          name="Home"
          component={HomeScreen}
          options={{
            title: 'RN Native Lab',
          }}
        />

        <Stack.Screen
          name="NativeDebug"
          component={NativeDebugScreen}
          options={{
            title: 'Native Debug Lab',
          }}
        />

        <Stack.Screen
          name="NativeDevice"
          component={NativeDeviceScreen}
          options={{
            title: 'Native Device Module',
          }}
        />
        <Stack.Screen
          name="NativeSecureStorage"
          component={NativeSecureStorageScreen}
          options={{
            title: 'Native Secure Storage',
          }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

export default AppNavigator;
