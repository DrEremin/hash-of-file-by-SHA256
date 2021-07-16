import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;

public class FileConvertorToHashBySha256 {

    public final long MOD = (long)Math.pow(2, 32);
    public final int SIZE_HASH_VALUES = 8;
    public final int SIZE_QUEUE_MESSAGES = 64;
    public final int[] ROUNDED_CONSTANTS;
    public final int[] PRIMES;
    private int[] hashValues;
    private int[] tempContainers;
    private int[] piece;
    private byte[] data;
    private int lengthData;

    FileConvertorToHashBySha256() {

        hashValues = new int[SIZE_HASH_VALUES];
        tempContainers = new int[SIZE_HASH_VALUES];
        ROUNDED_CONSTANTS = new int[SIZE_QUEUE_MESSAGES];
        PRIMES = generatorOfPrimes();
        piece = new int[SIZE_QUEUE_MESSAGES];
        hashValuesInit();
        roundedConstantsInit();
        lengthData = 0;
        data = null;
    }

    private void hashValuesInit() {

        double sqrtOfPrime;

        for (int i = 0; i < SIZE_HASH_VALUES; i++) {
            sqrtOfPrime = Math.sqrt(PRIMES[i]);
            hashValues[i] = doubleToHash(sqrtOfPrime);
        }
    }

    private void roundedConstantsInit() {

        double cbrtOfPrime;

        for (int i = 0; i < SIZE_QUEUE_MESSAGES; i++) {
            cbrtOfPrime = Math.cbrt(PRIMES[i]);
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


    private int[] generatorOfPrimes() {

        int[] primes = new int[SIZE_QUEUE_MESSAGES];
        int primesSize = 1;
        boolean flag = false;

        primes[0] = 2;
        for (int i = 3, j = 0, number; primesSize < SIZE_QUEUE_MESSAGES; i++, j = 0) {
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

    private void copyDataToBeginningPieces(int startIndex, int endIndex) {

        Arrays.fill(piece, 0);
        for (int i = startIndex, j = 1, k = 0; i <= endIndex; i++, j++) {
            piece[k] |= data[i] & 255;
            if (j == 4) {
                j = 0;
                k++;
            } else {
                piece[k] <<= 8;
            }
        }
    }

    public int rightRotate(int value, int rotateValue) {

        if (rotateValue == 32 || rotateValue <= 0) {
            return 0;
        }
        if (rotateValue > 32) {
            rotateValue -= 32;
        }

        int temp1 = value;
        int temp2 = temp1;

        temp1 <<= (32 - rotateValue);
        temp2 >>>= rotateValue;
        return temp2 | temp1;
    }

    private void fillingEndElementsPieces() {

        int s0, s1;

        for (int i = 16; i < piece.length; i++) {
            s0 = rightRotate(piece[i - 15], 7)
                    ^ rightRotate(piece[i - 15], 18)
                    ^ (piece[i - 15] >>> 3);
            s1 = rightRotate(piece[i - 2], 17)
                    ^ rightRotate(piece[i - 2], 19)
                    ^ (piece[i - 2] >>> 10);
            piece[i] = additionByMod2Pow32(piece[i - 16], s0, piece[i - 7], s1);
        }
    }

    private int additionByMod2Pow32(int ... operands) {

        long temp = 0;

        for (int i = 0; i < operands.length; i++) {
            temp += operands[i];
        }
        return (int)(temp % MOD);
    }

    private void createQueueMessages(int startIndex, int endIndex) {

        copyDataToBeginningPieces(startIndex, endIndex);
        fillingEndElementsPieces();
        /*for (int i = 0; i < piece.length; i++) {
            System.out.printf("%d - %x\n", i, piece[i]);
        }*/
    }

    private void compressionCycle() {

        int s0, s1, ch, temp1, temp2, maj;

        for (int i = 0; i < tempContainers.length; i++) {
            tempContainers[i] = hashValues[i];
        }
        for (int i = 0; i < SIZE_QUEUE_MESSAGES; i++) {
            s1 = rightRotate(tempContainers[4], 6)
                    ^ rightRotate(tempContainers[4], 11)
                    ^ rightRotate(tempContainers[4], 25);
            ch = (tempContainers[4] & tempContainers[5])
                    ^ ((~tempContainers[4]) & tempContainers[6]);
            temp1 = additionByMod2Pow32(tempContainers[7],
                    s1,
                    ch,
                    ROUNDED_CONSTANTS[i],
                    piece[i]);
            s0 = rightRotate(tempContainers[0], 2)
                    ^ rightRotate(tempContainers[0], 13)
                    ^ rightRotate(tempContainers[0], 22);
            maj = (tempContainers[0] & tempContainers[1])
                    ^ (tempContainers[0] & tempContainers[2])
                    ^ (tempContainers[1] & tempContainers[2]);
            temp2 = additionByMod2Pow32(s0, maj);
            tempContainers[7] = tempContainers[6];
            tempContainers[6] = tempContainers[5];
            tempContainers[5] = tempContainers[4];
            tempContainers[4] = additionByMod2Pow32(tempContainers[3], temp1);
            tempContainers[3] = tempContainers[2];
            tempContainers[2] = tempContainers[1];
            tempContainers[1] = tempContainers[0];
            tempContainers[0] = additionByMod2Pow32(temp1, temp2);
        }
        /*for (int i = 0; i < hashValues.length; i++) {
            System.out.printf("%d - %x\n", i, hashValues[i]);
        }
        for (int i = 0; i < tempContainers.length; i++) {
            System.out.printf("%d - %x\n", i, tempContainers[i]);
        }*/
    }

    public BigInteger generateHash(String absolutePath) throws IOException {

        readBytesFromFile(absolutePath);
        preprocessing();
        for (int counterPieces = 0;
                counterPieces + 1 <= data.length / SIZE_QUEUE_MESSAGES;
                counterPieces++) {
            createQueueMessages((counterPieces) * SIZE_QUEUE_MESSAGES,
                    (counterPieces + 1) * SIZE_QUEUE_MESSAGES - 1);
            compressionCycle();
        }
        return new BigInteger(data);
    }
}
