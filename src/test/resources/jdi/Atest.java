class Atest {

    public static void main(String[] args) throws InterruptedException {
        int increase = 0;
        long howLongSleep = 4000L;
        while(true) {
            Thread.currentThread().sleep(howLongSleep);
            int calculateResult = calculate(++increase);
            System.out.println(calculateResult);
        }
    }

    private static int calculate(int plus) {
        int two = 2;
        int three = 3 + plus;
        return two + three;
    }
}