import React, { useState } from 'react';
import {
  Alert,
  Button,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import NativeDebugModule from './src/native/NativeDebugModule';
import { SafeAreaView } from 'react-native-safe-area-context';

function App(): React.JSX.Element {
  const [result, setResult] = useState<string>('No native result yet');

  const handleGreeting = async () => {
    try {
      const message = await NativeDebugModule.getNativeGreeting('SaRa');
      setResult(message);
    } catch (error) {
      console.log('Greeting error:', error);
      Alert.alert('Native Error', 'Failed to get greeting from Kotlin');
    }
  };

  const handleOpenNativeScreen = async () => {
    try {
      const opened = await NativeDebugModule.openDebugScreen(
        'ORD-3001',
        '1500.00',
      );

      setResult(`Native screen opened: ${opened}`);
    } catch (error) {
      console.log('Open native screen error:', error);
      Alert.alert('Native Error', 'Failed to open native screen');
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar />

      <ScrollView contentInsetAdjustmentBehavior="automatic">
        <View style={styles.content}>
          <Text style={styles.title}>RN Native Lab</Text>

          <Text style={styles.description}>
            React Native → Kotlin NativeModule → Android Activity
          </Text>

          <View style={styles.buttonWrapper}>
            <Button title="Get Greeting From Kotlin" onPress={handleGreeting} />
          </View>

          <View style={styles.buttonWrapper}>
            <Button
              title="Open Native Android Screen"
              onPress={handleOpenNativeScreen}
            />
          </View>

          <Text style={styles.resultTitle}>Result:</Text>
          <Text style={styles.result}>{result}</Text>
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
    fontWeight: '700',
    marginBottom: 12,
  },
  description: {
    fontSize: 16,
    marginBottom: 24,
  },
  buttonWrapper: {
    marginBottom: 16,
  },
  resultTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginTop: 24,
  },
  result: {
    fontSize: 16,
    marginTop: 8,
  },
});

export default App;
