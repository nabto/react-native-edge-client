import EdgeClient from "./module";
import type { NabtoClient, Connection } from "./interface";
import { ConnectionImpl } from "./ConnectionImpl";

export async function createNabtoClient(): Promise<NabtoClient> {
    const client = new NabtoClientImpl();
    await EdgeClient.createNabtoClient(client.id);
    return client;
}

class NabtoClientImpl implements NabtoClient {
    private static nextId = 0
    id = NabtoClientImpl.nextId++

    version(): string {
        return EdgeClient.clientGetVersion(this.id);
    }

    createPrivateKey(): string {
        return EdgeClient.clientCreatePrivateKey(this.id);
    }

    async createConnection(): Promise<Connection> {
        const connection = new ConnectionImpl();
        await EdgeClient.clientCreateConnection(this.id, connection.id);
        return connection;
    }
}
