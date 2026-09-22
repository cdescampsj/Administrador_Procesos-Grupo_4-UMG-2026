/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package administrador.de.procesos;

/**
 *
 * @author Chris
 */
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {

    private JTextField campoProceso;
    private JTextField campoFiltro;
    private JComboBox<String> comboOrden;
    private JTextArea areaConsola;
    private JList<String> listaActivos;
    private DefaultListModel<String> modeloLista;
    private JComboBox<String> comboSugeridos;

    private Funciones funciones;
    private List<Funciones.ProcesoInfo> listaCompletaProcesos = new ArrayList<>();

    public Main() {
        funciones = new Funciones(this::registrarLog);

        setTitle("Administrador de Procesos Windows - Extendido");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- 1. PANEL SUPERIOR: Crear procesos ---
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel lblSugeridos = new JLabel("Aplicación:");
        String[] sugeridos = {
            "chrome.exe (Google Chrome)",
            "msedge.exe (Microsoft Edge)",
            "firefox.exe (Mozilla Firefox)",
            "winword.exe (Microsoft Word)",
            "excel.exe (Microsoft Excel)",
            "powerpnt.exe (Microsoft PowerPoint)",
            "code.exe (VS Code)",
            "spotify.exe (Spotify)",
            "discord.exe (Discord)",
            "vlc.exe (VLC Player)",
            "notepad.exe (Bloc de Notas)", 
            "calc.exe (Calculadora)", 
            "mspaint.exe (Paint)", 
            "cmd.exe (Símbolo del Sistema)", 
            "powershell.exe (PowerShell)",
            "taskmgr.exe (Administrador de Tareas)",
            "control.exe (Panel de Control)"
        };
        comboSugeridos = new JComboBox<>(sugeridos);
        
        JLabel lblManual = new JLabel(" O escribe un nombre/alias:");
        campoProceso = new JTextField(12);

        panelSuperior.add(lblSugeridos);
        panelSuperior.add(comboSugeridos);
        panelSuperior.add(lblManual);
        panelSuperior.add(campoProceso);

        // --- 2. PANEL IZQUIERDO: Lista de Procesos + Filtro y Orden ---
        JPanel panelIzquierdo = new JPanel(new BorderLayout(5, 5));
        panelIzquierdo.setBorder(BorderFactory.createTitledBorder("Procesos Activos en Windows"));
        
        JPanel panelControlesFiltro = new JPanel(new GridLayout(2, 1, 2, 2));
        
        campoFiltro = new JTextField();
        campoFiltro.setBorder(BorderFactory.createTitledBorder("🔍 Buscar en tiempo real:"));
        
        String[] opcionesOrden = {"Mayor consumo de RAM", "Alfabético (A-Z)", "Más Usados / Frecuentes"};
        comboOrden = new JComboBox<>(opcionesOrden);
        comboOrden.setBorder(BorderFactory.createTitledBorder("Ordenar por:"));

        panelControlesFiltro.add(campoFiltro);
        panelControlesFiltro.add(comboOrden);

        modeloLista = new DefaultListModel<>();
        listaActivos = new JList<>(modeloLista);
        listaActivos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollLista = new JScrollPane(listaActivos);
        scrollLista.setPreferredSize(new Dimension(320, 0));
        
        JButton btnActualizar = new JButton("🔄 Actualizar Lista");
        
        panelIzquierdo.add(panelControlesFiltro, BorderLayout.NORTH);
        panelIzquierdo.add(scrollLista, BorderLayout.CENTER);
        panelIzquierdo.add(btnActualizar, BorderLayout.SOUTH);

        // --- 3. PANEL CENTRAL: Consola y Botones ---
        JPanel panelCentral = new JPanel(new BorderLayout(5, 5));
        
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnAgregar = new JButton("➕ Crear/Iniciar");
        JButton btnQuitar = new JButton("❌ Quitar/Terminar");
        
        btnAgregar.setBackground(new Color(46, 204, 113));
        btnAgregar.setForeground(Color.WHITE);
        btnQuitar.setBackground(new Color(231, 76, 60));
        btnQuitar.setForeground(Color.WHITE);
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnQuitar);

        areaConsola = new JTextArea();
        areaConsola.setEditable(false);
        JScrollPane scrollConsola = new JScrollPane(areaConsola);
        scrollConsola.setBorder(BorderFactory.createTitledBorder("Historial de Acciones"));

        panelCentral.add(panelBotones, BorderLayout.NORTH);
        panelCentral.add(scrollConsola, BorderLayout.CENTER);

        // --- UNIR PANELES ---
        add(panelSuperior, BorderLayout.NORTH);
        add(panelIzquierdo, BorderLayout.WEST);
        add(panelCentral, BorderLayout.CENTER);

        // --- EVENTOS ---
        
        // Listener para la búsqueda mientras el usuario escribe en la caja
        campoFiltro.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicarFiltrosYOrden(); }
            public void removeUpdate(DocumentEvent e) { aplicarFiltrosYOrden(); }
            public void changedUpdate(DocumentEvent e) { aplicarFiltrosYOrden(); }
        });

        // Listener para cambiar el modo de ordenamiento
        comboOrden.addActionListener(e -> aplicarFiltrosYOrden());

        comboSugeridos.addActionListener(e -> {
            campoProceso.setText("");
            listaActivos.clearSelection();
        });

        listaActivos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaActivos.getSelectedValue() != null) {
                campoProceso.setText("");
            }
        });

        btnActualizar.addActionListener(e -> refrescarDatosSistema());

        btnAgregar.addActionListener(e -> {
            String procesoAObtener = obtenerProcesoSeleccionado();
            if (!procesoAObtener.isEmpty()) {
                funciones.ejecutarProceso(procesoAObtener);
                Timer timer = new Timer(1500, evt -> refrescarDatosSistema());
                timer.setRepeats(false);
                timer.start();
            } else {
                registrarLog("⚠️ No se ha especificado ningún proceso.");
            }
        });

        btnQuitar.addActionListener(e -> {
            String procesoAObtener = obtenerProcesoSeleccionado();
            if (!procesoAObtener.isEmpty()) {
                funciones.eliminarProceso(procesoAObtener);
                Timer timer = new Timer(1500, evt -> refrescarDatosSistema());
                timer.setRepeats(false);
                timer.start();
            } else {
                registrarLog("⚠️ Selecciona un proceso para terminar.");
            }
        });

        refrescarDatosSistema();
    }

    private String obtenerProcesoSeleccionado() {
        String manual = campoProceso.getText().trim();
        if (!manual.isEmpty()) {
            return manual;
        }

        String seleccionadoLista = listaActivos.getSelectedValue();
        if (seleccionadoLista != null && !seleccionadoLista.isEmpty()) {
            return seleccionadoLista.split(" ")[0]; // Extrae el nombre quitando los MB
        }

        String comboItem = (String) comboSugeridos.getSelectedItem();
        if (comboItem != null) {
            return comboItem.split(" ")[0];
        }

        return "";
    }

    private void refrescarDatosSistema() {
        listaCompletaProcesos = funciones.obtenerProcesosActivos();
        aplicarFiltrosYOrden();
    }

    private void aplicarFiltrosYOrden() {
        if (listaCompletaProcesos == null) return;

        List<Funciones.ProcesoInfo> resultado = funciones.filtrarYOrdenar(
            listaCompletaProcesos, 
            campoFiltro.getText(), 
            comboOrden.getSelectedIndex()
        );

        modeloLista.clear();
        for (Funciones.ProcesoInfo info : resultado) {
            modeloLista.addElement(info.toString());
        }
    }

    public void registrarLog(String mensaje) {
        areaConsola.append(mensaje + "\n");
        areaConsola.setCaretPosition(areaConsola.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
