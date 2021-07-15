import java.io.*;

public class FileConvertorToHashBySha256 {


    public final int SIZE_HASH_VALUES = 8;
    public final int SIZE_QUEUE_MESSAGES = 64;
    public final int[] ROUNDED_CONSTANTS;
    public final int[] PRIMES;
    private int[] hashValues;
    private byte[][] queueMessages;
    private byte[] data;
    private int lengthData;

    FileConvertorToHashBySha256() {
        hashValues = new int[SIZE_HASH_VALUES];
        ROUNDED_CONSTANTS = new int[SIZE_QUEUE_MESSAGES];
        PRIMES = generatorOfPrimes(SIZE_QUEUE_MESSAGES);
        hashValuesInit();
        roundedConstantsInit();
        lengthData = 0;
        queueMessages = null;
        data = null;
    }

    private void hashValuesInit() {
        for (int i = 0; i < SIZE_HASH_VALUES; i++) {
            double sqrtOfPrime = Math.sqrt(PRIMES[i]);
            hashValues[i] = doubleToHash(sqrtOfPrime);
        }
    }

    private void roundedConstantsInit() {
        for (int i = 0; i < SIZE_QUEUE_MESSAGES; i++) {
            double cbrtOfPrime = Math.cbrt(PRIMES[i]);
            ROUNDED_CONSTANTS[i] = doubleToHash(cbrtOfPrime);
        }
    }

    private int doubleToHash(double value) {
        value -= (long)value;
        value++;
        long hash = Double.doubleToLongBits(value);
        hash <<= 12;
        return (int)(hash >>> 32);
    }


    private int[] generatorOfPrimes(int amountOfPrimes) {
        int[] primes = new int[amountOfPrimes];
        primes[0] = 2;
        int primesSize = 1;
        boolean flag = false;
        for (int i = 3, j = 0, number; primesSize < amountOfPrimes; i++, j = 0) {
            number = (int) Math.sqrt(i);
            while (number >= primes[j]) {
                if (i % primes[j] == 0) {
                    flag = true;
                    break;
                } else {
                    j++;
                }
            }
            if (!flag) {
                primes[primesSize] = i;
                primesSize++;
            } else {
                flag = false;
            }
        }
        return primes;
    }

    public void readBytesFromFile(String absolutePath) throws IOException {

        File file = new File(absolutePath);
        try(FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis)) {
            lengthData = fis.available();
            data = new byte[SIZE_QUEUE_MESSAGES *
                    ((lengthData + SIZE_HASH_VALUES - 1) / SIZE_QUEUE_MESSAGES + 1)];
            bis.read(data, 0, lengthData);
            lengthData = 11;           //специально обрезал последний байт (убрать в конце разработки)
            data[lengthData] = 0;      //специально обрезал последний байт (убрать в конце разработки)
        } catch (FileNotFoundException e) {
            throw new IOException();
        }
    }

    public void preprocessing() {

        long bits = lengthData * SIZE_HASH_VALUES;

        data[lengthData] = (byte)-128;
        for (int i = data.length - 1; i > data.length - 1
                - SIZE_HASH_VALUES; i--) {
            data[i] = (byte)bits;
            bits = bits >>> 8;
        }
    }

    public void createQueueMessages() {

        try {
            readBytesFromFile("/home/ivan/Programming/Hello World");
        } catch(IOException e) {

        }
        preprocessing();

        queueMessages = new byte[64][4];
        for (int i = 0; i < queueMessages.length; i++) {
            for (int j = 0; j < queueMessages[i].length; j++) {
                queueMessages[i][j] = data[4 * i + j];
            }
        }

        for (int i = 0; i < queueMessages.length; i++) {
            for (int j = 0; j < queueMessages[i].length; j++) {
                System.out.printf("%5d", queueMessages[i][j]);
            }
            System.out.println();
        }
    }

}
