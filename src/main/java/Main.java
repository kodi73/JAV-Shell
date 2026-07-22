import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        
        while (true) {
            System.out.print("$ ");
            String command = sc.next();
            
            if (command.trim().equals("exit")) {
                break;
            } else if (command.startsWith("echo")) {
                System.out.println(command.substring(5));
            } else {
                System.out.println(command + ": command not found");
            }
        }
        sc.close();     
    }
}
