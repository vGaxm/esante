/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package esante;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Stack;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.util.UUID;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.text.Document;

/**
 *
 * @author ixlam
 */
public class AdminForm extends javax.swing.JFrame {

    /**
     * Creates new form AdminForm
     */
    
    SqlSession session;
    Employe selectedEmploye;
    Patient selectedPatient;
    Reservation selectedReservation;
    ArrayList<Employe> medinf;
    Date selectedDate;
    LinkedList<Reservation> mail_queue;
    boolean isSendingMail = false;
    Thread phoneThread;
    JFileChooser fileChooser;
    ArrayList<Service> services = new ArrayList<>();    
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy / MM / dd");
    
    void actualiserPat() {
        selectedPatient = Patient.findByNSS(session, (String)tablePatients.getValueAt(tablePatients.getSelectedRow(), 0));
        if (selectedPatient != null) {
            tfNom.setText(selectedPatient.getNom());
            tfPrenom.setText(selectedPatient.getPrenom());
            tfNss1.setText(selectedPatient.getNSS());
            tfAdresse.setText(selectedPatient.getAdresse());
            tfEmail.setText(selectedPatient.getEmail());
            tfTel.setText(selectedPatient.getTel());
            tfAler.setText(selectedPatient.getAlergie());
            loadStatsPatient();
        } else {
            tfNom.setText("");
            tfPrenom.setText("");
            tfNss1.setText("");
            tfAdresse.setText("");
            tfEmail.setText("");
            tfTel.setText("");
            tfAler.setText("");
        }
    }
    
    public AdminForm(SqlSession session) {
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choisir ou exporter le csv");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.canWrite() && f.isFile() && f.getName().endsWith(".csv");
            }

            @Override
            public String getDescription() {
                return "CSV (Commas Separated Values)";
            }
        });
        
        setResizable(false);
        initComponents();
        ComponentMover cm1 = new ComponentMover(this.getClass(), jPanel7);
        ComponentMover cm2 = new ComponentMover(this.getClass(), jPanel6);
        setLocationRelativeTo(null);
        this.session = session;
        selectedEmploye = null;
        mail_queue = new LinkedList<>();
        labelNomEmp.setText(session.getUsername());
        for (Specialite spec : Specialite.getAll(session))
            cbSpec.addItem(spec.getSpecName());
        for(Reservation r : Reservation.findAll(session)) {
            calander1.addReservedDate(r.getDate(), r);
        }
        tableEmployes.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
        tableReservations.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
        tablePatients.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
        tableEmployes.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (tableEmployes.getSelectedRowCount() > 0) {
                    selectedEmploye = Employe.findByCode(session, (Integer)tableEmployes.getValueAt(tableEmployes.getSelectedRow(), 0));
                    tfEmployeNom.setText(selectedEmploye.getNom());                
                    tfEmpPrenom.setText(selectedEmploye.getPrenom());
                    tfEmpTel.setText(selectedEmploye.getTel());
                    cbEmployeSecteur.setSelectedItem(selectedEmploye.getSecteur());
                    spinEmpExp.setValue(selectedEmploye.getExp());
                    int specCode = selectedEmploye.getSpecCode();
                    cbSpec.setSelectedIndex(specCode > 0 ? specCode - 1 : cbSpec.getSelectedIndex());
                    jPasswordField1.setText(selectedEmploye.getPass());
                    if (selectedEmploye.getSpecCode() > 0) {
                        jCheckBox2.setSelected(false);
                        cbSpec.setEnabled(true);
                    }
                    loadStatsEmploye();
                }
            }
        }); 
        tablePatients.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (tablePatients.getSelectedRowCount() > 0) {
                    actualiserPat();
                }
            }
        });
        calander1.addOnCalenderEvent(new CalenderEvent() {
            @Override
            public void OnCellSelected(CalenderCell cell) {
                if (cell.old) {
                    prepareNewReservation(true);
                    System.out.println(cell.old);
                    return;
                }
                selectedDate = cell.getDate();
                
                if (cell.isReserved()) {
                    Reservation res = cell.getReservation();
                    for(int i=0; i < tableReservations.getModel().getRowCount(); i++) {    
                        if ((int)tableReservations.getModel().getValueAt(i, 0) == res.getNumero()) {
                            System.out.println((int)tableReservations.getModel().getValueAt(i, 0));
                            tableReservations.getSelectionModel().setSelectionInterval(i, i);
                        }
                    }
                    selectReservation(res);
                }
                else prepareNewReservation(selectedReservation != null);
            }

            @Override
            public void OnReserved(CalenderCell cell, Reservation res) {
                Employe e = Employe.findByCode(session, res.getEmploye());
                cell.setInfo(e.getNom().substring(0, 1).toUpperCase() + ". " + e.getPrenom());
            }

            @Override
            public void OnReservedCellSelected(CalenderCell cell) {

            }
            
        });
        Document d = new JTextFieldLimit(18);
        d.addDocumentListener(new DocumentListener() {
            Thread loading;
            void update() {
                if (loading != null && loading.isAlive()) loading.interrupt();
                labelNssInfo.setText("recherche ...");
                loading = new Thread() {
                    @Override
                    public void run() {
                        int size = Patient.searchByNSS(session, tfNss.getText(), 10).size();
                        jButton3.setEnabled(tfNss.getText().length() == 18 && size == 0);
                        if (size == 0)
                            labelNssInfo.setText("aucun patient trouvé");
                        else
                            labelNssInfo.setText((tfNss.getText().length() == 18 && size == 1) ? "patient trouvé" : "trouvé: " + size + " patient" + (size > 1 ? "s" : ""));
                    }
                };
                loading.start();
            }
            
            @Override
            public void insertUpdate(DocumentEvent de) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                update();
            }
        });
        tfNss.setDocument(d);
        tableReservations.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            int sr = tableReservations.getSelectedRow();
            if (sr >= 0) { 
                Reservation r = Reservation.findByNumero(session,  (int)tableReservations.getValueAt(sr, 0));
                selectReservation(r);
            }
        });
        tableReservations.setCellEditor(new DefaultCellEditor(new JCheckBox()));
        searchbar5.search.getDocument().addDocumentListener(new DocumentListener() {
            Thread loading;
            void update() {
                if (loading != null && loading.isAlive()) loading.interrupt();
                //Loading
                loading = new Thread() {
                    @Override
                    public void run() {
                        ArrayList<Employe> emps = Employe.search(session, searchbar5.search.getText());
                        if (emps.isEmpty())
                            //EMPTY
                            ;
                        else
                            //FOUND
                            ;
                        loadEmployesArray(emps);
                    }
                };
                loading.start();
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!searchbar5.search.getText().isBlank())
                    update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
               
            }
        });
        searchbar4.search.getDocument().addDocumentListener(new DocumentListener() {
            Thread loading;
            void update() {
                if (loading != null && loading.isAlive()) loading.interrupt();
                //Loading
                loading = new Thread() {
                    @Override
                    public void run() {
                        ArrayList<Patient> pats = Patient.search(session, searchbar4.search.getText());
                        if (pats.isEmpty())
                            //EMPTY
                            ;
                        else
                            //FOUND
                            ;
                        loadPatientsArray(pats);
                    }
                };
                loading.start();
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!searchbar4.search.getText().isBlank())
                    update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
               
            }
        });
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("pinging...");
                    InetAddress gmail = InetAddress.getByName("mail.google.com");
                    if (gmail.isReachable(5000)) {
                        if (mail_queue.isEmpty())
                            System.out.println("List mail vide");
                        else while(!mail_queue.isEmpty())
                            sendReservationMail(mail_queue.pop(), 2);
                        return;
                    }
                } catch (Exception e) {
                    
                }
                System.out.println("Impossible d'obtenir une connection");
            }
        }, 0, 30000);
        loadEmployes();
        loadServices();
        loadReservations();
        loadPatients();
    }
    
    public boolean requestLogin() {
        return loginPopup.show(this, "admin").equals(session.getPassword());
    }

    public void loadStatsPatient() {
        
    }
    
    public void loadStatsEmploye() {
        if (selectedEmploye != null) {
            lblNombreVisitesE.setText(""+selectedEmploye.getNbrVisites());
        }
    }
    
    public void loadEmployesArray(ArrayList<Employe> emps) {
        DefaultTableModel dtb = (DefaultTableModel)tableEmployes.getModel();
        dtb.setRowCount(0);
        emps.forEach((employe) -> {
            dtb.addRow(new Object[]{
                employe.getCode(),
                employe.getNom(),
                employe.getSecteur(),
                Specialite.findByCode(session, employe.getSpecCode()).getSpecName(),
                employe.getExp(),
            });
        });
    }
    
    public void loadPatientsArray(ArrayList<Patient> pats) {
        DefaultTableModel dtb = (DefaultTableModel)tablePatients.getModel();
        dtb.setRowCount(0);
        pats.forEach((pat) -> {
            dtb.addRow(new Object[]{
                pat.getNSS(),
                pat.getNom(),
                pat.getPrenom(),
                pat.getTel()
            });
        });
    }
    
    
    public void loadPatients() {
        searchbar4.search.setText("");
        loadPatientsArray(Patient.findAll(session));
    }
    
    public void loadEmployes() {
        searchbar5.search.setText("");
        loadEmployesArray(Employe.findAll(session));
    }
    
    void prepareNewReservation(boolean clear) {
        btnReserver.setEnabled(true);
        selectedReservation = null;
        if (clear) {
            cbType.setSelectedIndex(0);
            tfNss.setText("");
        }
        tfDate.setText(sdf.format(selectedDate));
    }
    
    public void loadReservations() {
        DefaultTableModel dtb = (DefaultTableModel)tableReservations.getModel();
        dtb.setRowCount(0);
        Reservation.findAll(session).forEach((res) -> {
            dtb.addRow(new Object[]{
                res.getNumero(),
                res.getDate(),
                res.getNSS(),
                res.getPaye()
            });
        });
    }
    
    void selectReservation(Reservation r) {
        btnReserver.setEnabled(false);
        selectedReservation = r;
        tfNss.setText(r.getNSS());
        cbType.setSelectedIndex(r.getType().equals("MEDICAL") ? 0 : 1);
        for (int i = 0; i < services.size(); i++)
            if (services.get(i).getServiceCode() == r.getCodeService())
                cbService.setSelectedIndex(i);
        tfDate.setText(new SimpleDateFormat("yyyy / MM / dd").format(r.getDate()));
        for (int i=0; i < medinf.size(); i++)
            if (medinf.get(i).getCode() == r.getEmploye())
                cbMedecin.setSelectedIndex(i);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel9 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton4 = new javax.swing.JToggleButton();
        jPanel8 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        labelNomEmp = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        calander1 = new esante.Calender();
        btnReserver = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        tfNss = new javax.swing.JTextField();
        cbMedecin = new javax.swing.JComboBox<>();
        lblEmployeType = new javax.swing.JLabel();
        labelNssInfo = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        cbService = new javax.swing.JComboBox<>();
        cbType = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableReservations = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        tfDate = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        tfNss1 = new javax.swing.JTextField();
        tfNom = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        tfEmail = new javax.swing.JTextField();
        tfPrenom = new javax.swing.JTextField();
        tfTel = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        tfAdresse = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tablePatients = new javax.swing.JTable();
        btnEmpModif1 = new javax.swing.JButton();
        btnEmpAct1 = new javax.swing.JButton();
        btnEmpSup1 = new javax.swing.JButton();
        searchbar4 = new esante.Searchbar();
        jLabel17 = new javax.swing.JLabel();
        tfAler = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableEmployes = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        tfEmployeNom = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cbEmployeSecteur = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        btnEmpModif = new javax.swing.JButton();
        btnEmpAjou = new javax.swing.JButton();
        btnEmpSup = new javax.swing.JButton();
        btnEmpAct = new javax.swing.JButton();
        spinEmpExp = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        tfEmpTel = new javax.swing.JTextField();
        tfEmpPrenom = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jButton2 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        cbSpec = new javax.swing.JComboBox<>();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        lblNombreVisitesE = new javax.swing.JLabel();
        searchbar5 = new esante.Searchbar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Admin");
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 51, 51)));
        jPanel1.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N

        jSplitPane1.setDividerSize(0);

        jPanel9.setLayout(new java.awt.BorderLayout());

        jPanel6.setBackground(new java.awt.Color(57, 72, 103));
        jPanel6.setMinimumSize(new java.awt.Dimension(250, 16));
        jPanel6.setPreferredSize(new java.awt.Dimension(250, 748));
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/esante/logo_small.png"))); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(250, 200));
        jPanel6.add(jLabel19);

        jToggleButton2.setBackground(new java.awt.Color(57, 72, 103));
        panel.add(jToggleButton2);
        jToggleButton2.setFont(new java.awt.Font("Roboto Medium", 0, 14)); // NOI18N
        jToggleButton2.setForeground(new java.awt.Color(255, 255, 255));
        jToggleButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/esante/icons8-reservation-24.png"))); // NOI18N
        jToggleButton2.setSelected(true);
        jToggleButton2.setText("Gestion des réservations");
        jToggleButton2.setAlignmentY(0.0F);
        jToggleButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 20, 1, 1));
        jToggleButton2.setBorderPainted(false);
        jToggleButton2.setHideActionText(true);
        jToggleButton2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jToggleButton2.setIconTextGap(8);
        jToggleButton2.setMargin(new java.awt.Insets(2, 50, 3, 14));
        jToggleButton2.setMaximumSize(jPanel6.getMaximumSize());
        jToggleButton2.setMinimumSize(jPanel6.getMaximumSize());
        jToggleButton2.setPreferredSize(new java.awt.Dimension(250, 60));
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });
        jPanel6.add(jToggleButton2);

        jToggleButton3.setBackground(new java.awt.Color(57, 72, 103));
        panel.add(jToggleButton3);
        jToggleButton3.setFont(new java.awt.Font("Roboto Medium", 0, 14)); // NOI18N
        jToggleButton3.setForeground(new java.awt.Color(255, 255, 255));
        jToggleButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/esante/icons8-manager-24.png"))); // NOI18N
        jToggleButton3.setText("Gestion des employes");
        jToggleButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 20, 1, 1));
        jToggleButton3.setBorderPainted(false);
        jToggleButton3.setHideActionText(true);
        jToggleButton3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jToggleButton3.setIconTextGap(8);
        jToggleButton3.setMargin(new java.awt.Insets(2, 50, 3, 14));
        jToggleButton3.setPreferredSize(new java.awt.Dimension(250, 60));
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });
        jPanel6.add(jToggleButton3);

        jToggleButton4.setBackground(new java.awt.Color(57, 72, 103));
        panel.add(jToggleButton4);
        jToggleButton4.setFont(new java.awt.Font("Roboto Medium", 0, 14)); // NOI18N
        jToggleButton4.setForeground(new java.awt.Color(255, 255, 255));
        jToggleButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/esante/icons8-patient-24.png"))); // NOI18N
        jToggleButton4.setText("Gestion des patients");
        jToggleButton4.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 20, 1, 1));
        jToggleButton4.setBorderPainted(false);
        jToggleButton4.setHideActionText(true);
        jToggleButton4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jToggleButton4.setIconTextGap(8);
        jToggleButton4.setMargin(new java.awt.Insets(2, 50, 3, 14));
        jToggleButton4.setPreferredSize(new java.awt.Dimension(250, 60));
        jToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton4ActionPerformed(evt);
            }
        });
        jPanel6.add(jToggleButton4);

        jPanel9.add(jPanel6, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel9);

        jPanel8.setLayout(new java.awt.BorderLayout());

        jPanel7.setBackground(new java.awt.Color(57, 72, 103));

        jLabel1.setFont(new java.awt.Font("Roboto Medium", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Connecté en tant que:");

        labelNomEmp.setFont(new java.awt.Font("Roboto Medium", 0, 14)); // NOI18N
        labelNomEmp.setForeground(new java.awt.Color(255, 255, 255));
        labelNomEmp.setText("NOM DE L'EMPLOYÉ");

        jButton1.setBackground(new java.awt.Color(255, 102, 102));
        jButton1.setFont(new java.awt.Font("Roboto Medium", 0, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/esante/icons8-export-24.png"))); // NOI18N
        jButton1.setText("Se déconnecter");
        jButton1.setBorderPainted(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jButton1.setMargin(new java.awt.Insets(2, 5, 2, 2));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton6.setBackground(new java.awt.Color(255, 102, 102));
        jButton6.setFont(new java.awt.Font("Roboto Medium", 0, 14)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/esante/icons8-close-24.png"))); // NOI18N
        jButton6.setBorderPainted(false);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelNomEmp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 678, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(labelNomEmp)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.add(jPanel7, java.awt.BorderLayout.PAGE_START);

        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 51, 51)));
        jPanel5.setLayout(new java.awt.CardLayout());

        jPanel3.setBackground(new java.awt.Color(241, 246, 249));

        btnReserver.setBackground(new java.awt.Color(113, 177, 204));
        btnReserver.setFont(new java.awt.Font("Roboto Medium", 0, 24)); // NOI18N
        btnReserver.setForeground(new java.awt.Color(51, 51, 51));
        btnReserver.setText("Reserver");
        btnReserver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReserverActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(51, 51, 51));
        jLabel9.setText("Type:");

        jLabel10.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(51, 51, 51));
        jLabel10.setText("Service:");

        jLabel11.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(51, 51, 51));
        jLabel11.setText("NSS:");

        tfNss.setColumns(18);
        tfNss.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        tfNss.setForeground(new java.awt.Color(51, 51, 51));
        tfNss.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                tfNssInputMethodTextChanged(evt);
            }
        });
        tfNss.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfNssActionPerformed(evt);
            }
        });

        cbMedecin.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N

        lblEmployeType.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        lblEmployeType.setForeground(new java.awt.Color(51, 51, 51));
        lblEmployeType.setText("Medecin:");

        labelNssInfo.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        labelNssInfo.setForeground(new java.awt.Color(51, 51, 51));
        labelNssInfo.setText("aucun patient trouvé");

        jButton3.setFont(new java.awt.Font("Roboto Medium", 0, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(51, 51, 51));
        jButton3.setText("Ajouter");
        jButton3.setEnabled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        cbService.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        cbService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbServiceActionPerformed(evt);
            }
        });

        cbType.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        cbType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Consultation Medicale", "Consultation Paramedicale" }));
        cbType.setActionCommand("MEDICAL");
        cbType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbTypeActionPerformed(evt);
            }
        });

        tableReservations.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tableReservations.setForeground(new java.awt.Color(51, 51, 51));
        tableReservations.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Numero", "Date", "NSS", "Payé"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableReservations.setRowHeight(40);
        tableReservations.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableReservations.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableReservations.setShowHorizontalLines(true);
        tableReservations.setShowVerticalLines(true);
        jScrollPane2.setViewportView(tableReservations);
        if (tableReservations.getColumnModel().getColumnCount() > 0) {
            tableReservations.getColumnModel().getColumn(0).setPreferredWidth(100);
            tableReservations.getColumnModel().getColumn(0).setMaxWidth(100);
            tableReservations.getColumnModel().getColumn(3).setPreferredWidth(50);
            tableReservations.getColumnModel().getColumn(3).setMaxWidth(50);
        }

        jButton4.setFont(new java.awt.Font("Roboto Medium", 0, 24)); // NOI18N
        jButton4.setForeground(new java.awt.Color(51, 51, 51));
        jButton4.setText("Supprimer");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setFont(new java.awt.Font("Roboto Medium", 0, 24)); // NOI18N
        jButton5.setForeground(new java.awt.Color(51, 51, 51));
        jButton5.setText("Modifier");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 51, 51));
        jLabel2.setText("Date de reservation:");

        tfDate.setEditable(false);
        tfDate.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        tfDate.setForeground(new java.awt.Color(51, 51, 51));
        tfDate.setFocusable(false);
        tfDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfDateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(tfDate, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbType, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(cbService, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(cbMedecin, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEmployeType)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(tfNss, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel11)
                    .addComponent(labelNssInfo)
                    .addComponent(btnReserver, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(calander1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 568, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(calander1, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tfNss, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelNssInfo)
                        .addGap(29, 29, 29)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbType, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbService, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblEmployeType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbMedecin, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tfDate, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(btnReserver, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)))
                .addContainerGap())
        );

        jPanel5.add(jPanel3, "card1");

        jPanel4.setBackground(new java.awt.Color(241, 246, 249));

        jLabel12.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(51, 51, 51));
        jLabel12.setText("NSS:");

        jLabel14.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(51, 51, 51));
        jLabel14.setText("Telephone:");

        tfNss1.setEditable(false);
        tfNss1.setColumns(18);
        tfNss1.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tfNss1.setForeground(new java.awt.Color(51, 51, 51));
        tfNss1.setFocusable(false);
        tfNss1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                tfNss1InputMethodTextChanged(evt);
            }
        });

        tfNom.setColumns(18);
        tfNom.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tfNom.setForeground(new java.awt.Color(51, 51, 51));
        tfNom.setEnabled(false);
        tfNom.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                tfNomInputMethodTextChanged(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(51, 51, 51));
        jLabel15.setText("Nom:");

        tfEmail.setColumns(18);
        tfEmail.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tfEmail.setForeground(new java.awt.Color(51, 51, 51));
        tfEmail.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                tfEmailInputMethodTextChanged(evt);
            }
        });

        tfPrenom.setColumns(18);
        tfPrenom.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tfPrenom.setForeground(new java.awt.Color(51, 51, 51));
        tfPrenom.setEnabled(false);
        tfPrenom.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                tfPrenomInputMethodTextChanged(evt);
            }
        });

        tfTel.setColumns(18);
        tfTel.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tfTel.setForeground(new java.awt.Color(51, 51, 51));
        tfTel.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                tfTelInputMethodTextChanged(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(51, 51, 51));
        jLabel16.setText("Prenom:");

        tfAdresse.setColumns(18);
        tfAdresse.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tfAdresse.setForeground(new java.awt.Color(51, 51, 51));
        tfAdresse.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                tfAdresseInputMethodTextChanged(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(51, 51, 51));
        jLabel20.setText("Adresse:");

        jLabel21.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(51, 51, 51));
        jLabel21.setText("E-mail:");

        tablePatients.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tablePatients.setForeground(new java.awt.Color(51, 51, 51));
        tablePatients.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nss", "Nom", "Prenom", "Telephone"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(tablePatients);
        if (tablePatients.getColumnModel().getColumnCount() > 0) {
            tablePatients.getColumnModel().getColumn(0).setPreferredWidth(100);
            tablePatients.getColumnModel().getColumn(3).setPreferredWidth(60);
        }

        btnEmpModif1.setFont(new java.awt.Font("Roboto Medium", 0, 12)); // NOI18N
        btnEmpModif1.setForeground(new java.awt.Color(51, 51, 51));
        btnEmpModif1.setText("Modifier");
        btnEmpModif1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEmpModif1ActionPerformed(evt);
            }
        });

        btnEmpAct1.setFont(new java.awt.Font("Roboto Medium", 0, 12)); // NOI18N
        btnEmpAct1.setForeground(new java.awt.Color(51, 51, 51));
        btnEmpAct1.setText("Actualiser");
        btnEmpAct1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEmpAct1ActionPerformed(evt);
            }
        });

        btnEmpSup1.setFont(new java.awt.Font("Roboto Medium", 0, 12)); // NOI18N
        btnEmpSup1.setForeground(new java.awt.Color(51, 51, 51));
        btnEmpSup1.setText("Supprimer");
        btnEmpSup1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEmpSup1ActionPerformed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(51, 51, 51));
        jLabel17.setText("Alergies:");

        tfAler.setColumns(18);
        tfAler.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tfAler.setForeground(new java.awt.Color(51, 51, 51));
        tfAler.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                tfAlerInputMethodTextChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel20)
                        .addComponent(jLabel21)
                        .addComponent(tfAdresse, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel12)
                        .addComponent(tfNss1, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel15)
                                .addComponent(tfNom, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel16)
                                .addComponent(tfPrenom, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(btnEmpSup1, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnEmpAct1, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(226, 226, 226)
                                .addComponent(btnEmpModif1, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel14)
                    .addComponent(jLabel17)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(tfAler, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(tfTel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 126, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 572, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchbar4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfPrenom, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfNom, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfNss1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfAdresse, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfTel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfAler, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 163, Short.MAX_VALUE)
                .addComponent(btnEmpModif1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEmpSup1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEmpAct1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(searchbar4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3)
                .addContainerGap())
        );

        jPanel5.add(jPanel4, "card3");

        jPanel2.setBackground(new java.awt.Color(241, 246, 249));

        tableEmployes.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tableEmployes.setForeground(new java.awt.Color(51, 51, 51));
        tableEmployes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Code", "Nom", "Secteur", "Specialité", "Experience"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableEmployes.setRowHeight(40);
        tableEmployes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableEmployes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableEmployes.setShowHorizontalLines(true);
        tableEmployes.setShowVerticalLines(true);
        jScrollPane1.setViewportView(tableEmployes);
        if (tableEmployes.getColumnModel().getColumnCount() > 0) {
            tableEmployes.getColumnModel().getColumn(0).setPreferredWidth(30);
            tableEmployes.getColumnModel().getColumn(3).setPreferredWidth(60);
            tableEmployes.getColumnModel().getColumn(4).setPreferredWidth(10);
        }

        jLabel3.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(51, 51, 51));
        jLabel3.setText("Nom:");

        tfEmployeNom.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tfEmployeNom.setForeground(new java.awt.Color(51, 51, 51));

        jLabel4.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(51, 51, 51));
        jLabel4.setText("Prenom:");

        jLabel5.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(51, 51, 51));
        jLabel5.setText("Secteur:");

        jLabel6.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(51, 51, 51));
        jLabel6.setText("Specialité:");

        cbEmployeSecteur.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        cbEmployeSecteur.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "MEDICAL", "PARAMEDICAL" }));
        cbEmployeSecteur.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbEmployeSecteurActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(51, 51, 51));
        jLabel7.setText("Experience:");

        btnEmpModif.setFont(new java.awt.Font("Roboto Medium", 0, 12)); // NOI18N
        btnEmpModif.setForeground(new java.awt.Color(51, 51, 51));
        btnEmpModif.setText("Modifier");
        btnEmpModif.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEmpModifActionPerformed(evt);
            }
        });

        btnEmpAjou.setFont(new java.awt.Font("Roboto Medium", 0, 12)); // NOI18N
        btnEmpAjou.setForeground(new java.awt.Color(51, 51, 51));
        btnEmpAjou.setText("Ajouter");
        btnEmpAjou.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEmpAjouActionPerformed(evt);
            }
        });

        btnEmpSup.setFont(new java.awt.Font("Roboto Medium", 0, 12)); // NOI18N
        btnEmpSup.setForeground(new java.awt.Color(51, 51, 51));
        btnEmpSup.setText("Supprimer");
        btnEmpSup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEmpSupActionPerformed(evt);
            }
        });

        btnEmpAct.setFont(new java.awt.Font("Roboto Medium", 0, 12)); // NOI18N
        btnEmpAct.setForeground(new java.awt.Color(51, 51, 51));
        btnEmpAct.setText("Actualiser");
        btnEmpAct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEmpActActionPerformed(evt);
            }
        });

        spinEmpExp.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(51, 51, 51));
        jLabel8.setText("Telephone:");

        tfEmpTel.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tfEmpTel.setForeground(new java.awt.Color(51, 51, 51));

        tfEmpPrenom.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tfEmpPrenom.setForeground(new java.awt.Color(51, 51, 51));

        jLabel13.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(51, 51, 51));
        jLabel13.setText("Mot de passe:");

        jPasswordField1.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jPasswordField1.setForeground(new java.awt.Color(51, 51, 51));

        jButton2.setFont(new java.awt.Font("Roboto Medium", 0, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(51, 51, 51));
        jButton2.setText("Générer");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jCheckBox1.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jCheckBox1.setForeground(new java.awt.Color(51, 51, 51));
        jCheckBox1.setText("Afficher mot de passe");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jCheckBox2.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jCheckBox2.setForeground(new java.awt.Color(51, 51, 51));
        jCheckBox2.setSelected(true);
        jCheckBox2.setText("Generaliste");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        cbSpec.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        cbSpec.setEnabled(false);

        jLabel27.setFont(new java.awt.Font("Roboto Medium", 0, 12)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(51, 51, 51));
        jLabel27.setText("Statistiques :");

        jLabel28.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(51, 51, 51));
        jLabel28.setText("Nombre de visites :");

        lblNombreVisitesE.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        lblNombreVisitesE.setForeground(new java.awt.Color(51, 51, 51));
        lblNombreVisitesE.setText("0");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnEmpSup, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnEmpAct, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(tfEmployeNom, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(tfEmpPrenom, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(cbEmployeSecteur, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCheckBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCheckBox1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPasswordField1, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfEmpTel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spinEmpExp, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel6)
                    .addComponent(cbSpec, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnEmpAjou, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnEmpModif, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel27)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblNombreVisitesE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 130, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 563, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(searchbar5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel3)
                            .addGap(48, 48, 48))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfEmployeNom, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfEmpPrenom, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCheckBox2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbEmployeSecteur, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbSpec, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfEmpTel, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinEmpExp, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1)
                .addGap(18, 18, 18)
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(lblNombreVisitesE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEmpModif, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEmpAjou, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEmpSup, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEmpAct, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(64, Short.MAX_VALUE)
                .addComponent(searchbar5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 659, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel5.add(jPanel2, "card2");

        jPanel8.add(jPanel5, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(jPanel8);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1425, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tfNssInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_tfNssInputMethodTextChanged
        if (Patient.searchByNSS(session, tfNss.getText(), 1).isEmpty()) {
            labelNssInfo.setText("Patient introuvable");
        }
    }//GEN-LAST:event_tfNssInputMethodTextChanged

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        jPasswordField1.setEchoChar(jCheckBox1.isSelected() ? '\000' : '*');
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        generatePassword();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnEmpActActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmpActActionPerformed
        if (!requestLogin()) return;
        actualiser();
    }//GEN-LAST:event_btnEmpActActionPerformed

    private void btnEmpSupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmpSupActionPerformed
        if(tableEmployes.getSelectedRowCount() > 0) {
            int code_emp = (Integer)tableEmployes.getValueAt(tableEmployes.getSelectedRow(), 0);
            ArrayList<Reservation> res = Reservation.findByEmploye(session, code_emp);
            if (!res.isEmpty()) {
                int choice = JOptionPane.showOptionDialog(this, "Cet emplye a des reservations\nVoulez vous les supprimer ?", "Attention", 0, JOptionPane.WARNING_MESSAGE, null, new Object[]{"Oui","Non"}, "Non");
                if (choice == 1) return;
            }
            if (!requestLogin()) return;
            Reservation.deleteByEmploye(session, code_emp);
            loadReservations();
            Employe.deleteByCode(session, code_emp);
            actualiser();
        }
    }//GEN-LAST:event_btnEmpSupActionPerformed

    Employe getEmploye(boolean code) {
        Employe emp = new Employe();
        if (code)
        emp.data[0] = selectedEmploye.getCode();
        emp.data[1] = tfEmployeNom.getText();
        emp.data[2] = tfEmpPrenom.getText();
        emp.data[3] = cbEmployeSecteur.getSelectedItem();
        emp.data[4] = spinEmpExp.getValue();
        emp.data[5] =  isSpecEnabled() ? cbSpec.getSelectedIndex() + 1: 0;
        emp.data[6] = tfEmpTel.getText();
        emp.data[7] = jPasswordField1.getText();
        return emp;
    }
   
    Patient getPatient() {
        Patient pat = new Patient();
        pat.setNSS(selectedPatient.getNSS());
        pat.setAdresse(tfAdresse.getText());
        pat.setTel(tfTel.getText());
        pat.setEmail(tfEmail.getText());
        pat.setAlergie(tfAler.getText());
        return pat;
    }
    
    
    private void btnEmpAjouActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmpAjouActionPerformed
        if (!requestLogin()) return;
        if (jPasswordField1.getText().isBlank()) generatePassword();
        Employe.insert(session, getEmploye(false));
        loadEmployes();
    }//GEN-LAST:event_btnEmpAjouActionPerformed

    private void btnEmpModifActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmpModifActionPerformed
        if (!requestLogin()) return;
        Employe.modify(session, getEmploye(true));
        loadEmployes();
        actualiser();
    }//GEN-LAST:event_btnEmpModifActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        PatientDialog patientDialog = new PatientDialog(this, true);
        patientDialog.tfNss.setText(tfNss.getText());
        patientDialog.setVisible(true);
        tfNss.setText(tfNss.getText());
        loadPatients();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        cbSpec.setEnabled(!jCheckBox2.isSelected());
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    boolean isSpecEnabled() {
        boolean haveSpec = "PARAMEDICAL".equals((String)cbEmployeSecteur.getSelectedItem());
        return !haveSpec && !jCheckBox2.isSelected();
    }
    
    void updateSpecEnabled() {
        boolean haveSpec = "PARAMEDICAL".equals((String)cbEmployeSecteur.getSelectedItem());
        cbSpec.setEnabled(!haveSpec && !jCheckBox2.isSelected());
        jCheckBox2.setEnabled(!haveSpec);
    }
    
    private void cbEmployeSecteurActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbEmployeSecteurActionPerformed
        updateSpecEnabled();
    }//GEN-LAST:event_cbEmployeSecteurActionPerformed

    void loadServiceMedical() {
        services.clear();
        cbService.removeAllItems();
        for (Service s : Service.findBySecteur(session, "MEDICAL")) {
            services.add(s);
            cbService.addItem(s.getServiceName());
        }
    }
    
    void loadServiceParamedical() {
        services.clear();
        cbService.removeAllItems();
        for (Service s : Service.findBySecteur(session, "PARAMEDICAL")) {
            services.add(s);
            cbService.addItem(s.getServiceName());
        }
    }
    
    void loadServices() {
        switch(cbType.getSelectedIndex()) {
            case 0:
                loadServiceMedical();
                lblEmployeType.setText("Médecins:");
                break;
            case 1:
                loadServiceParamedical();
                lblEmployeType.setText("Infirmiers:");
                break;
        }    
        loadMedecins();
    }
    
    void loadMedecins() {
        medinf  = Employe.findBySecteur(session, cbType.getSelectedIndex() == 0 ? "MEDICAL" : "PARAMEDICAL");
        System.out.println(medinf.size());
        cbMedecin.removeAllItems();
        for (Employe emp : medinf) {
            cbMedecin.addItem(emp.getNom() + " " + emp.getPrenom());
        }
    }
    
    private void cbTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbTypeActionPerformed
        loadServices();
        loadMedecins();
    }//GEN-LAST:event_cbTypeActionPerformed

    private void cbServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbServiceActionPerformed
        int oldEmp = selectedReservation != null ? selectedReservation.getEmploye(): -1;
        loadMedecins();
        if (oldEmp != -1)
        for (int i=0; i < medinf.size(); i++) {
            if (medinf.get(i).getCode() == oldEmp) {
                cbMedecin.setSelectedIndex(i);
                return;
            }
        }
    }//GEN-LAST:event_cbServiceActionPerformed

    public void reloadReserv() {
        calander1.clearReserved();
        for(Reservation r : Reservation.findAll(session)) {
            calander1.addReservedDate(r.getDate(), r);
        }
        loadReservations();
        prepareNewReservation(true);
    }
    
    private void btnReserverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReserverActionPerformed
        if (!requestLogin()) return;
        Reservation res = new Reservation();
        res.setNSS(tfNss.getText());
        res.setType(cbType.getSelectedIndex() == 0 ? "MEDICAL" : "PARAMEDICAL");
        res.setDate(new java.sql.Date(selectedDate.getTime()));
        res.setEmploye(medinf.get(cbMedecin.getSelectedIndex()).getCode());
        res.setCodeService(services.get(cbService.getSelectedIndex()).getServiceCode());
        Reservation.insert(session, res);
        new Thread() {
            @Override
            public void run() {
                sendReservationMail(res, 3);
            }
        }.start();
        reloadReserv();
    }//GEN-LAST:event_btnReserverActionPerformed

    private void tfNss1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_tfNss1InputMethodTextChanged

    }//GEN-LAST:event_tfNss1InputMethodTextChanged

    private void tfEmailInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_tfEmailInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tfEmailInputMethodTextChanged

    private void tfTelInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_tfTelInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tfTelInputMethodTextChanged

    private void tfAdresseInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_tfAdresseInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tfAdresseInputMethodTextChanged

    private void tfPrenomInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_tfPrenomInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tfPrenomInputMethodTextChanged

    private void tfNomInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_tfNomInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tfNomInputMethodTextChanged

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if(!requestLogin()) return;
        if (selectedReservation != null) {
            selectedReservation.setDate(new java.sql.Date(selectedDate.getTime()));
            selectedReservation.setEmploye(medinf.get(cbMedecin.getSelectedIndex()).getCode());
            switch(cbType.getSelectedIndex()) {
                case 0:
                    selectedReservation.setType("MEDICAL");
                    break;
                case 1:
                    selectedReservation.setType("PARAMEDICAL");
                    break;
            }
            selectedReservation.setCodeService(services.get(cbService.getSelectedIndex()).getServiceCode());
            Reservation.modify(session, selectedReservation);
            selectReservation(selectedReservation);
            reloadReserv();
        }
        else ; //ERROR
    }//GEN-LAST:event_jButton5ActionPerformed

    private void tfDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfDateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfDateActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if(!requestLogin()) return;
        Reservation.deleteByNumero(session, selectedReservation.getNumero());
        reloadReserv();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        new LoginForm().setVisible(true);
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnEmpModif1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmpModif1ActionPerformed
        if(!requestLogin()) return;
        Patient.modify(session, getPatient());
        loadPatients();
    }//GEN-LAST:event_btnEmpModif1ActionPerformed

    private void btnEmpAct1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmpAct1ActionPerformed
        loadPatients();

    }//GEN-LAST:event_btnEmpAct1ActionPerformed

    private void btnEmpSup1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmpSup1ActionPerformed
        ArrayList<Reservation> res = Reservation.findByPatientNSS(session, selectedPatient.getNSS());
        if (!res.isEmpty()) {
            int choice = JOptionPane.showOptionDialog(this, "Ce patient a des reservations\nVoulez vous les supprimer ?", "Attention", 0, JOptionPane.WARNING_MESSAGE, null, new Object[]{"Oui","Non"}, "Non");
            if (choice == 1) return;
        }
        if (!requestLogin()) return;
        res.forEach((r) -> Reservation.deleteByNumero(session, r.getNumero()));
        loadReservations();
        Patient.deleteByNSS(session, selectedPatient.getNSS());
        loadPatients();
    }//GEN-LAST:event_btnEmpSup1ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        ((CardLayout)jPanel5.getLayout()).show(jPanel5, "card1");
        jPanel5.revalidate();
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
        ((CardLayout)jPanel5.getLayout()).show(jPanel5, "card2");
        jPanel5.revalidate();
    }//GEN-LAST:event_jToggleButton3ActionPerformed

    private void jToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton4ActionPerformed
        ((CardLayout)jPanel5.getLayout()).show(jPanel5, "card3");
        jPanel5.revalidate();
    }//GEN-LAST:event_jToggleButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void tfAlerInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_tfAlerInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tfAlerInputMethodTextChanged

    private void tfNssActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfNssActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfNssActionPerformed

    void generatePassword() {
        jPasswordField1.setText(UUID.randomUUID().toString().substring(0, 8));
    }
    
    void actualiser() {
        loadEmployes();
        tfEmployeNom.setText("");                
        tfEmpPrenom.setText("");
        tfEmpTel.setText("");
        cbEmployeSecteur.setSelectedIndex(0);
        spinEmpExp.setValue(0);
        cbSpec.setSelectedIndex(0);
        jPasswordField1.setText("");
    }
    
    void sendReservationMail(Reservation r, int tr) {
        Patient pat = Patient.findByNSS(session, r.getNSS());
        Employe emp = Employe.findByCode(session, r.getEmploye());
        
        String intetule = pat.getSexe().equals("H") ? "Mr. " : "Mde. ";
        String typemp = emp.getSecteur().equals("MEDICAL") ? "Medecin" : "Infirmier";
        
        final String username = "esantecorp@gmail.com";
        final String password = "pyxtdvbjpmcippap";

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS
        
        Session sess = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(sess);
            message.setFrom(new InternetAddress("ESante"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(pat.getEmail())
            );
            message.setSubject("Reservation ESante");
            String mailBlock = """
                               <!DOCTYPE html>
                               <html>
                                   <head></head>
                                   <body style="margin: 0; font-family: sans-serif;">
                                       <table width="700px">
                                           <tr style="text-align: center;"> 
                                               <td style="background: rgb(175,211,226); padding: 20px;">
                                                   ESante
                                               </td>
                                           </tr>
                                           <tr> 
                                               <td style="padding: 30px 50px;">
                                                   <p>Cher patient <b>${intetule} ${nomp} ${prenomp},</b></p>
                                                   <p>Merci d'avoir reserver une consultation chez ESanté.</p>
                                                   <p>Votre réservation est prévu pour le <b>${dater}</b></p>
                                                   <p>${typee} chargé de votre consultation: </p>
                                                   <p>
                                                       <p><b>Nom:</b> ${nome}</p>
                                                       <p><b>Prénom:</b> ${prenome}</p>
                                                       <p><b>Specialité:</b> ${spece}</p>
                                                   </p>
                                                   <p>Cliquez sur le button suivant pour payer et confirmer votre réservation: </p>
                                                   <p width="100%" style="text-align: center; margin: 20px;">
                                                       <a href="https://www.esante.com/confirmerReservation?NumeroR=${numr}" style="display: inline-block;background: rgb(175,211,226);padding: 20px;border-radius: 5px;color: rgb(255, 255, 255);font-weight: 600;cursor: pointer;">Confirmer ma réservation !</a>
                                                   </p>
                                                   <p>Ce lien expira dans <b>24H</b> et votre réservation sera anuller automatiquement !</p>
                                                   <p>Reservation N° ${numr}</p>
                                               </td>
                                           </tr>
                                       </table>
                                   </body>
                               </html>
                               """;
            String mailBlockFin = mailBlock
            .replace("${intetule}", intetule)            
            .replace("${nomp}", pat.getNom())
            .replace("${prenomp}", pat.getPrenom())
            .replace("${nome}", emp.getNom())
            .replace("${prenome}", emp.getPrenom())
            .replace("${spece}", Specialite.findByCode(session, emp.getSpecCode()).getSpecName())
            .replace("${numr}", Integer.toString(Reservation.getLastNumero(session)))        
            .replace("${typee}", typemp)
            .replace("${dater}", r.getDate().toString());

            message.setContent(mailBlockFin, "text/html");
            /*
            message.setText(intetule + pat.getNom() + " " + pat.getPrenom()
                    + "\n\n Votre reservation chez ESanté a bien été enregistrer, voici " + typemp + " qui est en charge de votre consulatation: \n\n"
                    + "\tNom: " + emp.getNom() + "\n"
                    + "\tPrenom: " + emp.getPrenom() + "\n"
                    + "\tSpecialité: " + Specialite.findByCode(session, emp.getSpecCode()).getSpecName()
                    + "\n\nVous avez 24H pour confirmer votre reservation!"
            );
            */

            Transport.send(message);

            System.out.println("Email envoyé!");

        } catch (MessagingException e) {
            if (tr > 1) {
                System.out.println("Impossible d'envoyer le mail, ressai ...");
                sendReservationMail(r, tr - 1);
                return;
            }
            System.out.println("Erreur d'evoie du mail");
            mail_queue.add(r);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEmpAct;
    private javax.swing.JButton btnEmpAct1;
    private javax.swing.JButton btnEmpAjou;
    private javax.swing.JButton btnEmpModif;
    private javax.swing.JButton btnEmpModif1;
    private javax.swing.JButton btnEmpSup;
    private javax.swing.JButton btnEmpSup1;
    private javax.swing.JButton btnReserver;
    private esante.Calender calander1;
    private javax.swing.JComboBox<String> cbEmployeSecteur;
    private javax.swing.JComboBox<String> cbMedecin;
    private javax.swing.JComboBox<String> cbService;
    private javax.swing.JComboBox<String> cbSpec;
    private javax.swing.JComboBox<String> cbType;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JLabel labelNomEmp;
    private javax.swing.JLabel labelNssInfo;
    private javax.swing.JLabel lblEmployeType;
    private javax.swing.JLabel lblNombreVisitesE;
    private javax.swing.ButtonGroup panel;
    private esante.Searchbar searchbar4;
    private esante.Searchbar searchbar5;
    private javax.swing.JSpinner spinEmpExp;
    private javax.swing.JTable tableEmployes;
    private javax.swing.JTable tablePatients;
    private javax.swing.JTable tableReservations;
    private javax.swing.JTextField tfAdresse;
    private javax.swing.JTextField tfAler;
    private javax.swing.JTextField tfDate;
    private javax.swing.JTextField tfEmail;
    private javax.swing.JTextField tfEmpPrenom;
    private javax.swing.JTextField tfEmpTel;
    private javax.swing.JTextField tfEmployeNom;
    private javax.swing.JTextField tfNom;
    private javax.swing.JTextField tfNss;
    public javax.swing.JTextField tfNss1;
    private javax.swing.JTextField tfPrenom;
    private javax.swing.JTextField tfTel;
    // End of variables declaration//GEN-END:variables
}
