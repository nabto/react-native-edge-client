import EdgeClient from "../module";
import type { MdnsResultAction, MdnsScanner, OnMdnsResultCallback } from "../interface";
import { NativeEventEmitter } from "react-native";
import type { EmitterSubscription } from "react-native";

interface Result {
    action: number,
    deviceId: string,
    productId: string,
    serviceInstanceName: string,
    txtItems: Record<string, string>
};

export class MdnsScannerImpl implements MdnsScanner {
    private static nextId = 0;
    id = MdnsScannerImpl.nextId++;
    listeners: OnMdnsResultCallback[] = []
    resultListener?: EmitterSubscription

    start(): Promise<void> {
        const emitter = new NativeEventEmitter(EdgeClient);
        this.resultListener = emitter.addListener(`ScannerOnResult#${this.id}`, (event: Result) => {
            
        });
        return EdgeClient.mdnsScannerStart(this.id);
    }

    stop(): Promise<void> {
        this.resultListener?.remove();
        return EdgeClient.mdnsScannerStop(this.id);
    }
    
    isStarted(): Promise<void> {
        return EdgeClient.mdnsScannerStart(this.id);
    }

    addMdnsResultReceiver(listener: OnMdnsResultCallback): void {
        this.listeners.push(listener);
    }

    removeMdnsResultReceiver(listener: OnMdnsResultCallback): boolean {
        const index = this.listeners.indexOf(listener);
        if (index >= 0) {
            this.listeners.splice(index, 1);
            return true;
        }
        return false;
    }
}
