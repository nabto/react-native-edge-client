import EdgeClient from "../module";
import type { NabtoClient, Connection, MdnsScanner } from "../interface";
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

    setLogLevel(level: string): Promise<void> {
        throw new Error("Method not implemented.");
    }

    createMdnsScanner(): Promise<MdnsScanner>;
    createMdnsScanner(subtype: string): Promise<MdnsScanner>;
    createMdnsScanner(subtype?: unknown): Promise<MdnsScanner> {
        throw new Error("Method not implemented.");
    }

    close(): Promise<void> {
        throw new Error("Method not implemented.");
    }
    
    dispose(): Promise<void> {
        throw new Error("Method not implemented.");
    }
}
