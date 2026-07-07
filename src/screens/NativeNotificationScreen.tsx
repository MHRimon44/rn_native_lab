import React, { useEffect, useState } from 'react';
import {
  Alert,
  Button,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { RouteProp, useRoute } from '@react-navigation/native';
import NativeNotificationModule from '../native/NativeNotificationModule';
import { NativeNotification } from '../types/nativeNotification';
import { RootStackParamList } from '../types/navigator';

function NativeNotificationScreen(): React.JSX.Element {
  const route = useRoute<RouteProp<RootStackParamList, 'NativeNotification'>>();
  const [title, setTitle] = useState<string>('SaRa Notification');
  const [message, setMessage] = useState<string>(
    'This notification was created from a native module.',
  );
  const [result, setResult] = useState<string>('No notification result yet');
  const [notifications, setNotifications] = useState<NativeNotification[]>([]);
  const [unreadCount, setUnreadCount] = useState<number>(0);

  const showNativeError = (screenTitle: string, error: unknown) => {
    const nativeError = error as {
      code?: string;
      message?: string;
    };

    console.log(`${screenTitle} raw:`, error);
    console.log(`${screenTitle} code:`, nativeError.code);
    console.log(`${screenTitle} message:`, nativeError.message);

    Alert.alert(
      screenTitle,
      `${nativeError.code ?? 'UNKNOWN'}: ${
        nativeError.message ?? 'Notification operation failed'
      }`,
    );
  };

  const loadNotifications = async () => {
    const [inbox, count] = await Promise.all([
      NativeNotificationModule.getNotifications(),
      NativeNotificationModule.getUnreadCount(),
    ]);

    setNotifications(inbox);
    setUnreadCount(count);

    return inbox;
  };

  useEffect(() => {
    const initialNotificationId = route.params?.initialNotificationId;

    if (!initialNotificationId) {
      loadNotifications().catch(error => {
        showNativeError('Load Inbox Error', error);
      });
      return;
    }

    const loadTappedNotification = async () => {
      try {
        const inbox = await loadNotifications();

        const tappedNotification = inbox.find(
          item => item.id === initialNotificationId,
        );

        setResult(
          tappedNotification
            ? `Opened from notification tap: ${tappedNotification.title}`
            : `Opened from notification tap. ID: ${initialNotificationId}`,
        );
      } catch (error) {
        showNativeError('Notification Tap Error', error);
      }
    };

    loadTappedNotification();
  }, [route.params?.initialNotificationId]);

  const handleRequestPermission = async () => {
    try {
      const granted =
        await NativeNotificationModule.requestNotificationPermission();

      setResult(`Notification permission granted: ${granted ? 'Yes' : 'No'}`);
    } catch (error) {
      showNativeError('Permission Error', error);
    }
  };

  const handleShowNotification = async () => {
    try {
      const shown = await NativeNotificationModule.showLocalNotification(
        title,
        message,
      );

      await loadNotifications();

      setResult(`Local notification shown and saved: ${shown ? 'Yes' : 'No'}`);
    } catch (error) {
      showNativeError('Notification Error', error);
    }
  };

  const handleSaveNotificationOnly = async () => {
    try {
      const notification: NativeNotification = {
        id: `${Date.now()}`,
        title,
        message,
        isRead: false,
        createdAt: new Date().toISOString(),
        source: 'local',
      };

      const saved = await NativeNotificationModule.saveNotification(
        notification,
      );

      await loadNotifications();

      setResult(`Notification saved only: ${saved ? 'Yes' : 'No'}`);
    } catch (error) {
      showNativeError('Save Notification Error', error);
    }
  };

  const handleLoadNotifications = async () => {
    try {
      const inbox = await loadNotifications();

      setResult(`Loaded ${inbox.length} notifications`);
    } catch (error) {
      showNativeError('Load Inbox Error', error);
    }
  };

  const handleMarkAsRead = async (id: string) => {
    try {
      const updated = await NativeNotificationModule.markAsRead(id);
      await NativeNotificationModule.syncBadgeCount();
      await loadNotifications();

      setResult(`Marked as read: ${updated ? 'Yes' : 'No'}`);
    } catch (error) {
      showNativeError('Mark Read Error', error);
    }
  };

  const handleClearNotifications = async () => {
    try {
      const cleared = await NativeNotificationModule.clearNotifications();
      await NativeNotificationModule.syncBadgeCount();
      await loadNotifications();

      setResult(`Notification inbox cleared: ${cleared ? 'Yes' : 'No'}`);
    } catch (error) {
      showNativeError('Clear Inbox Error', error);
    }
  };
  const handleDeleteNotification = async (id: string) => {
    try {
      const deleted = await NativeNotificationModule.deleteNotification(id);

      await NativeNotificationModule.syncBadgeCount();
      await loadNotifications();

      setResult(`Notification deleted: ${deleted ? 'Yes' : 'No'}`);
    } catch (error) {
      showNativeError('Delete Notification Error', error);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      const updated = await NativeNotificationModule.markAllAsRead();

      await NativeNotificationModule.syncBadgeCount();
      await loadNotifications();

      setResult(`All notifications marked as read: ${updated ? 'Yes' : 'No'}`);
    } catch (error) {
      showNativeError('Mark All Read Error', error);
    }
  };
  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>Native Notification Inbox</Text>

        <Text style={styles.description}>
          This screen requests notification permission, shows local
          notifications, and saves notification records into native SQLite.
        </Text>

        <Text style={styles.label}>Notification Title</Text>
        <TextInput
          style={styles.input}
          value={title}
          onChangeText={setTitle}
          placeholder="Enter title"
        />

        <Text style={styles.label}>Notification Message</Text>
        <TextInput
          style={[styles.input, styles.messageInput]}
          value={message}
          onChangeText={setMessage}
          placeholder="Enter message"
          multiline
        />

        <View style={styles.buttonWrapper}>
          <Button
            title="Request Notification Permission"
            onPress={handleRequestPermission}
          />
        </View>

        <View style={styles.buttonWrapper}>
          <Button
            title="Show + Save Local Notification"
            onPress={handleShowNotification}
          />
        </View>

        <View style={styles.buttonWrapper}>
          <Button
            title="Save Notification Only"
            onPress={handleSaveNotificationOnly}
          />
        </View>

        <View style={styles.buttonWrapper}>
          <Button
            title="Load Notification Inbox"
            onPress={handleLoadNotifications}
          />
        </View>

        <View style={styles.buttonWrapper}>
          <Button title="Mark All As Read" onPress={handleMarkAllAsRead} />
        </View>

        <View style={styles.buttonWrapper}>
          <Button
            title="Clear Notification Inbox"
            onPress={handleClearNotifications}
          />
        </View>

        <Text style={styles.resultTitle}>Result:</Text>
        <Text style={styles.result}>{result}</Text>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>
            Inbox ({notifications.length}) - Unread: {unreadCount}
          </Text>
          {notifications.length === 0 ? (
            <Text>No notifications saved yet.</Text>
          ) : (
            notifications.map(item => (
              <View key={item.id} style={styles.notificationItem}>
                <Text style={styles.notificationTitle}>
                  {item.isRead ? 'Read' : 'Unread'} - {item.title}
                </Text>
                <Text>{item.message}</Text>
                <Text style={styles.meta}>ID: {item.id}</Text>
                <Text style={styles.meta}>Source: {item.source}</Text>
                <Text style={styles.meta}>Created: {item.createdAt}</Text>

                {!item.isRead && (
                  <View style={styles.smallButton}>
                    <Button
                      title="Mark As Read"
                      onPress={() => handleMarkAsRead(item.id)}
                    />
                  </View>
                )}
                <View style={styles.smallButton}>
                  <Button
                    title="Delete"
                    onPress={() => handleDeleteNotification(item.id)}
                  />
                </View>
              </View>
            ))
          )}
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Test Flow</Text>
          <Text>1. Request notification permission</Text>
          <Text>2. Show + Save Local Notification</Text>
          <Text>3. Load Notification Inbox</Text>
          <Text>4. Mark notification as read</Text>
          <Text>5. Save Notification Only</Text>
          <Text>6. Clear Notification Inbox</Text>
        </View>
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
    fontSize: 28,
    fontWeight: '800',
    marginBottom: 12,
  },
  description: {
    fontSize: 16,
    lineHeight: 22,
    marginBottom: 24,
  },
  label: {
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 8,
  },
  input: {
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 16,
    marginBottom: 16,
  },
  messageInput: {
    minHeight: 90,
    textAlignVertical: 'top',
  },
  buttonWrapper: {
    marginBottom: 16,
  },
  smallButton: {
    marginTop: 10,
  },
  resultTitle: {
    fontSize: 18,
    fontWeight: '700',
    marginTop: 18,
  },
  result: {
    fontSize: 16,
    marginTop: 8,
  },
  card: {
    marginTop: 20,
    padding: 16,
    borderWidth: 1,
    borderRadius: 8,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: '700',
    marginBottom: 8,
  },
  notificationItem: {
    paddingVertical: 12,
    borderBottomWidth: 1,
  },
  notificationTitle: {
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 4,
  },
  meta: {
    fontSize: 12,
    marginTop: 4,
  },
});

export default NativeNotificationScreen;
