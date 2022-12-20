import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class connectServer {
   private static final int PORT = 4433;
   private static final String HOST = "www.secure-server.edu";
   public static void main(String[]args) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException{

      KeyStore trustStore = KeyStore.getInstance("PKCS12");
      trustStore.load(null,null);//inicialize
      // Assume que ficheiro CA1-int.cer está na diretoria de execução.
      FileInputStream in = new FileInputStream("CA1-int.cer");
      // Gera objeto para certificados X.509.
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      // Gera o certificado a partir do ficheiro.
      X509Certificate certificate = (X509Certificate) cf.generateCertificate(in);
      trustStore.setCertificateEntry("ca",certificate);

      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

      trustManagerFactory.init(trustStore);
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null,trustManagerFactory.getTrustManagers(),null);

      // Estabelece a conexão com o server server-secure.edu
      SSLSocketFactory sslFactory = sslContext.getSocketFactory();
      SSLSocket sslSocket = (SSLSocket) sslFactory.createSocket(HOST,PORT);
      sslSocket.startHandshake();
      System.out.println("Connected:"+sslSocket.isConnected());

      // Envia um pedido HTTP
      PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
      out.println("GET / HTTP/1.1");
      out.println("Host: " + HOST);
      out.println("");
      out.flush();

      // Recebe a resposta do server
      BufferedReader inBuf = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
      String line;
      while ((line = inBuf.readLine()) != null) {
         System.out.println(line);
      }
      sslSocket.close();
   }
}
