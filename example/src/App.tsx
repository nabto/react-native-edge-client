import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import { type NabtoClient, createNabtoClient, type Stream, type Connection } from 'react-native-edge-client';

export default function App() {
  const [client, setClient] = React.useState<NabtoClient | undefined>();
  const [conn, setConn] = React.useState<Connection | undefined>();
  const [privateKey, setPrivateKey] = React.useState<string | undefined>();
  const [stream, setStream] = React.useState<Stream | undefined>();
  const [version, setVersion] = React.useState<String | undefined>();

  React.useEffect(() => {
    createNabtoClient().then(setClient);
  }, []);

  React.useEffect(() => {
    const func = async () => {
      if (!client) return;
      await client.setLogLevel("trace");

      setVersion(await client.version());

      const pk = await client.createPrivateKey();
      setPrivateKey(pk);
      setConn(await client.createConnection());
    }

    func();
  }, [client]);

  React.useEffect(() => {
    const func = async () => {
      if (conn) {
        conn.updateOptions({
          DeviceId: "de-bgdqxtqs",
          ProductId: "pr-4fiowoh4",
          PrivateKey: privateKey,
          ServerConnectToken: "MW6oipDcshn7"
        });

        conn.addConnectionEventsListener(event => {
          console.log(`Connection event: ${event}`);
        });

        await conn.connect();
        await conn.passwordAuthenticate("reactnative", "6f617198-b161-42");
        
        console.log(`Device Fingerprint: ${await conn.getDeviceFingerprint()}`);
        console.log(`Client Fingerprint: ${await conn.getClientFingerprint()}`);
        console.log(`Type: ${await conn.getType()}`);
        console.log(`Options: ${JSON.stringify(await conn.getOptions())}`);

        const coap = await conn.createCoap("GET", "/webrtc/info");
        const coapResult = await coap.execute();
        console.log(`Coap result: ${JSON.stringify(coapResult)}`);
        const decodedPayload = Array.from(coapResult.responsePayload).map(v => String.fromCharCode(v)).join("");
        console.log(`Coap Payload: ${decodedPayload}`);

        const { SignalingStreamPort } = JSON.parse(decodedPayload) as {SignalingStreamPort: number};
        console.log(SignalingStreamPort)
        const stream = await conn.createStream();
        setStream(stream);
        try {
          await stream.open(SignalingStreamPort);
        } catch(err) {
          console.log(`Stream exception: ${err}`);
        }
        console.log(`Stream open?`);
        
        const msg = JSON.stringify({});
        const encodedMsg = Array.from(msg, v => v.charCodeAt(0));
        await stream.write(new Uint8Array([msg.length, ...encodedMsg]))
      }
    }

    func()
  }, [conn])

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
