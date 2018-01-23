/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI.Components;

import GUI.Storage.Grid;
import GUI.Storage.Point;
import ImageProcessing.ImageTools;
import ImageProcessing.PixelFormation;
import java.awt.image.BufferedImage;
import java.util.List;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;


/**
 *
 * @author 160471
 */
public class CropPanel {

    private Canvas CANVAS;
    private boolean GEN_FRAME = true;
    private Paint BACK_COLOUR = Paint.valueOf("WHITE");
    private Image ORIGINAL;
    private Image DOCUMENT;
    private double X_OFF =0;
    private double Y_OFF =0;
    private double SCALE= 1.0;
    
    private int RAT_X;
    private int RAT_Y;
    
    private Point prev = null;
    
    private Rectangle CROP_BOUND;
    
    private boolean moveCrop = false;
    
    List<PixelFormation> FORMATIONS = null;
    PixelFormation PRIMARY_FORM = null;
    
    public CropPanel(Image i, Canvas canv, int ratX, int ratY) {
        RAT_X = ratX;
        RAT_Y = ratY;
        
        
        CANVAS = canv;
        ORIGINAL = i;
        DOCUMENT = ORIGINAL;
        
        double height = CANVAS.getHeight() *0.8;
        double width = (height/ratY)*ratX;
        CROP_BOUND = new Rectangle((CANVAS.getWidth()/2)-(width/2),(CANVAS.getHeight()/2)-(height/2),width,height);
        
        
        addListeners();
        tick();
        
        
    }
    
    public void setScale(double scale){
        SCALE = scale;
        tick();
    }
    
    public void fitFormation(PixelFormation p){
        Rectangle b = p.getBounds();
        Rectangle r;
        double xOff = 0;
        double yOff =0;
        
        double x=0;
        if(b.getWidth()> b.getHeight())  {
            x = b.getWidth();
            yOff = (x-b.getHeight())/2.0;
        } 
        else {
            x = b.getHeight();
            xOff = (x-b.getWidth())/2.0;
        }
        r= new Rectangle(b.getX()-xOff,b.getY()-yOff,x,x);
        
        CROP_BOUND = new Rectangle((this.X_OFF+r.getX())*SCALE, (this.Y_OFF+r.getY())*SCALE, r.getWidth()*SCALE, r.getHeight()*SCALE);
    }   
    
    public void setBackgroundColour(Paint c){
        BACK_COLOUR = c;
        this.tick();
    }
   
    public void loadImage(Image img){
        this.DOCUMENT = img;
        this.ORIGINAL = img;
        System.out.println("Converting to B&W");
       
        tick();
    }
    
    public Image getImage(){
        double x = ( (this.CROP_BOUND.getX()-(this.X_OFF*SCALE))) /SCALE;
        double y =  ((this.CROP_BOUND.getY()- (this.Y_OFF*SCALE)))/SCALE;
        double width = this.CROP_BOUND.getWidth()/SCALE;
        double height = this.CROP_BOUND.getHeight()/SCALE;
        
       return ImageTools.convertBuffered(ImageTools.cropImage(this.DOCUMENT, (int)x, (int)y, (int)width, (int)height));
       // return this.DOCUMENT;
      
    }
    

    
    public void modifyImg(int threshold, boolean binarise, boolean detectForms){
        
        if(binarise){
        BufferedImage toRender = ImageTools.convertImgToBuf(ORIGINAL);
       
        toRender = ImageTools.toGreyScale(toRender, true, threshold);
        DOCUMENT = ImageTools.convertBuffered(toRender);
          
        if(detectForms) {
            System.out.println("Finding Forms");
            FORMATIONS = ImageTools.findEnclosedPixels(toRender);
        }
        }
        else{
            DOCUMENT = ORIGINAL;
        }
        tick();
    }
   
    
    private PixelFormation findFormIntercept(MouseEvent m){
        
        for(PixelFormation p : FORMATIONS) {
            Rectangle b = p.getBounds();
            if(this.pointIntersects(m.getX()/SCALE - X_OFF, m.getY()/SCALE - Y_OFF, b.getX(), b.getY(), b.getWidth(), b.getHeight())) return p;
        }
        return null;
            
        
    }
    

    private void addListeners() {
         CANVAS.setOnMouseClicked(new EventHandler<MouseEvent>() {
    @Override
    public void handle(MouseEvent m) {
        PixelFormation clickedForm = findFormIntercept(m);
        
        if(clickedForm != null){
        if(m.getClickCount() == 1){
            PRIMARY_FORM = clickedForm;
            
            tick();
        }
        if(m.getClickCount() == 2){
            PRIMARY_FORM = clickedForm;
            fitFormation(PRIMARY_FORM);
            tick();
        }
        }
    }
});
        
        
         CANVAS.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                if(!(prev == null)){
                    
                    if(moveCrop){
                        CROP_BOUND.setX(CROP_BOUND.getX()+(event.getX() - prev.getX()));
                        CROP_BOUND.setY(CROP_BOUND.getY()+(event.getY() - prev.getY()));
                    }
                    else{
                        X_OFF += (event.getX() - prev.getX());
                        Y_OFF += (event.getY() - prev.getY()); 
                    }
                    
                   
                }
               
                
                prev = new Point(event.getX(),event.getY());
               
                tick();
            }
        });
         
         CANVAS.addEventHandler(MouseEvent.MOUSE_RELEASED,
                new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                System.out.println("Mouse released");
               moveCrop = false;
               prev = null;
               tick();
            }
        });
         
         CANVAS.addEventHandler(MouseEvent.MOUSE_PRESSED,
                new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                System.out.println("Starting");
                boolean intersectsDoc = pointIntersects(event.getX(),event.getY(), X_OFF, Y_OFF, DOCUMENT.getWidth()*SCALE, DOCUMENT.getHeight()*SCALE);
                boolean intersectsCrop = pointIntersects(event.getX(),event.getY(), CROP_BOUND.getX(), CROP_BOUND.getY(), CROP_BOUND.getWidth(), CROP_BOUND.getHeight());
              if(intersectsCrop) moveCrop = true;
              if (intersectsDoc && !intersectsCrop) moveCrop = false;
              
              
            }
        });
       
        
      
        CANVAS.setOnScroll(new EventHandler() {
            @Override
            public void handle(Event event) {
                ScrollEvent s = (ScrollEvent) event;
                
              X_OFF +=s.getDeltaX();
                Y_OFF += s.getDeltaY();
                
                tick();
               
            }
    });
       
        /*
        CANVAS.getParent().getScene().widthProperty().addListener(new ChangeListener<Number>() {
    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
        System.out.println("Width: " + newSceneWidth);
    }
});*/

       
    }
    
    private boolean pointIntersects(double xp, double yp, double xi, double yi, double width, double height){
        if((xp>xi)&&(yp>yi)&&(xp<xi+width)&&(yp<yi+height)) return true;
        
        return false;
    }

    private void tick() {
        if (GEN_FRAME) {

            GraphicsContext g = CANVAS.getGraphicsContext2D();
            
            g.setFill(this.BACK_COLOUR);
           
          
            g.fillRect(0, 0,CANVAS.getWidth(), CANVAS.getHeight());
            
            drawImage(g);
            if(this.prev != null && !this.moveCrop){
                g.setStroke(Color.TRANSPARENT);
                g.setFill(Color.rgb(0, 100,125, 0.5));
                this.drawRect(g, new Rectangle(this.X_OFF*SCALE, this.Y_OFF*SCALE, DOCUMENT.getWidth()*SCALE, DOCUMENT.getHeight()*SCALE));
            }
            
            drawCropArea(g, this.CROP_BOUND);
           
            if(this.FORMATIONS !=null) {
                this.drawFormationBounds(g, FORMATIONS);
            }
        }
    }
    
    public void drawImage(GraphicsContext g){
        
           g.scale(SCALE, SCALE);
            g.drawImage(DOCUMENT, this.X_OFF,this.Y_OFF);
             g.scale(1.0/SCALE, 1.0/SCALE); //reverts scale back to original
    }
    
    public void drawRect(GraphicsContext g, Rectangle rect){
        g.beginPath();
        g.lineTo(rect.getX(), rect.getY());
        g.lineTo(rect.getX()+rect.getWidth(), rect.getY());
        g.lineTo(rect.getX()+rect.getWidth(), rect.getY()+rect.getHeight());
        g.lineTo(rect.getX(), rect.getY()+rect.getHeight());
        g.lineTo(rect.getX(), rect.getY());
        g.stroke();
        g.fill();
    }
    
    private void drawFormationBounds(GraphicsContext g, List<PixelFormation> formation){
        if(PRIMARY_FORM !=null){/*
        g.setStroke(Paint.valueOf("Black"));
        Rectangle r = this.PRIMARY_FORM.getBounds();
        this.drawRect(g, new Rectangle((this.X_OFF+r.getX())*SCALE, (this.Y_OFF+r.getY())*SCALE, r.getWidth()*SCALE, r.getHeight()*SCALE)); */
        } else{
        g.setStroke(Paint.valueOf("Grey"));
        for(PixelFormation f : formation) {
          
             Rectangle r = f.getBounds();
             this.drawRect(g, new Rectangle((this.X_OFF+r.getX())*SCALE, (this.Y_OFF+r.getY())*SCALE, r.getWidth()*SCALE, r.getHeight()*SCALE));
        }}
        
    }
    
    
    private  void drawPixelFormations(GraphicsContext g, List<PixelFormation> pF){
      for(PixelFormation p: pF) for(java.awt.Point po : p.getPoints()) g.getPixelWriter().setColor(po.x, po.y, Color.BLUE);
        
    }
    
    
    public void drawCropArea(GraphicsContext g, Rectangle rect){
        
        g.setStroke(Color.BLACK);
        
        if(moveCrop)g.setFill(Color.rgb(0, 100,125, 0.5));
        else g.setFill(Color.TRANSPARENT);
        
        g.setLineWidth(3.0);
        g.setLineDashes(10);
        
        drawRect(g, rect);
        
        
        
        
    }
   
}
