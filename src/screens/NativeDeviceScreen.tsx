import React, { useState } from 'react';
import {
  Alert,
  Button,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import NativeDeviceModule from '../native/NativeDeviceModule';
import { NativeDeviceSummary } from '../types/nativeDevice';
import { SafeAreaView } from 'react-native-safe-area-context';

function NativeDeviceScreen(): React.JSX.Element {
  const [result, setResult] = useState<string>('No device result yet');
  const [deviceSummary, setDeviceSummary] =
    useState<NativeDeviceSummary | null>(null);

  const handleGetDeviceSummary = async () => {
    try {
      const summary = await NativeDeviceModule.getDeviceSummary();

      setDeviceSummary(summary);
      setResult('Device summary loaded from Kotlin');
    } catch (error) {
      const nativeError = error as {
        code?: string;
        message?: string;
      };

      Alert.alert(
        'Device Error',
        `${nativeError.code ?? 'UNKNOWN'}: ${
          nativeError.message ?? 'Failed to get device summary'
        }`,
      );
    }
  };

  const handleGetAppVersion = async () => {
    try {
      const appVersion = await NativeDeviceModule.getAppVersion();
      setResult(`App Version: ${appVersion}`);
    } catch (error) {
      Alert.alert('Device Error', `Failed to get app version, ${error}`);
    }
  };

  const handleGetBuildNumber = async () => {
    try {
      const buildNumber = await NativeDeviceModule.getBuildNumber();
      setResult(`Build Number: ${buildNumber}`);
    } catch (error) {
      Alert.alert('Device Error', `Failed to get build number, ${error}`);
    }
  };

  const handleGetDeviceModel = async () => {
    try {
      const deviceModel = await NativeDeviceModule.getDeviceModel();
      setResult(`Device Model: ${deviceModel}`);
    } catch (error) {
      Alert.alert('Device Error', `Failed to get device model, ${error}`);
    }
  };

  const handleGetOSVersion = async () => {
    try {
      const osVersion = await NativeDeviceModule.getOSVersion();
      setResult(`OS Version: ${osVersion}`);
    } catch (error) {
      Alert.alert('Device Error', `Failed to get OS version, ${error}`);
    }
  };

  const handleGetBatteryLevel = async () => {
    try {
      const batteryLevel = await NativeDeviceModule.getBatteryLevel();
      setResult(`Battery Level: ${batteryLevel.toFixed(2)}%`);
    } catch (error) {
      Alert.alert('Device Error', `Failed to get battery level, ${error}`);
    }
  };

  const handleIsBatteryCharging = async () => {
    try {
      const isCharging = await NativeDeviceModule.isBatteryCharging();
      setResult(`Battery Charging: ${isCharging ? 'Yes' : 'No'}`);
    } catch (error) {
      Alert.alert(
        'Device Error',
        `Failed to get battery charging status, ${error}`,
      );
    }
  };

  const handleGetSyncDeviceInfo = async () => {
    try {
      const hasSyncMethods =
        typeof NativeDeviceModule.getPlatformNameSync === 'function' &&
        typeof NativeDeviceModule.getAppVersionSync === 'function' &&
        typeof NativeDeviceModule.getBuildNumberSync === 'function';

      if (hasSyncMethods) {
        const platformName = NativeDeviceModule.getPlatformNameSync!();
        const appVersion = NativeDeviceModule.getAppVersionSync!();
        const buildNumber = NativeDeviceModule.getBuildNumberSync!();

        setResult(
          `Sync Result → Platform: ${platformName}, App Version: ${appVersion}, Build: ${buildNumber}`,
        );

        return;
      }

      const appVersion = await NativeDeviceModule.getAppVersion();
      const buildNumber = await NativeDeviceModule.getBuildNumber();

      setResult(
        `Async fallback → Platform: ${Platform.OS}, App Version: ${appVersion}, Build: ${buildNumber}`,
      );
    } catch (error) {
      const nativeError = error as {
        code?: string;
        message?: string;
      };

      Alert.alert(
        'Device Error',
        `${nativeError.code ?? 'UNKNOWN'}: ${
          nativeError.message ?? 'Failed to get device info'
        }`,
      );
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>Native Device Module</Text>

        <Text style={styles.description}>
          This screen reads app, device, OS, and battery information from Kotlin
          on Android and Swift on iOS using a separate classic native module.
        </Text>
        <View style={styles.buttonWrapper}>
          <Button
            title="Get Fast Device Info"
            onPress={handleGetSyncDeviceInfo}
          />
        </View>
        <View style={styles.buttonWrapper}>
          <Button
            title="Get Device Summary From Native"
            onPress={handleGetDeviceSummary}
          />
        </View>

        <View style={styles.buttonWrapper}>
          <Button title="Get App Version" onPress={handleGetAppVersion} />
        </View>

        <View style={styles.buttonWrapper}>
          <Button title="Get Build Number" onPress={handleGetBuildNumber} />
        </View>

        <View style={styles.buttonWrapper}>
          <Button title="Get Device Model" onPress={handleGetDeviceModel} />
        </View>

        <View style={styles.buttonWrapper}>
          <Button title="Get OS Version" onPress={handleGetOSVersion} />
        </View>

        <View style={styles.buttonWrapper}>
          <Button title="Get Battery Level" onPress={handleGetBatteryLevel} />
        </View>

        <View style={styles.buttonWrapper}>
          <Button
            title="Check Battery Charging"
            onPress={handleIsBatteryCharging}
          />
        </View>

        <Text style={styles.resultTitle}>Result:</Text>
        <Text style={styles.result}>{result}</Text>

        {deviceSummary && (
          <View style={styles.card}>
            <Text style={styles.cardTitle}>Native Device Summary</Text>
            <Text>App Version: {deviceSummary.appVersion}</Text>
            <Text>Build Number: {deviceSummary.buildNumber}</Text>
            <Text>Device Model: {deviceSummary.deviceModel}</Text>
            <Text>OS Version: {deviceSummary.osVersion}</Text>
            <Text>Battery Level: {deviceSummary.batteryLevel.toFixed(2)}%</Text>
            <Text>
              Charging: {deviceSummary.isBatteryCharging ? 'Yes' : 'No'}
            </Text>
            <Text>Source: {deviceSummary.source}</Text>
          </View>
        )}
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

export default NativeDeviceScreen;
