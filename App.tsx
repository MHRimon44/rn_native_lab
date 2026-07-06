/* eslint-disable @typescript-eslint/no-shadow */
import React, { useEffect, useState } from 'react';
import {
  Alert,
  Button,
  DeviceEventEmitter,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import NativeDebugModule, {
  NativeCouponResult,
  NativeDebugEventPayload,
  NativeOrderArraySummary,
  NativeOrderInput,
  NativeOrderItem,
  NativeOrderSummary,
  OrderSyncProgressPayload,
} from './src/native/NativeDebugModule';
import { SafeAreaView } from 'react-native-safe-area-context';

function App(): React.JSX.Element {
  const [result, setResult] = useState<string>('No native result yet');
  const [orderSummary, setOrderSummary] = useState<NativeOrderSummary | null>(
    null,
  );
  const [recentOrders, setRecentOrders] = useState<NativeOrderItem[]>([]);
  const [arraySummary, setArraySummary] =
    useState<NativeOrderArraySummary | null>(null);
  const [nativeEventMessage, setNativeEventMessage] = useState<string>(
    'No native event received yet',
  );
  const [syncProgress, setSyncProgress] = useState<number>(0);
  const [syncMessage, setSyncMessage] = useState<string>(
    'Order sync not started',
  );
  const [couponResult, setCouponResult] = useState<NativeCouponResult | null>(
    null,
  );

  // Function to handle getting greeting from native module
  const handleGreeting = async () => {
    try {
      const message = await NativeDebugModule.getNativeGreeting('SaRa');
      setResult(message);
    } catch (error) {
      console.log('Greeting error:', error);
      Alert.alert('Native Error', 'Failed to get greeting from Kotlin');
    }
  };

  // Function to handle opening native screen
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

  // Function to handle getting order summary from native module
  const handleGetOrderSummary = async () => {
    try {
      const summary = await NativeDebugModule.getOrderSummary(
        'ORD-4001',
        2500.0,
      );

      setOrderSummary(summary);
      setResult(`Order summary received from ${summary.source}`);
    } catch (error) {
      console.log('Order summary error:', error);
      Alert.alert('Native Error', 'Failed to get order summary from Kotlin');
    }
  };

  // Function to handle getting recent orders from native module
  const handleGetRecentOrders = async () => {
    try {
      const orders = await NativeDebugModule.getRecentOrders(4);

      setRecentOrders(orders);
      setResult(`Received ${orders.length} orders from Kotlin`);
    } catch (error) {
      console.log('Recent orders error:', error);
      Alert.alert('Native Error', 'Failed to get recent orders from Kotlin');
    }
  };

  // Function to handle creating order from map in native module
  const handleCreateOrderFromMap = async () => {
    try {
      const input: NativeOrderInput = {
        orderId: 'ORD-6001',
        customerName: 'Mehedi Hasan',
        amount: 3500,
        status: 'processing',
      };

      const response = await NativeDebugModule.createOrderFromMap(input);

      setOrderSummary(response);
      setResult(response.message);
    } catch (error) {
      console.log('Create order from map error:', error);
      Alert.alert('Native Error', 'Failed to send order object to Kotlin');
    }
  };

  // Function to handle summarizing orders from array in native module
  const handleSummarizeOrdersFromArray = async () => {
    try {
      const orders: NativeOrderInput[] = [
        {
          orderId: 'ORD-7001',
          customerName: 'Customer 1',
          amount: 1200,
          status: 'DELIVERED',
        },
        {
          orderId: 'ORD-7002',
          customerName: 'Customer 2',
          amount: 6500,
          status: 'PROCESSING',
        },
        {
          orderId: 'ORD-7003',
          customerName: 'Customer 3',
          amount: 900,
          status: 'PENDING',
        },
      ];

      const summary = await NativeDebugModule.summarizeOrdersFromArray(orders);

      setArraySummary(summary);
      setResult(summary.message);
    } catch (error) {
      console.log('Summarize orders error:', error);
      Alert.alert('Native Error', 'Failed to summarize order array in Kotlin');
    }
  };

  // useEffect to set up event listeners for native events
  useEffect(() => {
    const debugEventSubscription = DeviceEventEmitter.addListener(
      'NativeDebugEvent',
      (event: NativeDebugEventPayload) => {
        setNativeEventMessage(`${event.message} | Source: ${event.source}`);
      },
    );

    const syncProgressSubscription = DeviceEventEmitter.addListener(
      'OrderSyncProgress',
      (event: OrderSyncProgressPayload) => {
        setSyncProgress(event.progress);
        setSyncMessage(
          `${event.message} (${event.progress}%) for ${event.orderId}`,
        );
      },
    );

    return () => {
      debugEventSubscription.remove();
      syncProgressSubscription.remove();
    };
  }, []);

  // Function to handle emitting test event from native module
  const handleEmitTestEvent = async () => {
    try {
      const emitted = await NativeDebugModule.emitTestEvent(
        'Hello React Native, this event came from Kotlin',
      );

      setResult(`Event emitted: ${emitted}`);
    } catch (error) {
      console.log('Emit event error:', error);
      Alert.alert('Native Error', 'Failed to emit native event');
    }
  };

  // Function to handle starting fake order sync in native module
  const handleStartFakeOrderSync = async () => {
    try {
      setSyncProgress(0);
      setSyncMessage('Starting order sync...');

      const message = await NativeDebugModule.startFakeOrderSync('ORD-8001');

      setResult(message);
    } catch (error) {
      console.log('Start fake order sync error:', error);
      Alert.alert('Native Error', 'Failed to start fake order sync');
    }
  };

  // Function to handle validating coupon with callback in native module
  const handleValidateCouponWithCallback = () => {
    NativeDebugModule.validateCouponWithCallback(
      'SARA10',
      2000,
      result => {
        setCouponResult(result);
        setResult(result.message);
      },
      (code, message) => {
        console.log('Coupon callback error:', code, message);
        Alert.alert('Coupon Error', `${code}: ${message}`);
      },
    );
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
          {/*Greeting button*/}
          <View style={styles.buttonWrapper}>
            <Button title="Get Greeting From Kotlin" onPress={handleGreeting} />
          </View>
          {/*Open Native Screen button*/}
          <View style={styles.buttonWrapper}>
            <Button
              title="Open Native Android Screen"
              onPress={handleOpenNativeScreen}
            />
          </View>
          {/*Get Order Summary button*/}
          <View style={styles.buttonWrapper}>
            <Button
              title="Get Order Summary From Kotlin"
              onPress={handleGetOrderSummary}
            />
          </View>
          {/*Get Recent Orders button*/}
          <View style={styles.buttonWrapper}>
            <Button
              title="Get Recent Orders From Kotlin"
              onPress={handleGetRecentOrders}
            />
          </View>
          {/*Send Order Object To Kotlin button*/}
          <View style={styles.buttonWrapper}>
            <Button
              title="Send Order Object To Kotlin"
              onPress={handleCreateOrderFromMap}
            />
          </View>
          {/*Send Order Array To Kotlin button*/}
          <View style={styles.buttonWrapper}>
            <Button
              title="Send Order Array To Kotlin"
              onPress={handleSummarizeOrdersFromArray}
            />
          </View>
          {/*Emit Event From Kotlin button*/}
          <View style={styles.buttonWrapper}>
            <Button
              title="Emit Event From Kotlin"
              onPress={handleEmitTestEvent}
            />
          </View>
          {/*Start Kotlin Order Sync Events button*/}
          <View style={styles.buttonWrapper}>
            <Button
              title="Start Kotlin Order Sync Events"
              onPress={handleStartFakeOrderSync}
            />
          </View>
          {/*Validate Coupon With Kotlin Callback button*/}
          <View style={styles.buttonWrapper}>
            <Button
              title="Validate Coupon With Kotlin Callback"
              onPress={handleValidateCouponWithCallback}
            />
          </View>
          {/*Result*/}
          <Text style={styles.resultTitle}>Result:</Text>
          <Text style={styles.result}>{result}</Text>
          {/*Kotlin Event Listener*/}
          {nativeEventMessage && (
            <View style={styles.card}>
              <Text style={styles.cardTitle}>Kotlin Event Listener</Text>
              <Text>{nativeEventMessage}</Text>
            </View>
          )}
          {/*Kotlin Order Sync Progress*/}
          {syncProgress !== null && (
            <View style={styles.card}>
              <Text style={styles.cardTitle}>Order Sync Progress</Text>
              <Text>Progress: {syncProgress}%</Text>
              <Text>{syncMessage}</Text>
            </View>
          )}

          {/*Display Order Summary*/}
          {orderSummary && (
            <View style={styles.card}>
              <Text style={styles.cardTitle}>Native Order Summary</Text>
              <Text>Order ID: {orderSummary.orderId}</Text>
              <Text>Customer: {orderSummary.customerName}</Text>
              <Text>Amount: {orderSummary.amount}</Text>
              <Text>Status: {orderSummary.status}</Text>
              <Text>High Value: {orderSummary.isHighValue ? 'Yes' : 'No'}</Text>
              <Text>Source: {orderSummary.source}</Text>
            </View>
          )}
          {/*Display Recent Orders*/}
          {recentOrders.length > 0 && (
            <View style={styles.card}>
              <Text style={styles.cardTitle}>Recent Orders From Kotlin</Text>

              {recentOrders.map(order => (
                <View key={order.orderId} style={styles.orderRow}>
                  <Text>Order ID: {order.orderId}</Text>
                  <Text>Customer: {order.customerName}</Text>
                  <Text>Amount: {order.amount}</Text>
                  <Text>Status: {order.status}</Text>
                  <Text>High Value: {order.isHighValue ? 'Yes' : 'No'}</Text>
                  <Text>Source: {order.source}</Text>
                </View>
              ))}
            </View>
          )}
          {/*Display Order Array Summary*/}
          {arraySummary && (
            <View style={styles.card}>
              <Text style={styles.cardTitle}>
                Order Array Summary From Kotlin
              </Text>
              <Text>Total Orders: {arraySummary.totalOrders}</Text>
              <Text>Total Amount: {arraySummary.totalAmount}</Text>
              <Text>High Value Orders: {arraySummary.highValueOrders}</Text>
              <Text>Delivered Orders: {arraySummary.deliveredOrders}</Text>
              <Text>Source: {arraySummary.source}</Text>
            </View>
          )}
          {/*Display Coupon Result*/}
          {couponResult && (
            <View style={styles.card}>
              <Text style={styles.cardTitle}>
                Coupon Result From Kotlin Callback
              </Text>
              <Text>Coupon Code: {couponResult.couponCode}</Text>
              <Text>Amount: {couponResult.amount}</Text>
              <Text>Discount Percent: {couponResult.discountPercent}%</Text>
              <Text>Discount Amount: {couponResult.discountAmount}</Text>
              <Text>Final Amount: {couponResult.finalAmount}</Text>
              <Text>Source: {couponResult.source}</Text>
            </View>
          )}
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
  orderRow: {
    paddingVertical: 12,
    borderBottomWidth: 1,
  },
});

export default App;
