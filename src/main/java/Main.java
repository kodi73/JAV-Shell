import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        
        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();
            String command = input.substring(0,4).trim();
            String arguments = input.substring(5).trim();
            
            if (command.trim().equals("exit")) {
                break;
            } else if (command.startsWith("echo")) {
                System.out.println(command.substring(5));
            } else if (command.startsWith("type")) {
                if (arguments.equals("echo") || arguments.equals("exit") || arguments.equals("type")) {
                    System.out.println(arguments + " is a shell builtin");
                } else {
                System.out.println(arguments + ": command not found");
                }
            } else {
                System.out.println(command + ": command not found");
            }
        }
        sc.close();     
    }
}
