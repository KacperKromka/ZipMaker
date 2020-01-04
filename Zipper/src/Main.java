import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.*;

public class Main extends JFrame
{
    public Main()
    {
        this.setBounds(300,350, 200, 150);

        this.setDefaultCloseOperation(3);
        this.getContentPane();
        this.setTitle("Zipper");
        this.setJMenuBar(menuBar);

        JMenu menu = menuBar.add(new JMenu("File"));

        Action adding = new Akcja("Add", "Adding new files", "ctrl A");
        Action deleting = new Akcja("Delete", "Deleting files","ctrl D");
        Action zipping = new Akcja("Zip","Zip","Ctrl Z");

        JMenuItem menuAdd = menu.add(adding);
        JMenuItem menuDelete = menu.add(deleting);
        JMenuItem menuZip = menu.add(zipping);

        bAdd = new JButton(adding);
        bDelete = new JButton(deleting);
        bZip = new JButton(zipping);

        list.setBorder(BorderFactory.createEtchedBorder());
        GroupLayout layout = new GroupLayout(this.getContentPane());

        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(
                    layout.createSequentialGroup()
                .addComponent(scroll, 30,100, Short.MAX_VALUE)
                .addContainerGap(0, Short.MAX_VALUE)
                .addGroup(
                        layout.createParallelGroup().addComponent(bAdd).addComponent(bDelete).addComponent(bZip)
                )
        );

        layout.setVerticalGroup(
                layout.createParallelGroup()
                .addComponent(scroll, 30, 100, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup().addComponent(bAdd).addComponent(bDelete).addGap(5, 40, Short.MAX_VALUE).addComponent(bZip))
        );

        this.getContentPane().setLayout(layout);
        this.pack();



    }

private DefaultListModel listModel = new DefaultListModel()
{
    @Override
    public void addElement(Object obj)
    {
        list.add(obj);
        super.addElement(((File)obj).getName());
    }
    @Override
    public Object get(int index)
    {
        return list.get(index);
    }
    @Override
    public Object remove(int index)
    {
        list.remove(index);
        return super.remove(index);
    }

    ArrayList list = new ArrayList();
};
private JButton bAdd;
private JButton bDelete ;
private JButton bZip;
private JMenuBar menuBar = new JMenuBar();
private JList list = new JList(listModel);
private JFileChooser chooser = new JFileChooser();
private JScrollPane scroll = new JScrollPane(list);



    public static void main(String[] args) {
        new Main().setVisible(true);
    }

    private class Akcja extends AbstractAction
    {
        public Akcja(String name, String description, String acceleratorKey)
        {
            this.putValue(Action.NAME, name);
            this.putValue(Action.SHORT_DESCRIPTION, description);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(acceleratorKey));
        }
        public Akcja(String name, String description, String acceleratorKey, Icon icon)
        {
            this(name, description, acceleratorKey);
            this.putValue(Action.SMALL_ICON, icon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Add"))
                addToList();
            if (e.getActionCommand().equals("Delete"))
                deleteEntry();
            if (e.getActionCommand().equals("Zip"))
                makeZip();
        }
        private void addToList()
        {
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(true);

            int tmp = chooser.showDialog(rootPane, "Add to zip");

            if (tmp == JFileChooser.APPROVE_OPTION)
            {
                File[] sciezki = chooser.getSelectedFiles();

                for (int i = 0; i < sciezki.length ;i++)
                    if (!isEntryRepeated(sciezki[i].getPath()))
                      listModel.addElement(sciezki[i]);

            }
        }
        private boolean isEntryRepeated(String entryToTest)
        {
            for (int i = 0; i < listModel.getSize(); i++)
                if (((File)listModel.get(i)).getPath().equals(entryToTest))
                    return true;

            return false;
        }
        private void deleteEntry()
        {
            int[] tmp = list.getSelectedIndices();

            for (int i = 0; i < tmp.length; i++)
            {
                listModel.remove(tmp[i]-i);
            }
        }
        private void makeZip()
        {
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setSelectedFile(new File(System.getProperty("user.dir")+File.separator+"name.zip"));
            int tmp = chooser.showDialog(rootPane, "Compress");

            if (tmp == JFileChooser.APPROVE_OPTION)
            {
                byte tmpData[] = new byte[BUFFOR];
                try
                {
                    ZipOutputStream zOutS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(chooser.getSelectedFile()),BUFFOR));

                    for(int i = 0; i < listModel.getSize(); i++)
                    {
                        if (!((File)listModel.get(i)).isDirectory())
                            toZip(zOutS, (File)listModel.get(i), tmpData, ((File)listModel.get(i)).getPath());
                        else
                        {
                            wypiszSciezki((File)listModel.get(i));

                            for (int j = 0; j < listOfPath.size(); j++)
                                toZip(zOutS, (File)listOfPath.get(j), tmpData, ((File)listModel.get(i)).getPath());

                            listOfPath.removeAll(listOfPath);
                        }

                    }
                    zOutS.close();

                }
                catch(IOException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
        private void toZip(ZipOutputStream zOutS, File sciezkaPliku, byte[] tmpData, String sciezkaBazowa) throws IOException
        {
            BufferedInputStream inS = new BufferedInputStream(new FileInputStream(sciezkaPliku), BUFFOR);

            zOutS.putNextEntry(new ZipEntry(sciezkaPliku.getPath().substring(sciezkaBazowa.lastIndexOf(File.separator)+1)));

            int counter;
            while ((counter = inS.read(tmpData, 0, BUFFOR)) != -1)
                zOutS.write(tmpData, 0, counter);


            zOutS.closeEntry();

            inS.close();
        }
        public static final int BUFFOR = 1024;

        private void wypiszSciezki(File nazwaSciezki)
        {
            String[] nazwyPlikowIKatalogow = nazwaSciezki.list();

            for (int i = 0; i < nazwyPlikowIKatalogow.length; i++)
            {
                File p = new File(nazwaSciezki.getPath(), nazwyPlikowIKatalogow[i]);

                if (p.isFile())
                    listOfPath.add(p);

                if (p.isDirectory())
                    wypiszSciezki(new File(p.getPath()));

            }
        }

        ArrayList listOfPath = new ArrayList();
    }
}
