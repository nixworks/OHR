/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import GUI.Components.DrawingGrid;
import InterfaceManagement.TabAttributes;
import ImageProcessing.ImageTools;
import InterfaceManagement.ControllerInterface;
import InterfaceManagement.SubControllerInterface;
import Util.Load;
import Util.Save;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import neural.Input;
import neural.Layer;
import neural.Neuron;
import neural.TrainingSet;
// meaning of everything

/**
 * FXML Controller class
 *
 * @author mathew
 */
public class NeuralNetInterfaceController extends TabAttributes<Layer> implements Initializable {

    //Tim
    @FXML
    BorderPane BORDER = new BorderPane();
    /*
    private String NAME = "UNTITLED.nns";
    private Tab TAB_INSTANCE;*/

    private Window WINDOW;

    private DrawingGrid DGRID;

    private Image LOADED_IMAGE = null;

    //FXML Nodes
    @FXML
    public ImageView IMAGE_VIEW = new ImageView();

    @FXML
    public Text OUT_LETTER = new Text();

    @FXML
    public Button CLOSE = new Button();

    @FXML
    public Canvas INPUT_PAD = new Canvas();

    @FXML
    public ChoiceBox CHARECTAR_SELECT = new ChoiceBox();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setFileType(".nns");
        setFileDes("Neural Net Struct");
        try {
            URL imageR = getClass().getResource("UpperCase.jpg");

            Image image = ImageTools.convertBuffered(ImageIO.read(imageR));
            IMAGE_VIEW.setImage(image);

        } catch (IOException ex) {
            Logger.getLogger(NeuralNetInterfaceController.class.getName()).log(Level.SEVERE, null, ex);
        }

        Platform.runLater(() -> WINDOW = this.INPUT_PAD.getScene().getWindow());
        FILE = new Layer();

        FILE.generateRandomNeurons(26, 900);
        FILE.setCharSet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");

        setGUI(this.FILE);

    }

    @Override
    public void setGUI(Layer l) {
        ObservableList<String> list = FXCollections.observableArrayList();
        for (Neuron n : this.FILE.NEURONS) {
            list.add(n.NAME);
        }
        CHARECTAR_SELECT.setItems(list);
        CHARECTAR_SELECT.getSelectionModel().selectFirst();
        DGRID = new DrawingGrid(30, 30, INPUT_PAD);

    }

   
   

    //Actions
    public void clear() {
        DGRID.clear();
        OUT.print("Clearing");
    }

    public void evaluate() {
        this.OUT.print("Evaluating");
        List<Double> out = this.FILE.forwardProp(new TrainingSet(this.DGRID.getOutput(), 0, this.FILE.NEURONS.size()));
        double biggest = out.get(0);
        int neuronPos = 0;
        for (int i = 1; i < out.size(); i++) {
            if (out.get(i) > biggest) {
                biggest = out.get(i);
                neuronPos = i;
            }
        }

        OUT_LETTER.setText(this.FILE.NEURONS.get(neuronPos).NAME);
        this.OUT.print("Recognised as " + this.FILE.NEURONS.get(neuronPos).NAME);

    }

    public void train() {
        // FILE.displayContents();
        int selection = this.getChoiceBox();
        OUT.print("Selected " + this.FILE.NEURONS.get(selection));
        this.FILE.backwardProp(new TrainingSet(this.DGRID.getOutput(), selection, this.FILE.NEURONS.size()));
        this.setModified();
    }

    private int getChoiceBox() {
        return this.CHARECTAR_SELECT.getSelectionModel().getSelectedIndex();

    }

    public void loadImage() throws InterruptedException {
        System.out.println("Loading Image");
        SubWindowLoader wind = new SubWindowLoader("CropWindow.fxml", WINDOW);
        wind.show();
        Runnable r = new Runnable() {
            public void run() {

                try {
                    LOADED_IMAGE = (Image) wind.getReturn();
                } catch (Exception e) {
                    OUT.print("Failed to retrieve photo, what did you do?!");
                }

                if (LOADED_IMAGE != null) {
                    DGRID.setContents(ImageTools.imageToBinaryGrid(ImageTools.convertImgToBuf(LOADED_IMAGE), 30, 30, 127).getList());
                } else {
                    OUT.print("Failed to retrieve photo, what did you do?!");
                }

                Platform.runLater(() -> IMAGE_VIEW.setImage(LOADED_IMAGE));
                System.out.println("Set Image");

            }
        };
        new Thread(r).start();
        // Platform.runLater(r);

    }

    /*
    @Override
    public void closeTab() {
      
        if(!SAVED ){
        save();}
       
        TAB_INSTANCE.getTabPane().getTabs().remove(TAB_INSTANCE);
       
        
    } */

    

    /*
    @Override
    public void setText(String title) {
        this.TAB_INSTANCE.setText(title);
        this.NAME = title;
    } */

    public void setContextMenu() {
        ContextMenu m = new ContextMenu();

        // MenuItem mi = new MenuItems("Close");
        // m.getItems()
        //this.TAB_INSTANCE.
    }

    /*
    @Override
    public void setTab(Tab t) {
        this.TAB_INSTANCE = t;
        this.TAB_INSTANCE.setClosable(true);
        this.TAB_INSTANCE.setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                closeTab();
            }
        });
        
                
        Resource r = new Resource("NeuralTab.fxml");

        try {
            t.setGraphic(r.getNode());
        } catch (IOException ex) {
            System.out.println("FAILED TO SET GRAPHICS");
        }
    }*/
    public void setModified() {
        SAVED = false;
    }

    private boolean saveMenu() {
        String[] buttons = {"Yes", "No"};
        int returnValue = JOptionPane.showOptionDialog(null, "Oh dear", "Would you like to save", JOptionPane.WARNING_MESSAGE, 0, null, buttons, buttons[0]);
        if (returnValue == 0) {
            return true;
        } else {
            return false;
        }
    }

    public void peek() {
        OUT.print("Peeking");
        List<Double> toSet = new ArrayList<Double>();
        for (Input i : this.FILE.NEURONS.get(this.getChoiceBox()).inputs) {
            toSet.add(i.getWeight());
        }

        double largestVal = toSet.get(0);
        double lowestVal = toSet.get(0);
        for (double d : toSet) {
            if (d > largestVal) {
                largestVal = d;
            }
        }
        for (double d : toSet) {
            if (d < lowestVal) {
                lowestVal = d;
            }
        }

        for (int i = 0; i < toSet.size(); i++) {

            if (toSet.get(i) > 0) {
                toSet.set(i, (toSet.get(i) + Math.abs(lowestVal)) / (largestVal - lowestVal));
            } else {
                toSet.set(i, (Math.abs(lowestVal) - Math.abs(toSet.get(i))) / (largestVal - lowestVal));
            }

        }
        //  for(Double d: toSet) System.out.println("New "+d);

        this.DGRID.setContents(toSet);

    }

    public void endPeek() {
        this.DGRID.clear();
    }

}
