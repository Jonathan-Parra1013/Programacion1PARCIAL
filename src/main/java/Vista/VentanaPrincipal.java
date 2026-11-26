package Vista;

import LogicaPaqueadero.TipoVehiculo;
import LogicaPaqueadero.Vehiculo;
import ManejoErrores.AccesoDatosException;
import ManejoErrores.IdentificadorInvalidoException;
import ManejoErrores.PropietarioInvalidoException;
import ManejoErrores.SalidaInvalidaException;
import ManejoErrores.VehiculoYaDentroException;
import Servicio.ParqueaderoServicio;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana unica de ParkLibre. Solo muestra y captura datos: toda regla de negocio
 * y todo acceso a datos pasan por ParqueaderoServicio.
 */
public class VentanaPrincipal extends JFrame {

    private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DecimalFormat FMT_DINERO;

    static {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        FMT_DINERO = new DecimalFormat("$#,##0", simbolos);
    }

    private final ParqueaderoServicio servicio;
    private final List<Vehiculo> registros = new ArrayList<>(); // fila de la tabla -> vehiculo
    private int idSeleccionado = 0; // 0 = ningun registro seleccionado
    private boolean cargando = false; // evita reaccionar a la seleccion mientras se refresca

    // formulario
    private final JComboBox<TipoVehiculo> cmbTipo = new JComboBox<>(TipoVehiculo.values());
    private final JTextField txtIdentificador = new JTextField(12);
    private final JTextField txtPropietario = new JTextField(20);
    private final JTextField txtIngreso = new JTextField(14);
    private final JTextField txtSalida = new JTextField(14);
    private final JCheckBox chkLavado = new JCheckBox("Pidio servicio de lavado");
    private final JButton btnGuardar = new JButton("Guardar nuevo");
    private final JButton btnActualizar = new JButton("Guardar cambios");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar formulario");
    private final JButton btnRefrescar = new JButton("Actualizar tabla");

    // tabla de registros
    private final DefaultTableModel modeloRegistros = modeloNoEditable(
            "Tipo", "Identificador", "Propietario", "Ingreso", "Salida", "Parqueo", "Lavado", "Total a pagar");
    private final JTable tablaRegistros = new JTable(modeloRegistros);

    // consultas
    private final DefaultTableModel modeloDentro = modeloNoEditable("Tipo", "Identificador", "Propietario", "Hora de ingreso");
    private final DefaultTableModel modeloRecaudo = modeloNoEditable("Tipo de vehiculo", "Total recaudado");
    private final JTextField txtDia = new JTextField(LocalDate.now().format(FMT_FECHA), 10);
    private final JLabel lblTotalDia = new JLabel(" ");

    private final JLabel lblEstado = new JLabel(" ");

    public VentanaPrincipal() {
        this(new ParqueaderoServicio());
    }

    public VentanaPrincipal(ParqueaderoServicio servicio) {
        super("ParkLibre - Parqueadero");
        this.servicio = servicio;
        construirVentana();
        alCambiarTipo();
        limpiarFormulario();
        refrescarTabla();
    }

    // ------------------------------------------------------------------ construccion
    private void construirVentana() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Registros", construirPanelRegistros());
        pestanas.addTab("Consultas", construirPanelConsultas());

        lblEstado.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pestanas, BorderLayout.CENTER);
        getContentPane().add(lblEstado, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(980, 640));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel construirPanelRegistros() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Datos del vehiculo"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        agregarFila(form, c, 0, "Tipo", cmbTipo, "Identificador", txtIdentificador);
        agregarFila(form, c, 1, "Propietario", txtPropietario, "Ingreso (aaaa-MM-dd HH:mm)", txtIngreso);
        agregarFila(form, c, 2, "Lavado", chkLavado, "Salida (vacio = sigue dentro)", txtSalida);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnGuardar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        botones.add(btnRefrescar);

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(form, BorderLayout.CENTER);
        arriba.add(botones, BorderLayout.SOUTH);

        tablaRegistros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaRegistros.setRowHeight(22);

        cmbTipo.addActionListener(e -> alCambiarTipo());
        btnGuardar.addActionListener(e -> guardarNuevo());
        btnActualizar.addActionListener(e -> guardarCambios());
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnRefrescar.addActionListener(e -> {
            refrescarTabla();
            limpiarFormulario();
        });
        tablaRegistros.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !cargando) {
                alSeleccionarFila();
            }
        });

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(arriba, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaRegistros), BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPanelConsultas() {
        // a) vehiculos dentro
        JButton btnDentro = new JButton("Ver vehiculos dentro ahora");
        btnDentro.addActionListener(e -> consultarDentro());
        JPanel panelA = new JPanel(new BorderLayout(0, 6));
        panelA.setBorder(BorderFactory.createTitledBorder("a) Vehiculos que estan dentro en este momento"));
        JPanel barraA = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraA.add(btnDentro);
        panelA.add(barraA, BorderLayout.NORTH);
        panelA.add(new JScrollPane(new JTable(modeloDentro)), BorderLayout.CENTER);

        // b) recaudo por tipo en un dia
        JButton btnRecaudo = new JButton("Calcular recaudo");
        btnRecaudo.addActionListener(e -> consultarRecaudo());
        JPanel panelB = new JPanel(new BorderLayout(0, 6));
        panelB.setBorder(BorderFactory.createTitledBorder("b) Total recaudado por tipo de vehiculo en un dia"));
        JPanel barraB = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraB.add(new JLabel("Dia (aaaa-MM-dd):"));
        barraB.add(txtDia);
        barraB.add(btnRecaudo);
        barraB.add(lblTotalDia);
        panelB.add(barraB, BorderLayout.NORTH);
        panelB.add(new JScrollPane(new JTable(modeloRecaudo)), BorderLayout.CENTER);

        JPanel panel = new JPanel(new java.awt.GridLayout(2, 1, 0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(panelA);
        panel.add(panelB);
        return panel;
    }

    private void agregarFila(JPanel p, GridBagConstraints c, int fila, String et1, java.awt.Component comp1,
            String et2, java.awt.Component comp2) {
        c.gridy = fila;
        c.gridx = 0;
        p.add(new JLabel(et1), c);
        c.gridx = 1;
        p.add(comp1, c);
        c.gridx = 2;
        p.add(new JLabel(et2), c);
        c.gridx = 3;
        p.add(comp2, c);
    }

    private static DefaultTableModel modeloNoEditable(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    // ------------------------------------------------------------------ acciones
    /** R6: el campo de lavado solo se habilita para los tipos que lo admiten. */
    private void alCambiarTipo() {
        TipoVehiculo tipo = (TipoVehiculo) cmbTipo.getSelectedItem();
        boolean admite = tipo != null && tipo.admiteLavado();
        chkLavado.setEnabled(admite);
        if (!admite) {
            chkLavado.setSelected(false);
        }
    }

    private void guardarNuevo() {
        try {
            Vehiculo v = servicio.registrar(tipoElegido(), txtIdentificador.getText(), txtPropietario.getText(),
                    leerFecha(txtIngreso, true, "ingreso"), leerFecha(txtSalida, false, "salida"), chkLavado.isSelected());
            refrescarTabla();
            limpiarFormulario();
            mostrarMensaje("Vehiculo " + v.getIdentificador() + " registrado." + detalleCobro(v), false);
        } catch (IdentificadorInvalidoException | PropietarioInvalidoException | SalidaInvalidaException
                | VehiculoYaDentroException | AccesoDatosException | IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void guardarCambios() {
        if (idSeleccionado == 0) {
            mostrarMensaje("Seleccione un registro de la tabla para editarlo.", true);
            return;
        }
        try {
            Vehiculo v = servicio.actualizar(idSeleccionado, tipoElegido(), txtIdentificador.getText(),
                    txtPropietario.getText(), leerFecha(txtIngreso, true, "ingreso"),
                    leerFecha(txtSalida, false, "salida"), chkLavado.isSelected());
            refrescarTabla();
            limpiarFormulario();
            mostrarMensaje("Vehiculo " + v.getIdentificador() + " actualizado." + detalleCobro(v), false);
        } catch (IdentificadorInvalidoException | PropietarioInvalidoException | SalidaInvalidaException
                | VehiculoYaDentroException | AccesoDatosException | IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void eliminarSeleccionado() {
        Vehiculo v = vehiculoSeleccionado();
        if (v == null) {
            mostrarMensaje("Seleccione un registro de la tabla para eliminarlo.", true);
            return;
        }
        if (!confirmarEliminacion(v)) {
            return;
        }
        try {
            servicio.eliminar(v.getId());
            refrescarTabla();
            limpiarFormulario();
            mostrarMensaje("Vehiculo " + v.getIdentificador() + " eliminado.", false);
        } catch (AccesoDatosException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void consultarDentro() {
        try {
            modeloDentro.setRowCount(0);
            List<Vehiculo> dentro = servicio.vehiculosDentro();
            for (Vehiculo v : dentro) {
                modeloDentro.addRow(new Object[]{v.getTipo(), v.getIdentificador(), v.getPropietario(),
                    v.getIngreso().format(FMT_FECHA_HORA)});
            }
            mostrarMensaje(dentro.size() + " vehiculo(s) dentro del parqueadero.", false);
        } catch (AccesoDatosException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void consultarRecaudo() {
        try {
            LocalDate dia = LocalDate.parse(txtDia.getText().trim(), FMT_FECHA);
            Map<TipoVehiculo, Long> totales = servicio.recaudoPorTipo(dia);
            modeloRecaudo.setRowCount(0);
            long general = 0;
            for (TipoVehiculo t : TipoVehiculo.values()) {
                long total = totales.getOrDefault(t, 0L);
                general += total;
                modeloRecaudo.addRow(new Object[]{t, FMT_DINERO.format(total)});
            }
            lblTotalDia.setText("   Total del dia: " + FMT_DINERO.format(general));
            mostrarMensaje("Recaudo del " + dia.format(FMT_FECHA) + " calculado.", false);
        } catch (DateTimeParseException e) {
            mostrarMensaje("Fecha invalida. Use el formato aaaa-MM-dd, por ejemplo 2026-11-20.", true);
        } catch (AccesoDatosException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    // ------------------------------------------------------------------ tabla y formulario
    private void refrescarTabla() {
        cargando = true;
        try {
            modeloRegistros.setRowCount(0);
            registros.clear();
            registros.addAll(servicio.listarTodos());
            for (Vehiculo v : registros) {
                boolean dentro = v.estaDentro();
                modeloRegistros.addRow(new Object[]{
                    v.getTipo(), v.getIdentificador(), v.getPropietario(),
                    v.getIngreso().format(FMT_FECHA_HORA),
                    dentro ? "Dentro" : v.getSalida().format(FMT_FECHA_HORA),
                    dentro ? "-" : FMT_DINERO.format(v.calcularCobroParqueo()),
                    v.tieneLavado() ? FMT_DINERO.format(v.calcularCostoLavado()) : "No",
                    dentro ? "-" : FMT_DINERO.format(v.calcularTotal())});
            }
        } catch (AccesoDatosException e) {
            mostrarMensaje(e.getMessage(), true);
        } finally {
            cargando = false;
            idSeleccionado = 0;
        }
    }

    private void alSeleccionarFila() {
        Vehiculo v = vehiculoSeleccionado();
        if (v == null) {
            idSeleccionado = 0;
            return;
        }
        idSeleccionado = v.getId();
        cmbTipo.setSelectedItem(v.getTipo());
        txtIdentificador.setText(v.getIdentificador());
        txtPropietario.setText(v.getPropietario());
        txtIngreso.setText(v.getIngreso().format(FMT_FECHA_HORA));
        txtSalida.setText(v.estaDentro() ? "" : v.getSalida().format(FMT_FECHA_HORA));
        alCambiarTipo();
        chkLavado.setSelected(v.tieneLavado());
    }

    private void limpiarFormulario() {
        cargando = true;
        tablaRegistros.clearSelection();
        cargando = false;
        idSeleccionado = 0;
        cmbTipo.setSelectedIndex(0);
        txtIdentificador.setText("");
        txtPropietario.setText("");
        txtIngreso.setText(LocalDateTime.now().withSecond(0).withNano(0).format(FMT_FECHA_HORA));
        txtSalida.setText("");
        chkLavado.setSelected(false);
        alCambiarTipo();
    }

    private Vehiculo vehiculoSeleccionado() {
        int fila = tablaRegistros.getSelectedRow();
        return (fila >= 0 && fila < registros.size()) ? registros.get(fila) : null;
    }

    private TipoVehiculo tipoElegido() {
        return (TipoVehiculo) cmbTipo.getSelectedItem();
    }

    /** Convierte el texto del campo en fecha y hora. Un campo opcional vacio devuelve null. */
    private LocalDateTime leerFecha(JTextField campo, boolean obligatoria, String nombre) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            if (obligatoria) {
                throw new IllegalArgumentException("La fecha y hora de " + nombre + " es obligatoria.");
            }
            return null;
        }
        try {
            return LocalDateTime.parse(texto, FMT_FECHA_HORA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha de " + nombre
                    + " invalida. Use el formato aaaa-MM-dd HH:mm, por ejemplo 2026-11-20 08:10.");
        }
    }

    private String detalleCobro(Vehiculo v) {
        return v.estaDentro() ? " Queda dentro del parqueadero." : " Total a pagar: " + FMT_DINERO.format(v.calcularTotal());
    }

    // ------------------------------------------------------------------ mensajes (protegidos para poder probarlos)
    /** Muestra el mensaje dentro de la ventana (barra inferior) y, si es error, tambien en un dialogo. */
    protected void mostrarMensaje(String mensaje, boolean error) {
        lblEstado.setForeground(error ? new Color(176, 0, 32) : new Color(0, 110, 40));
        lblEstado.setText(mensaje);
        if (error) {
            JOptionPane.showMessageDialog(this, mensaje, "ParkLibre", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** R8: confirmacion previa a eliminar. */
    protected boolean confirmarEliminacion(Vehiculo v) {
        int r = JOptionPane.showConfirmDialog(this,
                "Eliminar el registro de " + v.getTipo() + " " + v.getIdentificador() + "?",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }
}
