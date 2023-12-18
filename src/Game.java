import javax.swing.*;

public class Game  extends JFrame {


    public  Game(){
        initUI();

    }




    private void initUI() {
        int SCREEN_WIDTH = 1600;
        int SCREEN_HEIGHT = 600;
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setVisible(true);


        add(new Board(getContentPane().getSize().width,getContentPane().getSize().height));


        setTitle("Movement");
        setDefaultCloseOperation(EXIT_ON_CLOSE);



        setLocationRelativeTo(null);

    }



}



