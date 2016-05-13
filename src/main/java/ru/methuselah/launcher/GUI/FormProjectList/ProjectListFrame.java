package ru.methuselah.launcher.GUI.FormProjectList;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import ru.methuselah.launcher.Data.OfflineProject;
import ru.methuselah.launcher.GUI.Controls.TransparentPanel;
import ru.methuselah.launcher.Launcher;
import ru.methuselah.launcher.Utilities;
import ru.methuselah.securitylibrary.Data.Launcher.LauncherAnswerProjects;

public final class ProjectListFrame extends JFrame
{
	private static final Color background = Color.DARK_GRAY;
	private final JList projectsList = new JList();
	private final JCheckBox checkSwitchToProject = new JCheckBox("<html>Автоматически переключаться на выбранный проект<br/>при следующем запуске</html>");
	private final JButton btnSwitchToProject = new JButton("Переключиться на выбранный проект");
	private static class ProjectCellRenderer extends JLabel implements ListCellRenderer
	{
		private static final ImageIcon imageStarEmpty = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Launcher.class.getResource("star_empty.png")));
		private static final ImageIcon imageStarSemi = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Launcher.class.getResource("star_semi.png")));
		private static final ImageIcon imageStarFull = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Launcher.class.getResource("star_full.png")));
		/*
		private static final ImageIcon imageStarEmptyHovered = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Launcher.class.getResource("star_empty_hover.png")));
		private static final ImageIcon imageStarSemiHovered = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Launcher.class.getResource("star_semi_hover.png")));
		private static final ImageIcon imageStarFullHovered = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Launcher.class.getResource("star_full_hover.png")));
		*/
		private static final String tipAuthToVote = "<html>Для оценки проектов необходимо <b>авторизоваться</b></html>";
		private static final String tipProjectDetails = "<html><b><font color=#0000ff>{PROJECT}</font></b><br />"
			+ "Онлайн: <b>{ONLINE}</b>&#47;<b>{SLOTS}</b> ({PINGTIME} назад)<br />"
			+ "Серверов: <b>{SERVERS}</b> (средний аптайм {UPTIME}%)<br />"
			+ "Средняя оценка: <b>{RATING}</b> ({VOTES} голосов)</html>";
		private final JLabel caption = new JLabel();
		private final JLabel website = new JLabel();
		private final JLabel[] stars = new JLabel[5];
		ProjectCellRenderer()
		{
			caption.setFont(new Font("Verdana", Font.BOLD, 12));
			caption.setForeground(Color.YELLOW);
			website.setForeground(Color.CYAN);
			stars[0] = new JLabel(imageStarEmpty);
			stars[1] = new JLabel(imageStarEmpty);
			stars[2] = new JLabel(imageStarEmpty);
			stars[3] = new JLabel(imageStarEmpty);
			stars[4] = new JLabel(imageStarEmpty);
			final GridBagLayout layout = new GridBagLayout();
			setLayout(layout);
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.weightx = 0.5;
			constraints.weighty = 0.5;
			constraints.insets = new Insets(0, 5, 0, 5);
			add(caption, constraints);
			constraints.gridy = 1;
			constraints.insets = new Insets(0, 10, 0, 10);
			add(website, constraints);
			final TransparentPanel starsContainer = new TransparentPanel(new FlowLayout(FlowLayout.RIGHT, 1, 0));
			starsContainer.add(stars[0]);
			starsContainer.add(stars[1]);
			starsContainer.add(stars[2]);
			starsContainer.add(stars[3]);
			starsContainer.add(stars[4]);
			starsContainer.setToolTipText(tipAuthToVote);
			constraints.insets = new Insets(0, 2, 0, 2);
			constraints.fill = GridBagConstraints.VERTICAL;
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.gridheight = 2;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;
			add(starsContainer, constraints);
		}
		@Override
		public Component getListCellRendererComponent(JList list, Object object, int index, boolean selected, boolean focused)
		{
			final OfflineProject project = (OfflineProject)object;
			setBorder(BorderFactory.createLineBorder(selected ? Color.LIGHT_GRAY : background));
			setBorder(BorderFactory.createLineBorder(selected ? Color.LIGHT_GRAY : background));
			setBackground(selected ? Color.YELLOW : background);
			// caption.setForeground(selected ? Color.BLUE : Color.RED);
			caption.setText("<html><b>" + project.caption + "</b></html>");
			website.setText("<html><a>Среднемесячный онлайн: " + project.averageOnlineTime + "%</a></html>");
			stars[0].setIcon(imageStarEmpty);
			stars[1].setIcon(imageStarEmpty);
			stars[2].setIcon(imageStarEmpty);
			stars[3].setIcon(imageStarEmpty);
			stars[4].setIcon(imageStarEmpty);
			float numberOfStars = project.rating * 5.0f / LauncherAnswerProjects.maximumAvailableRating;
			if(numberOfStars > 0.35f)
				stars[0].setIcon((numberOfStars > 0.65f) ? imageStarFull : imageStarSemi);
			if(numberOfStars > 1.35f)
				stars[1].setIcon((numberOfStars > 1.65f) ? imageStarFull : imageStarSemi);
			if(numberOfStars > 2.35f)
				stars[2].setIcon((numberOfStars > 2.65f) ? imageStarFull : imageStarSemi);
			if(numberOfStars > 3.35f)
				stars[3].setIcon((numberOfStars > 3.65f) ? imageStarFull : imageStarSemi);
			if(numberOfStars > 4.35f)
				stars[4].setIcon((numberOfStars > 4.65f) ? imageStarFull : imageStarSemi);
			final String detailedTooltip = tipProjectDetails
				.replace("{PROJECT}", project.caption)
				.replace("{RATING}", String.format("%3.2f", project.rating))
				.replace("{VOTES}", "79")
				.replace("{SERVERS}", "2")
				.replace("{UPTIME}", "98.5")
				.replace("{ONLINE}", "11")
				.replace("{SLOTS}", "450")
				.replace("{PINGTIME}", "12 мин");
			setToolTipText(detailedTooltip);
			return this;
		}
	}
	public ProjectListFrame(final Launcher launcher)
	{
		super("Выбор проекта");
		try
		{
			final URL favicon = Launcher.class.getResource("favicon.png");
			if(favicon != null)
				setIconImage(ImageIO.read(favicon));
		} catch(IOException ex) {
		}
		setMinimumSize(new Dimension(350, 250));
		setPreferredSize(new Dimension(500, 300));
		setResizable(true);
		final Container pane = getContentPane();
		projectsList.setBackground(background);
		projectsList.setCellRenderer(new ProjectCellRenderer());
		projectsList.setFixedCellHeight(28);
		projectsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		checkSwitchToProject.setForeground(Color.WHITE);
		checkSwitchToProject.setBackground(background);
		checkSwitchToProject.setSelected(true);
		checkSwitchToProject.setEnabled(false);
		btnSwitchToProject.setBackground(background);
		btnSwitchToProject.setEnabled(false);
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridy = 0;
		constraints.weightx = 0.5;
		constraints.weighty = 0.5;
		pane.add(projectsList, constraints);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridy = 1;
		constraints.weightx = 0.5;
		constraints.weighty = 0.0;
		pane.add(checkSwitchToProject, constraints);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridy = 2;
		constraints.weightx = 0.5;
		constraints.weighty = 0.0;
		pane.add(btnSwitchToProject, constraints);
		layout.setConstraints(pane, constraints);
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent we)
			{
				launcher.properties.data.lastOpenedProject = null;
				launcher.properties.saveToDisk();
				System.exit(0);
			}
		});
		projectsList.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent lse)
			{
				boolean selected = (projectsList.getSelectedIndex() >= 0);
				checkSwitchToProject.setEnabled(selected);
				btnSwitchToProject.setEnabled(selected);
			}
		});
		btnSwitchToProject.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				final ListModel model = projectsList.getModel();
				final OfflineProject project = (OfflineProject)model.getElementAt(projectsList.getSelectedIndex());
				launcher.properties.data.lastOpenedProject = (checkSwitchToProject.isSelected() ? project.code : "");
				launcher.properties.saveToDisk();
				launcher.onSwitchToProject(project);
				ProjectListFrame.this.setVisible(false);
			}
		});
	}
	public void setProjectsList(OfflineProject[] projects)
	{
		projectsList.setListData(projects);
		projectsList.clearSelection();
		checkSwitchToProject.setEnabled(false);
		btnSwitchToProject.setEnabled(false);
	}
	public void selectProjectIfExist(String code)
	{
		projectsList.clearSelection();
		checkSwitchToProject.setEnabled(false);
		btnSwitchToProject.setEnabled(false);
		if(Utilities.nonEmptyString(code))
		{
			final ListModel model = projectsList.getModel();
			int count = model.getSize();
			for(int index = 0; index < count; index += 1)
			{
				final OfflineProject project = (OfflineProject)model.getElementAt(index);
				if(project.code.equalsIgnoreCase(code))
				{
					projectsList.setSelectedIndex(index);
					checkSwitchToProject.setEnabled(true);
					btnSwitchToProject.setEnabled(true);
					break;
				}
			}
		}
	}
	public OfflineProject getSelectedProject()
	{
		final int index = projectsList.getSelectedIndex();
		return (index == -1) ? null : (OfflineProject)projectsList.getModel().getElementAt(index);
	}
}
