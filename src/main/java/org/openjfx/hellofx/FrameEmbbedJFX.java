package org.openjfx.hellofx;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.awt.BorderLayout;
import javax.swing.JButton;

public class FrameEmbbedJFX extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FrameEmbbedJFX frame = new FrameEmbbedJFX();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FrameEmbbedJFX() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.EAST);
		
		JButton btnNewButton_1 = new JButton("New button");
		panel.add(btnNewButton_1);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.SOUTH);
		
		final JFXPanel fxPanel = new JFXPanel();
        // This method is invoked on the JavaFX thread
    	VisualisationMoteurAvecGroup sc = new VisualisationMoteurAvecGroup();
    	Pane pane = sc.createScene();
        Scene scene = new Scene(pane, 400, 300);
        fxPanel.setScene(scene);
		contentPane.add(fxPanel, BorderLayout.CENTER);
		
		JButton btnNewButton = new JButton("New button");
		panel_1.add(btnNewButton);
	}

}
