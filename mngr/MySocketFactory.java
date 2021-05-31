package mngr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.jcraft.jsch.SocketFactory;

class MySocketFactory implements SocketFactory {
  
  String _bindHost;
  int _bindPort;
  
  public MySocketFactory (String bindHost, int bindPort) {
    this._bindHost = bindHost;
    this._bindPort = bindPort;
  }
  
  //@Override
  public Socket createSocket(String host, int port) throws IOException {
      Socket socket = new Socket();
      //socket.bind(new InetSocketAddress("1.1.1.1", 0));
      socket.bind(new InetSocketAddress(_bindHost, _bindPort));
      socket.connect(new InetSocketAddress(host, port));

      return socket;
  }

  //@Override
  public InputStream getInputStream(Socket socket) throws IOException {
      return socket.getInputStream();
  }

  //@Override
  public OutputStream getOutputStream(Socket socket) throws IOException {
      return socket.getOutputStream();
  }

}
