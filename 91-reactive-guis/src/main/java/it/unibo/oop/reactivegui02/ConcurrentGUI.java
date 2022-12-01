package it.unibo.oop.reactivegui02;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.script.ScriptEngine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.DimensionUIResource;

/**
 * Second example of reactive GUI.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class ConcurrentGUI extends JFrame {

    private static final double WIDTH_PERC = 0.4;
    private static final double HEIGHT_PERC = 0.1;

    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up"); 
    private final JButton down = new JButton("down"); 
    private final JButton stop = new JButton("stop");

    
    public ConcurrentGUI(){
        
        super("Concurrent GUI");
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(new Dimension((int)(screenSize.getWidth() * WIDTH_PERC) ,(int)(screenSize.getHeight() * HEIGHT_PERC)));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        getContentPane().add(panel);
        setVisible(true); 

        final Agent agent = new Agent();
        /* Adding the ActionListeners */
        up.addActionListener(e -> agent.setCountingStrategy(CountingUtilities::increment));
        down.addActionListener(e -> agent.setCountingStrategy(CountingUtilities::decrement));
        stop.addActionListener(e -> agent.stop());
        
        
        new Thread(agent).start();
        
    }

    private class CountingUtilities {

        public static int increment(final int arg0){
            return arg0 + 1;
        }

        public static int decrement(final int arg0){
            return arg0 - 1;
        }
    }

    private class Agent implements Runnable{

        private volatile boolean stop;
        private int count = 0;

        private UnaryOperator<Integer> countingStrategy = UnaryOperator.identity();

        public void setCountingStrategy(UnaryOperator<Integer> countingStrategy) {
            this.countingStrategy = countingStrategy;
        }

        public Agent(){
            this.stop = false;
        }        

        @Override
        public void run() {
            while(!isStopped()){
                try {
                   // Letting the EDT updating the view  
                   SwingUtilities.invokeAndWait(() ->  ConcurrentGUI.this.display.setText(Integer.toString(getCount())));
                   updateCounter(countingStrategy);
                   Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean isStopped(){
            return this.stop;
        }

        public void stop(){
            this.stop = true;
        }
        
        private void updateCounter(final UnaryOperator<Integer> countingStrategy){
            this.count = countingStrategy.apply(this.count);
        }

        public int getCount(){
            return this.count;
        }

    }

}
