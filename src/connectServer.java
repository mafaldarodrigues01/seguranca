import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class connectServer {
   private static final int PORT = 4433;
   public static void main(String[]args) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

      KeyStore ks = KeyStore.getInstance("PKCS12");
      ks.load(null,null);//inicialize
      // Assume que ficheiro CA1.cer está na diretoria de execução.
      FileInputStream in = new FileInputStream("CA1.cer");//trust-anchor do servidor
      // Gera objeto para certificados X.509.
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      // Gera o certificado a partir do ficheiro.
      X509Certificate certificate = (X509Certificate) cf.generateCertificate(in);
      ks.setCertificateEntry("alias19",certificate);

      //criar um contexto SSL com o certificado do servidor
      TrustManagerFactory trustManagerFactory =
              TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(ks);
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null,trustManagerFactory.getTrustManagers(),null);
      /*print cipher suites avaliable at the client
      String[] cipherSuites = sslFactory.getSupportedCipherSuites();
      for (int i=0; i<cipherSuites.length; ++i) {
         System.out.println("option " + i + " " + cipherSuites[i]);
      }*/

       //SSLSocketFactory do contexto SSL criado

       SSLSocketFactory sslFactory = sslContext.getSocketFactory();
              /*HttpsURLConnection.getDefaultSSLSocketFactory();*/
      // Estabelece a conexão com o servidor "www.server-secure.edu"
      SSLSocket client = (SSLSocket) sslFactory.createSocket("www.server-secure.edu", PORT);
      client.startHandshake();

      SSLSession session = client.getSession();
      System.out.println("Cipher suite: " + session.getCipherSuite());
      System.out.println("Protocol version: " + session.getProtocol());
      System.out.println(session.getPeerCertificates()[0]);

      client.close();


   }
}
