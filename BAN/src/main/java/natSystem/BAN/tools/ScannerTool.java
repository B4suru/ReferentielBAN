package natSystem.BAN.tools;


import java.util.Scanner;

public class ScannerTool {
    private Scanner scanner;


    public ScannerTool() {
        this.scanner = new Scanner(System.in);
    }

    public void close(){
        scanner.close();
    }

    public String getString (String text) {
        System.out.println(text);
        return scanner.nextLine().trim();
    }

    public Long getLong (String text) {
        Long res = null;
        String resString;


        do {
            System.out.println(text);
            resString = scanner.nextLine().trim();
            if (resString.isEmpty()) {
                return null;
            }

            try {
                res = Long.parseLong(resString);
            } catch (NumberFormatException e) {
                System.err.println("Entrée invalide.");
            }
        } while (res == null);
        return res;
    }
}
