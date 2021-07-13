public class FileConvertorToHashBySha256 {

    public final int SIZE_HASH_VALUES = 8;
    public final int SIZE_QUEUE_MESSAGES = 64;
    public final int[] ROUNDED_CONSTANTS;
    public final int[] PRIMES;
    private int[] HashValues;

    FileConvertorToHashBySha256() {
        HashValues = new int[SIZE_HASH_VALUES];
        PRIMES = generatorOfPrimes(SIZE_QUEUE_MESSAGES);
        ROUNDED_CONSTANTS = new int[SIZE_QUEUE_MESSAGES];

        for (int i = 0; i < SIZE_HASH_VALUES; i++) {
            double sqrtOfPrime = Math.sqrt(PRIMES[i]);
            sqrtOfPrime -= (long)sqrtOfPrime;
            sqrtOfPrime++;
            long hash = Double.doubleToLongBits(sqrtOfPrime);
            hash <<= 12;
            HashValues[i] = (int)(hash >>> 32);
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


}
