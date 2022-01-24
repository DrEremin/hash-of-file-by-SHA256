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

    /**
     * This method fill the array hashValues[] hashes
     * from square roots first 8 primes
     */

    private void hashValuesInit() {

        double sqrtOfPrime;

        for (int i = 0; i < SIZE_HASH_VALUES; i++) {
            sqrtOfPrime = Math.sqrt(PRIMES[i]);
            hashValues[i] = doubleToHash(sqrtOfPrime);
        }
    }

    /**
     * This method fill the array ROUNDED_CONSTANTS[] hashes
     * from square roots first 64 primes
     */

    private void roundedConstantsInit() {

        double cbrtOfPrime;

        for (int i = 0; i < SIZE_QUEUE_MESSAGES; i++) {
            cbrtOfPrime = Math.cbrt(PRIMES[i]);
            ROUNDED_CONSTANTS[i] = doubleToHash(cbrtOfPrime);
        }
    }

    /**
     * This method generate a hash on base a fraction part of the double argument
     * @param value double argument for getting the hash
     * @return hash value
     */

    private int doubleToHash(double value) {

        value -= (long)value;
        value++;

        long hash = Double.doubleToLongBits(value);

        hash <<= 12;
        return (int)(hash >>> 32);
    }

    /**
     * This method create the array from 64 primes
     * @return array of primes
     */

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

    /**
     * This method read bytes from the file and write it's to the array data[]
     * This method also makes sure that the total number of bits of information
     * is a multiple of 512, but without trimming the data, i.e. by adding zero
     * bits to the end.
     * @param absolutePath the absolute path of the file
     * @throws IOException if file not find
     */

    private void readBytesFromFile(String absolutePath) throws IOException {

        File file = new File(absolutePath);

        try(FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis)) {
            lengthData = fis.available();
            data = new byte[SIZE_QUEUE_MESSAGES *
                    ((lengthData + SIZE_HASH_VALUES - 1) / SIZE_QUEUE_MESSAGES + 1)];
            bis.read(data, 0, lengthData);
        } catch (FileNotFoundException e) {
            throw new IOException();
        }
    }

    /**
     * This method writes the length of all data (bits) to the last 64 bits
     * of byte array data[].
     */

    private void preprocessing() {

        long bits = (long)lengthData * SIZE_HASH_VALUES;

        data[lengthData] = (byte)-128;
        for (int i = data.length - 1; i > data.length - 1
                - SIZE_HASH_VALUES; i--) {
            data[i] = (byte)bits;
            bits = bits >>> 8;
        }
    }

    /**
     * This method copies into each element of the 32-bit piece[] array by
     * 4 bytes from the data[] array piece.
     * @param startIndex is index of the initial element of array data[]
     * @param endIndex is index of the end element of array data[]
     */

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

    /**
     * This method modifies the last 48 elements of a 32-bit piece[]
     * array in a special way, using the first 16 elements.
     */

    private void fillingEndElementsPieces() {

        int s0, s1;

        for (int i = 16; i < piece.length; i++) {
            s0 = rightRotate(piece[i - 15], 7)
                    ^ rightRotate(piece[i - 15], 18)
                    ^ (piece[i - 15] >>> 3);
            s1 = rightRotate(piece[i - 2], 17)
                    ^ rightRotate(piece[i - 2], 19)
                    ^ (piece[i - 2] >>> 10);
            piece[i] = additionByMOD(piece[i - 16], s0, piece[i - 7], s1);
        }
    }

    private int additionByMOD(int ... operands) {

        long temp = 0;

        for (int i = 0; i < operands.length; i++) {
            temp += operands[i];
        }
        return (int)(temp % MOD);
    }

    /**
     * This method creates a message queue (32 bit array of 64 elements)
     * encoded in a special way.
     * @param startIndex is index of the initial element of array data[]
     * @param endIndex is index of the end element of array data[]
     */

    private void createQueueMessages(int startIndex, int endIndex) {

        copyDataToBeginningPieces(startIndex, endIndex);
        fillingEndElementsPieces();
    }

    /**
     * This method writes the hash values to the tempContainers[]
     * array of integers. Then these values are changed in a special way.
     */

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
            temp1 = additionByMOD(tempContainers[7],
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
            temp2 = additionByMOD(s0, maj);
            tempContainers[7] = tempContainers[6];
            tempContainers[6] = tempContainers[5];
            tempContainers[5] = tempContainers[4];
            tempContainers[4] = additionByMOD(tempContainers[3], temp1);
            tempContainers[3] = tempContainers[2];
            tempContainers[2] = tempContainers[1];
            tempContainers[1] = tempContainers[0];
            tempContainers[0] = additionByMOD(temp1, temp2);
        }
    }

    /**
     * This method modifies the hash values in the hashValues[] array by adding
     * to these hashes to the elements of the tempContainers[] array modulo 2^32.
     */

    private void changeHashValues() {

        for (int i = 0; i < hashValues.length; i++) {
            hashValues[i] = additionByMOD(hashValues[i], tempContainers[i]);
        }
    }

    /**
     * This method reads data from the specified file and changes 8 hash values
     * based on this data. Ultimately, these hashes composed together will be
     * the hashcode of the file by the sha-256 algorithm.
     * @param absolutePath is absolute path to file for heshing
     */

    private void mainCycle(String absolutePath) throws IOException {

        readBytesFromFile(absolutePath);
        preprocessing();
        for (int counterPieces = 0;
                counterPieces + 1 <= data.length / SIZE_QUEUE_MESSAGES;
                counterPieces++) {
            createQueueMessages((counterPieces) * SIZE_QUEUE_MESSAGES,
                    (counterPieces + 1) * SIZE_QUEUE_MESSAGES - 1);
            compressionCycle();
            changeHashValues();
        }
    }

    /**
     * This method join together the modified hashes from the hashValues[]
     * array into a BigInteger variable in hexadecimal notation.
     * @param absolutePath is absolute path to file for heshing
     */

    public BigInteger generateHash(String absolutePath) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        String str;

        mainCycle(absolutePath);
        for (int i = 0; i < hashValues.length; i++) {
            str = String.format("%X", hashValues[i]);
            while (str.length() < 8) {
                str = "0" + str;
            }
            stringBuilder.append(str);
        }
        return new BigInteger(stringBuilder.toString(), 16);
    }
}
