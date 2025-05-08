package fenetre;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Application permettant d'illustrer visuellement le fonctionnement des tableaux en Java
 * Montre la déclaration, l'allocation et l'affectation des valeurs avec deux méthodes
 */
public class TableauIllustrator extends JFrame {

	// ----- ATTRIBUTS PRINCIPAUX -----
	// Types disponibles pour les éléments du tableau
	private final String[] TYPES_ELEMENTS = {"int", "double", "String", "char", "boolean"};

	// Exemples de valeurs pour chaque type
	private Map<String, String[]> exemples = new HashMap<>();

	// État du tableau
	private String nomTableau = "tableau";
	private int taille = 4;
	private int methodeChoisie = 1;

	// ----- ÉLÉMENTS D'INTERFACE -----
	// Panels principaux
	private JPanel mainPanel;
	private JPanel illustrationPanel;
	private JPanel codePanel1; // Pour méthode 1
	private JPanel codePanel2; // Pour méthode 2
	private JPanel codeMainPanel; // Conteneur des deux panels de code

	// Contrôles pour configurer le tableau
	private JComboBox<String> typeComboBox;
	private JTextField nomTextField;
	private JSpinner tailleSpinner;
	private JRadioButton methode1Radio;
	private JRadioButton methode2Radio;

	// Éléments pour l'affichage et l'interaction
	private JTextField[] valeursFields; // Champs de saisie des valeurs
	private JLabel[] illustrationLabels; // Affichage des valeurs dans l'illustration
	private JPanel[] illustrationCasePanels; // Cases du tableau dans l'illustration

	// Scrollbars pour la navigation
	private JScrollPane scrollPane1;
	private JScrollPane scrollPane2;
	private JScrollPane illustrationScrollPane;

	// ----- EXÉCUTION ET ANIMATION -----
	private JButton executerButton;
	private JButton executerLigneButton;
	private JButton stopButton;
	private List<JPanel> codeLignes; // Lignes de code pour l'animation
	private Timer executionTimer;
	private int currentLineIndex = -1;
	private boolean enExecution = false;
	private boolean filtresActifs = false;
	private boolean updatingUI = false;
	
	
////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * STRUCTURE PRINCIPALE DU PROGRAMME
	 * Cette section contient les méthodes fondamentales pour l'initialisation et 
	 * le lancement de l'application : constructeur, initialisation des composants
	 * et point d'entrée du programme.
	 */
////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Point d'entrée du programme
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			new TableauIllustrator();
		});
	}
	
	/**
	 * Constructeur principal de l'application
	 */
	public TableauIllustrator() {
		setTitle("Initialisation de tableaux Java");
		setSize(950, 700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		// Initialisation des données et des composants
		initialiserExemples();
		initialiserComposants();

		setVisible(true);
	}

	/**
	 * Initialise les exemples de valeurs pour chaque type de données
	 */
	private void initialiserExemples() {
		exemples.put("String", new String[]{"Ceci", "est", "un", "exemple"});
		exemples.put("int", new String[]{"1", "2", "3", "4"});
		exemples.put("double", new String[]{"1.0", "2.5", "3.7", "4.2"});
		exemples.put("char", new String[]{"W", "S", "S", "A"});
		exemples.put("boolean", new String[]{"true", "false", "true", "false"});
	}

	/**
	 * Initialise tous les composants d'interface utilisateur
	 */
	private void initialiserComposants() {
		// Configuration du panel principal
		mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Création et ajout des sections d'interface
		JPanel methodePanel = creerPanelChoixMethode();
		JPanel controlsPanel = creerPanelControles();

		// Panels pour le code et l'illustration
		creerPanelsCode();
		creerPanelIllustration();

		// Organisation des panels dans l'interface
		mainPanel.add(controlsPanel, BorderLayout.NORTH);
		mainPanel.add(methodePanel, BorderLayout.CENTER);

		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(codeMainPanel, BorderLayout.NORTH);
		southPanel.add(illustrationScrollPane, BorderLayout.CENTER);
		mainPanel.add(southPanel, BorderLayout.SOUTH);

		getContentPane().add(mainPanel);

		// Gestion du redimensionnement
		ajouterEcouteurRedimensionnement();

		// Initialisation du timer pour l'exécution animée
		executionTimer = new Timer(400, e -> executerLigneSuivante());

		// Mise à jour initiale des panels
		mettreAJourPanels();
	}

////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * CRÉATION DES COMPOSANTS D'INTERFACE
	 * Ces méthodes sont responsables de la création et de l'organisation
	 * des différents éléments visuels de l'application.
	 */
	/**
	 * Crée le panel pour choisir entre les deux méthodes de création de tableau
	 */
////////////////////////////////////////////////////////////////////////////////////////////////

	private JPanel creerPanelChoixMethode() {
		JPanel methodePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		methode1Radio = new JRadioButton("Méthode 1: Déclaration + Allocation + Affectation", true);
		methode2Radio = new JRadioButton("Méthode 2: Initialisation directe");

		// Regrouper les boutons radio
		ButtonGroup group = new ButtonGroup();
		group.add(methode1Radio);
		group.add(methode2Radio);

		// Ajouter les boutons au panel
		methodePanel.add(methode1Radio);
		methodePanel.add(methode2Radio);

		// Ajouter les écouteurs d'événements
		methode1Radio.addActionListener(e -> {
			if (methode1Radio.isSelected()) {
				methodeChoisie = 1;
				mettreAJourPanels();
			}
		});

		methode2Radio.addActionListener(e -> {
			if (methode2Radio.isSelected()) {
				methodeChoisie = 2;
				mettreAJourPanels();
			}
		});

		return methodePanel;
	}

	/**
	 * Crée le panel contenant les contrôles pour configurer le tableau
	 */
	private JPanel creerPanelControles() {
		JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.X_AXIS));
		controlsPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

		// Panel pour le type
		JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JLabel typeLabel = new JLabel("Type des éléments:");
		typeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
		typeComboBox = new JComboBox<>(TYPES_ELEMENTS);
		typeComboBox.setFont(new Font("SansSerif", Font.BOLD, 14));
		typeComboBox.setPreferredSize(new Dimension(120, 30));

		typePanel.add(typeLabel);
		typePanel.add(typeComboBox);
		controlsPanel.add(typePanel);
		controlsPanel.add(Box.createHorizontalStrut(20));

		// Panel pour le nom
		JPanel nomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JLabel nomLabel = new JLabel("Nom du tableau:");
		nomLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
		nomTextField = new JTextField(nomTableau, 10);
		nomTextField.setFont(new Font("SansSerif", Font.BOLD, 14));
		nomTextField.setPreferredSize(new Dimension(150, 30));

		nomPanel.add(nomLabel);
		nomPanel.add(nomTextField);
		controlsPanel.add(nomPanel);
		controlsPanel.add(Box.createHorizontalStrut(20));

		// Panel pour la taille
		JPanel taillePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JLabel tailleLabel = new JLabel("Nombre de cases:");
		tailleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
		tailleSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 100, 1));
		tailleSpinner.setFont(new Font("SansSerif", Font.BOLD, 14));
		tailleSpinner.setPreferredSize(new Dimension(80, 30));

		// Personnalisation de l'éditeur du spinner
		JComponent editor = tailleSpinner.getEditor();
		if (editor instanceof JSpinner.DefaultEditor) {
			((JSpinner.DefaultEditor)editor).getTextField().setFont(new Font("SansSerif", Font.BOLD, 14));
		}

		taillePanel.add(tailleLabel);
		taillePanel.add(tailleSpinner);
		controlsPanel.add(taillePanel);

		// Ajouter les écouteurs de changement
		typeComboBox.addActionListener(e -> {
			reinitialiserExecution();
			mettreAJourPanels();
		});

		nomTextField.getDocument().addDocumentListener(new SimpleDocumentListener(this::reinitialiserExecution));
		nomTextField.getDocument().addDocumentListener(new SimpleDocumentListener(this::mettreAJourPanels));

		tailleSpinner.addChangeListener(e -> {
			reinitialiserExecution();
			mettreAJourPanels();
		});

		return controlsPanel;
	}

	/**
	 * Crée les panels qui afficheront le code pour les deux méthodes
	 */
	private void creerPanelsCode() {
		// Panel principal pour l'affichage du code
		codeMainPanel = new JPanel(new CardLayout());

		// Boutons d'exécution
		executerButton = new JButton("Exécuter tout");
		executerLigneButton = new JButton("Exécuter ligne");
		stopButton = new JButton("Stop");

		executerButton.addActionListener(e -> executerTout());
		executerLigneButton.addActionListener(e -> executerLigne());
		stopButton.addActionListener(e -> reinitialiserExecution());

		// Panel pour la méthode 1 (déclaration + allocation + affectation)
		codePanel1 = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				int height = getLayout().preferredLayoutSize(this).height + 20;
				return new Dimension(800, Math.max(255, height));
			}
		};
		codePanel1.setLayout(new BoxLayout(codePanel1, BoxLayout.Y_AXIS));
		codePanel1.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Code Java - Méthode 1"),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)
				));

		// Panel pour la méthode 2 (initialisation directe)
		codePanel2 = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				int width = getLayout().preferredLayoutSize(this).width + 20;
				return new Dimension(Math.max(255, width), 100);
			}
		};
		codePanel2.setLayout(new FlowLayout(FlowLayout.LEFT));
		codePanel2.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Code Java - Méthode 2"),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)
				));

		// Scrollpanes pour les deux panels de code
		scrollPane1 = creerScrollPaneCode(codePanel1, true);
		scrollPane2 = creerScrollPaneCode(codePanel2, false);

		// Ajout des boutons d'exécution
		JPanel buttonPanel1 = creerPanelBoutons(executerButton, executerLigneButton, stopButton);
		JPanel buttonPanel2 = creerPanelBoutons(
				new JButton("Exécuter tout") {{ addActionListener(e -> executerTout()); }},
				new JButton("Exécuter ligne") {{ addActionListener(e -> executerLigne()); }},
				new JButton("Stop") {{ addActionListener(e -> reinitialiserExecution()); }}
				);

		// Assemblage des panels
		JPanel container1 = new JPanel(new BorderLayout());
		container1.add(scrollPane1, BorderLayout.CENTER);
		container1.add(buttonPanel1, BorderLayout.SOUTH);

		JPanel container2 = new JPanel(new BorderLayout());
		container2.add(scrollPane2, BorderLayout.CENTER);
		container2.add(buttonPanel2, BorderLayout.SOUTH);

		codeMainPanel.add(container1, "1");
		codeMainPanel.add(container2, "2");
	}

	/**
	 * Crée un JScrollPane pour un panel de code
	 * @param panel Le panel à faire défiler
	 * @param vertical Utiliser un défilement vertical (true) ou horizontal (false)
	 * @return Le JScrollPane configuré
	 */
	private JScrollPane creerScrollPaneCode(JPanel panel, boolean vertical) {
		JScrollPane scrollPane = new JScrollPane(panel);

		if (vertical) {
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			scrollPane.setPreferredSize(new Dimension(800, panel.getPreferredSize().height));
		} else {
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			scrollPane.setPreferredSize(new Dimension(800, 150));
		}

		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

		return scrollPane;
	}

	/**
	 * Crée un panel avec des boutons alignés à droite
	 */
	private JPanel creerPanelBoutons(JButton... boutons) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		for (JButton bouton : boutons) {
			panel.add(bouton);
		}
		return panel;
	}

	/**
	 * Crée le panel d'illustration du tableau
	 */
	private void creerPanelIllustration() {
		illustrationPanel = new JPanel();
		illustrationPanel.setLayout(new BoxLayout(illustrationPanel, BoxLayout.Y_AXIS));
		illustrationPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Représentation du tableau"),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)
				));

		// Création du JScrollPane pour l'illustration
		illustrationScrollPane = new JScrollPane() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(800, 200);
			}
		};
		illustrationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		illustrationScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		illustrationScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		illustrationScrollPane.setViewportView(illustrationPanel);
	}

	/**
	 * Ajoute un écouteur pour gérer le redimensionnement de la fenêtre
	 */
	private void ajouterEcouteurRedimensionnement() {
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				scrollPane1.revalidate();
				scrollPane2.revalidate();
				illustrationScrollPane.revalidate();
			}
		});
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * UTILITAIRES D'INTERFACE GRAPHIQUE
	 * Fonctions auxiliaires pour la création et la manipulation
	 * des éléments d'interface graphique comme les labels et les champs de texte.
	 */
////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Crée une étiquette avec le style de code
	 * @param text Le texte à afficher
	 * @return Une JLabel formatée pour l'affichage de code
	 */
	private JLabel createCodeLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(new Font("Monospaced", Font.PLAIN, 16));
		return label;
	}
	
	/**
	 * Crée un champ de texte avec restrictions selon le type de données
	 * @param text Le texte initial
	 * @param columns Le nombre de colonnes
	 * @param type Le type de données
	 * @param index L'indice dans le tableau des champs
	 * @return Un JTextField configuré
	 */
	private JTextField createTypeRestrictedTextField(String text, int columns, String type, int index) {
		JTextField textField = new JTextField(text, columns);
		textField.setFont(new Font("Monospaced", Font.PLAIN, 16));

		// Ajout d'un écouteur pour réagir aux changements
		textField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
			if (!updatingUI && !enExecution && illustrationLabels != null && index < illustrationLabels.length) {
				// Vérification de validité si en mode d'exécution avec filtres actifs
				if (filtresActifs && enExecution) {
					boolean valide = estValeurValide(textField.getText(), type);
					textField.setBackground(valide ? Color.WHITE : new Color(255, 200, 200));
				}
			}
		}));

		return textField;
	}
	
	/**
	 * Crée un panneau contenant un champ de texte avec délimiteurs appropriés selon le type
	 * @param valeurDefaut La valeur par défaut à afficher
	 * @param type Le type de données (String, char, int, etc.)
	 * @param index L'indice dans le tableau des champs
	 * @return Un JPanel contenant le champ
	 */
	private JPanel createTypeFieldPanel(String valeurDefaut, String type, int index) {
	    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    panel.setBackground(null); // Hérite de la couleur du parent

	    // Création du champ de texte avec la valeur par défaut
	    // Si ce n'est pas la valeur par défaut, ajouter les délimiteurs à l'intérieur
	    String displayValue = valeurDefaut;
	    
	    if (type.equals("String") && !valeurDefaut.equals("null")) {
	        displayValue = "\"" + valeurDefaut + "\"";
	    } else if (type.equals("char") && !valeurDefaut.equals("\\u0000")) {
	        displayValue = "'" + valeurDefaut + "'";
	    }

	    // Création et ajout du champ de texte
	    JTextField textField = createTypeRestrictedTextField(displayValue, 10, type, index);
	    valeursFields[index] = textField;
	    panel.add(textField);

	    return panel;
	}
	
	/**
	 * Crée un filtre de document pour restreindre les entrées selon le type
	 * @param type Le type de données (String, char, int, etc.)
	 * @return Un DocumentFilter configuré
	 */
	private DocumentFilter createFilterForType(String type) {
	    return new DocumentFilter() {
	        // Définition des modèles de validation selon le type
	        private final Pattern intPattern = Pattern.compile("-?\\d*");
	        private final Pattern doublePattern = Pattern.compile("-?\\d*\\.?\\d*");
	        private final Pattern charPattern = Pattern.compile("'.'|\\\\u0000");  // 'c' ou \u0000
	        private final Pattern booleanPattern = Pattern.compile("true|false|");
	        private final Pattern stringPattern = Pattern.compile("\".*\"|null");  // "texte" ou null

	        @Override
	        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
	            if (isValidInput(fb, offset, string)) {
	                super.insertString(fb, offset, string, attr);
	            } else {
	                Toolkit.getDefaultToolkit().beep();
	            }
	        }

	        @Override
	        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
	            if (isValidInput(fb, offset, text)) {
	                super.replace(fb, offset, length, text, attrs);
	            } else {
	                Toolkit.getDefaultToolkit().beep();
	            }
	        }

	        /**
	         * Vérifie si l'entrée est valide selon le type de données
	         */
	        private boolean isValidInput(FilterBypass fb, int offset, String text) {
	            try {
	                String futureText = getFutureText(fb, offset, 0, text);

	                switch (type) {
	                case "int":
	                    return intPattern.matcher(futureText).matches();
	                case "double":
	                    return doublePattern.matcher(futureText).matches();
	                case "char":
	                    // Autoriser soit '\u0000', soit un seul caractère entre apostrophes
	                    if (futureText.equals("\\u0000")) return true;
	                    return charPattern.matcher(futureText).matches() || 
	                           (futureText.startsWith("'") && futureText.length() <= 3);
	                case "boolean":
	                    return booleanPattern.matcher(futureText).matches();
	                case "String":
	                    // Autoriser "null" ou du texte entre guillemets
	                    if (futureText.equals("null")) return true;
	                    return stringPattern.matcher(futureText).matches() || 
	                           (futureText.startsWith("\"") && (futureText.length() == 1 || 
	                            !futureText.substring(1).contains("\"")));
	                default:
	                    return true;
	                }
	            } catch (BadLocationException e) {
	                return false;
	            }
	        }

	        /**
	         * Calcule le texte résultant après modification
	         */
	        private String getFutureText(FilterBypass fb, int offset, int length, String text) throws BadLocationException {
	            Document doc = fb.getDocument();
	            String currentText = doc.getText(0, doc.getLength());
	            return currentText.substring(0, offset) + text + currentText.substring(offset + length);
	        }
	    };
	}

////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * MISE À JOUR DE L'INTERFACE
	 * Ces méthodes gèrent la mise à jour dynamique des panneaux
	 * en fonction des paramètres choisis par l'utilisateur.
	 */
////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Met à jour les panels selon les paramètres actuels
	 */
	private void mettreAJourPanels() {
		if (updatingUI) return;

		updatingUI = true;
		try {
			String type = (String) typeComboBox.getSelectedItem();
			String nom = nomTextField.getText();
			int taille = (Integer) tailleSpinner.getValue();
			this.taille = taille;

			String[] valeurDefauts = exemples.get(type);

			codeLignes = new ArrayList<>();

			// Mettre à jour le panel selon la méthode choisie
			if (methodeChoisie == 1) {
				updateMethode1Panel(type, nom, taille, valeurDefauts);
				((CardLayout) codeMainPanel.getLayout()).show(codeMainPanel, "1");
			} else {
				updateMethode2Panel(type, nom, taille, valeurDefauts);
				((CardLayout) codeMainPanel.getLayout()).show(codeMainPanel, "2");
			}

			// Mettre à jour le panel d'illustration
			updateIllustrationPanel(type, nom, taille, valeurDefauts);

			// Mise à jour dynamique des scrollpanes
			SwingUtilities.invokeLater(() -> {
				scrollPane1.revalidate();
				scrollPane2.revalidate();
				illustrationScrollPane.revalidate();
			});
		} finally {
			updatingUI = false;
		}
	}
	
	/**
	 * Met à jour le panneau d'affichage pour la méthode 1 (déclaration + allocation + affectation)
	 * @param type Le type des éléments du tableau
	 * @param nom Le nom du tableau
	 * @param taille La taille du tableau
	 * @param valeurDefauts Les valeurs par défaut à afficher
	 */
	private void updateMethode1Panel(String type, String nom, int taille, String[] valeurDefauts) {
		// Nettoyage des composants existants
		codePanel1.removeAll();
		codeLignes.clear();

		// Création de la ligne de déclaration du tableau
		JPanel ligne1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ligne1.add(createCodeLabel(type + "[] "));
		ligne1.add(createCodeLabel(nom));
		ligne1.add(createCodeLabel(" = new "));
		ligne1.add(createCodeLabel(type));
		ligne1.add(createCodeLabel("["));
		ligne1.add(createCodeLabel(String.valueOf(taille)));
		ligne1.add(createCodeLabel("];"));
		codePanel1.add(ligne1);
		codeLignes.add(ligne1);

		// Création des lignes d'affectation pour chaque élément du tableau
		valeursFields = new JTextField[taille];
		for (int i = 0; i < taille; i++) {
			JPanel ligne = new JPanel(new FlowLayout(FlowLayout.LEFT));
			ligne.add(createCodeLabel(nom + "[" + i + "] = "));

			// Récupération d'une valeur par défaut si disponible
			String valeurDefaut = (i < valeurDefauts.length) ? valeurDefauts[i] : getDefaultValue(type);

			// Création du champ de saisie avec délimiteurs en fonction du type
			JPanel fieldPanel = createTypeFieldPanel(valeurDefaut, type, i);
			ligne.add(fieldPanel);

			ligne.add(createCodeLabel(";"));
			codePanel1.add(ligne);
			codeLignes.add(ligne);
		}

		// Mise à jour de l'affichage
		codePanel1.revalidate();
		codePanel1.repaint();
	}
	
	/**
	 * Met à jour le panneau d'affichage pour la méthode 2 (initialisation directe)
	 * @param type Le type des éléments du tableau
	 * @param nom Le nom du tableau
	 * @param taille La taille du tableau
	 * @param valeurDefauts Les valeurs par défaut à afficher
	 */
	private void updateMethode2Panel(String type, String nom, int taille, String[] valeurDefauts) {
		// Nettoyage des composants existants
		codePanel2.removeAll();
		codeLignes.clear();

		// Création de la ligne unique d'initialisation du tableau
		JPanel ligne = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ligne.add(createCodeLabel(type + "[] "));
		ligne.add(createCodeLabel(nom));
		ligne.add(createCodeLabel(" = {"));

		// Ajout des champs pour les valeurs du tableau
		valeursFields = new JTextField[taille];
		for (int i = 0; i < taille; i++) {
			String valeurDefaut = (i < valeurDefauts.length) ? valeurDefauts[i] : getDefaultValue(type);

			// Création du champ de saisie avec délimiteurs en fonction du type
			JPanel fieldPanel = createTypeFieldPanel(valeurDefaut, type, i);
			ligne.add(fieldPanel);

			// Ajout d'une virgule entre les valeurs (sauf la dernière)
			if (i < taille - 1) {
				ligne.add(createCodeLabel(", "));
			}
		}

		ligne.add(createCodeLabel("};"));
		codePanel2.add(ligne);
		codeLignes.add(ligne);

		// Mise à jour de l'affichage
		codePanel2.revalidate();
		codePanel2.repaint();
	}
	
	/**
	 * Met à jour le panneau d'illustration avec les cases du tableau
	 * @param type Le type des éléments du tableau
	 * @param nom Le nom du tableau
	 * @param taille La taille du tableau
	 * @param valeurDefauts Les valeurs par défaut à afficher
	 */
	private void updateIllustrationPanel(String type, String nom, int taille, String[] valeurDefauts) {
	    // Nettoyage des composants existants
	    illustrationPanel.removeAll();

	    JPanel illustrationContentPanel = new JPanel(new BorderLayout());

	    // Création du panneau de titre
	    JPanel topPanel = new JPanel(new BorderLayout());
	    JLabel titreLabel = new JLabel(type + "[] " + nom, SwingConstants.CENTER);
	    titreLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
	    titreLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
	    topPanel.add(titreLabel, BorderLayout.CENTER);

	    // Création du panneau contenant les cases du tableau
	    JPanel tableauPanel = new JPanel(new GridLayout(1, taille, 5, 0));
	    tableauPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

	    illustrationLabels = new JLabel[taille];
	    illustrationCasePanels = new JPanel[taille];

	    // Valeur par défaut selon le type
	    String valeurDefaut = getDefaultValue(type);

	    // Création des cases du tableau
	    for (int i = 0; i < taille; i++) {
	        // Création du panneau pour la case
	        JPanel casePanel = new JPanel(new BorderLayout());
	        casePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
	        casePanel.setBackground(Color.WHITE);
	        casePanel.setVisible(false);  // Initialement invisible
	        illustrationCasePanels[i] = casePanel;

	        // Ajout de l'étiquette d'indice
	        JLabel indexLabel = new JLabel("[" + i + "]", SwingConstants.CENTER);
	        indexLabel.setFont(new Font("Monospaced", Font.BOLD, 12));

	        // Création du label pour la valeur
	        JPanel valeurPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
	        valeurPanel.setBackground(Color.WHITE);

	        // Création et ajout de l'étiquette pour la valeur (sans délimiteurs)
	        illustrationLabels[i] = new JLabel(valeurDefaut, SwingConstants.CENTER);
	        illustrationLabels[i].setFont(new Font("Monospaced", Font.PLAIN, 14));
	        valeurPanel.add(illustrationLabels[i]);

	        // Assemblage de la case
	        JPanel indexPanel = new JPanel(new BorderLayout());
	        indexPanel.add(indexLabel, BorderLayout.CENTER);

	        casePanel.add(indexPanel, BorderLayout.NORTH);
	        casePanel.add(valeurPanel, BorderLayout.CENTER);

	        tableauPanel.add(casePanel);
	    }

	    // Assemblage du panneau d'illustration
	    illustrationContentPanel.add(topPanel, BorderLayout.NORTH);
	    illustrationContentPanel.add(tableauPanel, BorderLayout.CENTER);

	    illustrationPanel.add(illustrationContentPanel);

	    // Mise à jour de l'affichage
	    illustrationPanel.revalidate();
	    illustrationPanel.repaint();
	}
	
	/**
	 * Rafraîchit les composants principaux de l'interface
	 */
	private void rafraichirInterface() {
		if (illustrationPanel != null) {
			illustrationPanel.revalidate();
			illustrationPanel.repaint();
		}
		if (codePanel1 != null) {
			codePanel1.revalidate();
			codePanel1.repaint();
		}
		if (codePanel2 != null) {
			codePanel2.revalidate();
			codePanel2.repaint();
		}
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * LOGIQUE D'EXÉCUTION
	 * Ces méthodes implémentent la logique d'exécution pas à pas
	 * du code et l'animation correspondante.
	 */
////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Prépare l'interface pour l'exécution 
	 */
	private void preparerExecution() {
	    enExecution = true;

	    // Désactiver les contrôles pendant l'exécution
	    executerButton.setEnabled(false);
	    typeComboBox.setEnabled(false);
	    nomTextField.setEnabled(false);
	    tailleSpinner.setEnabled(false);
	    methode1Radio.setEnabled(false);
	    methode2Radio.setEnabled(false);

	    // Réinitialiser l'état
	    currentLineIndex = -1;

	    // Réinitialiser les couleurs de fond des lignes de code
	    for (JPanel ligne : codeLignes) {
	        ligne.setBackground(null);
	    }

	    // Réinitialiser l'illustration - mettre la valeur par défaut dans chaque case
	    String type = (String) typeComboBox.getSelectedItem();
	    String valeurDefaut = getDefaultValue(type);
	    for (int i = 0; i < taille; i++) {
	        illustrationCasePanels[i].setBackground(Color.WHITE);
	        illustrationLabels[i].setText(valeurDefaut);
	        illustrationCasePanels[i].setVisible(false);
	    }
	}
	
	/**
	 * Exécute toutes les lignes de code automatiquement
	 */
	private void executerTout() {
		preparerExecution();
		executionTimer.start();
	}
	
	/**
	 * Exécute une seule ligne de code
	 */
	private void executerLigne() {
		if (!enExecution) {
			preparerExecution();
		}
		executerLigneSuivante();
	}

	/**
	 * Exécute la ligne suivante dans l'animation
	 */
	private void executerLigneSuivante() {
		currentLineIndex++;

		// Vérifier si on a terminé l'exécution
		if (currentLineIndex >= codeLignes.size()) {
			arreterExecution();
			return;
		}

		// Mettre en évidence la ligne en cours d'exécution
		for (int i = 0; i < codeLignes.size(); i++) {
			codeLignes.get(i).setBackground(i == currentLineIndex ? new Color(255, 255, 200) : null);
		}

		// Activer les filtres de validation pendant l'exécution
		if (!filtresActifs) {
			filtresActifs = true;
			activerFiltresType();
		}

		// Logique d'exécution différente selon la méthode choisie
		if (methodeChoisie == 2) {
			executerLigneMethode2();
		} else {
			executerLigneMethode1();
		}

		// Faire défiler le scroll pour voir la ligne en cours
		if (methodeChoisie == 1) {
			Rectangle bounds = codeLignes.get(currentLineIndex).getBounds();
			scrollPane1.getViewport().scrollRectToVisible(bounds);
		}
	}
	
	/**
	 * Exécute une ligne pour la méthode 1 (déclaration + allocation + affectation)
	 */
	private void executerLigneMethode1() {
	    // Si c'est la première ligne (déclaration du tableau)
	    if (currentLineIndex == 0) {
	        // Rendre toutes les cases visibles avec leur valeur par défaut (tableau déclaré et initialisé)
	        for (int i = 0; i < taille; i++) {
	            illustrationCasePanels[i].setVisible(true);
	            // Afficher la valeur par défaut du type sans délimiteurs
	            String valeurDefaut = getDefaultValue((String) typeComboBox.getSelectedItem());
	            illustrationLabels[i].setText(valeurDefaut);
	        }
	    } 
	    // Pour les lignes d'affectation
	    else if (currentLineIndex > 0) {
	        int indexTableau = currentLineIndex - 1;
	        if (indexTableau < taille) {
	            // Vérifier si la valeur est valide
	            String valeur = valeursFields[indexTableau].getText();
	            String type = (String) typeComboBox.getSelectedItem();

	            if (!estValeurValide(valeur, type)) {
	                // Marquer le champ et la ligne en rouge en cas d'erreur
	                valeursFields[indexTableau].setBackground(new Color(255, 200, 200));
	                codeLignes.get(currentLineIndex).setBackground(new Color(255, 200, 200));
	                
	                // Ajouter un message d'erreur
	                afficherMessageErreur("Erreur : vérifier le format");
	                
	                // Rester sur cette ligne jusqu'à correction
	                currentLineIndex--;
	                if (executionTimer.isRunning()) {
	                    executionTimer.stop();
	                }
	            } else {
	                // Réinitialiser la couleur du champ et mettre à jour la case
	                valeursFields[indexTableau].setBackground(Color.WHITE);
	                if (illustrationCasePanels != null && indexTableau < illustrationCasePanels.length) {
	                    illustrationCasePanels[indexTableau].setBackground(new Color(200, 255, 200));
	                    
	                    // Extraire la valeur sans délimiteurs pour l'affichage
	                    String valeurAffichage = extraireValeurSansDelimiteurs(valeur, type);
	                    illustrationLabels[indexTableau].setText(valeurAffichage);
	                }
	            }
	        }
	    }
	}
	
	/**
	 * Exécute une ligne pour la méthode 2 (initialisation directe)
	 */
	private void executerLigneMethode2() {
	    if (currentLineIndex == 0) {
	        // Vérifier que toutes les valeurs sont valides
	        boolean toutesValides = true;
	        int premierIndexInvalide = -1;
	        for (int i = 0; i < taille; i++) {
	            if (!estValeurValide(valeursFields[i].getText(), (String) typeComboBox.getSelectedItem())) {
	                toutesValides = false;
	                // Surligner en rouge le champ invalide
	                valeursFields[i].setBackground(new Color(255, 200, 200));
	                if (premierIndexInvalide == -1) {
	                    premierIndexInvalide = i;
	                }
	            } else {
	                valeursFields[i].setBackground(Color.WHITE);
	            }
	        }

	        // Si toutes les valeurs sont valides, mettre à jour l'illustration
	        if (toutesValides) {
	            // Mise à jour de toutes les cases en une seule fois
	            for (int i = 0; i < taille; i++) {
	                // Rendre la case visible et mettre à jour son contenu
	                illustrationCasePanels[i].setVisible(true);
	                illustrationCasePanels[i].setBackground(new Color(200, 255, 200));
	                
	                String valeur = valeursFields[i].getText();
	                String type = (String) typeComboBox.getSelectedItem();
	                
	                // Extraire la valeur sans délimiteurs pour l'affichage
	                String valeurAffichage = extraireValeurSansDelimiteurs(valeur, type);
	                illustrationLabels[i].setText(valeurAffichage);
	            }
	        } else {
	            // Marquer la ligne en rouge pour indiquer une erreur
	            codeLignes.get(currentLineIndex).setBackground(new Color(255, 200, 200));
	            
	            // Ajouter un message d'erreur
	            afficherMessageErreur("Erreur : vérifier le format");
	            
	            // Rester sur cette ligne jusqu'à correction
	            currentLineIndex--;
	            if (executionTimer.isRunning()) {
	                executionTimer.stop();
	            }
	        }
	    }
	}
	
	/**
	 * Arrête l'exécution animée et restaure l'interface
	 */
	private void arreterExecution() {
		enExecution = false;
		executionTimer.stop();

		// Réactiver les contrôles
		executerButton.setEnabled(true);
		executerLigneButton.setEnabled(true);
		typeComboBox.setEnabled(true);
		nomTextField.setEnabled(true);
		tailleSpinner.setEnabled(true);
		methode1Radio.setEnabled(true);
		methode2Radio.setEnabled(true);

		// Désactiver les filtres de validation
		filtresActifs = false;
		desactiverFiltresType();
	}

	/**
	 * Réinitialise l'exécution et restaure l'état initial
	 */
	private void reinitialiserExecution() {
	    if (enExecution) {
	        arreterExecution();
	    }

	    // Réinitialiser l'index et l'apparence des lignes de code
	    currentLineIndex = -1;
	    if (codeLignes != null) {
	        for (JPanel ligne : codeLignes) {
	            ligne.setBackground(null);
	        }
	    }

	    // Réinitialiser l'état des boutons
	    executerButton.setEnabled(true);
	    executerLigneButton.setEnabled(true);

	    // Réinitialiser les cases dans l'illustration
	    if (illustrationCasePanels != null) {
	        String type = (String) typeComboBox.getSelectedItem();
	        String valeurDefaut = getDefaultValue(type);
	        for (int i = 0; i < illustrationCasePanels.length; i++) {
	            if (illustrationCasePanels[i] != null) {
	                illustrationCasePanels[i].setBackground(Color.WHITE);
	                illustrationCasePanels[i].setVisible(false);
	            }
	            if (illustrationLabels[i] != null) {
	                illustrationLabels[i].setText(valeurDefaut);
	            }
	        }
	    }

	    // Réinitialiser la couleur des champs de saisie
	    if (valeursFields != null) {
	        for (JTextField field : valeursFields) {
	            if (field != null) {
	                field.setBackground(Color.WHITE);
	            }
	        }
	    }

	    // Désactiver les filtres
	    filtresActifs = false;
	    desactiverFiltresType();

	    // Rafraîchir l'interface
	    rafraichirInterface();
	}

////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * VALIDATION ET GESTION DES ERREURS
	 * Méthodes pour valider les entrées utilisateur, afficher les erreurs
	 * et gérer les restrictions de saisie.
	 */
////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Vérifie si une valeur est valide pour un type donné
	 * @param valeur La valeur à vérifier
	 * @param type Le type souhaité
	 * @return true si la valeur est valide pour ce type
	 */
	private boolean estValeurValide(String valeur, String type) {
	    if (valeur == null) return false;

	    switch (type) {
	    case "int":
	        return valeur.matches("-?\\d+");
	    case "double":
	        return valeur.matches("-?\\d*\\.?\\d*") && !valeur.equals(".");
	    case "char":
	        // Accepte 'c' ou \u0000
	        return (valeur.matches("'.'") && valeur.length() == 3) || valeur.equals("\\u0000");
	    case "boolean":
	        return valeur.equals("true") || valeur.equals("false");
	    case "String":
	        // Accepte "texte" ou null
	        return (valeur.startsWith("\"") && valeur.endsWith("\"")) || valeur.equals("null");
	    default:
	        return true;
	    }
	}

	/**
	 * Active les filtres de validation des champs selon le type sélectionné
	 */
	private void activerFiltresType() {
		String type = (String) typeComboBox.getSelectedItem();
		for (int i = 0; i < valeursFields.length; i++) {
			// Supprimer l'ancien filtre s'il existe et ajouter le nouveau
			AbstractDocument doc = (AbstractDocument) valeursFields[i].getDocument();
			doc.setDocumentFilter(createFilterForType(type));
		}
	}

	/**
	 * Désactive les filtres de validation des champs
	 */
	private void desactiverFiltresType() {
		for (int i = 0; i < valeursFields.length; i++) {
			AbstractDocument doc = (AbstractDocument) valeursFields[i].getDocument();
			doc.setDocumentFilter(null);
		}
	}
	
	/**
	 * Affiche un message d'erreur dans une boîte de dialogue
	 * @param message Le message d'erreur à afficher
	 */
	private void afficherMessageErreur(String message) {
	    JOptionPane.showMessageDialog(
	        this,
	        message,
	        "Erreur de format",
	        JOptionPane.ERROR_MESSAGE
	    );
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * UTILITAIRES DE TRAITEMENT DES DONNÉES
	 * Fonctions auxiliaires pour la manipulation et la conversion des données.
	 */
////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Renvoie une valeur par défaut selon le type (valeur Java réelle)
	 * @param type Le type de données
	 * @return Une valeur par défaut appropriée
	 */
	private String getDefaultValue(String type) {
	    switch (type) {
	    case "int": return "0";
	    case "double": return "0.0";
	    case "String": return "null";  // Valeur par défaut réelle pour un String
	    case "char": return "\\u0000";  // Caractère nul Unicode
	    case "boolean": return "false";
	    default: return "";
	    }
	}
	
	/**
	 * Extrait la valeur sans délimiteurs (guillemets/apostrophes) pour l'affichage
	 * @param valeur La valeur avec potentiellement des délimiteurs
	 * @param type Le type de la valeur
	 * @return La valeur sans délimiteurs
	 */
	private String extraireValeurSansDelimiteurs(String valeur, String type) {
	    if (valeur == null || valeur.isEmpty()) return valeur;
	    
	    if (type.equals("String") && valeur.startsWith("\"") && valeur.endsWith("\"") && valeur.length() >= 2) {
	        return valeur.substring(1, valeur.length() - 1);
	    } else if (type.equals("char") && valeur.startsWith("'") && valeur.endsWith("'") && valeur.length() >= 3) {
	        return valeur.substring(1, valeur.length() - 1);
	    }
	    
	    return valeur;
	}

////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * CLASSES INTERNES ET INTERFACES
	 * Définition des classes internes et interfaces utilisées par l'application.
	 */
////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Interface fonctionnelle pour les actions de document
	 */
	private interface SimpleDocumentAction {
		void execute();
	}

	/**
	 * Écouteur simplifié pour les événements de document
	 */
	private class SimpleDocumentListener implements javax.swing.event.DocumentListener {
		private SimpleDocumentAction action;

		public SimpleDocumentListener(SimpleDocumentAction action) {
			this.action = action;
		}

		@Override
		public void insertUpdate(javax.swing.event.DocumentEvent e) {
			action.execute();
		}

		@Override
		public void removeUpdate(javax.swing.event.DocumentEvent e) {
			action.execute();
		}

		@Override
		public void changedUpdate(javax.swing.event.DocumentEvent e) {
			action.execute();
		}
	}
}
