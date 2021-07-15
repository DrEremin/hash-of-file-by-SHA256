import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {

        FileConvertorToHashBySha256 convertor = new FileConvertorToHashBySha256();
        /*for (int i = 0; i < 8; i++) {
            System.out.printf("%x\n", convertor.HashValues[i]);
        }

        for (int i = 0; i < 64; i += 8) {
            for (int j = 0; j < 8; j++) {
                System.out.printf("%x ", convertor.ROUNDED_CONSTANTS[i + j]);
            }
            System.out.println();
        }*/
        //File file = new File("/home/ivan/Programming/Hello World");
        //byte b = (byte)0b10000000;
        //b = (byte)(b | 0b01111111);
        //System.out.println(b);
        //convertor.readBytesFromFile("/home/ivan/Programming/Hello World");
        //convertor.preprocessing();
        //convertor.createQueueMessages();
        convertor.generateHash("/home/ivan/Programming/Hello World");
        //System.out.printf("%x\n", convertor.rightRotate(1, 18));
    }
}
