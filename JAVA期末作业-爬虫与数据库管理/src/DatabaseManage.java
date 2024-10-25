import java.util.Scanner;

public class DatabaseManage {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        jdbc db = new jdbc();
        db.begin();
        sc.close();
    }
}
