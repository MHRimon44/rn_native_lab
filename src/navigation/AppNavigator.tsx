import React, { useCallback, useEffect } from 'react';
import { AppState, NativeEventEmitter } from 'react-native';
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
import { NativeNotificationTap } from '../types/nativeNotification';

const Stack = createNativeStackNavigator<RootStackParamList>();

function AppNavigator(): React.JSX.Element {
  const navigationRef = useNavigationContainerRef<RootStackParamList>();
  const handleNotificationTap = useCallback(
    async (tap: NativeNotificationTap) => {
      try {
        if (!tap?.id) {
          return;
        }

        await NativeNotificationModule.markAsRead(tap.id);

        if (navigationRef.isReady()) {
          navigationRef.navigate('NativeNotification', {
            initialNotificationId: tap.id,
          });
        }

        await NativeNotificationModule.clearInitialNotification();
      } catch (error) {
        console.log('Notification tap event error:', error);
      }
    },
    [navigationRef],
  );

  const handlePendingNotificationTap = useCallback(async () => {
    try {
      const tap = await NativeNotificationModule.getInitialNotification();

      if (!tap) {
        return;
      }

      await handleNotificationTap(tap);
    } catch (error) {
      console.log('Handle initial notification tap error:', error);
    }
  }, [handleNotificationTap]);

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
  useEffect(() => {
    const emitter = new NativeEventEmitter(NativeNotificationModule as any);

    const subscription = emitter.addListener(
      'NativeNotificationTapped',
      async (tap: NativeNotificationTap) => {
        console.log('NativeNotificationTapped event:', tap);
        await handleNotificationTap(tap);
      },
    );

    return () => {
      subscription.remove();
    };
  }, [handleNotificationTap]);
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
