import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import { type NabtoClient, createNabtoClient, type Connection } from 'react-native-edge-client';

export default function App() {
  const [client, setClient] = React.useState<NabtoClient | undefined>();
  const [version, setVersion] = React.useState<String | undefined>();

  React.useEffect(() => {
    createNabtoClient().then(setClient);
  }, []);

  React.useEffect(() => {
    const func = async () => {
      if (!client) return;

      setVersion(client.version());

      const pk = client.createPrivateKey();
      const conn = await client.createConnection();
      conn.updateOptions({
        DeviceId: "de-9jiqspvw",
        ProductId: "pr-gocdozxt",
        PrivateKey: pk,
        ServerConnectToken: "MW6oipDcshn7"
      });

      await conn.connect();
    }

    func();
  }, [client]);

  return (
    <View style={styles.container}>
      <Text>Result: {version}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
