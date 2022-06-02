import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
//import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
//import javax.swing.filechooser.FileSystemView;

import java.awt.Dimension;
import java.awt.FlowLayout;
//import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Toolkit;
import java.awt.Desktop;


public class FileItem extends JPanel implements ClipboardOwner{
    protected Path path;
    protected String name;
    protected Date   dateModified;
    protected String type;
    protected long size;
    protected boolean isDirectory;

    protected File file;

    protected JCheckBox selectBox;
    protected JLabel fileIcon;
    protected JLabel nameBox;
    protected JTextField nameChangeBox;
    protected JLabel modifiedBox;
    protected JLabel typeBox;
    protected JLabel sizeBox;

    protected RightClickMenu rightClick;

    public static final Clipboard ClipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat ("dd/MM/yyyy hh:mm a ");
    
    public FileItem(Path p){
        path = p;
        file = path.toFile();

        try{config();}
        catch(FileNotFoundException e){
            //TODO add something to be done here
        }
        refresh();
        
        
        this.setVisible(true);
        
    }
    public FileItem(String p){this(Path.of(p));}


    public void config()throws FileNotFoundException{
        if(!file.exists()) {
            throw new FileNotFoundException();
        };

        //Frame configs
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.setMaximumSize(new Dimension(600,30));
        this.setMinimumSize(new Dimension(600,30));

        //Initalizes boxes with no values
        selectBox = new JCheckBox();
        nameBox = new JLabel();
        nameChangeBox = new JTextField();
        modifiedBox = new JLabel();
        typeBox = new JLabel();
        sizeBox = new JLabel();  

        //Rename()
        nameChangeBox.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                File f =  new File(file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf("\\")).replace('\\', '/')+"/"+nameChangeBox.getText()+type);
                file.renameTo(f);
                file = f;
                refresh();
            }
        });
        
        rightClick = new RightClickMenu();

        refresh();

        //Adds text boxs to the Frame
        this.add(selectBox);
        //this.add(fileIcon);
        this.add(nameBox);
        this.add(nameChangeBox);
        nameChangeBox.setVisible(false);
        nameChangeBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                nameChangeBox.setVisible(false);
                nameBox.setVisible(true); 
            }
            
        });

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

        this.addMouseListener(new MouseListener(){
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    doPop(e);
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    doPop(e);
            }
            private void doPop(MouseEvent e) {
                rightClick.show(e.getComponent(), e.getX(), e.getY());
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    open();
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });

        nameBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        typeBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    

    }

    public void refresh(){
    //Gets file Info
        
        dateModified = new Date(file.lastModified());
        //Handels type and name
        if(file.isDirectory()){
            isDirectory = true;
            type = "";
            typeBox.setVisible(false);
            name = file.getName();
        }else if(file.getName().substring(1).contains(".")){
            isDirectory = false;
            typeBox.setVisible(true);
            type = file.getName().substring(file.getName().lastIndexOf("."));
            name = file.getName().substring(0,file.getName().lastIndexOf("."));
        }else{
            isDirectory = false;
            type = "";
            typeBox.setVisible(true);
            name = file.getName();
        }        
        size = file.length();

    //Loads the Values into text boxes
        nameBox.setText(name);
        //fileIcon.setIcon(getIcon());
        modifiedBox.setText(dateFormat.format(dateModified));
        typeBox.setText(type);
        sizeBox.setText(bytesToString(size)); 

    }


    //helper Functions
    private String bytesToString(long b){
        if(b<1024) return b + " B";
        else if(b<(1024*1024)) return b/(1024) + " KB";
        else if(b<(1024*1024*1024)) return b/(1024*1024) + " MB";
        else return b/(1024*1024*1024) + "GB";
    }
/*    private ImageIcon getIcon(int width,int height){
        Image img = ((Image)FileSystemView.getFileSystemView().getSystemIcon(file)).getScaledInstance(width, height, 0);
        return new ImageIcon(img);
    }
    private ImageIcon getIcon(){return getIcon(20,20);}
*/ 
    
    public void setSelected(boolean b){
        selectBox.setSelected(b);
    }
    public ArrayList<FileItem> getSubFiles(){
        String[] pl = file.list();
        ArrayList<FileItem> f = new ArrayList<>();
        for(String p : pl){
            f.add(new FileItem(file + "\\"+p));
        }
            return f;
    }



    //Right click menu stuff
    public void cut(){
        //TODO eventually fix this
        copy();
        file.deleteOnExit(); 
        this.setVisible(false);
    }

    public void copy(){
        List<File> f = new ArrayList<File>();
        f.add(file);
        ClipBoard.setContents(new FileTransferable(f),this);
    }

    public void open(){
        if(isDirectory){
            if(this.getParent() instanceof FileItem){
                ((FileSection)this.getParent()).changePath(path,true);
            }
        }else{
            try{Desktop.getDesktop().open(file);}
            catch(IOException e){System.err.println("IOException in opening file");}
        }
    }

    public void rename(){
        nameChangeBox.setText(name);
        nameChangeBox.setVisible(true);
        nameBox.setVisible(false); 
    }

    public void delete(){
        deleteFile(file);
        refresh();
        ((FileSection)this.getParent()).refresh();
    }

    private void deleteFile(File f){
        if(f.isDirectory()){
            for(File item : f.listFiles()){
                deleteFile(item);
            }
            f.delete();
        }else{f.delete();}
    }

    public class RightClickMenu extends JPopupMenu{
        
        
        private static JButton OpenButton;
        private static JButton CopyButton;
        private static JButton CutButton;
        private static JButton RenameButton;
        private static JButton DeleteButton;


        public RightClickMenu(){
            makeButtons();
        }
        protected void makeButtons(){
            
            OpenButton = new JButton("Open");
            CopyButton = new JButton("Copy");
            CutButton = new JButton("Cut");
            RenameButton = new JButton("Rename");
            DeleteButton = new JButton("Delete");


            this.add(OpenButton);
            this.add(CopyButton);            
            //this.add(CutButton);
            this.add(RenameButton);
            this.add(DeleteButton);   

            makeListeners();
        }

        protected void makeListeners(){
            OpenButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    open();
                }});
            CopyButton.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        copy();
                    }});
            CutButton.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cut();
                    }});
            RenameButton.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        rename();
                    }});
            DeleteButton.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        delete();
                    }});
           
        }

    }
 
    
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        
    }
    
    public static class FileTransferable implements Transferable {

        private List listOfFiles;

        public FileTransferable(List listOfFiles) {
            this.listOfFiles = listOfFiles;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.javaFileListFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.javaFileListFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return listOfFiles;
        }
    }


}