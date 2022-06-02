

public class App {
    public static void main(String[] args) throws Exception {
        MainFrame gui;
        if(args.length < 1 ){
            gui = new MainFrame();
        }else{
            gui = new MainFrame(args[0]);
        }
    }
}
