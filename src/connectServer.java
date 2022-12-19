import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class connectServer {
   private static final int PORT = 4433;
   private static final String HOST = "www.secure-server.edu";
   public static void main(String[]args) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException {

      KeyStore trustStore = KeyStore.getInstance("PKCS12");
      trustStore.load(null,null);//inicialize
      // Assume que ficheiro CA1.cer está na diretoria de execução.
      FileInputStream in = new FileInputStream("CA1.cer");//trust-anchor do servidor
      // Gera objeto para certificados X.509.
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      // Gera o certificado a partir do ficheiro.
      X509Certificate certificate = (X509Certificate) cf.generateCertificate(in);
      trustStore.setCertificateEntry("ca",certificate);

      Certificate cert = trustStore.getCertificate("ca");
      //System.out.println(cert);

      //criar um contexto SSL com o certificado do servidor
      TrustManagerFactory trustManagerFactory =
              TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

      trustManagerFactory.init(trustStore);
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null,trustManagerFactory.getTrustManagers(),null);


       //SSLSocketFactory do contexto SSL criado

       SSLSocketFactory sslFactory = sslContext.getSocketFactory();
              /*HttpsURLConnection.getDefaultSSLSocketFactory();*/


      //print cipher suites avaliable at the client
      /*
      String[] cipherSuites = sslFactory.getDefaultCipherSuites();
      for (int i=0; i<cipherSuites.length; ++i) {
         System.out.println("option " + i + " " + cipherSuites[i]);
      }*/
      // Estabelece a conexão com o servidor "www.server-secure.edu"
      SSLSocket sslSocket = (SSLSocket) sslFactory.createSocket(HOST,PORT);
      sslSocket.startHandshake();
      SSLSession session = sslSocket.getSession();
      //System.out.println("Cipher suite: " + session.getCipherSuite());
      /*System.out.println("Protocol version: " + session.getProtocol());
      System.out.println(session.getPeerCertificates()[0]);*/
      System.out.println("Connected:"+sslSocket.isConnected());

      // Envia a solicitação HTTP
      PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
      out.println("GET /path HTTP/1.1");
      out.println("Host: " + HOST);
      out.println("");
      out.flush();

      // Recebe a resposta do servidor
      BufferedReader inBuf = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
      String line;
      while ((line = inBuf.readLine()) != null) {
         System.out.println(line);
      }
      //sslSocket.close();
   }
}
