import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {

        FileConvertorToHashBySha256 convertor = new FileConvertorToHashBySha256();
        System.out.printf("%x\n", convertor.generateHash(
                "/home/ivan/Изображения/DB_diagram.png"));
    }
}
