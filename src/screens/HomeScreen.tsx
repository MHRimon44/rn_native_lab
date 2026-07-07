import React, { useCallback, useState } from 'react';
import {
  AppState,
  Button,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { useFocusEffect, useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { SafeAreaView } from 'react-native-safe-area-context';

import { RootStackParamList } from '../types/navigator';
import NativeNotificationModule from '../native/NativeNotificationModule';
import { NativeNotification } from '../types/nativeNotification';

type HomeNavigationProp = NativeStackNavigationProp<RootStackParamList, 'Home'>;

type MenuItem = {
  title: string;
  description: string;
  routeName: keyof RootStackParamList;
};

const menuItems: MenuItem[] = [
  {
    title: 'Native Debug Lab',
    description:
      'Practice Android Activity, Intent, Promise, Callback, EventEmitter, and native screen launching.',
    routeName: 'NativeDebug',
  },
  {
    title: 'Native Device Module',
    description:
      'Read app version, build number, device model, OS version, battery level, and charging status.',
    routeName: 'NativeDevice',
  },
  {
    title: 'Native Secure Storage',
    description:
      'Save, read, delete, and protect secure values using Android encrypted storage and iOS Keychain.',
    routeName: 'NativeSecureStorage',
  },
  {
    title: 'Native Notification Inbox',
    description:
      'Request permission, show local notifications, save inbox records, mark as read, and handle notification taps.',
    routeName: 'NativeNotification',
  },
];

function HomeScreen(): React.JSX.Element {
  const navigation = useNavigation<HomeNavigationProp>();

  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [latestNotification, setLatestNotification] =
    useState<NativeNotification | null>(null);
  const [notificationResult, setNotificationResult] = useState<string>(
    'Notification summary not loaded yet',
  );

  const loadNotificationSummary = useCallback(async () => {
    try {
      const [count, inbox] = await Promise.all([
        NativeNotificationModule.getUnreadCount(),
        NativeNotificationModule.getNotifications(),
      ]);

      setUnreadCount(count);
      setLatestNotification(inbox.length > 0 ? inbox[0] : null);

      setNotificationResult(
        inbox.length > 0
          ? `Loaded ${inbox.length} notifications`
          : 'No notifications saved yet',
      );
    } catch (error) {
      console.log('Home notification summary error:', error);
      setNotificationResult('Failed to load notification summary');
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      loadNotificationSummary();

      return undefined;
    }, [loadNotificationSummary]),
  );

  useFocusEffect(
    useCallback(() => {
      const subscription = AppState.addEventListener('change', state => {
        if (state === 'active') {
          loadNotificationSummary();
        }
      });

      return () => {
        subscription.remove();
      };
    }, [loadNotificationSummary]),
  );

  const handleOpenInbox = () => {
    navigation.navigate('NativeNotification');
  };

  const handleOpenLatestNotification = () => {
    if (!latestNotification) {
      navigation.navigate('NativeNotification');
      return;
    }

    navigation.navigate('NativeNotification', {
      initialNotificationId: latestNotification.id,
    });
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>RNNativeLab</Text>

        <Text style={styles.description}>
          React Native native integration lab for Kotlin, Swift, classic native
          modules, secure storage, notifications, and advanced native features.
        </Text>

        <View style={styles.notificationSummaryCard}>
          <View style={styles.notificationHeaderRow}>
            <Text style={styles.notificationCardTitle}>
              Notification Inbox Summary
            </Text>

            <View style={styles.badge}>
              <Text style={styles.badgeText}>{unreadCount}</Text>
            </View>
          </View>

          <Text style={styles.notificationMeta}>
            Unread Notifications: {unreadCount}
          </Text>

          {latestNotification ? (
            <View style={styles.latestNotificationBox}>
              <Text style={styles.latestTitle}>
                Latest: {latestNotification.title}
              </Text>

              <Text style={styles.latestMessage}>
                {latestNotification.message}
              </Text>

              <Text style={styles.latestMeta}>
                Status: {latestNotification.isRead ? 'Read' : 'Unread'}
              </Text>

              <Text style={styles.latestMeta}>
                Created: {latestNotification.createdAt}
              </Text>

              <View style={styles.buttonWrapper}>
                <Button
                  title="Open Latest Notification"
                  onPress={handleOpenLatestNotification}
                />
              </View>
            </View>
          ) : (
            <Text style={styles.noNotificationText}>
              No latest notification available.
            </Text>
          )}

          <View style={styles.buttonWrapper}>
            <Button title="Open Notification Inbox" onPress={handleOpenInbox} />
          </View>

          <View style={styles.buttonWrapper}>
            <Button
              title="Refresh Notification Summary"
              onPress={loadNotificationSummary}
            />
          </View>

          <Text style={styles.summaryResult}>{notificationResult}</Text>
        </View>

        <Text style={styles.sectionTitle}>Native Lessons</Text>

        {menuItems.map(item => (
          <TouchableOpacity
            key={item.routeName}
            style={styles.card}
            onPress={() => navigation.navigate(item.routeName as never)}
          >
            <Text style={styles.cardTitle}>{item.title}</Text>
            <Text style={styles.cardDescription}>{item.description}</Text>
          </TouchableOpacity>
        ))}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    padding: 24,
  },
  title: {
    fontSize: 32,
    fontWeight: '800',
    marginBottom: 12,
  },
  description: {
    fontSize: 16,
    lineHeight: 22,
    marginBottom: 24,
  },
  sectionTitle: {
    fontSize: 22,
    fontWeight: '800',
    marginTop: 24,
    marginBottom: 12,
  },
  notificationSummaryCard: {
    padding: 16,
    borderWidth: 1,
    borderRadius: 12,
    marginBottom: 20,
  },
  notificationHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  notificationCardTitle: {
    fontSize: 20,
    fontWeight: '800',
  },
  badge: {
    minWidth: 34,
    height: 34,
    borderRadius: 17,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 8,
  },
  badgeText: {
    fontSize: 16,
    fontWeight: '800',
  },
  notificationMeta: {
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 12,
  },
  latestNotificationBox: {
    padding: 12,
    borderWidth: 1,
    borderRadius: 8,
    marginBottom: 12,
  },
  latestTitle: {
    fontSize: 16,
    fontWeight: '800',
    marginBottom: 6,
  },
  latestMessage: {
    fontSize: 15,
    lineHeight: 21,
    marginBottom: 6,
  },
  latestMeta: {
    fontSize: 12,
    marginTop: 3,
  },
  noNotificationText: {
    fontSize: 15,
    marginBottom: 12,
  },
  buttonWrapper: {
    marginTop: 10,
  },
  summaryResult: {
    fontSize: 14,
    marginTop: 12,
  },
  card: {
    padding: 16,
    borderWidth: 1,
    borderRadius: 12,
    marginBottom: 14,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: '800',
    marginBottom: 6,
  },
  cardDescription: {
    fontSize: 15,
    lineHeight: 21,
  },
});

export default HomeScreen;
