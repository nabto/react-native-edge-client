import type { Stream } from "../interface";
import EdgeClient from "../module";
import { toBase64 } from "./util";

export class StreamImpl implements Stream {
    private static nextId = 0
    id = StreamImpl.nextId++

    open(streamPort: number): Promise<void> {
        return EdgeClient.streamOpen(this.id, streamPort);
    }

    async readSome(): Promise<Uint8Array> {
        const result = await EdgeClient.streamReadSome(this.id);
        return new Uint8Array(result);
    }

    async readAll(length: number): Promise<Uint8Array> {
        const result = await EdgeClient.streamReadAll(this.id, length);
        return new Uint8Array(result);
    }

    write(bytes: Uint8Array): Promise<void> {
        return EdgeClient.streamWrite(this.id, toBase64(bytes));
    }

    close(): Promise<void> {
        return EdgeClient.streamClose(this.id);
    }

    dispose(): Promise<void> {
        return EdgeClient.streamDispose(this.id);
    }
}
