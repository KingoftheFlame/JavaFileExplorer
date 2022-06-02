

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.datatransfer.*;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;



public class FileSection extends FileItem{
    
    private ArrayList<FileItem> fileList;
    private Header header;
    
    private static MainFrame parent;


    public FileSection(Path p){
        super(p);
    }
    public FileSection(Path p,MainFrame f){
        this(p);
        parent = f;
    }
    public FileSection(String p){this(Paths.get(p));}

    @Override
    public void config(){
        //Frame configs
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setMaximumSize(new Dimension(600,600));
        this.setMinimumSize(new Dimension(600,20));
        
        header = new Header();
        this.add(header);
        
        rightClick = new RightClickMenu(){
            
            private JButton NewButton;
            private JButton RefreshButton;
            private JButton PasteButton;

            @Override
            public void makeButtons() {
                NewButton = new JButton("New");
                RefreshButton = new JButton("Refresh");
                PasteButton = new JButton("Paste");
                
                this.add(NewButton);
                this.add(RefreshButton);
                this.add(PasteButton);

                makeListeners();
            }

            @Override
            protected void makeListeners(){
                NewButton.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        NewFile();
                    }});
                

                RefreshButton.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        refresh();
                    }});
                PasteButton.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        paste();
                    }});
                    
            }
            
            

        };
        this.addMouseListener(new MouseListener(){
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    doPop(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    doPop(e);
            }
            private void doPop(MouseEvent e) {
                rightClick.show(e.getComponent(), e.getX(), e.getY());
            }
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {}
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {}
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {}
            
        });

    }

    public void changePath(Path p){changePath(p,false);}
    public void changePath(Path p,boolean updatePathBox){
        file = p.toFile();
        refresh();
        if(updatePathBox) parent.setPathBox(p);
    }


    //Right Click Menu
    public void NewFile(){NewFile(0);}
    public void NewFile(int n){
        File temp;
        if(n==0) temp = new File(file + "\\New File");
        else{temp = new File(file + "\\New File ("+n+")");}
        try{
            if(!temp.createNewFile()) NewFile(n+1);
            else{
                refresh();                              
                for(FileItem f : fileList){
                    if(f.path.toString().equals(temp.toString())){
                        f.rename();
                        break;
                    }
                }
            }
            
        }catch(IOException e){

        }
    }

    @Override   
    public void refresh(){
        this.removeAll();
        this.add(header);
        if(fileList != null)fileList.clear();
        fileList = this.getSubFiles();
        for(FileItem f : fileList){
            f.setAlignmentX(Component.LEFT_ALIGNMENT);
            f.setBorder(BorderFactory.createLineBorder(Color.black));
            this.add(f);
        }
        this.setVisible(false);
        this.setVisible(true);
    }
    public void paste(){
        Transferable tran = FileItem.ClipBoard.getContents(null);
        
        try
        {

            if(tran != null && tran.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                String str = tran.getTransferData(DataFlavor.javaFileListFlavor).toString();
                str = str.substring(1,str.length()-1);
                File fileSource = new File(str);
                

                InputStream inStream = null;
                OutputStream outStream = null;

                File outputFile = new File(file.getAbsolutePath() + "\\" + fileSource.getName());

                inStream = new FileInputStream(fileSource);
                outStream = new FileOutputStream(outputFile);

                byte[] buffer = new byte[1024];


                int fileLength;
                while ((fileLength = inStream.read(buffer)) > 0){

                    outStream.write(buffer, 0, fileLength );

                }

                inStream.close();
                outStream.close();

                refresh();
            }

        }catch(Exception e){
            System.out.println("Exception in paste... again");
            System.out.println("This time the exception is...");
            System.out.println(e);
        }
    }

    //Header at top of file
    protected class Header extends FileItem{

        private String dateModified;
        private String size;

        public Header(){
            super("");
        }

        @Override
        public void config(){

            this.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.setBorder(BorderFactory.createLineBorder(Color.black));

            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            this.setMaximumSize(new Dimension(600,30));
            this.setMinimumSize(new Dimension(600,30));

            //sets values of variables
            name = "Name";
            dateModified = "Date Modified";
            type = "Type";
            size = "Size";

            //Initalizes boxes with no values
            selectBox = new JCheckBox();
            nameBox = new JLabel(name);
            modifiedBox = new JLabel(dateModified);
            typeBox = new JLabel(type);
            sizeBox = new JLabel(size);

            refresh();

            this.add(selectBox);
            this.add(nameBox);
            this.add(modifiedBox);
            this.add(typeBox);
            this.add(sizeBox);

            this.addFocusListener(new FocusListener(){
                    @Override
                    public void focusGained(FocusEvent e) {
                        selectBox.setVisible(true);
                    }
                    @Override
                    public void focusLost(FocusEvent e) {
                        selectBox.setVisible(false);
                    }
                });

            selectBox.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                if(selectBox.isSelected()){
                    for(FileItem x : fileList) x.setSelected(true);
                }else if(!selectBox.isSelected()){
                    for(FileItem x : fileList) x.setSelected(false);
                }
            }});


        }
        @Override
        public void refresh() {}

    }

}