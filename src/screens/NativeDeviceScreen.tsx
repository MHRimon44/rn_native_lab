import React from 'react';
import {
  Alert,
  Button,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';

function NativeDeviceScreen(): React.JSX.Element {
  const handleStart = () => {
    Alert.alert(
      'Next Lesson',
      'Here we will build NativeDeviceModule with Kotlin.',
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>Native Device Module</Text>

        <Text style={styles.description}>
          This screen is for the next mini-project from your roadmap: app
          version, build number, device model, OS version, battery level, and
          charging status.
        </Text>

        <View style={styles.buttonWrapper}>
          <Button
            title="Start NativeDeviceModule Lesson"
            onPress={handleStart}
          />
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Planned Methods</Text>
          <Text>getAppVersion()</Text>
          <Text>getBuildNumber()</Text>
          <Text>getDeviceModel()</Text>
          <Text>getOSVersion()</Text>
          <Text>getBatteryLevel()</Text>
          <Text>isBatteryCharging()</Text>
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
  buttonWrapper: {
    marginBottom: 16,
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
