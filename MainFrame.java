

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;
import java.nio.file.Path;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MainFrame extends JFrame{

    private TopBar topBar;

    private FileSection FileDisplay;


    public MainFrame(){this("C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\");}
    public MainFrame(String p){
        //JFrame configs
        this.setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
        this.setMaximumSize(new Dimension(600,600));
        this.setMinimumSize(new Dimension(600,400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        topBar = new TopBar(Path.of(p));
        topBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        FileDisplay = new FileSection(Path.of(p),this);
        
        this.add(topBar);
        this.add(FileDisplay);

        this.setVisible(true);

    }

    public void refresh(){
        this.setVisible(false);
        this.setVisible(true);
    }

    public void setPathBox(Path p){
        topBar.setPath(p);
        this.refresh();
    }

    public void updateFileSection(){
        FileDisplay.changePath(topBar.getPath());
        FileDisplay.refresh();
        this.refresh();
    }


    private class TopBar extends JPanel{
        private JTextField PathBox;
        private JButton upButton;

        public TopBar(Path p){
            //Top Bar configs
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            this.setMaximumSize(new Dimension(600,30));
            this.setMinimumSize(new Dimension(600,30));

            //Initilization of subcomponents
            PathBox = new JTextField(p.toString());
            upButton = new JButton("/|\\"); //convert to icon

            //Action Listeners
            upButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    String newPath = PathBox.getText();
                    newPath = newPath.substring(0,newPath.lastIndexOf("\\"));
                    PathBox.setText(newPath);
                    updateFileSection();
                }

            });
            PathBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    updateFileSection();
                }
                
            });

            //Adds components to top bar
            this.add(PathBox);
            this.add(upButton);
        }


        public Path getPath(){
            return Path.of(PathBox.getText());
        }

        public void setPath(Path p){
            PathBox.setText(p.toString());
        }
    }



}
