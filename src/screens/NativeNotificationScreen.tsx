import React, { useState } from 'react';
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

import NativeNotificationModule from '../native/NativeNotificationModule';

function NativeNotificationScreen(): React.JSX.Element {
  const [title, setTitle] = useState<string>('SaRa Notification');
  const [message, setMessage] = useState<string>(
    'This notification was created from a native module.',
  );
  const [result, setResult] = useState<string>('No notification result yet');

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

      setResult(`Local notification shown: ${shown ? 'Yes' : 'No'}`);
    } catch (error) {
      showNativeError('Notification Error', error);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>Native Notification Module</Text>

        <Text style={styles.description}>
          This screen requests notification permission and shows a local
          notification using Android NotificationManager and iOS
          UNUserNotificationCenter.
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
            title="Show Local Notification"
            onPress={handleShowNotification}
          />
        </View>

        <Text style={styles.resultTitle}>Result:</Text>
        <Text style={styles.result}>{result}</Text>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Test Flow</Text>
          <Text>1. Request notification permission</Text>
          <Text>2. Allow permission</Text>
          <Text>3. Tap Show Local Notification</Text>
          <Text>4. Check notification tray</Text>
          <Text>5. Test while app is foreground/background</Text>
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
});

export default NativeNotificationScreen;
