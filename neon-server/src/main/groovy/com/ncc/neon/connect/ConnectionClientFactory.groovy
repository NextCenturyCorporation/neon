package com.ncc.neon.connect



public interface ConnectionClientFactory {

    ConnectionClient createConnectionClient(ConnectionInfo info)
}