import javafx.scene.control.ToolBar;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseListener;
import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseMotionListener;
import static java.lang.Math.abs;

class Sketchpad extends Frame {
    class obj {
        int xi,yi,xj,yj,md,indexo;
        int [] xpol;
        int [] ypol;
        Color col;
        Point [] hit;
        //Note: s=start, e=end, sm = sketchpad mode, hits used for freehand
        obj(int sa, int sb, int ea, int eb, int sm, Color co, Point [] hits, int i, int x[], int y[]) {
            xi=sa; yi=sb; xj=ea; yj=eb; md=sm; col=co; hit=hits;indexo=i;xpol=x;ypol=y;}
    }
    ArrayList<obj> sketchlist = new ArrayList<>();
    ArrayList<obj> deletedBuff = new ArrayList<>();
    int x0,y0,mode=0;
    Color color = Color.BLACK;
    //Free Hand
    int index = 0;
    Point[] arr = new Point[100000];
    int [] xp = new int[20];
    int [] yp = new int[20];

    Sketchpad() {

        setSize(900,700);
        //setExtendedState(Frame.MAXIMIZED_BOTH);
        setTitle("SketchPad - SE3353 - HCI");
        setLayout(new FlowLayout());


        //Format option
        Button btnSave = new Button("Save");
        Button btnOpen = new Button("Open");
        Button btnUndo = new Button("Undo");
        Button btnRedo = new Button("Redo");
        Button btnClr = new Button("Clear All");
        Button btn2 = new Button("Selector");
        Button btn3 = new Button("Color Selector");

        //Drawing Options
        Button btnFree = new Button("Free Hand");
        Button btnLine = new Button("Line");
        Button btnRec = new Button("Rectangle");
        Button btnElps = new Button("Ellipse");
        Button btnSqr = new Button("Square");
        Button btnCir = new Button("Circle"); //rem
        Button btnPolc = new Button("Polygon");//rem

        Label options = new Label("Options:");
        options.setFont(new Font("Serif", Font.BOLD, 20));
        options.setPreferredSize(new Dimension(70, 40));

        Label modeL = new Label("Modes:");
        modeL.setFont(new Font("Serif", Font.BOLD, 20));
        modeL.setPreferredSize(new Dimension(70, 40));

        //Current Mode
        Label currMode = new Label("Selected: Line");
        //Label blank = new Label("");
        currMode.setFont(new Font("Serif", Font.BOLD, 20));
        //currMode.setSize(100,5);
        currMode.setPreferredSize(new Dimension(230, 40));

        MenuBar menu = new MenuBar();
        Menu save = new Menu("Save");
        Menu load = new Menu("Load");
        menu.add(save);
        menu.add(load);
        //setMenuBar(menu);

        PopupMenu Option = new PopupMenu();
        Menu delete = new Menu("Delete");
        Option.add(delete);

        Button btnDel = new Button("Delete");

        add(btnOpen);
        add(btnSave);
        add(options);
        add(btnUndo);
        add(btnRedo);
        add(btnClr);
        //add(btnDel);
        add(btn2);
        add(btn3);
        add(modeL);
        add(btnFree);
        add(btnLine);
        add(btnRec);
        add(btnElps);
        add(btnSqr);
        add(btnCir);
        add(btnPolc);
        add(currMode);

        //Save Graphics
        btnSave.addActionListener(e -> {
            Dimension d = getSize();
            BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            paintAll(g2d);

            try {
                if (ImageIO.write(image, "png", new File("./output_image.png")))
                {
                    System.out.println("-- image saved");
                    JOptionPane.showMessageDialog(this, "Image Saved as 'output_image.png' !");
                    File img = new File("./output_image.png");
                    Desktop.getDesktop().open(img);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        });

        /*btnOpen.addActionListener(e -> {
            BufferedImage imgInput = null;
            try {
                imgInput = ImageIO.read(new File("./input.jpg"));
                JOptionPane.showMessageDialog(this, "Image Input!");

            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Graphics b = imgInput.getGraphics();
            b.drawImage(imgInput, 0, 0, null);
            //this.paint(b);
            JLabel back = new JLabel();
            ImageIcon icon = new ImageIcon(imgInput);
            back.setIcon(icon);
            add(back);
            setVisible(true);
        });*/

        btnUndo.addActionListener( e -> {
            if(sketchlist.size()!= 0) {
                obj itemA = sketchlist.get(sketchlist.size()-1);
                deletedBuff.add(itemA);
                sketchlist.remove( sketchlist.size()-1);
            }
            repaint();
        });

        btnRedo.addActionListener(e -> {
            if(deletedBuff.size()!= 0) {
                obj itemB = deletedBuff.get(deletedBuff.size()-1);
                sketchlist.add(itemB);
                deletedBuff.remove( deletedBuff.size()-1);
            }
            repaint();
        });

        btnLine.addActionListener(e -> {mode=0; currMode.setText("Selected: Line");});
        btnRec.addActionListener(e -> {mode=1; currMode.setText("Selected: Rectangle");});
        btnElps.addActionListener(e -> {mode=2; currMode.setText("Selected: Ellipse");});
        btnSqr.addActionListener(e -> {mode=3; currMode.setText("Selected: Square");});
        btnCir.addActionListener(e -> {mode=4; currMode.setText("Selected: Circle");});
        btnFree.addActionListener(e -> {mode=5; currMode.setText("Selected: Free Hand");});
        btnPolc.addActionListener(e -> {mode=6; currMode.setText("Selected: Polygon C/O");});
        btn3.addActionListener(
                e -> {
                    color = JColorChooser.showDialog(this,"Select Color", color);
                    if(color==null) color = Color.BLACK;
                }
        );
        btnDel.addActionListener(e -> {mode=7; currMode.setText("Selected: Delete");});
        btnClr.addActionListener(e -> {
            sketchlist.clear();
            repaint();
        });

        addMouseListener( new MouseAdapter() {
            //@Override
            public void mousePressed(MouseEvent e){
                x0 = e.getX();
                y0 = e.getY();

                //Polygon Open or Closed
                if(mode==6){
                    xp[xp.length-1] = x0;
                    yp[yp.length-1] = y0;
                }
                sketchlist.add( new obj(x0,y0,x0,y0,mode,color,arr,index,xp,yp) );

                //Free hand
                if(mode==5){
                    arr[index] = new Point(e.getX(), e.getY());
                    index++;
                }

                //Delete
                /*if(mode==7){
                    int min, max;
                    min = -50;
                    max = 50;
                    sketchlist.forEach(l ->{
                        if(x0-l.xi <max && x0-l.xi > min && y0-l.yi <max && y0-l.yi > min){
                            sketchlist.remove(this);
                        }
                        repaint();
                    });
                }*/

            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            //@Override
            public void mouseDragged(MouseEvent e) {
                sketchlist.remove( sketchlist.size()-1 );
                sketchlist.add(new obj(x0,y0,e.getX(),e.getY(),mode,color,arr,index,xp,yp));

                if(mode==5){
                    arr[index] = new Point(e.getX(), e.getY());
                    index++;
                }
                repaint(); }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                arr = new Point[100000];
                index = 0;

                if(mode==6){
                    xp[xp.length-1] = x0;
                    yp[yp.length-1] = y0;
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });
    }

    public void paint(Graphics g){ sketchlist.forEach(l -> {
        if (l.md==5){
            g.setColor(l.col);
            for (int i = 0; i < l.indexo - 1; i++){
                g.drawLine(l.hit[i].x, l.hit[i].y, l.hit[i+1].x, l.hit[i + 1].y);
                //g.drawLine(l.xi,l.yi,l.xj,l.yj);
            }
        }
        //straight line
        else if (l.md==0) {
            g.setColor(l.col);
            g.drawLine(l.xi,l.yi,l.xj,l.yj);}
        //rectange
        else if(l.md == 1) {
            g.setColor(l.col);
            g.drawRect(l.xi,l.yi,(l.xj-l.xi),(l.yj-l.yi));}
        //ellipse
        else if(l.md == 2) {
            g.setColor(l.col);
            g.drawArc(l.xi,l.yi,(l.xj-l.xi),(l.yj-l.yi), 0,180);}
        //square
        else if(l.md == 3) {
            g.setColor(l.col);
            g.drawRect(l.xi,l.yi,(l.xj-l.xi),(l.xj-l.xi));}
        //circle
        else if(l.md == 4) {
            g.setColor(l.col);
            g.drawOval(l.xi,l.yi,(l.xj-l.xi),(l.xj-l.xi));}
        //polygon
        else if(l.md == 6) {
            g.setColor(l.col);
            g.drawPolygon(l.xpol,l.ypol,l.xpol.length);
        }

    }); }
    public static void main(String[] args){ new Sketchpad().setVisible(true); }
}

