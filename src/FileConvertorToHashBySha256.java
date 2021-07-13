public class FileConvertorToHashBySha256 {

    public final int[] ROUNDED_CONSTANTS;
    public final int[] PRIMES;
    private int[] HashValues;

    FileConvertorToHashBySha256() {
        HashValues = new int[8];
        PRIMES = generatorOfPrimes(64);
        ROUNDED_CONSTANTS = new int[64];
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
