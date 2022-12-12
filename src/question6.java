import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Enumeration;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

import javax.crypto.*;


public class question6 {
    final static String COMMAND_ENC = "-enc";
    final static String COMMAND_DEC = "-dec";
    public static void main(String[] args) throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, KeyStoreException, UnrecoverableKeyException {
        String command = "-enc";

        // chave simetrica cifra o ficheiro .txt -> chave publica do certificado cifra a chave simetrica ->
        // chave


        /////////////////////////ENCODE///////////////////////////
        if(command.equals(COMMAND_ENC)) {
            System.out.println("Begin encrypt");
            String file = args[1];
            String cert = args[2];

            //certificado
            FileInputStream in = new FileInputStream(cert);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(in);


            // Obtém a chave pública do certificado.
            PublicKey publicKey = certificate.getPublicKey();


            //ficheiro
            FileInputStream inFile = new FileInputStream(file);
            Base64InputStream inBase = new Base64InputStream(inFile);

            //gerar cifra simetrica
            Cipher cipherSim = Cipher.getInstance("AES");
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            SecretKey key = keyGen.generateKey();

            //Associa chave key a cifra
            cipherSim.init(Cipher.ENCRYPT_MODE, key);
            //Cifra o ficheiro
            CipherInputStream inStream = new CipherInputStream(inBase, cipherSim);

            FileOutputStream outputStream = new FileOutputStream("ficheiro_cifrado.opt");
            Base64OutputStream out = new Base64OutputStream(outputStream);
            out.write(inStream.readAllBytes()); //escreve no ficheiro cena encriptada
            out.close();

            Cipher cipherCer = Cipher.getInstance("RSA");
            cipherCer.init(Cipher.WRAP_MODE, publicKey);

            FileOutputStream outputStreamKey = new FileOutputStream("cifra_chaveSimetrica.opt");
            Base64OutputStream outCifKey = new Base64OutputStream(outputStreamKey);
            outCifKey.write(cipherCer.wrap(key));
            outCifKey.close();
            System.out.println("End encrypt");

            ////////////////////////////DECODE/////////////////////////////////

        }else if(command.equals(COMMAND_DEC)) {
            System.out.println("Begin decrypt");
            String fileEnc = args[1];
            String fileKey = args[2];
            String filePfx = args[3];


            Cipher cipherCer = Cipher.getInstance("RSA"); // private key do .pfx

            Cipher cipherSim = Cipher.getInstance("AES");


            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(filePfx), "changeit".toCharArray());

            Enumeration<String> entries = ks.aliases();
            PrivateKey privateKey = (PrivateKey) ks.getKey(entries.nextElement(), "changeit".toCharArray());

            FileInputStream decKey = new FileInputStream(fileKey);
            Base64InputStream dec64 = new Base64InputStream(decKey);
            cipherCer.init(Cipher.UNWRAP_MODE, privateKey);
            SecretKey cifKey = (SecretKey) cipherCer.unwrap(dec64.readAllBytes(), "AES", Cipher.SECRET_KEY);

            // associa a chave decript à cifra
            cipherSim.init(Cipher.DECRYPT_MODE, cifKey);

            FileInputStream cifFile = new FileInputStream(fileEnc);
            Base64InputStream inDec64 = new Base64InputStream(cifFile);
            byte[] inputCifFile = inDec64.readAllBytes();

            byte[] check = cipherSim.doFinal(inputCifFile); // decifra o ficheiro???????

            FileOutputStream fileOut = new FileOutputStream("dec_file.txt");
            Base64OutputStream fileOut64 = new Base64OutputStream(fileOut);
            fileOut64.write(check);

            System.out.println("End decrypt");
        }else{
            System.out.println("Command not suported");
        }

    }

    /* Imprime array de bytes em hexadecimal */

    private static void prettyPrint(byte[] tag) {
        for (byte b: tag) {
            System.out.printf("%02x", b);
        }
        System.out.println();
    }
}