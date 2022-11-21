package client.Network;

import Server.AbstractObjects.ByteObject;
@FunctionalInterface
public interface Callback {
    void onReceived(ByteObject byteObject);
}
