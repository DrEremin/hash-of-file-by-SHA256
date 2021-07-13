public class Main {
    public static void main(String[] args) {
        /*double d = Math.sqrt(11);
        d -= (long)d;
        d++;
        System.out.printf("%.30f\n", d);
        System.out.printf("%a\n", d);
        long l = Double.doubleToLongBits(d);
        l <<= 12;
        int hash = (int)(l >>> 32);
        System.out.printf("%x", hash);*/

        FileConvertorToHashBySha256 convertor = new FileConvertorToHashBySha256();
        for (int i = 0; i < 8; i++) {
            System.out.printf("%x\n", convertor.HashValues[i]);
        }
    }
}
