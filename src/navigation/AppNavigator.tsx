import React, { useCallback, useEffect } from 'react';
import { AppState } from 'react-native';
import {
  NavigationContainer,
  useNavigationContainerRef,
} from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import HomeScreen from '../screens/HomeScreen';
import NativeDebugScreen from '../screens/NativeDebugScreen';
import NativeDeviceScreen from '../screens/NativeDeviceScreen';
import NativeSecureStorageScreen from '../screens/NativeSecureStorageScreen';
import NativeNotificationScreen from '../screens/NativeNotificationScreen';
import NativeNotificationModule from '../native/NativeNotificationModule';
import { RootStackParamList } from '../types/navigator';

const Stack = createNativeStackNavigator<RootStackParamList>();

function AppNavigator(): React.JSX.Element {
  const navigationRef = useNavigationContainerRef<RootStackParamList>();

  const handlePendingNotificationTap = useCallback(async () => {
    try {
      if (!navigationRef.isReady()) {
        return;
      }

      const tap = await NativeNotificationModule.getInitialNotification();

      if (!tap) {
        return;
      }

      await NativeNotificationModule.markAsRead(tap.id);

      navigationRef.navigate('NativeNotification', {
        initialNotificationId: tap.id,
      });

      await NativeNotificationModule.clearInitialNotification();
    } catch (error) {
      console.log('Handle notification tap error:', error);
    }
  }, [navigationRef]);

  useEffect(() => {
    const subscription = AppState.addEventListener('change', state => {
      if (state === 'active') {
        setTimeout(() => {
          handlePendingNotificationTap();
        }, 300);
      }
    });

    return () => {
      subscription.remove();
    };
  }, [handlePendingNotificationTap]);

  return (
    <NavigationContainer
      ref={navigationRef}
      onReady={handlePendingNotificationTap}
    >
      <Stack.Navigator initialRouteName="Home">
        <Stack.Screen
          name="Home"
          component={HomeScreen}
          options={{
            title: 'RNNativeLab',
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

        <Stack.Screen
          name="NativeNotification"
          component={NativeNotificationScreen}
          options={{
            title: 'Native Notification Inbox',
          }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

export default AppNavigator;
