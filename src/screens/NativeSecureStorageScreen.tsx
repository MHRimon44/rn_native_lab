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

  const handleSaveToken = async () => {
    try {
      const response = await NativeSecureStorageModule.saveToken(storageValue);

      setResult(
        `${response.message} | Key: ${response.key ?? '-'} | Source: ${
          response.source
        }`,
      );
    } catch (error) {
      showNativeError('Save Token Error', error);
    }
  };

  const handleGetToken = async () => {
    try {
      const token = await NativeSecureStorageModule.getToken();

      setResult(token === null ? 'Token not found' : `Token: ${token}`);
    } catch (error) {
      showNativeError('Get Token Error', error);
    }
  };

  const handleDeleteToken = async () => {
    try {
      const response = await NativeSecureStorageModule.deleteToken();

      setResult(
        `${response.message} | Key: ${response.key ?? '-'} | Source: ${
          response.source
        }`,
      );
    } catch (error) {
      showNativeError('Delete Token Error', error);
    }
  };

  const handleHasValue = async () => {
    try {
      const exists = await NativeSecureStorageModule.hasValue(storageKey);

      setResult(`Has value for "${storageKey}": ${exists ? 'Yes' : 'No'}`);
    } catch (error) {
      showNativeError('Has Value Error', error);
    }
  };

  const handleCheckBiometricAvailable = async () => {
    try {
      const available = await NativeSecureStorageModule.isBiometricAvailable();

      setResult(
        `Biometric/device authentication available: ${
          available ? 'Yes' : 'No'
        }`,
      );
    } catch (error) {
      showNativeError('Biometric Check Error', error);
    }
  };

  const handleGetTokenWithBiometric = async () => {
    try {
      const token = await NativeSecureStorageModule.getTokenWithBiometric();

      setResult(
        token === null
          ? 'Token not found after biometric authentication'
          : `Biometric Token: ${token}`,
      );
    } catch (error) {
      showNativeError('Biometric Token Error', error);
    }
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

        <View style={styles.buttonWrapper}>
          <Button title="Check Key Exists" onPress={handleHasValue} />
        </View>

        <View style={styles.buttonWrapper}>
          <Button title="Save Token" onPress={handleSaveToken} />
        </View>

        <View style={styles.buttonWrapper}>
          <Button title="Read Token" onPress={handleGetToken} />
        </View>

        <View style={styles.buttonWrapper}>
          <Button title="Delete Token" onPress={handleDeleteToken} />
        </View>

        <View style={styles.buttonWrapper}>
          <Button
            title="Check Biometric Available"
            onPress={handleCheckBiometricAvailable}
          />
        </View>

        <View style={styles.buttonWrapper}>
          <Button
            title="Read Token With Biometric"
            onPress={handleGetTokenWithBiometric}
          />
        </View>

        <Text style={styles.resultTitle}>Result:</Text>
        <Text style={styles.result}>{result}</Text>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Test Flow</Text>
          <Text>1. Save Secure Value</Text>
          <Text>2. Read Secure Value</Text>
          <Text>3. Check Key Exists</Text>
          <Text>4. Delete Secure Value</Text>
          <Text>5. Save Token</Text>
          <Text>6. Read Token</Text>
          <Text>7. Check Biometric Available</Text>
          <Text>8. Read Token With Biometric</Text>
          <Text>9. Delete Token</Text>
          <Text>10. Clear All Secure Values</Text>
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
