/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vista;

import LogicaPaqueadero.TipoVehiculo;
import LogicaPaqueadero.Vehiculo;
import ManejoErrores.AccesoDatosException;
import Servicio.ParqueaderoServicio;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author salas
 */
public class RegistrosVista extends javax.swing.JFrame {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ParqueaderoServicio servicio = new ParqueaderoServicio();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "TIPO", "IDENTIFICADOR", "PROPIETARIO", "HORA INGRESO",
                "HORA SALIDA", "PRECIO", "LAVADO", "TOTAL PAGAR"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTextField fechaConsulta = new JTextField(LocalDate.now().toString(), 10);
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RegistrosVista.class.getName());

    /**
     * Creates new form RegistrosVista
     */
    public RegistrosVista() {
        initComponents();
        configurarTabla();
        cargarTodos();
    }

    private void configurarTabla() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(new JPanel(new BorderLayout(8, 8)));
        JPanel contenido = (JPanel) getContentPane();
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        jTable1.setModel(modelo);
        jTable1.setAutoCreateRowSorter(true);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        JPanel encabezado = new JPanel(new BorderLayout(8, 8));
        JPanel titulo = new JPanel(new BorderLayout());
        titulo.add(jButton1, BorderLayout.WEST);
        titulo.add(jLabel1, BorderLayout.CENTER);
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        encabezado.add(titulo, BorderLayout.NORTH);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton todos = new JButton("TODOS");
        JButton dentro = new JButton("VEHICULOS DENTRO");
        JButton recaudo = new JButton("CONSULTAR RECAUDO");
        JButton editar = new JButton("EDITAR");
        JButton eliminar = new JButton("ELIMINAR");
        acciones.add(todos);
        acciones.add(dentro);
        acciones.add(new javax.swing.JLabel("Fecha (AAAA-MM-DD):"));
        acciones.add(fechaConsulta);
        acciones.add(recaudo);
        acciones.add(editar);
        acciones.add(eliminar);
        encabezado.add(acciones, BorderLayout.CENTER);

        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(jScrollPane1, BorderLayout.CENTER);
        jButton1.addActionListener(event -> dispose());
        todos.addActionListener(event -> cargarTodos());
        dentro.addActionListener(event -> cargarDentro());
        recaudo.addActionListener(event -> consultarRecaudo());
        editar.addActionListener(event -> editarSeleccionado());
        eliminar.addActionListener(event -> eliminarSeleccionado());

        setSize(1100, 520);
    }

    private void cargarTodos() {
        try {
            mostrarVehiculos(servicio.listarTodos());
            jLabel1.setText("REGISTROS");
        } catch (AccesoDatosException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cargarDentro() {
        try {
            List<Vehiculo> dentro = servicio.vehiculosDentro();
            mostrarVehiculos(dentro);
            jLabel1.setText("VEHICULOS DENTRO DEL PARQUEADERO");
        } catch (AccesoDatosException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void mostrarVehiculos(List<Vehiculo> vehiculos) {
        modelo.setRowCount(0);
        for (Vehiculo vehiculo : vehiculos) {
            String ingreso = vehiculo.getIngreso().format(FORMATO_FECHA_HORA);
            String salida = vehiculo.getSalida() == null ? "Dentro"
                    : vehiculo.getSalida().format(FORMATO_FECHA_HORA);
            modelo.addRow(new Object[]{
                vehiculo.getId(),
                vehiculo.getTipo().getEtiqueta(),
                vehiculo.getIdentificador(),
                vehiculo.getPropietario(),
                ingreso,
                salida,
                moneda(vehiculo.calcularCobroParqueo()),
                vehiculo.tieneLavado() ? moneda(vehiculo.calcularCostoLavado()) : "No",
                moneda(vehiculo.calcularTotal())
            });
        }
    }

    private void consultarRecaudo() {
        try {
            LocalDate dia = LocalDate.parse(fechaConsulta.getText().trim());
            var recaudos = servicio.recaudoPorTipo(dia);
            DefaultTableModel modeloRecaudo = new DefaultTableModel(
                    new Object[]{"TIPO", "TOTAL RECAUDADO"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            for (TipoVehiculo tipo : TipoVehiculo.values()) {
                modeloRecaudo.addRow(new Object[]{tipo.getEtiqueta(), moneda(recaudos.get(tipo))});
            }
            JTable tablaRecaudo = new JTable(modeloRecaudo);
            JOptionPane.showMessageDialog(this, new JScrollPane(tablaRecaudo),
                    "Recaudo por tipo - " + dia, JOptionPane.INFORMATION_MESSAGE);
        } catch (java.time.format.DateTimeParseException ex) {
            mostrarError("Ingrese la fecha con formato AAAA-MM-DD.");
        } catch (AccesoDatosException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void editarSeleccionado() {
        Vehiculo vehiculo = vehiculoSeleccionado();
        if (vehiculo == null) {
            mostrarError("Seleccione un registro para editar.");
            return;
        }
        new RegistroVista(RegistroVista.Modo.EDICION, vehiculo, this::cargarTodos).setVisible(true);
    }

    private void eliminarSeleccionado() {
        Vehiculo vehiculo = vehiculoSeleccionado();
        if (vehiculo == null) {
            mostrarError("Seleccione un registro para eliminar.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar el registro de " + vehiculo.getIdentificador() + "?",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.eliminar(vehiculo.getId());
            cargarTodos();
        } catch (AccesoDatosException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private Vehiculo vehiculoSeleccionado() {
        int fila = jTable1.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        int filaModelo = jTable1.convertRowIndexToModel(fila);
        int id = (int) modelo.getValueAt(filaModelo, 0);
        try {
            return servicio.listarTodos().stream().filter(v -> v.getId() == id).findFirst().orElse(null);
        } catch (AccesoDatosException ex) {
            mostrarError(ex.getMessage());
            return null;
        }
    }

    private String moneda(long valor) {
        return java.text.NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO")).format(valor);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 153));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "TIPO", "PROPIETARIO", "HORA INGRESO", "HORA SALIDA", "PRECIO", "LAVADO", "TOTAL PAGAR"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setText("REGISTROS");

        jButton1.setText("SALIR");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(249, 249, 249)
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jButton1))
                .addGap(32, 32, 32)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(163, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new RegistrosVista().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
