/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vista;

import LogicaPaqueadero.TipoVehiculo;
import LogicaPaqueadero.Vehiculo;
import ManejoErrores.AccesoDatosException;
import ManejoErrores.IdentificadorInvalidoException;
import ManejoErrores.PropietarioInvalidoException;
import ManejoErrores.SalidaInvalidaException;
import ManejoErrores.VehiculoYaDentroException;
import Servicio.ParqueaderoServicio;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author salas
 */
public class RegistroVista extends javax.swing.JFrame {

    public enum Modo {
        ENTRADA,
        SALIDA,
        EDICION
    }

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final ParqueaderoServicio servicio = new ParqueaderoServicio();
    private final Modo modo;
    private final Vehiculo vehiculoInicial;
    private final Runnable alGuardar;
    private javax.swing.JTextField DatoIdentificador;
    private JCheckBox SolicitarLavado;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RegistroVista.class.getName());

    /**
     * Creates new form RegistroVista
     */
    public RegistroVista() {
        this(Modo.ENTRADA, null, null);
    }

    public RegistroVista(Modo modo) {
        this(modo, null, null);
    }

    public RegistroVista(Modo modo, Vehiculo vehiculo, Runnable alGuardar) {
        initComponents();
        this.modo = modo;
        this.vehiculoInicial = vehiculo;
        this.alGuardar = alGuardar;
        configurarFormulario();
    }

    private void configurarFormulario() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        DatoIdentificador = new javax.swing.JTextField(12);
        SolicitarLavado = new JCheckBox("Solicitar lavado");
        SolicitarLavado.setOpaque(false);
        jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel2.setForeground(new java.awt.Color(160, 30, 30));

        TipoVehiculoSeleccionar.removeAllItems();
        if (modo == Modo.SALIDA) {
            try {
                for (Vehiculo vehiculo : servicio.vehiculosDentro()) {
                    TipoVehiculoSeleccionar.addItem(vehiculo);
                }
            } catch (AccesoDatosException ex) {
                mostrarError(ex.getMessage());
            }
            TipoVehiculoSeleccionar.addActionListener(event -> cargarVehiculoSeleccionado());
            TipoVehiculoSeleccionar.setRenderer(new javax.swing.DefaultListCellRenderer());
        } else {
            for (TipoVehiculo tipo : TipoVehiculo.values()) {
                TipoVehiculoSeleccionar.addItem(tipo);
            }
            TipoVehiculoSeleccionar.addActionListener(event -> actualizarLavado());
        }

        EspacioFecha.setColumns(10);
        EspacioHora.setColumns(10);
        DatoPropietario.setColumns(14);
        EspacioFecha.setText(LocalDate.now().toString());
        EspacioHora.setText(LocalTime.now().format(FORMATO_HORA));

        if (modo == Modo.EDICION && vehiculoInicial != null) {
            TipoVehiculoSeleccionar.setSelectedItem(vehiculoInicial.getTipo());
            DatoIdentificador.setText(vehiculoInicial.getIdentificador());
            DatoPropietario.setText(vehiculoInicial.getPropietario());
            EspacioFecha.setText(vehiculoInicial.getIngreso().toLocalDate().toString());
            EspacioHora.setText(vehiculoInicial.getIngreso().toLocalTime().format(FORMATO_HORA));
            SolicitarLavado.setSelected(vehiculoInicial.tieneLavado());
            if (vehiculoInicial.getSalida() != null) {
                salidaFecha.setText(vehiculoInicial.getSalida().toLocalDate().toString());
                salidaHora.setText(vehiculoInicial.getSalida().toLocalTime().format(FORMATO_HORA));
            }
        }

        boolean esSalida = modo == Modo.SALIDA;
        boolean esEdicion = modo == Modo.EDICION;
        DatoIdentificador.setEditable(esEdicion);
        DatoPropietario.setEditable(esEdicion);
        EspacioFecha.setEditable(esEdicion);
        EspacioHora.setEditable(esEdicion);
        if (!esEdicion) {
            EspacioFecha.setEditable(false);
            EspacioHora.setEditable(false);
        }
        if (esSalida) {
            DatoIdentificador.setEditable(false);
            DatoPropietario.setEditable(false);
        }

        jLabel1.setText(esSalida ? "REGISTRO DE SALIDA"
                : esEdicion ? "EDITAR REGISTRO" : "REGISTRO DE ENTRADA");
        BotonRegistrar.setText(esSalida ? "REGISTRAR SALIDA" : esEdicion ? "GUARDAR CAMBIOS" : "REGISTRAR");
        BotonRegistrar.addActionListener(event -> guardar());
        BotonSalir.addActionListener(event -> dispose());
        TipoVehiculoSeleccionar.addActionListener(event -> actualizarLavado());

        jPanel1.removeAll();
        jPanel1.setLayout(new GridBagLayout());
        jPanel1.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        jPanel1.setBackground(new java.awt.Color(204, 255, 204));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        jPanel1.add(jLabel1, c);

        c.gridy = 1;
        c.gridwidth = 2;
        jPanel1.add(jLabel5, c);
        c.gridx = 2;
        jPanel1.add(jLabel6, c);
        c.gridy = 2;
        c.gridx = 0;
        jPanel1.add(EspacioFecha, c);
        c.gridx = 2;
        jPanel1.add(EspacioHora, c);

        c.gridy = 3;
        c.gridx = 0;
        c.gridwidth = 1;
        jPanel1.add(jLabel4, c);
        c.gridx = 1;
        jPanel1.add(jLabel3, c);
        c.gridx = 2;
        jPanel1.add(identificadorLabel, c);
        c.gridy = 4;
        c.gridx = 0;
        jPanel1.add(TipoVehiculoSeleccionar, c);
        c.gridx = 1;
        jPanel1.add(DatoPropietario, c);
        c.gridx = 2;
        jPanel1.add(DatoIdentificador, c);

        c.gridy = 5;
        c.gridx = 0;
        c.gridwidth = 2;
        jPanel1.add(salidaFechaLabel, c);
        c.gridx = 2;
        jPanel1.add(salidaHoraLabel, c);
        c.gridy = 6;
        c.gridx = 0;
        jPanel1.add(salidaFecha, c);
        c.gridx = 2;
        jPanel1.add(salidaHora, c);
        c.gridy = 7;
        c.gridx = 0;
        jPanel1.add(SolicitarLavado, c);
        c.gridx = 2;
        c.gridwidth = 2;
        jPanel1.add(jLabel2, c);
        c.gridy = 8;
        c.gridx = 1;
        jPanel1.add(BotonRegistrar, c);
        c.gridy = 9;
        jPanel1.add(BotonSalir, c);

        if (esSalida) {
            jLabel5.setText("FECHA SALIDA");
            jLabel6.setText("HORA SALIDA");
            jPanel1.remove(SolicitarLavado);
            jPanel1.remove(jLabel2);
            jPanel1.remove(DatoIdentificador);
            jPanel1.remove(DatoPropietario);
            jPanel1.remove(salidaFechaLabel);
            jPanel1.remove(salidaHoraLabel);
            jPanel1.remove(salidaFecha);
            jPanel1.remove(salidaHora);
            cargarVehiculoSeleccionado();
        } else if (esEdicion) {
            jLabel5.setText("FECHA INGRESO (AAAA-MM-DD)");
            jLabel6.setText("HORA INGRESO (HH:MM:SS)");
            salidaFechaLabel.setText("FECHA SALIDA (opcional)");
            salidaHoraLabel.setText("HORA SALIDA (opcional)");
            SolicitarLavado.setVisible(((TipoVehiculo) TipoVehiculoSeleccionar.getSelectedItem()).admiteLavado());
        } else {
            jLabel5.setText("FECHA");
            jLabel6.setText("HORA");
            jPanel1.remove(salidaFechaLabel);
            jPanel1.remove(salidaHoraLabel);
            jPanel1.remove(salidaFecha);
            jPanel1.remove(salidaHora);
            actualizarLavado();
        }
        pack();
        setMinimumSize(getSize());
    }

    private final JLabel identificadorLabel = new JLabel("Identificador");
    private final JLabel salidaFechaLabel = new JLabel();
    private final JLabel salidaHoraLabel = new JLabel();
    private final javax.swing.JTextField salidaFecha = new javax.swing.JTextField(10);
    private final javax.swing.JTextField salidaHora = new javax.swing.JTextField(10);

    private void actualizarLavado() {
        Object seleccionado = TipoVehiculoSeleccionar.getSelectedItem();
        boolean permiteLavado = seleccionado instanceof TipoVehiculo tipo && tipo.admiteLavado();
        SolicitarLavado.setVisible(modo != Modo.SALIDA && permiteLavado);
        if (!permiteLavado) {
            SolicitarLavado.setSelected(false);
        }
    }

    private void cargarVehiculoSeleccionado() {
        Object seleccionado = TipoVehiculoSeleccionar.getSelectedItem();
        if (seleccionado instanceof Vehiculo vehiculo) {
            DatoIdentificador.setText(vehiculo.getIdentificador());
            DatoPropietario.setText(vehiculo.getPropietario());
        }
    }

    private void guardar() {
        try {
            if (modo == Modo.SALIDA) {
                Object seleccionado = TipoVehiculoSeleccionar.getSelectedItem();
                if (!(seleccionado instanceof Vehiculo vehiculo)) {
                    mostrarError("No hay vehiculos dentro del parqueadero para registrar salida.");
                    return;
                }
                servicio.registrarSalida(vehiculo.getId(), LocalDateTime.now());
            } else {
                TipoVehiculo tipo = (TipoVehiculo) TipoVehiculoSeleccionar.getSelectedItem();
                LocalDateTime ingreso = LocalDateTime.of(LocalDate.parse(EspacioFecha.getText().trim()),
                        LocalTime.parse(EspacioHora.getText().trim(), FORMATO_HORA));
                if (modo == Modo.EDICION) {
                    LocalDateTime salida = salidaFecha.getText().isBlank() ? null
                                    : LocalDateTime.of(LocalDate.parse(salidaFecha.getText().trim()),
                                            LocalTime.parse(salidaHora.getText().trim(), FORMATO_HORA));
                    servicio.actualizar(vehiculoInicial.getId(), tipo, DatoIdentificador.getText(),
                            DatoPropietario.getText(), ingreso, salida, SolicitarLavado.isSelected());
                } else {
                    servicio.registrar(tipo, DatoIdentificador.getText(), DatoPropietario.getText(),
                            ingreso, null, SolicitarLavado.isSelected());
                }
            }
            JOptionPane.showMessageDialog(this, "Operacion realizada correctamente.", "ParkLibre",
                    JOptionPane.INFORMATION_MESSAGE);
            if (alGuardar != null) {
                alGuardar.run();
            }
            dispose();
        } catch (java.time.format.DateTimeParseException ex) {
            mostrarError("Verifique la fecha (AAAA-MM-DD) y la hora (HH:MM:SS).");
        } catch (IdentificadorInvalidoException | PropietarioInvalidoException | SalidaInvalidaException
                | VehiculoYaDentroException | AccesoDatosException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo completar la operacion",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        TipoVehiculoSeleccionar = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        DatoPropietario = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        BotonRegistrar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        EspacioFecha = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        EspacioHora = new javax.swing.JTextField();
        BotonSalir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 255, 204));

        jLabel1.setText("Registro");

        TipoVehiculoSeleccionar.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Carro", "Moto", "Bicicleta" }));

        jLabel3.setText("Propietario");

        DatoPropietario.addActionListener(this::DatoPropietarioActionPerformed);

        jLabel4.setText("Tipo Vehiculo");

        BotonRegistrar.setText("REGISTRAR");

        jLabel5.setText("FECHA");

        jLabel6.setText("HORA");

        BotonSalir.setText("SALIR");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(TipoVehiculoSeleccionar, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(BotonSalir)
                        .addGap(132, 132, 132))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(DatoPropietario, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(BotonRegistrar)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(jLabel4))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel5)))
                .addGap(66, 66, 66)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel1))
                .addGap(30, 30, 30)
                .addComponent(jLabel6)
                .addGap(24, 24, 24))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(EspacioFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(EspacioHora, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(EspacioFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(EspacioHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TipoVehiculoSeleccionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DatoPropietario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BotonRegistrar))
                .addGap(21, 21, 21)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                .addComponent(BotonSalir)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void DatoPropietarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DatoPropietarioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_DatoPropietarioActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new RegistroVista().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BotonRegistrar;
    private javax.swing.JButton BotonSalir;
    private javax.swing.JTextField DatoPropietario;
    private javax.swing.JTextField EspacioFecha;
    private javax.swing.JTextField EspacioHora;
    private javax.swing.JComboBox<Object> TipoVehiculoSeleccionar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
