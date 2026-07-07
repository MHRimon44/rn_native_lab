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

import NativeSecureStorageModule from '../native/NativeSecureStorageModule';

function NativeSecureStorageScreen(): React.JSX.Element {
  const [storageKey, setStorageKey] = useState<string>('access_token');
  const [storageValue, setStorageValue] = useState<string>('SARA_TOKEN_123456');
  const [result, setResult] = useState<string>('No secure storage result yet');

  const handleSaveValue = async () => {
    try {
      const saved = await NativeSecureStorageModule.saveValue(
        storageKey,
        storageValue,
      );

      setResult(`Saved: ${saved ? 'Yes' : 'No'}`);
    } catch (error) {
      showNativeError('Save Error', error);
    }
  };

  const handleGetValue = async () => {
    try {
      const value = await NativeSecureStorageModule.getValue(storageKey);

      setResult(value === null ? 'Value not found' : `Value: ${value}`);
    } catch (error) {
      showNativeError('Read Error', error);
    }
  };

  const handleDeleteValue = async () => {
    try {
      const deleted = await NativeSecureStorageModule.deleteValue(storageKey);

      setResult(`Deleted: ${deleted ? 'Yes' : 'No'}`);
    } catch (error) {
      showNativeError('Delete Error', error);
    }
  };

  const handleClearAll = async () => {
    try {
      const cleared = await NativeSecureStorageModule.clearAll();

      setResult(`Storage cleared: ${cleared ? 'Yes' : 'No'}`);
    } catch (error) {
      showNativeError('Clear Error', error);
    }
  };

  const showNativeError = (title: string, error: unknown) => {
    const nativeError = error as {
      code?: string;
      message?: string;
    };

    console.log(`${title} raw:`, error);
    console.log(`${title} code:`, nativeError.code);
    console.log(`${title} message:`, nativeError.message);

    Alert.alert(
      title,
      `${nativeError.code ?? 'UNKNOWN'}: ${
        nativeError.message ?? 'Secure storage operation failed'
      }`,
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>Native Secure Storage</Text>

        <Text style={styles.description}>
          This screen saves and reads secure values using
          EncryptedSharedPreferences on Android and Keychain on iOS.
        </Text>

        <Text style={styles.label}>Key</Text>
        <TextInput
          style={styles.input}
          value={storageKey}
          onChangeText={setStorageKey}
          autoCapitalize="none"
          autoCorrect={false}
          placeholder="Enter storage key"
        />

        <Text style={styles.label}>Value</Text>
        <TextInput
          style={styles.input}
          value={storageValue}
          onChangeText={setStorageValue}
          autoCapitalize="none"
          autoCorrect={false}
          placeholder="Enter value"
        />

        <View style={styles.buttonWrapper}>
          <Button title="Save Secure Value" onPress={handleSaveValue} />
        </View>

        <View style={styles.buttonWrapper}>
          <Button title="Read Secure Value" onPress={handleGetValue} />
        </View>

        <View style={styles.buttonWrapper}>
          <Button title="Delete Secure Value" onPress={handleDeleteValue} />
        </View>

        <View style={styles.buttonWrapper}>
          <Button title="Clear All Secure Values" onPress={handleClearAll} />
        </View>

        <Text style={styles.resultTitle}>Result:</Text>
        <Text style={styles.result}>{result}</Text>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Test Flow</Text>
          <Text>1. Save Secure Value</Text>
          <Text>2. Read Secure Value</Text>
          <Text>3. Delete Secure Value</Text>
          <Text>4. Read again to confirm null</Text>
          <Text>5. Save multiple keys and Clear All</Text>
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

export default NativeSecureStorageScreen;
