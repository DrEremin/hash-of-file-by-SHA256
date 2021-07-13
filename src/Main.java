public class Main {
    public static void main(String[] args) {
        FileConvertorToHashBySha256 fileConvertorToHashBySha256
                = new FileConvertorToHashBySha256();
        for (int i = 0; i < fileConvertorToHashBySha256.PRIMES.length; i++) {
            System.out.println(fileConvertorToHashBySha256.PRIMES[i]);
        }
    }
}
