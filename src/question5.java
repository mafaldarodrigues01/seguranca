import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class question5 {

        public static void main(String[]args) throws NoSuchAlgorithmException, IOException {
            String hashAlg = args[0];
            String fileName = args[1];
            if(hashAlg.isEmpty()||fileName.isEmpty()){
                System.out.println("Not enough arguments");
                return;
            }
            Path path = Paths.get(fileName).toAbsolutePath();
            File directory = new File(String.valueOf(path));
            String file = directory.getName();
            System.out.print("Valor de hash "+ fileName+" = ");
            hash(hashAlg,String.valueOf(path));
        }


    /** produz um valor de hash com o algoritmo passado no parametro type
     */
    public static void hash(String type, String file) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance(type);
        //assumimos que o ficheiro se encontra na mesma diretoria
        FileInputStream fl = new FileInputStream(file);
        byte[] msg = fl.readAllBytes(); //guarda a informção do ficheiro em bytes
        md.update(msg);
        byte[] h = md.digest();
        prettyPrint(h);
    }

    private static void prettyPrint(byte[] h) {
        for (byte b : h) {
            System.out.printf("%02x", b);
        }
        System.out.println();
    }
}