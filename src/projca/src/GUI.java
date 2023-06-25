package projca.src;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.io.PrintStream;

public class GUI {

	public GUI() {
		// TODO Auto-generated constructor stub
	}
	
	public static class PipelineExecutionGUI extends JFrame {
	    private JTextArea textArea;

	    public PipelineExecutionGUI() {
	    	 setTitle("Pipeline Execution Output");
	         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	         setPreferredSize(new Dimension(600, 400));

	         textArea = new JTextArea();
	         textArea.setEditable(false);
	         textArea.setBackground(new Color(200,162,180));
	         textArea.setForeground(Color.black);
	         textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

	         JScrollPane scrollPane = new JScrollPane(textArea);
	         getContentPane().setBackground(Color.DARK_GRAY);
	         add(scrollPane);
	         
	         
	         JButton button = new JButton("Done");
	         setLayout(new BorderLayout());
	         add(button, BorderLayout.SOUTH);
	        // add(textArea, BorderLayout.NORTH);
	         add(scrollPane, BorderLayout.CENTER);
	         
	         
	         
	         button.addActionListener(new ActionListener() {
	             @Override
	             public void actionPerformed(ActionEvent e) {
	                 // Exit the application
	                 System.exit(0);
	             }
	         });
	         

	         pack();
	         setLocationRelativeTo(null);

	        // Redirect console output to the text area
	        PrintStream printStream = new PrintStream(new ConsoleOutputStream(textArea));
	        System.setOut(printStream);
	        System.setErr(printStream);
	    }

	    public static void main(String[] args) {
	        SwingUtilities.invokeLater(() -> {
	            PipelineExecutionGUI gui = new PipelineExecutionGUI();
	            gui.setVisible(true);
	            // Call your PipelineExecuteCycle() method here
	            gui.PipelineExecuteCycle();
	        });
	        
	    }

	    // Example method to simulate console output
	    public void PipelineExecuteCycle() {
	    	readfile f = new readfile();
	    	f.mainForGui();
	    	
	    }

	    // Custom OutputStream to redirect console output to the text area
	    private static class ConsoleOutputStream extends OutputStream {
	        private final JTextArea textArea;

	        public ConsoleOutputStream(JTextArea textArea) {
	            this.textArea = textArea;
	        }

	        @Override
	        public void write(int b) {
	            SwingUtilities.invokeLater(() -> textArea.append(String.valueOf((char) b)));
	        }
	    }
	}


}
