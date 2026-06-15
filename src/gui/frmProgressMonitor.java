package gui;

import javax.swing.*;

import lang.Language;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class frmProgressMonitor {

	private final JDialog dialog;
	private final JProgressBar progressBar;
	private final JLabel messageLabel;
	private final JLabel noteLabel;
	private final JButton cancelButton;

	private volatile boolean canceled = false;
	private int min;
	private int max;

	public frmProgressMonitor(Frame owner, String message, String note, int min, int max, Language language) {
		this.min = min;
		this.max = max;

		dialog = new JDialog(owner, true);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.setResizable(false);
		dialog.setTitle(language.message.getString("info.progress"));
		dialog.setIconImage(owner.getIconImage());

		// Components
		messageLabel = new JLabel(message);
		noteLabel    = new JLabel(note != null ? note : " ");
		progressBar  = new JProgressBar(min, max);
		cancelButton = new JButton(language.base.getString("button.cancel"));
		
		// Apply font
		dialog.setFont(language.font);
		messageLabel.setFont(language.font);
		noteLabel.setFont(language.font);
		cancelButton.setFont(language.font);

		progressBar.setStringPainted(true);
		progressBar.setValue(min);

		cancelButton.addActionListener(e -> {
			canceled = true;
			// do not dispose automatically; let caller decide via close()
		});

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Treat closing the window as cancel
				canceled = true;
			}
		});

		// Layout
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
		textPanel.add(messageLabel);
		textPanel.add(noteLabel);

		JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
		bottomPanel.add(progressBar, BorderLayout.CENTER);
		bottomPanel.add(cancelButton, BorderLayout.EAST);

		mainPanel.add(textPanel, BorderLayout.NORTH);
		mainPanel.add(bottomPanel, BorderLayout.CENTER);

		dialog.setContentPane(mainPanel);
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
	}

	// Show the dialog (call from EDT)
	public void show() {
		dialog.setVisible(true);
	}

	public void setMaximum(int max) {
		this.max = max;
		SwingUtilities.invokeLater(() -> progressBar.setMaximum(max));
	}

	public void setProgress(int value) {
		final int v = value;
		SwingUtilities.invokeLater(() -> {
			progressBar.setValue(v);
			progressBar.setString(v + " / " + max);
		});
	}

	public void setNote(String note) {
		final String n = (note != null ? note : " ");
		SwingUtilities.invokeLater(() -> noteLabel.setText(n));
	}
	
	public void setMessage(String message) {
		final String n = (message != null ? message : " ");
		SwingUtilities.invokeLater(() -> messageLabel.setText(n));
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void close() {
		SwingUtilities.invokeLater(() -> {
			dialog.setVisible(false);
			dialog.dispose();
		});
	}

}