import React from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';

import type { RootStackParamList } from '../types/navigator';
import { SafeAreaView } from 'react-native-safe-area-context';

type Props = NativeStackScreenProps<RootStackParamList, 'Home'>;

type MenuItem = {
  title: string;
  description: string;
  routeName: keyof RootStackParamList;
};

const menuItems: MenuItem[] = [
  {
    title: 'Native Debug Lab',
    description:
      'Previous lessons: NativeModule, WritableMap, ReadableMap, events, callback, permission, camera.',
    routeName: 'NativeDebug',
  },
  {
    title: 'Native Device Module',
    description:
      'Next mini-project: app version, build number, device model, OS version, battery status.',
    routeName: 'NativeDevice',
  },
];

function HomeScreen({ navigation }: Props): React.JSX.Element {
  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>RN Native Lab</Text>

        <Text style={styles.subtitle}>
          Select a lesson screen to continue your Kotlin + React Native native
          module practice.
        </Text>

        {menuItems.map(item => (
          <Pressable
            key={item.routeName}
            style={styles.card}
            onPress={() => navigation.navigate(item.routeName)}
          >
            <View style={styles.cardHeader}>
              <Text style={styles.cardTitle}>{item.title}</Text>
              <Text style={styles.arrow}>›</Text>
            </View>

            <Text style={styles.cardDescription}>{item.description}</Text>
          </Pressable>
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
    fontSize: 30,
    fontWeight: '800',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    lineHeight: 22,
    marginBottom: 24,
  },
  card: {
    padding: 18,
    borderWidth: 1,
    borderRadius: 12,
    marginBottom: 16,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  cardTitle: {
    fontSize: 20,
    fontWeight: '700',
  },
  arrow: {
    fontSize: 30,
    fontWeight: '300',
  },
  cardDescription: {
    fontSize: 15,
    lineHeight: 21,
    marginTop: 8,
  },
});

export default HomeScreen;
