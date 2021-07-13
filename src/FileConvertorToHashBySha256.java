import java.io.*;

public class FileConvertorToHashBySha256 {

    public final int SIZE_HASH_VALUES = 8;
    public final int SIZE_QUEUE_MESSAGES = 64;
    public final int SIZE_PIECE = 512;
    public final int[] ROUNDED_CONSTANTS;
    public final int[] PRIMES;
    private int[] HashValues;
    private byte[] data;
    private int lengthData;

    FileConvertorToHashBySha256() {
        HashValues = new int[SIZE_HASH_VALUES];
        ROUNDED_CONSTANTS = new int[SIZE_QUEUE_MESSAGES];
        PRIMES = generatorOfPrimes(SIZE_QUEUE_MESSAGES);
        hashValuesInit();
        roundedConstantsInit();
        lengthData = 0;
        data = null;
    }

    private void hashValuesInit() {
        for (int i = 0; i < SIZE_HASH_VALUES; i++) {
            double sqrtOfPrime = Math.sqrt(PRIMES[i]);
            sqrtOfPrime -= (long)sqrtOfPrime;
            sqrtOfPrime++;
            long hash = Double.doubleToLongBits(sqrtOfPrime);
            hash <<= 12;
            HashValues[i] = (int)(hash >>> 32);
        }
    }

    private void roundedConstantsInit() {
        for (int i = 0; i < SIZE_QUEUE_MESSAGES; i++) {
            double cbrtOfPrime = Math.cbrt(PRIMES[i]);
            cbrtOfPrime -= (long)cbrtOfPrime;
            cbrtOfPrime++;
            long hash = Double.doubleToLongBits(cbrtOfPrime);
            hash <<= 12;
            ROUNDED_CONSTANTS[i] = (int)(hash >>> 32);
        }
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
            data[11] = (byte)-128; // исправить на data[lengthData]
            bis.read(data, 0, 11);
            /*for (int i = 0; i < data.length; i++) {
                System.out.println(data[i]);
            }*/
        } catch (FileNotFoundException e) {
            throw new IOException();
        }
    }
}
