import java.io.*;
import java.math.BigInteger;

public class FileConvertorToHashBySha256 {


    public final int SIZE_HASH_VALUES = 8;
    public final int SIZE_QUEUE_MESSAGES = 64;
    public final int[] ROUNDED_CONSTANTS;
    public final int[] PRIMES;
    private int[] hashValues;
    private int[] pieces;
    private byte[] data;
    private int lengthData;

    FileConvertorToHashBySha256() {
        hashValues = new int[SIZE_HASH_VALUES];
        ROUNDED_CONSTANTS = new int[SIZE_QUEUE_MESSAGES];
        PRIMES = generatorOfPrimes(SIZE_QUEUE_MESSAGES);
        pieces = new int[SIZE_QUEUE_MESSAGES];
        hashValuesInit();
        roundedConstantsInit();
        lengthData = 0;
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

    private void readBytesFromFile(String absolutePath) throws IOException {

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

    private void preprocessing() {

        long bits = (long)lengthData * SIZE_HASH_VALUES;

        data[lengthData] = (byte)-128;
        for (int i = data.length - 1; i > data.length - 1
                - SIZE_HASH_VALUES; i--) {
            data[i] = (byte)bits;
            bits = bits >>> 8;
        }
    }

    private void createQueueMessages(int startIndex, int endIndex) {
        for (int i = startIndex, j = 1, k = 0; i <= endIndex; i++, j++) {
            pieces[k] |= data[i] & 255;
            if (j == 4) {
                j = 0;
                k++;
            } else {
                pieces[k] <<= 8;
            }
        }
    }

    /*public void rightShift() {
        int value = 3;
        int temp;
        int[] array = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        for (int i = array.length - value; i < array.length; i++) {

        }
    }*/

    public void rightRotate(int index, int rotateValue) {
        if (rotateValue == 32 || rotateValue <= 0) {
            return;
        }
        if (rotateValue > 32) {
            rotateValue -= 32;
        }
        int temp = pieces[index];
        temp = temp << (32 - rotateValue);
        pieces[index] = pieces[index] >>> rotateValue;
        pieces[index] = pieces[index] | temp;
    }

    public BigInteger generateHash(String absolutePath) throws IOException {

        readBytesFromFile(absolutePath);
        preprocessing();
        for (int counterPieces = 0;
                counterPieces + 1 <= data.length / SIZE_QUEUE_MESSAGES;
                counterPieces++) {
            createQueueMessages((counterPieces) * SIZE_QUEUE_MESSAGES,
                    (counterPieces + 1) * SIZE_QUEUE_MESSAGES - 1);
        }

        return new BigInteger(data);
    }
}
